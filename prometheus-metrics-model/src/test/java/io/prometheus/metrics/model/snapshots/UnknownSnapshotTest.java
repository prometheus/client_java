package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import org.junit.jupiter.api.Test;

public class UnknownSnapshotTest {

  @Test
  public void testCompleteGoodCase() {
    long exemplarTimestamp = System.currentTimeMillis();
    UnknownSnapshot snapshot =
        UnknownSnapshot.builder()
            .name("my_unknown_seconds")
            .help("something in seconds")
            .unit(Unit.SECONDS)
            .dataPoint(
                UnknownSnapshot.UnknownDataPointSnapshot.builder()
                    .value(0.3)
                    .exemplar(
                        Exemplar.builder()
                            .value(0.12)
                            .traceId("abc123")
                            .spanId("123457")
                            .timestampMillis(exemplarTimestamp)
                            .build())
                    .labels(Labels.builder().label("env", "prod").build())
                    .build())
            .dataPoint(
                UnknownSnapshot.UnknownDataPointSnapshot.builder()
                    .value(0.29)
                    .labels(Labels.builder().label("env", "dev").build())
                    .build())
            .build();
    SnapshotTestUtil.assertMetadata(
        snapshot, "my_unknown_seconds", "something in seconds", "seconds");
    assertThat(snapshot.getDataPoints()).hasSize(2);
    UnknownSnapshot.UnknownDataPointSnapshot data = snapshot.getDataPoints().get(1); // env="prod"
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("env", "prod"));
    assertThat(data.getValue()).isCloseTo(0.3, offset(0.0));
    assertThat(data.getExemplar().getValue()).isCloseTo(0.12, offset(0.0));
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
  }

  @Test
  public void testMinimal() {
    UnknownSnapshot snapshot =
        UnknownSnapshot.builder()
            .name("test")
            .dataPoint(UnknownSnapshot.UnknownDataPointSnapshot.builder().value(1.0).build())
            .build();
    assertThat(snapshot.getDataPoints()).hasSize(1);
  }

  @Test
  public void testEmpty() {
    UnknownSnapshot snapshot = UnknownSnapshot.builder().name("test").build();
    assertThat(snapshot.getDataPoints()).isEmpty();
  }

  @Test
  public void testNameMissing() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> UnknownSnapshot.builder().build());
  }

  @Test
  public void testValueMissing() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> UnknownSnapshot.UnknownDataPointSnapshot.builder().build());
  }

  @Test
  public void testUnknownDataPointSnapshot() {
    Labels labels = Labels.of("k1", "v1");
    Exemplar exemplar = Exemplar.builder().value(2.0).build();

    UnknownSnapshot.UnknownDataPointSnapshot data =
        new UnknownSnapshot.UnknownDataPointSnapshot(1.0, labels, exemplar);
    assertThat(data.getValue()).isCloseTo(1.0, offset(0.1));
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(labels);
    assertThat(data.getExemplar()).isEqualTo(exemplar);

    data = new UnknownSnapshot.UnknownDataPointSnapshot(1.0, labels, exemplar, 0L);
    assertThat(data.getValue()).isCloseTo(1.0, offset(0.1));
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(labels);
    assertThat(data.getExemplar()).isEqualTo(exemplar);
  }
}
