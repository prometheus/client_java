package io.prometheus.metrics.model.registry;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  void registerDuplicateName_withoutTypeInfo_allowedForBackwardCompatibility() {
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
  void register_duplicateName_differentTypes_notAllowed() {
    PrometheusRegistry registry = new PrometheusRegistry();

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

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }
        };

    Collector gaugeA1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("counter_a").build();
          }

          @Override
          public String getPrometheusName() {
            return "counter_a";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.GAUGE;
          }
        };

    registry.register(counterA1);

    assertThatThrownBy(() -> registry.register(gaugeA1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Conflicting metric types");
  }

  @Test
  public void register_sameName_sameType_differentLabelSchemas_allowed() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector counterWithPathLabel =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path", "status"));
          }
        };

    Collector counterWithRegionLabel =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("region"));
          }
        };

    // Both collectors have same name and type, but different label schemas
    // This should succeed
    registry.register(counterWithPathLabel);
    assertThatCode(() -> registry.register(counterWithRegionLabel)).doesNotThrowAnyException();
  }

  @Test
  public void register_sameName_sameType_sameLabelSchema_notAllowed() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path", "status"));
          }
        };

    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path", "status"));
          }
        };

    registry.register(counter1);

    // Second collector has same name, type, and label schema - should fail
    assertThatThrownBy(() -> registry.register(counter2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate metric name with identical label schema");
  }

  @Test
  public void register_backwardCompatibility_nullTypeAndLabels_skipsValidation() {
    PrometheusRegistry registry = new PrometheusRegistry();

    // Collector without getMetricType() and getLabelNames() - returns null (default)
    Collector legacyCollector1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("legacy_metric").build();
          }

          @Override
          public String getPrometheusName() {
            return "legacy_metric";
          }
        };

    Collector legacyCollector2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("legacy_metric").build();
          }

          @Override
          public String getPrometheusName() {
            return "legacy_metric";
          }
        };

    // Both collectors have the same name but no type/label info
    // Should succeed because validation is skipped
    registry.register(legacyCollector1);
    assertThatCode(() -> registry.register(legacyCollector2)).doesNotThrowAnyException();
  }

  @Test
  public void register_multiCollector_withTypeValidation() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector counter =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("shared_metric").build();
          }

          @Override
          public String getPrometheusName() {
            return "shared_metric";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }
        };

    MultiCollector multiWithGauge =
        new MultiCollector() {
          @Override
          public MetricSnapshots collect() {
            return new MetricSnapshots(GaugeSnapshot.builder().name("shared_metric").build());
          }

          @Override
          public List<String> getPrometheusNames() {
            return asList("shared_metric");
          }

          @Override
          public MetricType getMetricType(String prometheusName) {
            return MetricType.GAUGE;
          }
        };

    registry.register(counter);

    // MultiCollector tries to register a Gauge with the same name as existing Counter
    assertThatThrownBy(() -> registry.register(multiWithGauge))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Conflicting metric types");
  }

  @Test
  void registerOk() {
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
  void registerDuplicateMultiCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(multiCollector);
    // Registering the same instance twice should fail
    assertThatThrownBy(() -> registry.register(multiCollector))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already registered");
  }

  @Test
  void registerOkMultiCollector() {
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
  public void unregister_shouldRemoveLabelSchemaFromRegistrationInfo() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector counterWithPathLabel =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path", "status"));
          }
        };

    Collector counterWithRegionLabel =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(List.of("region"));
          }
        };

    Collector counterWithPathLabelAgain =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests_total").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path", "status"));
          }
        };

    registry.register(counterWithPathLabel);
    registry.register(counterWithRegionLabel);

    registry.unregister(counterWithPathLabel);

    assertThatCode(() -> registry.register(counterWithPathLabelAgain)).doesNotThrowAnyException();
  }

  @Test
  public void scrape_withFilter_shouldValidateDuplicateLabelSchemas() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector collector1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder()
                .name("requests")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .value(100)
                        .labels(io.prometheus.metrics.model.snapshots.Labels.of("path", "/api"))
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }
          // No getMetricType() or getLabelNames() - returns null by default
        };

    Collector collector2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder()
                .name("requests")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .value(200)
                        .labels(io.prometheus.metrics.model.snapshots.Labels.of("path", "/home"))
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }
          // No getMetricType() or getLabelNames() - returns null by default
        };

    // Both collectors can register because they don't provide type/label info
    registry.register(collector1);
    registry.register(collector2);

    assertThatThrownBy(registry::scrape)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("duplicate metric name with identical label schema");

    // Filtered scrape should also detect duplicate label schemas
    assertThatThrownBy(() -> registry.scrape(name -> name.equals("requests")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("duplicate metric name with identical label schema");
  }

  @Test
  public void register_withEmptyLabelSets_shouldDetectDuplicates() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector collector1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("requests").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.GAUGE;
          }

          // getLabelNames() returns null by default
        };

    // Register another collector with same name and type, also no labels
    Collector collector2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("requests").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.GAUGE;
          }

          // getLabelNames() returns null by default
        };

    registry.register(collector1);

    assertThatThrownBy(() -> registry.register(collector2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate metric name with identical label schema");
  }

  @Test
  public void register_withMixedNullAndEmptyLabelSets_shouldDetectDuplicates() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector collector1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("requests").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.GAUGE;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>();
          }
        };

    Collector collector2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("requests").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.GAUGE;
          }

          // getLabelNames() returns null by default
        };

    registry.register(collector1);

    // null and empty should be treated the same
    assertThatThrownBy(() -> registry.register(collector2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate metric name with identical label schema");
  }
}
