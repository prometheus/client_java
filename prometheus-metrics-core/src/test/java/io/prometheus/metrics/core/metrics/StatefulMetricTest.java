package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StatefulMetricTest {

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
  @SuppressWarnings("unchecked")
  public void testLabelRemoveIf() throws Exception {
    Counter counter = Counter.builder().name("testLabelRemoveIf").labelNames("label1", "label2").build();
    Field data = counter.getClass().getSuperclass().getDeclaredField("data");
    data.setAccessible(true);

    counter.labelValues("a", "b").inc(1.0);
    counter.labelValues("a", "c").inc(3.0);
    counter.labelValues("a", "d").inc(7.0);
    counter.labelValues("e", "f").inc(8.0);

    counter.removeIf(labels -> labels.size() == 2 && labels.get(0).equals("a"));

    Map<List<String>, Counter.DataPoint> dataPoints =
        (Map<List<String>, Counter.DataPoint>) data.get(counter);

    assertThat(dataPoints).hasSize(1);
    assertThat(dataPoints.get(Arrays.asList("e", "f"))).isNotNull();
    assertThat(dataPoints.get(Arrays.asList("e", "f")).get()).isEqualTo(8.0D);
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
    assertThat(counter.collect().getDataPoints().get(0).getValue()).isEqualTo(1.0);

    counter.clear();
    // No labels is always present, but as no value has been observed after clear() the value should
    // be 0.0
    assertThat(counter.collect().getDataPoints()).hasSize(1);
    assertThat(counter.collect().getDataPoints().get(0).getValue()).isEqualTo(0.0);

    // Making inc() works correctly after clear()
    counter.inc();
    assertThat(counter.collect().getDataPoints()).hasSize(1);
    assertThat(counter.collect().getDataPoints().get(0).getValue()).isEqualTo(1.0);
  }

  @Test
  public void testNullLabel() {
    Counter counter = Counter.builder().name("test").labelNames("l1", "l2").build();
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> counter.labelValues("l1", null))
        .withMessage("null label value for metric test and label l2");
  }
}
