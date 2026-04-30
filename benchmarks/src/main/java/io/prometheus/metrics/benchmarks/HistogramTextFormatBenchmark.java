package io.prometheus.metrics.benchmarks;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmarks for writing a classic histogram (10 label combinations × 12 buckets) to text formats.
 *
 * <pre>
 * Benchmark                                                                     Mode  Cnt       Score        Error   Units
 * HistogramTextFormatBenchmark.openMetricsWriteToByteArray                     thrpt    3   37567.116 ±  14566.571   ops/s
 * HistogramTextFormatBenchmark.openMetricsWriteToByteArray:gc.alloc.rate       thrpt    3    1466.289 ±    568.584  MB/sec
 * HistogramTextFormatBenchmark.openMetricsWriteToByteArray:gc.alloc.rate.norm  thrpt    3   40928.019 ±      0.006    B/op
 * HistogramTextFormatBenchmark.openMetricsWriteToByteArray:gc.count            thrpt    3     147.000               counts
 * HistogramTextFormatBenchmark.openMetricsWriteToByteArray:gc.time             thrpt    3      77.000                   ms
 * HistogramTextFormatBenchmark.openMetricsWriteToNull                          thrpt    3   36179.016 ±   1149.646   ops/s
 * HistogramTextFormatBenchmark.openMetricsWriteToNull:gc.alloc.rate            thrpt    3    1412.112 ±     44.791  MB/sec
 * HistogramTextFormatBenchmark.openMetricsWriteToNull:gc.alloc.rate.norm       thrpt    3   40928.019 ±      0.001    B/op
 * HistogramTextFormatBenchmark.openMetricsWriteToNull:gc.count                 thrpt    3     142.000               counts
 * HistogramTextFormatBenchmark.openMetricsWriteToNull:gc.time                  thrpt    3      74.000                   ms
 * HistogramTextFormatBenchmark.prometheusWriteToByteArray                      thrpt    3   36616.472 ±   5189.952   ops/s
 * HistogramTextFormatBenchmark.prometheusWriteToByteArray:gc.alloc.rate        thrpt    3    1434.773 ±    203.524  MB/sec
 * HistogramTextFormatBenchmark.prometheusWriteToByteArray:gc.alloc.rate.norm   thrpt    3   41088.019 ±      0.003    B/op
 * HistogramTextFormatBenchmark.prometheusWriteToByteArray:gc.count             thrpt    3     144.000               counts
 * HistogramTextFormatBenchmark.prometheusWriteToByteArray:gc.time              thrpt    3      73.000                   ms
 * HistogramTextFormatBenchmark.prometheusWriteToNull                           thrpt    3   36357.284 ±   4298.616   ops/s
 * HistogramTextFormatBenchmark.prometheusWriteToNull:gc.alloc.rate             thrpt    3    1424.614 ±    168.607  MB/sec
 * HistogramTextFormatBenchmark.prometheusWriteToNull:gc.alloc.rate.norm        thrpt    3   41088.019 ±      0.003    B/op
 * HistogramTextFormatBenchmark.prometheusWriteToNull:gc.count                  thrpt    3     143.000               counts
 * HistogramTextFormatBenchmark.prometheusWriteToNull:gc.time                   thrpt    3      73.000                   ms
 * </pre>
 */
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

  private static final ExpositionFormatWriter OPEN_METRICS_TEXT_FORMAT_WRITER =
      OpenMetricsTextFormatWriter.create();
  private static final ExpositionFormatWriter PROMETHEUS_TEXT_FORMAT_WRITER =
      PrometheusTextFormatWriter.create();

  @State(Scope.Benchmark)
  public static class WriterState {

    final ByteArrayOutputStream byteArrayOutputStream;

    public WriterState() {
      this.byteArrayOutputStream = new ByteArrayOutputStream();
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
}
