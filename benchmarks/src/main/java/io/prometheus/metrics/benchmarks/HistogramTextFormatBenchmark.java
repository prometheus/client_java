package io.prometheus.metrics.benchmarks;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmarks for writing a classic histogram (10 label combinations × 12 buckets) to text formats.
 * Output goes to /dev/null to isolate pure formatting CPU cost with zero IO overhead.
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

  @Benchmark
  public OutputStream openMetricsWriteToNull() throws IOException {
    OutputStream nullOutputStream = TextFormatUtilBenchmark.NullOutputStream.INSTANCE;
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(nullOutputStream, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return nullOutputStream;
  }

  @Benchmark
  public OutputStream prometheusWriteToNull() throws IOException {
    OutputStream nullOutputStream = TextFormatUtilBenchmark.NullOutputStream.INSTANCE;
    PROMETHEUS_TEXT_FORMAT_WRITER.write(nullOutputStream, SNAPSHOTS, EscapingScheme.ALLOW_UTF8);
    return nullOutputStream;
  }
}
