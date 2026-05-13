package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.prometheus.metrics.model.registry.MetricType;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MetricFamilyDescriptorTest {

  @Test
  void counterDescriptorDerivesMetadata() {
    MetricFamilyDescriptor descriptor =
        MetricFamilyDescriptor.counter("events_total")
            .help("help")
            .unit(Unit.SECONDS)
            .labelNames(Arrays.asList("method", "status"))
            .build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.COUNTER);
    assertThat(descriptor.getPrometheusName()).isEqualTo("events_seconds");
    assertThat(descriptor.getLabelNames()).containsExactly("method", "status");
    assertThat(descriptor.getMetadata().getName()).isEqualTo("events_seconds");
    assertThat(descriptor.getMetadata().getExpositionBaseName()).isEqualTo("events_total_seconds");
    assertThat(descriptor.getMetadata().getOriginalName()).isEqualTo("events_total");
  }

  @Test
  void infoDescriptorDerivesMetadata() {
    MetricFamilyDescriptor descriptor =
        MetricFamilyDescriptor.info("jvm_info").help("JVM info").labelName("vendor").build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.INFO);
    assertThat(descriptor.getPrometheusName()).isEqualTo("jvm");
    assertThat(descriptor.getLabelNames()).containsExactly("vendor");
    assertThat(descriptor.getMetadata().getName()).isEqualTo("jvm");
    assertThat(descriptor.getMetadata().getExpositionBaseName()).isEqualTo("jvm_info");
    assertThat(descriptor.getMetadata().getOriginalName()).isEqualTo("jvm_info");
  }

  @Test
  void gaugeDescriptorKeepsLiteralName() {
    MetricFamilyDescriptor descriptor = MetricFamilyDescriptor.gauge("test_total").build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.GAUGE);
    assertThat(descriptor.getPrometheusName()).isEqualTo("test_total");
    assertThat(descriptor.getMetadata().getExpositionBaseName()).isEqualTo("test_total");
    assertThat(descriptor.getMetadata().getOriginalName()).isEqualTo("test_total");
  }

  @Test
  void genericFactoryUsesTypedBuilderSemantics() {
    MetricFamilyDescriptor counter = MetricFamilyDescriptor.of(MetricType.COUNTER, "http_requests_total").build();
    MetricFamilyDescriptor info = MetricFamilyDescriptor.of(MetricType.INFO, "build_info").build();

    assertThat(counter.getPrometheusName()).isEqualTo("http_requests");
    assertThat(info.getPrometheusName()).isEqualTo("build");
  }

  @Test
  void infoDescriptorRejectsUnit() {
    assertThatThrownBy(() -> MetricFamilyDescriptor.info("jvm_info").unit(Unit.SECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Info metric cannot have a unit.");
  }

  @Test
  void stateSetDescriptorRejectsUnit() {
    assertThatThrownBy(() -> MetricFamilyDescriptor.stateSet("feature_flags").unit(Unit.SECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("State set metric cannot have a unit.");
  }
}
