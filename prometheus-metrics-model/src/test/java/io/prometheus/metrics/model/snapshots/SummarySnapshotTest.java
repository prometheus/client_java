package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SummarySnapshotTest {

  @Test
  public void testCompleteGoodCase() {
    long createdTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
    long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
    long exemplarTimestamp = System.currentTimeMillis();
    SummarySnapshot snapshot =
        SummarySnapshot.builder()
            .name("latency_seconds")
            .help("latency in seconds")
            .unit(Unit.SECONDS)
            .dataPoint(
                SummarySnapshot.SummaryDataPointSnapshot.builder()
                    .createdTimestampMillis(createdTimestamp)
                    .scrapeTimestampMillis(scrapeTimestamp)
                    .labels(Labels.of("endpoint", "/"))
                    .quantiles(
                        Quantiles.builder()
                            .quantile(0.5, 0.2)
                            .quantile(0.95, 0.22)
                            .quantile(0.99, 0.23)
                            .build())
                    .exemplars(
                        Exemplars.builder()
                            .exemplar(
                                Exemplar.builder()
                                    .value(0.2)
                                    .traceId("abc123")
                                    .spanId("123457")
                                    .timestampMillis(exemplarTimestamp)
                                    .build())
                            .exemplar(
                                Exemplar.builder()
                                    .value(0.21)
                                    .traceId("abc124")
                                    .spanId("123458")
                                    .timestampMillis(exemplarTimestamp)
                                    .build())
                            .build())
                    .count(1093)
                    .sum(218.6)
                    .build())
            .dataPoint(
                SummarySnapshot.SummaryDataPointSnapshot.builder()
                    .labels(Labels.of("endpoint", "/test"))
                    .count(1093)
                    .sum(218.6)
                    .build())
            .build();
    SnapshotTestUtil.assertMetadata(snapshot, "latency_seconds", "latency in seconds", "seconds");
    assertThat(snapshot.getDataPoints()).hasSize(2);
    SummarySnapshot.SummaryDataPointSnapshot data = snapshot.getDataPoints().get(0);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("endpoint", "/"));
    assertThat(data.hasCount()).isTrue();
    assertThat(data.getCount()).isEqualTo(1093);
    assertThat(data.hasSum()).isTrue();
    assertThat(data.getSum()).isEqualTo(218.6);
    assertThat(data.hasCreatedTimestamp()).isTrue();
    assertThat(data.getCreatedTimestampMillis()).isEqualTo(createdTimestamp);
    assertThat(data.hasScrapeTimestamp()).isTrue();
    assertThat(data.getScrapeTimestampMillis()).isEqualTo(scrapeTimestamp);
    Quantiles quantiles = data.getQuantiles();
    assertThat(quantiles.size()).isEqualTo(3);
    // quantiles are tested in QuantilesTest already, skipping here.
    assertThat(data.getExemplars().size()).isEqualTo(2);
    // exemplars are tested in ExemplarsTest already, skipping here.

    data = snapshot.getDataPoints().get(1);
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
    assertThat(data.hasCount()).isTrue();
    assertThat(data.hasSum()).isTrue();
  }

  @Test
  public void testMinimal() {
    SummarySnapshot snapshot =
        SummarySnapshot.builder()
            .name("size_bytes")
            .dataPoint(
                SummarySnapshot.SummaryDataPointSnapshot.builder().count(10).sum(12.0).build())
            .build();
    assertThat(snapshot.getDataPoints()).hasSize(1);
    assertThat((Iterable<? extends Label>) snapshot.getDataPoints().get(0).getLabels())
        .isEqualTo(Labels.EMPTY);
  }

  @Test
  public void testEmptySnapshot() {
    SummarySnapshot snapshot = SummarySnapshot.builder().name("empty_summary").build();
    assertThat(snapshot.getDataPoints()).isEmpty();
  }

  @Test
  public void testEmptyData() {
    SummarySnapshot.SummaryDataPointSnapshot data =
        SummarySnapshot.SummaryDataPointSnapshot.builder().build();
    assertThat(data.getQuantiles().size()).isZero();
    assertThat(data.hasCount()).isFalse();
    assertThat(data.hasSum()).isFalse();
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
    assertThat(data.getExemplars().size()).isZero();
  }

  @Test
  void escape() {
    SummarySnapshot.SummaryDataPointSnapshot data =
        SummarySnapshot.SummaryDataPointSnapshot.builder().sum(12.0).build();
    assertThat(data.escape(EscapingScheme.UNDERSCORE_ESCAPING))
        .usingRecursiveComparison()
        .isEqualTo(data);
  }
}
