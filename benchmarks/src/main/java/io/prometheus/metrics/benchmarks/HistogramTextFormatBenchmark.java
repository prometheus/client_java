package io.prometheus.metrics.benchmarks;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmarks for writing a classic histogram (10 label combinations × 12 buckets) to text formats.
 *
 * <p>Three variants per format:
 *
 * <ul>
 *   <li>{@code writeToByteArray} — OutputStream path, new BufferedWriter created per call (~41
 *       KB/op; IO stack allocation dominates).
 *   <li>{@code writeToNull} — OutputStream path to /dev/null, new BufferedWriter still created per
 *       call (same allocation as writeToByteArray; shows ByteArrayOutputStream write cost is
 *       negligible).
 *   <li>{@code reusingWriter} — Writer path, BufferedWriter reused across calls (~18 KB/op; saves
 *       the ~25 KB BufferedWriter char[] buffer and OutputStreamWriter per call).
 *   <li>{@code reusingWriterToNull} — Writer path, BufferedWriter reused, output to /dev/null.
 *       Lowest-allocation floor; isolates pure formatting CPU cost with zero IO stack overhead.
 * </ul>
 *
 * <p>Key allocation sources:
 *
 * <ul>
 *   <li>BufferedWriter internal char[8192] buffer: 16,384 bytes per call (eliminated by reuse).
 *   <li>OutputStreamWriter + StreamEncoder state: ~1–2 KB per call (eliminated by reuse).
 *   <li>Remaining ~18 KB/op in reuse variants: ByteArrayOutputStream byte[] growth + any per-call
 *       String allocations remaining in the format path.
 * </ul>
 */
@Fork(3)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
public class HistogramTextFormatBenchmark {

  private static final MetricSnapshots SNAPSHOTS;

  static {
    double[] upperBounds = {
      .005, .01, .025, .05, .1, .25, .5, 1.0, 2.5, 5.0, 10.0, Double.POSITIVE_INFINITY
    };
    Number[] counts = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L};
    ClassicHistogramBuckets buckets = ClassicHistogramBuckets.of(upperBounds, counts);

    HistogramSnapshot.Builder builder =
        HistogramSnapshot.builder().name("http_request_duration_seconds");

    for (int i = 0; i < 10; i++) {
      builder.dataPoint(
          HistogramDataPointSnapshot.builder()
              .classicHistogramBuckets(buckets)
              .labels(Labels.of("status", "value_" + i))
              .sum(123.456)
              .createdTimestampMillis(1000L)
              .build());
    }

    SNAPSHOTS = MetricSnapshots.of(builder.build());
  }

  private static final OpenMetricsTextFormatWriter OPEN_METRICS_TEXT_FORMAT_WRITER =
      OpenMetricsTextFormatWriter.create();
  private static final PrometheusTextFormatWriter PROMETHEUS_TEXT_FORMAT_WRITER =
      PrometheusTextFormatWriter.create();

  @State(Scope.Benchmark)
  public static class WriterState {

    final ByteArrayOutputStream byteArrayOutputStream;

    public WriterState() {
      this.byteArrayOutputStream = new ByteArrayOutputStream();
    }
  }

  @State(Scope.Benchmark)
  public static class ReusableWriterState {

    final ByteArrayOutputStream openMetricsByteArrayOutputStream;
    final ByteArrayOutputStream prometheusByteArrayOutputStream;
    final BufferedWriter openMetricsWriter;
    final BufferedWriter prometheusWriter;

    public ReusableWriterState() {
      this.openMetricsByteArrayOutputStream = new ByteArrayOutputStream();
      this.prometheusByteArrayOutputStream = new ByteArrayOutputStream();
      this.openMetricsWriter =
          new BufferedWriter(
              new OutputStreamWriter(openMetricsByteArrayOutputStream, StandardCharsets.UTF_8));
      this.prometheusWriter =
          new BufferedWriter(
              new OutputStreamWriter(prometheusByteArrayOutputStream, StandardCharsets.UTF_8));
    }
  }

  @State(Scope.Benchmark)
  public static class ReusableWriterToNullState {

    final BufferedWriter openMetricsWriter;
    final BufferedWriter prometheusWriter;

    public ReusableWriterToNullState() {
      OutputStream nullOutputStream = TextFormatUtilBenchmark.NullOutputStream.INSTANCE;
      this.openMetricsWriter =
          new BufferedWriter(new OutputStreamWriter(nullOutputStream, StandardCharsets.UTF_8));
      this.prometheusWriter =
          new BufferedWriter(new OutputStreamWriter(nullOutputStream, StandardCharsets.UTF_8));
    }
  }

  @Benchmark
  public OutputStream openMetricsWriteToByteArray(WriterState writerState) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = writerState.byteArrayOutputStream;
    byteArrayOutputStream.reset();
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(
        byteArrayOutputStream, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return byteArrayOutputStream;
  }

  @Benchmark
  public OutputStream openMetricsWriteToNull() throws IOException {
    OutputStream nullOutputStream = TextFormatUtilBenchmark.NullOutputStream.INSTANCE;
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(nullOutputStream, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return nullOutputStream;
  }

  @Benchmark
  public Writer openMetricsReusingWriter(ReusableWriterState state) throws IOException {
    state.openMetricsByteArrayOutputStream.reset();
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(
        state.openMetricsWriter, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return state.openMetricsWriter;
  }

  @Benchmark
  public Writer openMetricsReusingWriterToNull(ReusableWriterToNullState state) throws IOException {
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(
        state.openMetricsWriter, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return state.openMetricsWriter;
  }

  @Benchmark
  public OutputStream prometheusWriteToByteArray(WriterState writerState) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = writerState.byteArrayOutputStream;
    byteArrayOutputStream.reset();
    PROMETHEUS_TEXT_FORMAT_WRITER.write(
        byteArrayOutputStream, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return byteArrayOutputStream;
  }

  @Benchmark
  public OutputStream prometheusWriteToNull() throws IOException {
    OutputStream nullOutputStream = TextFormatUtilBenchmark.NullOutputStream.INSTANCE;
    PROMETHEUS_TEXT_FORMAT_WRITER.write(nullOutputStream, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return nullOutputStream;
  }

  @Benchmark
  public Writer prometheusReusingWriter(ReusableWriterState state) throws IOException {
    state.prometheusByteArrayOutputStream.reset();
    PROMETHEUS_TEXT_FORMAT_WRITER.write(
        state.prometheusWriter, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return state.prometheusWriter;
  }

  @Benchmark
  public Writer prometheusReusingWriterToNull(ReusableWriterToNullState state) throws IOException {
    PROMETHEUS_TEXT_FORMAT_WRITER.write(
        state.prometheusWriter, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return state.prometheusWriter;
  }
}
