package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.MetricType;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricFamilyDescriptor;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetricWithFixedMetadataTest {

  @Test
  @SuppressWarnings("deprecation")
  void getMetricFamilyDescriptorAdaptsDeprecatedMetricTypeOverride() {
    LegacyMetric metric =
        LegacyMetric.builder()
            .name("legacy.metric")
            .constLabels(Labels.of("const.label", "value"))
            .labelNames("dynamic.label")
            .build();

    MetricFamilyDescriptor descriptor = metric.getMetricFamilyDescriptor();

    assertThat(descriptor).isNotNull();
    assertThat(descriptor.getType()).isEqualTo(MetricType.GAUGE);
    assertThat(descriptor.getPrometheusName()).isEqualTo("legacy_metric");
    assertThat(descriptor.getLabelNames())
        .containsExactlyInAnyOrder("const_label", "dynamic_label");
  }

  private static class LegacyMetric extends MetricWithFixedMetadata {

    private LegacyMetric(Builder builder) {
      super(builder);
    }

    private static Builder builder() {
      return new Builder();
    }

    @Override
    public MetricSnapshot collect() {
      throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Use {@link #getMetricFamilyDescriptor()} instead.
     */
    @Override
    @Deprecated
    public MetricType getMetricType() {
      return MetricType.GAUGE;
    }

    private static class Builder extends MetricWithFixedMetadata.Builder<Builder, LegacyMetric> {

      private Builder() {
        super(Collections.emptyList(), PrometheusProperties.builder().build());
      }

      @Override
      public LegacyMetric build() {
        return new LegacyMetric(this);
      }

      @Override
      protected Builder self() {
        return this;
      }
    }
  }
}
