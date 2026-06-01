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
            .labelNames(Arrays.asList("method.name", "status"))
            .build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.COUNTER);
    assertThat(descriptor.getPrometheusName()).isEqualTo("events_seconds");
    assertThat(descriptor.getLabelNames()).containsExactly("method_name", "status");
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
  void histogramDescriptorKeepsLiteralName() {
    MetricFamilyDescriptor descriptor =
        MetricFamilyDescriptor.histogram("request_duration_seconds")
            .help("Request duration")
            .labelName("method")
            .build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.HISTOGRAM);
    assertThat(descriptor.getPrometheusName()).isEqualTo("request_duration_seconds");
    assertThat(descriptor.getLabelNames()).containsExactly("method");
  }

  @Test
  void summaryDescriptorKeepsLiteralName() {
    MetricFamilyDescriptor descriptor =
        MetricFamilyDescriptor.summary("request_size_bytes")
            .help("Request size")
            .labelName("method")
            .build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.SUMMARY);
    assertThat(descriptor.getPrometheusName()).isEqualTo("request_size_bytes");
    assertThat(descriptor.getLabelNames()).containsExactly("method");
  }

  @Test
  void stateSetDescriptorKeepsLiteralName() {
    MetricFamilyDescriptor descriptor =
        MetricFamilyDescriptor.stateSet("feature_flags").help("Flags").labelName("service").build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.STATESET);
    assertThat(descriptor.getPrometheusName()).isEqualTo("feature_flags");
    assertThat(descriptor.getLabelNames()).containsExactly("service");
  }

  @Test
  void unknownDescriptorKeepsLiteralName() {
    MetricFamilyDescriptor descriptor =
        MetricFamilyDescriptor.unknown("vendor_metric").help("Vendor metric").build();

    assertThat(descriptor.getType()).isEqualTo(MetricType.UNKNOWN);
    assertThat(descriptor.getPrometheusName()).isEqualTo("vendor_metric");
  }

  @Test
  void genericFactoryUsesTypedBuilderSemanticsForAllKinds() {
    MetricFamilyDescriptor counter =
        MetricFamilyDescriptor.of(MetricType.COUNTER, "http_requests_total").build();
    MetricFamilyDescriptor gauge =
        MetricFamilyDescriptor.of(MetricType.GAUGE, "queue_depth").build();
    MetricFamilyDescriptor histogram =
        MetricFamilyDescriptor.of(MetricType.HISTOGRAM, "request_duration_seconds").build();
    MetricFamilyDescriptor summary =
        MetricFamilyDescriptor.of(MetricType.SUMMARY, "request_size_bytes").build();
    MetricFamilyDescriptor info = MetricFamilyDescriptor.of(MetricType.INFO, "build_info").build();
    MetricFamilyDescriptor stateSet =
        MetricFamilyDescriptor.of(MetricType.STATESET, "feature_flags").build();
    MetricFamilyDescriptor unknown =
        MetricFamilyDescriptor.of(MetricType.UNKNOWN, "vendor_metric").build();

    assertThat(counter.getPrometheusName()).isEqualTo("http_requests");
    assertThat(gauge.getPrometheusName()).isEqualTo("queue_depth");
    assertThat(histogram.getPrometheusName()).isEqualTo("request_duration_seconds");
    assertThat(summary.getPrometheusName()).isEqualTo("request_size_bytes");
    assertThat(info.getPrometheusName()).isEqualTo("build");
    assertThat(stateSet.getPrometheusName()).isEqualTo("feature_flags");
    assertThat(unknown.getPrometheusName()).isEqualTo("vendor_metric");
  }

  @Test
  void infoDescriptorRejectsUnit() {
    assertThatThrownBy(() -> MetricFamilyDescriptor.info("jvm_info").unit(Unit.SECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Info metric cannot have a unit.");
  }

  @Test
  void buildersRejectNullHelp() {
    assertThatThrownBy(() -> MetricFamilyDescriptor.counter("events_total").help(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Missing required field: help is null");
  }

  @Test
  void buildersRejectNullUnit() {
    assertThatThrownBy(() -> MetricFamilyDescriptor.counter("events_total").unit(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Missing required field: unit is null");
  }

  @Test
  void stateSetDescriptorRejectsUnit() {
    assertThatThrownBy(() -> MetricFamilyDescriptor.stateSet("feature_flags").unit(Unit.SECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("State set metric cannot have a unit.");
  }
}
