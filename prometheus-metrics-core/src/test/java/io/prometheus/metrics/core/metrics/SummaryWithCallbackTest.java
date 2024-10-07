package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

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

    assertThat(snapshot.getDataPoints().size()).isOne();
    SummarySnapshot.SummaryDataPointSnapshot datapoint = snapshot.getDataPoints().get(0);
    assertThat(datapoint.getCount()).isEqualTo(count.get());
    assertThat(datapoint.getSum()).isCloseTo(sum.doubleValue(), offset(0.1));
    assertThat(datapoint.getQuantiles()).isEqualTo(quantiles);
    assertThat(datapoint.getLabels().size()).isEqualTo(labelValues.size());

    count.incrementAndGet();
    sum.incrementAndGet();
    snapshot = gauge.collect();
    assertThat(snapshot.getDataPoints().size()).isOne();
    datapoint = snapshot.getDataPoints().get(0);
    assertThat(datapoint.getCount()).isEqualTo(count.get());
    assertThat(datapoint.getSum()).isCloseTo(sum.doubleValue(), offset(0.1));
  }

  @Test
  public void testSummaryNoCallback() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> SummaryWithCallback.builder().name("summary").labelNames("l1", "l2").build());
  }
}
