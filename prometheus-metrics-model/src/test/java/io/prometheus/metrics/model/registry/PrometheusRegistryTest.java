package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrometheusRegistryTest {

  Collector noName = () -> GaugeSnapshot.builder().name("no_name_gauge").build();

  Collector counterA1 =
      new Collector() {
        @Override
        public MetricSnapshot collect() {
          return CounterSnapshot.builder().name("counter_a").build();
        }

        @Override
        public String getPrometheusName() {
          return "counter_a";
        }
      };

  Collector counterA2 =
      new Collector() {
        @Override
        public MetricSnapshot collect() {
          return CounterSnapshot.builder().name("counter.a").build();
        }

        @Override
        public String getPrometheusName() {
          return "counter_a";
        }
      };

  Collector counterB =
      new Collector() {
        @Override
        public MetricSnapshot collect() {
          return CounterSnapshot.builder().name("counter_b").build();
        }

        @Override
        public String getPrometheusName() {
          return "counter_b";
        }
      };

  Collector gaugeA =
      new Collector() {
        @Override
        public MetricSnapshot collect() {
          return GaugeSnapshot.builder().name("gauge_a").build();
        }

        @Override
        public String getPrometheusName() {
          return "gauge_a";
        }
      };

  MultiCollector multiCollector =
      new MultiCollector() {
        @Override
        public MetricSnapshots collect() {
          return new MetricSnapshots(gaugeA.collect(), counterB.collect());
        }

        @Override
        public List<String> getPrometheusNames() {
          return Arrays.asList(gaugeA.getPrometheusName(), counterB.getPrometheusName());
        }
      };

  @Test
  public void registerNoName() {
    PrometheusRegistry registry = new PrometheusRegistry();
    // If the collector does not have a name at registration time, there is no conflict during
    // registration.
    registry.register(noName);
    registry.register(noName);
    // However, at scrape time the collector has to provide a metric name, and then we'll get a
    // duplicate name error.
    assertThatCode(registry::scrape)
        .hasMessageContaining("duplicate")
        .hasMessageContaining("no_name_gauge");
  }

  @Test
  public void registerDuplicateName() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> registry.register(counterA2));
  }

  @Test
  public void registerOk() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    registry.register(counterB);
    registry.register(gaugeA);
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(3);

    registry.unregister(counterB);
    snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    registry.register(counterB);
    snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(3);
  }

  @Test
  public void registerDuplicateMultiCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> registry.register(multiCollector));
  }

  @Test
  public void registerOkMultiCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    registry.unregister(multiCollector);
    snapshots = registry.scrape();
    assertThat(snapshots.size()).isZero();
  }

  @Test
  public void clearOk() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    registry.register(counterB);
    registry.register(gaugeA);
    assertThat(registry.scrape().size()).isEqualTo(3);

    registry.clear();
    assertThat(registry.scrape().size()).isZero();
  }

  @Test
  public void allowDuplicateRegistration_disabled() {
    // Default behavior: duplicates not allowed
    PrometheusRegistry registry = new PrometheusRegistry(false);
    registry.register(counterA1);
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> registry.register(counterA2))
        .withMessageContaining("counter_a")
        .withMessageContaining("already registered");
  }

  @Test
  public void allowDuplicateRegistration_enabled() {
    // With flag enabled: duplicates allowed at registration time
    PrometheusRegistry registry = new PrometheusRegistry(true);
    registry.register(counterA1);
    assertThatCode(() -> registry.register(counterA2)).doesNotThrowAnyException();

    // Both collectors are registered
    assertThat(registry.scrape().size()).isEqualTo(2);
  }

  @Test
  public void allowDuplicateRegistration_multiCollector() {
    // MultiCollector with duplicate names allowed
    PrometheusRegistry registry = new PrometheusRegistry(true);
    registry.register(multiCollector);
    assertThatCode(() -> registry.register(multiCollector)).doesNotThrowAnyException();
  }

  @Test
  public void allowDuplicateRegistration_mixed() {
    // Test mixing regular collectors and multi-collectors with same names
    PrometheusRegistry registry = new PrometheusRegistry(true);
    registry.register(counterA1);
    registry.register(counterA2);
    registry.register(counterB);

    // Should have 3 collectors registered (2 with same name, 1 different)
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(3);
  }

  @Test
  public void allowDuplicateRegistration_scrapeSucceeds() {
    // Test that scraping actually works with duplicates
    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Register two counters with same name
    registry.register(counterA1);
    registry.register(counterA2);

    // Scrape should succeed and return both snapshots
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    // Verify both have the same prometheus name
    String firstName = snapshots.get(0).getMetadata().getPrometheusName();
    String secondName = snapshots.get(1).getMetadata().getPrometheusName();
    assertThat(firstName).isEqualTo("counter_a");
    assertThat(secondName).isEqualTo("counter_a");
  }
}
