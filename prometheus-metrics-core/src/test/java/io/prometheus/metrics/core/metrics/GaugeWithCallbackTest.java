package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class GaugeWithCallbackTest {

  @Test
  void testGauge() {
    final AtomicInteger value = new AtomicInteger(1);
    List<String> labelValues = Arrays.asList("v1", "v2");
    GaugeWithCallback gauge =
        GaugeWithCallback.builder()
            .name("gauge")
            .labelNames("l1", "l2")
            .callback(
                callback -> callback.call(value.doubleValue(), labelValues.toArray(new String[0])))
            .build();
    GaugeSnapshot snapshot = gauge.collect();

    assertThat(snapshot.getDataPoints().size()).isOne();
    GaugeSnapshot.GaugeDataPointSnapshot datapoint = snapshot.getDataPoints().get(0);
    assertThat(datapoint.getValue()).isCloseTo(value.doubleValue(), offset(0.1));
    assertThat(datapoint.getLabels().size()).isEqualTo(labelValues.size());

    value.incrementAndGet();
    snapshot = gauge.collect();
    assertThat(snapshot.getDataPoints().size()).isOne();
    datapoint = snapshot.getDataPoints().get(0);
    assertThat(datapoint.getValue()).isCloseTo(value.doubleValue(), offset(0.1));
  }

  @Test
  void testGaugeNoCallback() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> GaugeWithCallback.builder().name("gauge").labelNames("l1", "l2").build());
  }
}
