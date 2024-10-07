package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SlidingWindowTest {

  class Observer {

    final List<Double> values = new ArrayList<>();

    public void observe(double value) {
      values.add(value);
    }

    void assertValues(double... expectedValues) {
      ArrayList<Double> expectedList = new ArrayList<>();
      for (double expectedValue : expectedValues) {
        expectedList.add(expectedValue);
      }
      assertThat(values)
          .as(
              "Start time: "
                  + startTime
                  + ", current time: "
                  + currentTimeMillis.get()
                  + ", elapsed time: "
                  + (currentTimeMillis.get() - startTime))
          .isEqualTo(expectedList);
    }
  }

  private long startTime;
  private final AtomicLong currentTimeMillis = new AtomicLong();
  private SlidingWindow<Observer> ringBuffer;
  private final long maxAgeSeconds = 30;
  private final int ageBuckets = 5;
  private final long timeBetweenRotateMillis = maxAgeSeconds * 1000 / ageBuckets + 2;

  @BeforeEach
  public void setUp() {
    startTime = System.currentTimeMillis();
    currentTimeMillis.set(startTime);
    ringBuffer =
        new SlidingWindow<>(
            Observer.class, Observer::new, Observer::observe, maxAgeSeconds, ageBuckets);
    ringBuffer.currentTimeMillis = currentTimeMillis::get;
  }

  @Test
  public void testRotate() {
    for (int i = 0; i < ageBuckets; i++) {
      currentTimeMillis.addAndGet(timeBetweenRotateMillis);
      ringBuffer.observe(1.0);
    }
    ringBuffer.current().assertValues(1.0, 1.0, 1.0, 1.0, 1.0);
    currentTimeMillis.addAndGet(timeBetweenRotateMillis);
    ringBuffer.current().assertValues(1.0, 1.0, 1.0, 1.0);
    currentTimeMillis.addAndGet(timeBetweenRotateMillis);
    ringBuffer.current().assertValues(1.0, 1.0, 1.0);
    currentTimeMillis.addAndGet(timeBetweenRotateMillis);
    ringBuffer.current().assertValues(1.0, 1.0);
    currentTimeMillis.addAndGet(timeBetweenRotateMillis);
    ringBuffer.current().assertValues(1.0);
    currentTimeMillis.addAndGet(timeBetweenRotateMillis);
    ringBuffer.current().assertValues();
  }

  @Test
  public void testMultiRotate() {
    ringBuffer.observe(1.0);
    currentTimeMillis.addAndGet(2 * timeBetweenRotateMillis); // 2/5 of max aqe
    ringBuffer.observe(2.0);
    ringBuffer.current().assertValues(1.0, 2.0);
    currentTimeMillis.addAndGet(
        3 * timeBetweenRotateMillis); // 5/5 of max age -> first observation evicted
    ringBuffer.current().assertValues(2.0);
    ringBuffer.observe(3.0);
    ringBuffer.current().assertValues(2.0, 3.0);
    currentTimeMillis.addAndGet(2 * timeBetweenRotateMillis); // 7/5 of max age
    ringBuffer.current().assertValues(3.0);
    currentTimeMillis.addAndGet(3 * timeBetweenRotateMillis); // 10/5 of max age
    ringBuffer.current().assertValues(); // empty
  }
}
