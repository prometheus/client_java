package io.prometheus.metrics.model.registry;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.List;
import org.junit.jupiter.api.Test;

class PrometheusRegistryTest {

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
          return asList(gaugeA.getPrometheusName(), counterB.getPrometheusName());
        }
      };

  @Test
  public void register_duplicateName_IsAllowed() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    registry.register(counterA2);

    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);
  }

  @Test
  public void register_duplicateName_differentType_failsAtScrapeTime() {
    Collector counter =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("my_metric").build();
          }

          @Override
          public String getPrometheusName() {
            return "my_metric";
          }
        };

    Collector gauge =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("my_metric").build();
          }

          @Override
          public String getPrometheusName() {
            return "my_metric";
          }
        };

    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counter);
    registry.register(gauge); // Registration succeeds

    // But scrape fails due to type conflict
    assertThatThrownBy(registry::scrape)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Conflicting metric types")
        .hasMessageContaining("my_metric")
        .hasMessageContaining("CounterSnapshot")
        .hasMessageContaining("GaugeSnapshot");
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
  public void registerDuplicateMultiCollectorIsAllowed() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    registry.register(multiCollector);

    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(4);
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
  public void duplicateRegistration_multiCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    assertThatCode(() -> registry.register(multiCollector)).doesNotThrowAnyException();
  }

  @Test
  public void duplicateRegistration_mixed() {
    // Test mixing regular collectors and multi-collectors with same names
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(counterA1);
    registry.register(counterA2);
    registry.register(counterB);

    // Should have 3 collectors registered (2 with same name, 1 different)
    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(3);
  }

  @Test
  public void allowDuplicateRegistration_scrapeSucceeds() {
    PrometheusRegistry registry = new PrometheusRegistry();

    registry.register(counterA1);
    registry.register(counterA2);

    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    String firstName = snapshots.get(0).getMetadata().getPrometheusName();
    String secondName = snapshots.get(1).getMetadata().getPrometheusName();
    assertThat(firstName).isEqualTo("counter_a");
    assertThat(secondName).isEqualTo("counter_a");
  }

  @Test
  void testDuplicateNames_sameLabels_throwsException() {
    PrometheusRegistry registry = new PrometheusRegistry();

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
                        .value(50)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    registry.register(counter1);
    registry.register(counter2);

    // Scrape should throw exception due to duplicate time series (same name + same labels)
    assertThatThrownBy(registry::scrape)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Duplicate labels detected")
        .hasMessageContaining("api_responses");
  }

  @Test
  void testDuplicateNames_multipleDataPoints_scrapesSuccessfully() {
    PrometheusRegistry registry = new PrometheusRegistry();

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

    registry.register(counter1);
    registry.register(counter2);

    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);

    assertThat(snapshots.get(0).getDataPoints()).hasSize(2);
    assertThat(snapshots.get(1).getDataPoints()).hasSize(2);

    int totalDataPoints = snapshots.stream().mapToInt(s -> s.getDataPoints().size()).sum();
    assertThat(totalDataPoints).isEqualTo(4);
  }

  @Test
  void testDuplicateNames_mixedMetricTypes_scrapesSuccessfully() {
    PrometheusRegistry registry = new PrometheusRegistry();

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

    Collector gauge =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("requests")
                .help("Request gauge")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
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

    MetricSnapshots snapshots = registry.scrape();
    assertThat(snapshots.size()).isEqualTo(2);
  }

  @Test
  void testDuplicateNames_samePrometheusNameDifferentTypes_throwsExceptionAtScrape() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector counter =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_metrics")
                .help("API metrics counter")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("method", "GET"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_metrics";
          }
        };

    Collector gauge =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder()
                .name("api_metrics")
                .help("API metrics gauge")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(Labels.of("method", "POST"))
                        .value(50)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_metrics"; // Same Prometheus name as counter
          }
        };

    registry.register(counter);
    registry.register(gauge); // Registration succeeds

    // Scrape should throw exception due to conflicting metric types
    assertThatThrownBy(registry::scrape)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("api_metrics")
        .hasMessageContaining("CounterSnapshot")
        .hasMessageContaining("GaugeSnapshot");
  }

  @Test
  void testUnregister_withDuplicateNames() {
    PrometheusRegistry registry = new PrometheusRegistry();

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

    registry.register(counter1);
    registry.register(counter2);
    assertThat(registry.scrape().size()).isEqualTo(2);

    registry.unregister(counter1);
    assertThat(registry.scrape().size()).isEqualTo(1);

    registry.unregister(counter2);
    assertThat(registry.scrape().size()).isEqualTo(0);
  }

  @Test
  void testClear_withDuplicateNames() {
    PrometheusRegistry registry = new PrometheusRegistry();

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

    registry.register(counter1);
    registry.register(counter2);
    assertThat(registry.scrape().size()).isEqualTo(2);

    registry.clear();
    assertThat(registry.scrape().size()).isEqualTo(0);
  }

}
