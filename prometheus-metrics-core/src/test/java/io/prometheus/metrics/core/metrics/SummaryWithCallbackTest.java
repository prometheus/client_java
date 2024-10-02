package io.prometheus.metrics.core.metrics;

import static org.junit.Assert.assertEquals;

import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class SummaryWithCallbackTest {

  @Test
  public void testGauge() {
    final AtomicInteger count = new AtomicInteger(1);
    final AtomicInteger sum = new AtomicInteger(1);
    final Quantiles quantiles = Quantiles.of(new Quantile(0.5, 10));
    List<String> labelValues = Arrays.asList("v1", "v2");
    SummaryWithCallback gauge =
        SummaryWithCallback.builder()
            .name("summary")
            .labelNames("l1", "l2")
            .callback(
                callback -> {
                  callback.call(
                      count.get(), sum.get(), quantiles, labelValues.toArray(new String[0]));
                })
            .build();
    SummarySnapshot snapshot = gauge.collect();

    assertEquals(1, snapshot.getDataPoints().size());
    SummarySnapshot.SummaryDataPointSnapshot datapoint = snapshot.getDataPoints().get(0);
    assertEquals(count.get(), datapoint.getCount());
    assertEquals(sum.doubleValue(), datapoint.getSum(), 0.1);
    assertEquals(quantiles, datapoint.getQuantiles());
    assertEquals(labelValues.size(), datapoint.getLabels().size());

    count.incrementAndGet();
    sum.incrementAndGet();
    snapshot = gauge.collect();
    assertEquals(1, snapshot.getDataPoints().size());
    datapoint = snapshot.getDataPoints().get(0);
    assertEquals(count.get(), datapoint.getCount());
    assertEquals(sum.doubleValue(), datapoint.getSum(), 0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSummaryNoCallback() {
    SummaryWithCallback.builder().name("summary").labelNames("l1", "l2").build();
  }
}
