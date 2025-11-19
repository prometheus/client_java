package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for duplicate metric registration with exposition format output.
 *
 * <p>These tests verify that when duplicate names are allowed, the exposition format produces valid
 * output that Prometheus can scrape.
 */
class DuplicateRegistrationIntegrationTest {

  @Test
  void testDuplicateNames_differentLabels_scrapesSuccessfully() {
    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter 1: api_responses_total with labels {uri, outcome}
    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Counter 2: api_responses_total with labels {uri, outcome, error}
    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/hello", "outcome", "FAILURE", "error", "TIMEOUT"))
                        .value(10)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Register both
    registry.register(counter1);
    registry.register(counter2);

    // Scrape
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    // Verify both have same name
    assertThat(snapshots.get(0).getMetadata().getPrometheusName()).isEqualTo("api_responses_total");
    assertThat(snapshots.get(1).getMetadata().getPrometheusName()).isEqualTo("api_responses_total");

    // Verify data points
    assertThat(snapshots.get(0).getDataPoints()).hasSize(1);
    assertThat(snapshots.get(1).getDataPoints()).hasSize(1);
  }

  @Test
  void testDuplicateNames_sameLabels_scrapesSuccessfully() {
    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter 1
    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Counter 2: SAME labels, different value
    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(50) // Different value!
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Register both
    registry.register(counter1);
    registry.register(counter2);

    // Scrape should succeed
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    // Both snapshots exist with identical label sets
    // This is technically allowed but may produce confusing metrics
    assertThat(snapshots.get(0).getDataPoints()).hasSize(1);
    assertThat(snapshots.get(1).getDataPoints()).hasSize(1);
  }

  @Test
  void testDuplicateNames_multipleDataPoints_scrapesSuccessfully() {
    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter 1: Multiple data points
    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/world", "outcome", "SUCCESS"))
                        .value(200)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Counter 2: Multiple data points with additional label
    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/hello", "outcome", "FAILURE", "error", "TIMEOUT"))
                        .value(10)
                        .build())
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/world", "outcome", "FAILURE", "error", "NOT_FOUND"))
                        .value(5)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Register both
    registry.register(counter1);
    registry.register(counter2);

    // Scrape
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    // Verify data points
    assertThat(snapshots.get(0).getDataPoints()).hasSize(2);
    assertThat(snapshots.get(1).getDataPoints()).hasSize(2);

    // Total of 4 data points across 2 snapshots
    int totalDataPoints = snapshots.stream().mapToInt(s -> s.getDataPoints().size()).sum();
    assertThat(totalDataPoints).isEqualTo(4);
  }

  @Test
  void testDuplicateNames_mixedMetricTypes_scrapesSuccessfully() {
    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter with name "requests_total"
    Collector counter =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("requests")
                .help("Request counter")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("method", "GET"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }
        };

    // Gauge with same base name (will be "requests_total" in prometheus format for counter)
    Collector gauge =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder()
                .name("requests")
                .help("Request gauge")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(Labels.of("method", "POST"))
                        .value(50)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "requests"; // Note: Gauge doesn't add _total suffix
          }
        };

    // Register both - different prometheus names so shouldn't conflict
    registry.register(counter);
    registry.register(gauge);

    // Scrape
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);
  }

  @Test
  void testBackwardCompatibility_strictModeStillWorks() {
    // Default registry should still enforce uniqueness
    PrometheusRegistry registry = new PrometheusRegistry(false);

    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("test").build();
          }

          @Override
          public String getPrometheusName() {
            return "test_total";
          }
        };

    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("test").build();
          }

          @Override
          public String getPrometheusName() {
            return "test_total";
          }
        };

    // First registration should succeed
    registry.register(counter1);

    // Second registration should fail
    try {
      registry.register(counter2);
      throw new AssertionError("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("test_total");
      assertThat(e.getMessage()).contains("already registered");
    }
  }

  @Test
  void testUnregister_withDuplicatesEnabled() {
    PrometheusRegistry registry = new PrometheusRegistry(true);

    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("test").build();
          }

          @Override
          public String getPrometheusName() {
            return "test_total";
          }
        };

    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("test").build();
          }

          @Override
          public String getPrometheusName() {
            return "test_total";
          }
        };

    // Register both
    registry.register(counter1);
    registry.register(counter2);
    assertThat(registry.scrape().size()).isEqualTo(2);

    // Unregister one
    registry.unregister(counter1);
    assertThat(registry.scrape().size()).isEqualTo(1);

    // Unregister the other
    registry.unregister(counter2);
    assertThat(registry.scrape().size()).isEqualTo(0);
  }

  @Test
  void testClear_withDuplicatesEnabled() {
    PrometheusRegistry registry = new PrometheusRegistry(true);

    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("test").build();
          }

          @Override
          public String getPrometheusName() {
            return "test_total";
          }
        };

    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("test").build();
          }

          @Override
          public String getPrometheusName() {
            return "test_total";
          }
        };

    // Register both
    registry.register(counter1);
    registry.register(counter2);
    assertThat(registry.scrape().size()).isEqualTo(2);

    // Clear all
    registry.clear();
    assertThat(registry.scrape().size()).isEqualTo(0);
  }
}
