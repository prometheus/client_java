package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNames.sanitizeMetricName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class MetricMetadataTest {

  @Test
  public void testEmptyName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricMetadata(""));
  }

  @Test
  public void testNullName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricMetadata(null));
  }

  @Test
  public void testSanitizationIllegalCharacters() {
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
  public void testSanitizationCounter() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_total"));
    assertThat(metadata.getName()).isEqualTo("my_events");
  }

  @Test
  public void testSanitizationInfo() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("target_info"));
    assertThat(metadata.getName()).isEqualTo("target");
  }

  @Test
  public void testSanitizationWeirdCornerCase() {
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("_total_created"));
    assertThat(metadata.getName()).isEqualTo("total");
  }

  @Test
  public void testSanitizeEmptyString() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeMetricName(""));
  }

  @Test
  public void testUnitSuffixRequired() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricMetadata("my_counter", "help", Unit.SECONDS));
  }

  @Test
  public void testUnitSuffixAdded() {
    new MetricMetadata(sanitizeMetricName("my_counter", Unit.SECONDS), "help", Unit.SECONDS);
  }

  @Test
  public void testUnitNotDuplicated() {
    assertThat(sanitizeMetricName("my_counter_bytes", Unit.BYTES)).isEqualTo("my_counter_bytes");
  }
}
