package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.config.OpenMetrics2Properties;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class OpenMetrics2TextFormatWriterTest {

  @Test
  void testContentTypeWithContentNegotiationDisabled() {
    OpenMetrics2TextFormatWriter writer =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().contentNegotiation(false).build())
            .build();

    // Should masquerade as OM1 when contentNegotiation is disabled
    assertThat(writer.getContentType())
        .isEqualTo("application/openmetrics-text; version=1.0.0; charset=utf-8");
  }

  @Test
  void testContentTypeWithContentNegotiationEnabled() {
    OpenMetrics2TextFormatWriter writer =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().contentNegotiation(true).build())
            .build();

    assertThat(writer.getContentType())
        .isEqualTo("application/openmetrics-text; version=2.0.0; charset=utf-8");
  }

  @Test
  void testContentTypeDefault() {
    OpenMetrics2TextFormatWriter writer = OpenMetrics2TextFormatWriter.create();

    // Default should masquerade as OM1 for compatibility
    assertThat(writer.getContentType())
        .isEqualTo("application/openmetrics-text; version=1.0.0; charset=utf-8");
  }

  @Test
  void testAcceptsOpenMetricsHeader() {
    OpenMetrics2TextFormatWriter writer = OpenMetrics2TextFormatWriter.create();

    assertThat(writer.accepts("application/openmetrics-text")).isTrue();
    assertThat(writer.accepts("application/openmetrics-text; version=1.0.0")).isTrue();
    assertThat(writer.accepts("application/openmetrics-text; version=2.0.0")).isTrue();
    assertThat(
            writer.accepts(
                "text/html,application/xhtml+xml,application/xml;q=0.9,application/openmetrics-text;q=0.8"))
        .isTrue();

    assertThat(writer.accepts(null)).isFalse();
    assertThat(writer.accepts("text/plain")).isFalse();
    assertThat(writer.accepts("application/json")).isFalse();
    assertThat(writer.accepts("application/vnd.google.protobuf")).isFalse();
  }

  @Test
  void testGetOpenMetrics2Properties() {
    OpenMetrics2Properties props =
        OpenMetrics2Properties.builder().contentNegotiation(true).compositeValues(true).build();

    OpenMetrics2TextFormatWriter writer =
        OpenMetrics2TextFormatWriter.builder().setOpenMetrics2Properties(props).build();

    assertThat(writer.getOpenMetrics2Properties()).isEqualTo(props);
    assertThat(writer.getOpenMetrics2Properties().getContentNegotiation()).isTrue();
    assertThat(writer.getOpenMetrics2Properties().getCompositeValues()).isTrue();
  }

  @Test
  void testOutputIdenticalToOM1ForCounter() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            CounterSnapshot.builder()
                .name("my_counter_seconds")
                .help("Test counter")
                .unit(Unit.SECONDS)
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .value(42.0)
                        .labels(Labels.of("method", "GET"))
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1ForGauge() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            GaugeSnapshot.builder()
                .name("my_gauge")
                .help("Test gauge")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .value(123.45)
                        .labels(Labels.of("status", "active"))
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1ForHistogram() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            HistogramSnapshot.builder()
                .name("my_histogram")
                .help("Test histogram")
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .sum(1.0)
                        .classicHistogramBuckets(
                            io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets.builder()
                                .bucket(0.1, 0)
                                .bucket(1.0, 1)
                                .bucket(10.0, 2)
                                .bucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1ForSummary() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            SummarySnapshot.builder()
                .name("my_summary")
                .help("Test summary")
                .dataPoint(
                    SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .sum(100.0)
                        .count(10)
                        .quantiles(
                            io.prometheus.metrics.model.snapshots.Quantiles.builder()
                                .quantile(0.5, 50.0)
                                .quantile(0.9, 90.0)
                                .quantile(0.99, 99.0)
                                .build())
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1ForInfo() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            InfoSnapshot.builder()
                .name("my")
                .help("Test info")
                .dataPoint(
                    InfoSnapshot.InfoDataPointSnapshot.builder()
                        .labels(Labels.of("version", "1.0", "platform", "linux"))
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1ForStateSet() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            StateSetSnapshot.builder()
                .name("my_stateset")
                .help("Test stateset")
                .dataPoint(
                    StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .state("starting", false)
                        .state("running", true)
                        .state("stopped", false)
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1WithExemplars() throws IOException {
    Exemplar exemplar =
        Exemplar.builder()
            .value(100.0)
            .traceId("abcde")
            .spanId("12345")
            .timestampMillis(1672850685829L)
            .build();

    MetricSnapshots snapshots =
        MetricSnapshots.of(
            CounterSnapshot.builder()
                .name("requests")
                .help("Total requests")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .value(1000.0)
                        .exemplar(exemplar)
                        .build())
                .build());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testOutputIdenticalToOM1WithCreatedTimestamps() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            CounterSnapshot.builder()
                .name("my_counter")
                .help("Test counter")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .value(42.0)
                        .createdTimestampMillis(1672850385800L)
                        .build())
                .build());

    OpenMetricsTextFormatWriter om1Writer =
        OpenMetricsTextFormatWriter.builder().setCreatedTimestampsEnabled(true).build();

    OpenMetrics2TextFormatWriter om2Writer =
        OpenMetrics2TextFormatWriter.builder().setCreatedTimestampsEnabled(true).build();

    String om1Output = write(snapshots, om1Writer);
    String om2Output = write(snapshots, om2Writer);

    assertThat(om2Output).isEqualTo(om1Output);
  }

  @Test
  void testEmptySnapshot() throws IOException {
    MetricSnapshots snapshots = new MetricSnapshots();

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
    assertThat(om2Output).isEqualTo("# EOF\n");
  }

  private String writeWithOM1(MetricSnapshots snapshots) throws IOException {
    OpenMetricsTextFormatWriter writer = OpenMetricsTextFormatWriter.create();
    return write(snapshots, writer);
  }

  private String writeWithOM2(MetricSnapshots snapshots) throws IOException {
    OpenMetrics2TextFormatWriter writer = OpenMetrics2TextFormatWriter.create();
    return write(snapshots, writer);
  }

  private String write(MetricSnapshots snapshots, ExpositionFormatWriter writer)
      throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writer.write(out, snapshots, EscapingScheme.ALLOW_UTF8);
    return out.toString(StandardCharsets.UTF_8);
  }
}
