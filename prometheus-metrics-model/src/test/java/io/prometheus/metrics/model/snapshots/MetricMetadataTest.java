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
    // sanitizeMetricName strips the reserved _total suffix.
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_total"));
    assertThat(metadata.getName()).isEqualTo("my_events");
  }

  @Test
  void testNameWithInfoSuffix() {
    // sanitizeMetricName strips the reserved _info suffix.
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("target_info"));
    assertThat(metadata.getName()).isEqualTo("target");
  }

  @Test
  void testNameWithCreatedSuffix() {
    // sanitizeMetricName strips the reserved _created suffix.
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_created"));
    assertThat(metadata.getName()).isEqualTo("my_events");
  }

  @Test
  void testNameWithBucketSuffix() {
    // sanitizeMetricName strips the reserved _bucket suffix.
    MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_histogram_bucket"));
    assertThat(metadata.getName()).isEqualTo("my_histogram");
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

  @SuppressWarnings("deprecation")
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

  @SuppressWarnings("deprecation")
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

  @Test
  void builder_noUnit() {
    MetricMetadata m = MetricMetadata.builder().name("requests").help("total requests").build();
    assertThat(m.getName()).isEqualTo("requests");
    assertThat(m.getExpositionBaseName()).isEqualTo("requests");
    assertThat(m.getOriginalName()).isEqualTo("requests");
    assertThat(m.getHelp()).isEqualTo("total requests");
    assertThat(m.getUnit()).isNull();
  }

  @Test
  void builder_unitAppendedWhenAbsent() {
    MetricMetadata m = MetricMetadata.builder().name("requests").unit(Unit.BYTES).build();
    assertThat(m.getName()).isEqualTo("requests_bytes");
    assertThat(m.getExpositionBaseName()).isEqualTo("requests_bytes");
    assertThat(m.getOriginalName()).isEqualTo("requests");
  }

  @Test
  void builder_unitNotDuplicatedWhenPresent() {
    MetricMetadata m = MetricMetadata.builder().name("requests_bytes").unit(Unit.BYTES).build();
    assertThat(m.getName()).isEqualTo("requests_bytes");
    assertThat(m.getExpositionBaseName()).isEqualTo("requests_bytes");
    assertThat(m.getOriginalName()).isEqualTo("requests_bytes");
  }

  @Test
  void builder_counterSuffixAppended() {
    MetricMetadata m = MetricMetadata.builder().name("requests").counterSuffix(true).build();
    assertThat(m.getName()).isEqualTo("requests");
    assertThat(m.getExpositionBaseName()).isEqualTo("requests_total");
    assertThat(m.getOriginalName()).isEqualTo("requests");
  }

  @Test
  void builder_counterSuffixAndUnit() {
    MetricMetadata m =
        MetricMetadata.builder().name("requests").unit(Unit.BYTES).counterSuffix(true).build();
    assertThat(m.getName()).isEqualTo("requests_bytes");
    assertThat(m.getExpositionBaseName()).isEqualTo("requests_bytes_total");
    assertThat(m.getOriginalName()).isEqualTo("requests");
  }

  @Test
  void builder_utf8NameWithCounterSuffix() {
    MetricMetadata m = MetricMetadata.builder().name("my.requests").counterSuffix(true).build();
    assertThat(m.getName()).isEqualTo("my.requests");
    assertThat(m.getExpositionBaseName()).isEqualTo("my.requests_total");
    assertThat(m.getPrometheusName()).isEqualTo("my_requests");
    assertThat(m.getExpositionBasePrometheusName()).isEqualTo("my_requests_total");
  }

  @Test
  void builder_nonCounterExpositionBaseEqualsName() {
    MetricMetadata m = MetricMetadata.builder().name("active_connections").build();
    assertThat(m.getExpositionBaseName()).isEqualTo(m.getName());
  }

  @Test
  void builder_nameRequired() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> MetricMetadata.builder().help("help").build())
        .withMessage("name is required");
  }
}
