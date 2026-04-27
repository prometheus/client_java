package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.config.OpenMetrics2Properties;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.OpenMetrics2TextFormatWriter;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class OpenMetrics2TextFormatWriterTest {

  @Test
  void counterPreservesOriginalNameWhenUnitIsConfigured() throws IOException {
    Counter counter =
        Counter.builder()
            .name("my_counter")
            .unit(Unit.SECONDS)
            .help("Test counter")
            .labelNames("method")
            .build();
    counter.labelValues("GET").inc(42.0);
    MetricSnapshots snapshots = MetricSnapshots.of(counter.collect());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om1Output).contains("my_counter_seconds_total{method=\"GET\"} 42.0");
    assertThat(om2Output)
        .contains("# TYPE my_counter counter\n")
        .contains("# UNIT my_counter seconds\n")
        .contains("# HELP my_counter Test counter\n")
        .containsPattern("(?m)^my_counter\\{method=\"GET\"} 42\\.0 st@\\d+\\.\\d{3}$")
        .doesNotContain("my_counter_seconds");
  }

  @Test
  void classicHistogramPreservesOriginalNameWhenUnitIsConfigured() throws IOException {
    Histogram histogram =
        Histogram.builder()
            .name("request_duration")
            .unit(Unit.SECONDS)
            .help("Request duration in seconds")
            .labelNames("path")
            .classicOnly()
            .classicUpperBounds(10.0)
            .build();
    histogram.labelValues("/hello").observe(3.2);
    MetricSnapshots snapshots = MetricSnapshots.of(histogram.collect());

    String om1Output = writeWithOM1(snapshots);
    String om2Output = writeWithOM2(snapshots);

    assertThat(om1Output).contains("request_duration_seconds_bucket");
    assertThat(om2Output)
        .contains("# TYPE request_duration histogram\n")
        .contains("# UNIT request_duration seconds\n")
        .contains("# HELP request_duration Request duration in seconds\n")
        .contains("request_duration_bucket{path=\"/hello\",le=\"10.0\"} 1\n")
        .contains("request_duration_bucket{path=\"/hello\",le=\"+Inf\"} 1\n")
        .contains("request_duration_count{path=\"/hello\"} 1\n")
        .contains("request_duration_sum{path=\"/hello\"} 3.2\n")
        .doesNotContain("request_duration_seconds");
  }

  @Test
  void nativeHistogramPreservesOriginalNameWhenUnitIsConfigured() throws IOException {
    Histogram histogram =
        Histogram.builder()
            .name("my.request.duration")
            .unit(Unit.SECONDS)
            .help("Request duration in seconds")
            .labelNames("http.path")
            .nativeOnly()
            .build();
    histogram.labelValues("/hello").observe(3.2);
    MetricSnapshots snapshots = MetricSnapshots.of(histogram.collect());

    String om2Output = writeWithNativeHistograms(snapshots);

    assertThat(om2Output)
        .contains("# TYPE \"my.request.duration\" histogram\n")
        .contains("# UNIT \"my.request.duration\" seconds\n")
        .contains("# HELP \"my.request.duration\" Request duration in seconds\n")
        .contains("{\"my.request.duration\",\"http.path\"=\"/hello\"} {count:1,sum:3.2,")
        .doesNotContain("my.request.duration_seconds");
  }

  private String writeWithOM1(MetricSnapshots snapshots) throws IOException {
    return write(snapshots, OpenMetricsTextFormatWriter.create());
  }

  private String writeWithOM2(MetricSnapshots snapshots) throws IOException {
    return write(snapshots, OpenMetrics2TextFormatWriter.create());
  }

  private String writeWithNativeHistograms(MetricSnapshots snapshots) throws IOException {
    OpenMetrics2TextFormatWriter writer =
        OpenMetrics2TextFormatWriter.builder()
            .setOpenMetrics2Properties(
                OpenMetrics2Properties.builder().nativeHistograms(true).build())
            .build();
    return write(snapshots, writer);
  }

  private String write(MetricSnapshots snapshots, ExpositionFormatWriter writer)
      throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writer.write(out, snapshots, EscapingScheme.ALLOW_UTF8);
    return out.toString(StandardCharsets.UTF_8.name());
  }
}
