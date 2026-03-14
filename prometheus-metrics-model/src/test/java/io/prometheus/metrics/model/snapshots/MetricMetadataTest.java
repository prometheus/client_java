package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class MetricMetadataTest {

  @Test
  void testEmptyName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricMetadata(""));
  }

  @Test
  void testNullName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricMetadata(null));
  }

  @Test
  void testSanitizationIllegalCharacters() {
    MetricMetadata metadata =
        new MetricMetadata(
            sanitizeMetricName("my_namespace/http.server.duration", Unit.SECONDS),
            "help string",
            Unit.SECONDS);
    assertThat(metadata.getName()).isEqualTo("my_namespace/http.server.duration_seconds");
    assertThat(metadata.getPrometheusName()).isEqualTo("my_namespace_http_server_duration_seconds");
    assertThat(metadata.getHelp()).isEqualTo("help string");
    assertThat(metadata.getUnit()).hasToString("seconds");
  }

  @Test
  void testNameWithTotalSuffix() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_total"));
    assertThat(metadata.getName()).isEqualTo("my_events_total");
  }

  @Test
  void testNameWithInfoSuffix() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("target_info"));
    assertThat(metadata.getName()).isEqualTo("target_info");
  }

  @Test
  void testNameWithCreatedSuffix() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_created"));
    assertThat(metadata.getName()).isEqualTo("my_events_created");
  }

  @Test
  void testNameWithBucketSuffix() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_histogram_bucket"));
    assertThat(metadata.getName()).isEqualTo("my_histogram_bucket");
  }

  @Test
  void testSanitizeEmptyString() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeMetricName(""));
  }

  @Test
  void testUnitSuffixRequired() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricMetadata("my_counter", "help", Unit.SECONDS));
  }

  @Test
  void testUnitSuffixAdded() {
    new MetricMetadata(sanitizeMetricName("my_counter", Unit.SECONDS), "help", Unit.SECONDS);
  }

  @Test
  void testUnitNotDuplicated() {
    assertThat(sanitizeMetricName("my_counter_bytes", Unit.BYTES)).isEqualTo("my_counter_bytes");
  }

  @Test
  void testFiveArgConstructor() {
    MetricMetadata metadata =
        new MetricMetadata("req_bytes", "req_bytes", "req", "help", Unit.BYTES);
    assertThat(metadata.getName()).isEqualTo("req_bytes");
    assertThat(metadata.getExpositionBaseName()).isEqualTo("req_bytes");
    assertThat(metadata.getOriginalName()).isEqualTo("req");
    assertThat(metadata.getHelp()).isEqualTo("help");
    assertThat(metadata.getUnit()).isEqualTo(Unit.BYTES);
  }

  @Test
  void testFourArgConstructorDefaultsOriginalName() {
    MetricMetadata metadata = new MetricMetadata("req_bytes", "req_bytes", "help", Unit.BYTES);
    assertThat(metadata.getOriginalName()).isEqualTo("req_bytes");
    assertThat(metadata.getExpositionBaseName()).isEqualTo("req_bytes");
  }

  @Test
  void testThreeArgConstructorDefaultsOriginalName() {
    MetricMetadata metadata = new MetricMetadata("req_bytes", "help", Unit.BYTES);
    assertThat(metadata.getOriginalName()).isEqualTo("req_bytes");
    assertThat(metadata.getExpositionBaseName()).isEqualTo("req_bytes");
  }
}
