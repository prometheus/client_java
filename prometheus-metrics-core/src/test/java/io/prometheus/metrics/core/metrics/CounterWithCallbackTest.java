package io.prometheus.metrics.core.metrics;

import static org.junit.Assert.assertEquals;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

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

    assertEquals(1, snapshot.getDataPoints().size());
    CounterSnapshot.CounterDataPointSnapshot datapoint = snapshot.getDataPoints().get(0);
    assertEquals(value.doubleValue(), datapoint.getValue(), 0.1);
    assertEquals(labelValues.size(), datapoint.getLabels().size());

    value.incrementAndGet();
    snapshot = counter.collect();
    assertEquals(1, snapshot.getDataPoints().size());
    datapoint = snapshot.getDataPoints().get(0);
    assertEquals(value.doubleValue(), datapoint.getValue(), 0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCounterNoCallback() {
    CounterWithCallback.builder().name("counter").labelNames("l1", "l2").build();
  }
}
