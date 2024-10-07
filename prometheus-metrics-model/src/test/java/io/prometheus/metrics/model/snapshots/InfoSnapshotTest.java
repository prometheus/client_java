package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import org.junit.Test;

public class InfoSnapshotTest {

  @Test
  public void testCompleteGoodCase() {
    InfoSnapshot snapshot =
        InfoSnapshot.builder()
            .name("target")
            .help("Target info")
            .dataPoint(
                InfoSnapshot.InfoDataPointSnapshot.builder()
                    .labels(Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway"))
                    .build())
            .build();
    assertThat(snapshot.getMetadata().getName()).isEqualTo("target");
    assertThat(snapshot.getMetadata().getHelp()).isEqualTo("Target info");
    assertThat(snapshot.getMetadata().hasUnit()).isFalse();
    assertThat(snapshot.getDataPoints().size()).isOne();
  }

  @Test
  public void testEmptyInfo() {
    InfoSnapshot snapshot = InfoSnapshot.builder().name("target").build();
    assertThat(snapshot.getDataPoints()).isEmpty();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDataImmutable() {
    InfoSnapshot snapshot =
        InfoSnapshot.builder()
            .name("target")
            .dataPoint(
                InfoSnapshot.InfoDataPointSnapshot.builder()
                    .labels(
                        Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway.v1"))
                    .build())
            .dataPoint(
                InfoSnapshot.InfoDataPointSnapshot.builder()
                    .labels(
                        Labels.of("instance_id", "127.0.0.1:9200", "service_name", "gateway.v2"))
                    .build())
            .build();
    Iterator<InfoSnapshot.InfoDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
    iterator.next();
    iterator.remove();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNameMustNotIncludeSuffix() {
    InfoSnapshot.builder().name("jvm_info").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNameMustNotIncludeSuffixDot() {
    InfoSnapshot.builder().name("jvm.info").build();
  }
}
