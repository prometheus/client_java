package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class PrometheusRegistryTest {

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
    try {
      registry.scrape();
    } catch (IllegalStateException e) {
      Assert.assertTrue(
          e.getMessage().contains("duplicate") && e.getMessage().contains("no_name_gauge"));
      return;
    }
    Assert.fail("Expected duplicate name exception");
  }

  @Test(expected = IllegalStateException.class)
  public void registerDuplicateName() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    registry.register(counterA2);
  }

  @Test
  public void registerOk() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    registry.register(counterB);
    registry.register(gaugeA);
    MetricSnapshots snapshots = registry.scrape();
    Assert.assertEquals(3, snapshots.size());

    registry.unregister(counterB);
    snapshots = registry.scrape();
    Assert.assertEquals(2, snapshots.size());

    registry.register(counterB);
    snapshots = registry.scrape();
    Assert.assertEquals(3, snapshots.size());
  }

  @Test(expected = IllegalStateException.class)
  public void registerDuplicateMultiCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    registry.register(multiCollector);
  }

  @Test
  public void registerOkMultiCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    MetricSnapshots snapshots = registry.scrape();
    Assert.assertEquals(2, snapshots.size());

    registry.unregister(multiCollector);
    snapshots = registry.scrape();
    Assert.assertEquals(0, snapshots.size());
  }
}
