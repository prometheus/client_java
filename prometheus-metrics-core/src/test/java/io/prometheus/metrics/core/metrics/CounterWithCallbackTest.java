package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class CounterWithCallbackTest {

  @Test
  public void testCounter() {
    final AtomicInteger value = new AtomicInteger(1);
    List<String> labelValues = Arrays.asList("v1", "v2");
    CounterWithCallback counter =
        CounterWithCallback.builder()
            .name("counter")
            .labelNames("l1", "l2")
            .callback(
                callback -> callback.call(value.doubleValue(), labelValues.toArray(new String[0])))
            .build();
    CounterSnapshot snapshot = counter.collect();

    assertThat(snapshot.getDataPoints().size()).isOne();
    CounterSnapshot.CounterDataPointSnapshot datapoint = snapshot.getDataPoints().get(0);
    assertThat(datapoint.getValue()).isCloseTo(value.doubleValue(), offset(0.1));
    assertThat(datapoint.getLabels().size()).isEqualTo(labelValues.size());

    value.incrementAndGet();
    snapshot = counter.collect();
    assertThat(snapshot.getDataPoints().size()).isOne();
    datapoint = snapshot.getDataPoints().get(0);
    assertThat(datapoint.getValue()).isCloseTo(value.doubleValue(), offset(0.1));
  }

  @Test
  public void testCounterNoCallback() {
    assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(
                () ->
    CounterWithCallback.builder().name("counter").labelNames("l1", "l2").build());
  }
}
