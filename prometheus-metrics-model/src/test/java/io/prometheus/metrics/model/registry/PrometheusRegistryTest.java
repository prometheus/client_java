package io.prometheus.metrics.model.registry;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.Arrays;
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
  void register_duplicateName_withoutTypeInfo_notAllowed() {
    PrometheusRegistry registry = new PrometheusRegistry();

    registry.register(noName);

    assertThatThrownBy(() -> registry.register(noName))
        .hasMessageContaining("Collector instance is already registered");
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
  public void register_nullTypeAndLabels_fallbackToCollect_validatesFromSnapshot() {
    PrometheusRegistry registry = new PrometheusRegistry();

    // Collector without getMetricType() and getLabelNames() - returns null (default).
    // Registry falls back to collect() and derives type/labels from snapshot.
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

    registry.register(legacyCollector1);
    // Second collector has same name but different type (from snapshot) - should fail
    assertThatThrownBy(() -> registry.register(legacyCollector2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Conflicting metric types");
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
            return new HashSet<>(asList("region"));
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

  @Test
  public void register_sameName_differentHelp_notAllowed() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector withHelpOne =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests").help("First help").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path"));
          }

          @Override
          public MetricMetadata getMetadata() {
            return new MetricMetadata("requests", "First help", null);
          }
        };

    Collector withHelpTwo =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests").help("Second help").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("status"));
          }

          @Override
          public MetricMetadata getMetadata() {
            return new MetricMetadata("requests", "Second help", null);
          }
        };

    registry.register(withHelpOne);
    assertThatThrownBy(() -> registry.register(withHelpTwo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Conflicting help strings");
  }

  @Test
  public void register_sameName_sameHelpAndUnit_allowed() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector withPath =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests").help("Total requests").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("path"));
          }

          @Override
          public MetricMetadata getMetadata() {
            return new MetricMetadata("requests", "Total requests", null);
          }
        };

    Collector withStatus =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder().name("requests").help("Total requests").build();
          }

          @Override
          public String getPrometheusName() {
            return "requests";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.COUNTER;
          }

          @Override
          public Set<String> getLabelNames() {
            return new HashSet<>(asList("status"));
          }

          @Override
          public MetricMetadata getMetadata() {
            return new MetricMetadata("requests", "Total requests", null);
          }
        };

    registry.register(withPath);
    assertThatCode(() -> registry.register(withStatus)).doesNotThrowAnyException();
  }
}
