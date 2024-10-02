package io.prometheus.metrics.core.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Test;

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
      assertNotNull(entry.getKey());
      assertNotNull(entry.getValue());
    }
  }

  @Test
  public void testClear() {
    Counter counter = Counter.builder().name("test").labelNames("label1", "label2").build();
    counter.labelValues("a", "b").inc(3.0);
    counter.labelValues("c", "d").inc(3.0);
    counter.labelValues("a", "b").inc();
    assertEquals(2, counter.collect().getDataPoints().size());

    counter.clear();
    assertEquals(0, counter.collect().getDataPoints().size());

    counter.labelValues("a", "b").inc();
    assertEquals(1, counter.collect().getDataPoints().size());
  }

  @Test
  public void testClearNoLabels() {
    Counter counter = Counter.builder().name("test").build();
    counter.inc();
    assertEquals(1, counter.collect().getDataPoints().size());
    assertEquals(1.0, counter.collect().getDataPoints().get(0).getValue(), 0.0);

    counter.clear();
    // No labels is always present, but as no value has been observed after clear() the value should
    // be 0.0
    assertEquals(1, counter.collect().getDataPoints().size());
    assertEquals(0.0, counter.collect().getDataPoints().get(0).getValue(), 0.0);

    // Making inc() works correctly after clear()
    counter.inc();
    assertEquals(1, counter.collect().getDataPoints().size());
    assertEquals(1.0, counter.collect().getDataPoints().get(0).getValue(), 0.0);
  }
}
