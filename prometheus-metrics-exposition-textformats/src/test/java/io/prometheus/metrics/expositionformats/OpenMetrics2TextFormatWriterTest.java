package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.config.OpenMetrics2Properties;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantiles;
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
  void testCounterNoTotalSuffix() throws IOException {
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

    String om2Output = writeWithOM2(snapshots);

    // OM2: name as provided, no _total appending
    assertThat(om2Output)
        .isEqualTo(
            "# TYPE my_counter_seconds counter\n"
                + "# UNIT my_counter_seconds seconds\n"
                + "# HELP my_counter_seconds Test counter\n"
                + "my_counter_seconds{method=\"GET\"} 42.0\n"
                + "# EOF\n");
  }

  @Test
  void testCounterWithTotalSuffix() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            CounterSnapshot.builder()
                .name("requests_total")
                .help("Total requests")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(100.0).build())
                .build());

    String om2Output = writeWithOM2(snapshots);

    // OM2: preserves _total if user provided it
    assertThat(om2Output)
        .isEqualTo(
            "# TYPE requests_total counter\n"
                + "# HELP requests_total Total requests\n"
                + "requests_total 100.0\n"
                + "# EOF\n");
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
  void testInfoHelpNameMatchesMeterName() throws IOException {
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

    String om2Output = writeWithOM2(snapshots);

    // OM2: TYPE/HELP use the full name including _info (help name == meter name)
    assertThat(om2Output)
        .isEqualTo(
            "# TYPE my_info info\n"
                + "# HELP my_info Test info\n"
                + "my_info{platform=\"linux\",version=\"1.0\"} 1\n"
                + "# EOF\n");
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
  void testCounterWithExemplars() throws IOException {
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

    String om2Output = writeWithOM2(snapshots);

    // OM2: no _total, but exemplar is preserved
    assertThat(om2Output)
        .isEqualTo(
            "# TYPE requests counter\n"
                + "# HELP requests Total requests\n"
                + "requests 1000.0 # {span_id=\"12345\",trace_id=\"abcde\"}"
                + " 100.0 1672850685.829\n"
                + "# EOF\n");
  }

  @Test
  void testCounterWithCreatedTimestamps() throws IOException {
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

    OpenMetrics2TextFormatWriter om2Writer =
        OpenMetrics2TextFormatWriter.builder().setCreatedTimestampsEnabled(true).build();

    String om2Output = write(snapshots, om2Writer);

    // OM2: no _total, start timestamp uses st@ inline.
    assertThat(om2Output)
        .isEqualTo(
            "# TYPE my_counter counter\n"
                + "# HELP my_counter Test counter\n"
                + "my_counter 42.0 st@1672850385.800\n"
                + "# EOF\n");
  }

  @Test
  void testEmptySnapshot() throws IOException {
    MetricSnapshots snapshots = new MetricSnapshots();

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om2Output).isEqualTo(om1Output);
    assertThat(om2Output).isEqualTo("# EOF\n");
  }

  @Test
  void testCompositeHistogram() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            HistogramSnapshot.builder()
                .name("http_request_duration_seconds")
                .help("Request duration")
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .sum(324789.3)
                        .classicHistogramBuckets(
                            ClassicHistogramBuckets.builder()
                                .bucket(0.1, 8)
                                .bucket(0.25, 2)
                                .bucket(0.5, 1)
                                .bucket(1.0, 3)
                                .bucket(Double.POSITIVE_INFINITY, 3)
                                .build())
                        .build())
                .build());

    String output = writeWithCompositeValues(snapshots);

    assertThat(output)
        .isEqualTo(
            "# TYPE http_request_duration_seconds histogram\n"
                + "# HELP http_request_duration_seconds Request duration\n"
                + "http_request_duration_seconds"
                + " {count:17,sum:324789.3,bucket:[0.1:8,0.25:10,0.5:11,1.0:14,+Inf:17]}\n"
                + "# EOF\n");
  }

  @Test
  void testCompositeHistogramWithLabelsTimestampAndCreated() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            HistogramSnapshot.builder()
                .name("foo")
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .labels(Labels.of("method", "GET"))
                        .sum(324789.3)
                        .classicHistogramBuckets(
                            ClassicHistogramBuckets.builder()
                                .bucket(0.1, 8)
                                .bucket(Double.POSITIVE_INFINITY, 9)
                                .build())
                        .createdTimestampMillis(1520430000123L)
                        .scrapeTimestampMillis(1520879607789L)
                        .build())
                .build());

    String output = writeWithCompositeValues(snapshots);

    assertThat(output)
        .isEqualTo(
            "# TYPE foo histogram\n"
                + "foo{method=\"GET\"} {count:17,sum:324789.3,bucket:[0.1:8,+Inf:17]}"
                + " 1520879607.789 st@1520430000.123\n"
                + "# EOF\n");
  }

  @Test
  void testCompositeHistogramWithExemplar() throws IOException {
    Exemplar exemplar =
        Exemplar.builder().value(0.67).traceId("shaZ8oxi").timestampMillis(1520879607789L).build();

    MetricSnapshots snapshots =
        MetricSnapshots.of(
            HistogramSnapshot.builder()
                .name("foo")
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .sum(1.5)
                        .classicHistogramBuckets(
                            ClassicHistogramBuckets.builder()
                                .bucket(1.0, 1)
                                .bucket(Double.POSITIVE_INFINITY, 0)
                                .build())
                        .exemplars(Exemplars.of(exemplar))
                        .build())
                .build());

    String output = writeWithCompositeValues(snapshots);

    assertThat(output)
        .isEqualTo(
            "# TYPE foo histogram\n"
                + "foo {count:1,sum:1.5,bucket:[1.0:1,+Inf:1]}"
                + " # {trace_id=\"shaZ8oxi\"} 0.67 1520879607.789\n"
                + "# EOF\n");
  }

  @Test
  void testCompositeGaugeHistogram() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            HistogramSnapshot.builder()
                .name("queue_size")
                .gaugeHistogram(true)
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .sum(3289.3)
                        .classicHistogramBuckets(
                            ClassicHistogramBuckets.builder()
                                .bucket(0.1, 20)
                                .bucket(1.0, 14)
                                .bucket(Double.POSITIVE_INFINITY, 8)
                                .build())
                        .build())
                .build());

    String output = writeWithCompositeValues(snapshots);

    // GaugeHistogram uses gcount/gsum per spec
    assertThat(output)
        .isEqualTo(
            "# TYPE queue_size gaugehistogram\n"
                + "queue_size {gcount:42,gsum:3289.3,bucket:[0.1:20,1.0:34,+Inf:42]}\n"
                + "# EOF\n");
  }

  @Test
  void testCompositeSummary() throws IOException {
    MetricSnapshots snapshots =
        MetricSnapshots.of(
            SummarySnapshot.builder()
                .name("rpc_duration_seconds")
                .help("RPC duration")
                .dataPoint(
                    SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .count(17)
                        .sum(324789.3)
                        .quantiles(
                            Quantiles.builder().quantile(0.95, 123.7).quantile(0.99, 150.0).build())
                        .build())
                .build());

    String output = writeWithCompositeValues(snapshots);

    assertThat(output)
        .isEqualTo(
            "# TYPE rpc_duration_seconds summary\n"
                + "# HELP rpc_duration_seconds RPC duration\n"
                + "rpc_duration_seconds"
                + " {count:17,sum:324789.3,quantile:[0.95:123.7,0.99:150.0]}\n"
                + "# EOF\n");
  }

  @Test
  void testCompositeSummaryWithCreatedAndExemplar() throws IOException {
    Exemplar exemplar1 =
        Exemplar.builder().value(0.5).traceId("abc123").timestampMillis(1520879607000L).build();
    Exemplar exemplar2 =
        Exemplar.builder().value(1.5).traceId("def456").timestampMillis(1520879608000L).build();

    MetricSnapshots snapshots =
        MetricSnapshots.of(
            SummarySnapshot.builder()
                .name("rpc_duration_seconds")
                .dataPoint(
                    SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .count(10)
                        .sum(100.0)
                        .createdTimestampMillis(1520430000000L)
                        .exemplars(Exemplars.of(exemplar1, exemplar2))
                        .build())
                .build());

    String output = writeWithCompositeValues(snapshots);

    assertThat(output)
        .isEqualTo(
            "# TYPE rpc_duration_seconds summary\n"
                + "rpc_duration_seconds {count:10,sum:100.0,quantile:[]} st@1520430000.000"
                + " # {trace_id=\"abc123\"} 0.5 1520879607.000"
                + " # {trace_id=\"def456\"} 1.5 1520879608.000\n"
                + "# EOF\n");
  }

  @Test
  void testExemplarComplianceSkipsExemplarWithoutTimestamp() throws IOException {
    Exemplar exemplarWithTs =
        Exemplar.builder().value(1.0).traceId("aaa").timestampMillis(1672850685829L).build();
    Exemplar exemplarWithoutTs = Exemplar.builder().value(2.0).traceId("bbb").build();

    OpenMetrics2TextFormatWriter complianceWriter =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().exemplarCompliance(true).build())
            .build();
    OpenMetrics2TextFormatWriter defaultWriter = OpenMetrics2TextFormatWriter.create();

    MetricSnapshots withTs =
        MetricSnapshots.of(
            CounterSnapshot.builder()
                .name("requests")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .value(1.0)
                        .exemplar(exemplarWithTs)
                        .build())
                .build());
    MetricSnapshots withoutTs =
        MetricSnapshots.of(
            CounterSnapshot.builder()
                .name("requests")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .value(1.0)
                        .exemplar(exemplarWithoutTs)
                        .build())
                .build());

    // Compliance mode: exemplar WITH timestamp is emitted
    assertThat(write(withTs, complianceWriter)).contains("# {trace_id=\"aaa\"} 1.0 1672850685.829");

    // Compliance mode: exemplar WITHOUT timestamp is skipped
    assertThat(write(withoutTs, complianceWriter)).doesNotContain("# {");

    // Default mode: exemplar without timestamp is still emitted (just no timestamp)
    assertThat(write(withoutTs, defaultWriter)).contains("# {trace_id=\"bbb\"} 2.0\n");
  }

  @Test
  void testExemplarComplianceSkipsHistogramExemplarWithoutTimestamp() throws IOException {
    Exemplar exemplarWithoutTs = Exemplar.builder().value(2.0).traceId("bbb").build();
    OpenMetrics2TextFormatWriter complianceWriter =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().exemplarCompliance(true).build())
            .build();

    MetricSnapshots snapshots =
        MetricSnapshots.of(
            HistogramSnapshot.builder()
                .name("requests")
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .sum(2.0)
                        .classicHistogramBuckets(
                            ClassicHistogramBuckets.builder()
                                .bucket(1.0, 1)
                                .bucket(Double.POSITIVE_INFINITY, 1)
                                .build())
                        .exemplars(Exemplars.of(exemplarWithoutTs))
                        .build())
                .build());

    assertThat(write(snapshots, complianceWriter)).doesNotContain("# {");
  }

  @Test
  void testExemplarComplianceSkipsSummaryExemplarWithoutTimestamp() throws IOException {
    Exemplar exemplarWithoutTs = Exemplar.builder().value(2.0).traceId("bbb").build();
    OpenMetrics2TextFormatWriter complianceWriter =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().exemplarCompliance(true).build())
            .build();

    MetricSnapshots snapshots =
        MetricSnapshots.of(
            SummarySnapshot.builder()
                .name("requests")
                .dataPoint(
                    SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .count(1)
                        .sum(2.0)
                        .exemplars(Exemplars.of(exemplarWithoutTs))
                        .build())
                .build());

    assertThat(write(snapshots, complianceWriter)).doesNotContain("# {");
  }

  private String writeWithCompositeValues(MetricSnapshots snapshots) throws IOException {
    OpenMetrics2TextFormatWriter writer =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().compositeValues(true).build())
            .build();
    return write(snapshots, writer);
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
