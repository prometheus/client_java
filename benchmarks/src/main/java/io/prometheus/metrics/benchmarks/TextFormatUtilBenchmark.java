package io.prometheus.metrics.benchmarks;

import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot.SummaryDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class TextFormatUtilBenchmark {

  private static final MetricSnapshots SNAPSHOTS;

  static {
    MetricSnapshot gaugeSnapshot =
        GaugeSnapshot.builder()
            .name("gauge_snapshot_name")
            .dataPoint(
                GaugeDataPointSnapshot.builder()
                    .labels(Labels.of("name", "value"))
                    .scrapeTimestampMillis(1000L)
                    .value(123.45d)
                    .build())
            .build();

    MetricSnapshot summaryDataPointSnapshot =
        SummarySnapshot.builder()
            .name("summary_snapshot_name_bytes")
            .dataPoint(
                SummaryDataPointSnapshot.builder()
                    .count(5)
                    .labels(Labels.of("name", "value"))
                    .sum(123456d)
                    .build())
            .unit(Unit.BYTES)
            .build();

    SNAPSHOTS = MetricSnapshots.of(gaugeSnapshot, summaryDataPointSnapshot);
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
    // avoid growing the array
    ByteArrayOutputStream byteArrayOutputStream = writerState.byteArrayOutputStream;
    byteArrayOutputStream.reset();
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(byteArrayOutputStream, SNAPSHOTS);
    return byteArrayOutputStream;
  }

  @Benchmark
  public OutputStream openMetricsWriteToNull() throws IOException {
    OutputStream nullOutputStream = NullOutputStream.INSTANCE;
    OPEN_METRICS_TEXT_FORMAT_WRITER.write(nullOutputStream, SNAPSHOTS);
    return nullOutputStream;
  }

  @Benchmark
  public OutputStream prometheusWriteToByteArray(WriterState writerState) throws IOException {
    // avoid growing the array
    ByteArrayOutputStream byteArrayOutputStream = writerState.byteArrayOutputStream;
    byteArrayOutputStream.reset();
    PROMETHEUS_TEXT_FORMAT_WRITER.write(byteArrayOutputStream, SNAPSHOTS);
    return byteArrayOutputStream;
  }

  @Benchmark
  public OutputStream prometheusWriteToNull() throws IOException {
    OutputStream nullOutputStream = NullOutputStream.INSTANCE;
    PROMETHEUS_TEXT_FORMAT_WRITER.write(nullOutputStream, SNAPSHOTS);
    return nullOutputStream;
  }

  static final class NullOutputStream extends OutputStream {

    static final OutputStream INSTANCE = new NullOutputStream();

    private NullOutputStream() {
      super();
    }

    @Override
    public void write(int b) {
      // No-op: this is a null output stream
    }

    @Override
    public void write(byte[] b) {
      // No-op: this is a null output stream
    }

    @Override
    public void write(byte[] b, int off, int len) {
      // No-op: this is a null output stream
    }

    @Override
    public void flush() {
      // No-op: this is a null output stream
    }

    @Override
    public void close() {
      // No-op: this is a null output stream
    }
  }
}
