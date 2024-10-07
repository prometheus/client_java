package io.prometheus.metrics.core.metrics;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class StatefulMetricTest {

  @Test
  public void testLabelRemoveWhileCollecting() throws Exception {
    Counter counter = Counter.builder().name("test").labelNames("label1", "label2").build();
    Field data = counter.getClass().getSuperclass().getDeclaredField("data");
    data.setAccessible(true);

    counter.labelValues("a", "b").inc(1.0);
    counter.labelValues("c", "d").inc(3.0);
    counter.labelValues("e", "f").inc(7.0);

    // collect() iterates over data.entrySet().
    // remove() removes entries from data.
    // Make sure iterating does not yield null while removing.

    int i = 0;
    for (Map.Entry<?, ?> entry : ((Map<?, ?>) data.get(counter)).entrySet()) {
      i++;
      if (i == 2) {
        counter.remove("c", "d");
        counter.remove("e", "f");
      }
      assertThat(entry.getKey()).isNotNull();
      assertThat(entry.getValue()).isNotNull();
    }
  }

  @Test
  public void testClear() {
    Counter counter = Counter.builder().name("test").labelNames("label1", "label2").build();
    counter.labelValues("a", "b").inc(3.0);
    counter.labelValues("c", "d").inc(3.0);
    counter.labelValues("a", "b").inc();
    assertThat(counter.collect().getDataPoints()).hasSize(2);

    counter.clear();
    assertThat(counter.collect().getDataPoints()).isEmpty();

    counter.labelValues("a", "b").inc();
    assertThat(counter.collect().getDataPoints()).hasSize(1);
  }

  @Test
  public void testClearNoLabels() {
    Counter counter = Counter.builder().name("test").build();
    counter.inc();
    assertThat(counter.collect().getDataPoints()).hasSize(1);
    assertThat(counter.collect().getDataPoints().get(0).getValue()).isCloseTo(1.0, offset(0.0));

    counter.clear();
    // No labels is always present, but as no value has been observed after clear() the value should
    // be 0.0
    assertThat(counter.collect().getDataPoints()).hasSize(1);
    assertThat(counter.collect().getDataPoints().get(0).getValue()).isCloseTo(0.0, offset(0.0));

    // Making inc() works correctly after clear()
    counter.inc();
    assertThat(counter.collect().getDataPoints()).hasSize(1);
    assertThat(counter.collect().getDataPoints().get(0).getValue()).isCloseTo(1.0, offset(0.0));
  }
}
