package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class TextFormatUtilTest {

  @Test
  void writeEscapedLabelValue() throws IOException {
    assertEquals("aa\\\\bb\\\"cc\\ndd\\nee\\\\ff\\\"gg", escape("aa\\bb\"cc\ndd\nee\\ff\"gg"));
    assertEquals("\\\\", escape("\\"));
    assertEquals("\\\\\\\\", escape("\\\\"));
    assertEquals("text", escape("text"));
  }

  private static String escape(String s) throws IOException {
    StringWriter writer = new StringWriter();
    TextFormatUtil.writeEscapedString(writer, s);
    return writer.toString();
  }

  @Test
  void testWritePrometheusTimestamp() throws IOException {
    assertThat(writePrometheusTimestamp(true)).isEqualTo("1000");
    assertThat(writePrometheusTimestamp(false)).isEqualTo("1.000");
  }

  private static String writePrometheusTimestamp(boolean timestampsInMs) throws IOException {
    StringWriter writer = new StringWriter();
    TextFormatUtil.writePrometheusTimestamp(writer, 1000, timestampsInMs);
    return writer.toString();
  }

  @Test
  public void testMergeDuplicates_sameName_mergesDataPoints() {
    CounterSnapshot counter1 =
        CounterSnapshot.builder()
            .name("api_responses")
            .dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                    .value(100)
                    .build())
            .build();

    CounterSnapshot counter2 =
        CounterSnapshot.builder()
            .name("api_responses")
            .dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(Labels.of("uri", "/hello", "outcome", "FAILURE"))
                    .value(10)
                    .build())
            .build();

    MetricSnapshots snapshots = new MetricSnapshots(counter1, counter2);
    MetricSnapshots result = TextFormatUtil.mergeDuplicates(snapshots);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getMetadata().getName()).isEqualTo("api_responses");
    assertThat(result.get(0).getDataPoints()).hasSize(2);

    CounterSnapshot merged = (CounterSnapshot) result.get(0);
    assertThat(merged.getDataPoints())
        .anyMatch(
            dp ->
                dp.getLabels().equals(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                    && dp.getValue() == 100);
    assertThat(merged.getDataPoints())
        .anyMatch(
            dp ->
                dp.getLabels().equals(Labels.of("uri", "/hello", "outcome", "FAILURE"))
                    && dp.getValue() == 10);
  }

  @Test
  public void testMergeDuplicates_multipleDataPoints_allMerged() {
    CounterSnapshot counter1 =
        CounterSnapshot.builder()
            .name("api_responses")
            .dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                    .value(100)
                    .build())
            .dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(Labels.of("uri", "/world", "outcome", "SUCCESS"))
                    .value(200)
                    .build())
            .build();

    CounterSnapshot counter2 =
        CounterSnapshot.builder()
            .name("api_responses")
            .dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(Labels.of("uri", "/hello", "outcome", "FAILURE"))
                    .value(10)
                    .build())
            .dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                    .labels(Labels.of("uri", "/world", "outcome", "FAILURE"))
                    .value(5)
                    .build())
            .build();

    MetricSnapshots snapshots = new MetricSnapshots(counter1, counter2);
    MetricSnapshots result = TextFormatUtil.mergeDuplicates(snapshots);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getDataPoints()).hasSize(4);
  }

  @Test
  public void testMergeDuplicates_emptySnapshots_returnsEmpty() {
    MetricSnapshots snapshots = MetricSnapshots.builder().build();
    MetricSnapshots result = TextFormatUtil.mergeDuplicates(snapshots);

    assertThat(result).isEmpty();
  }
}
