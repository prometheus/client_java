package io.prometheus.metrics.core.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

class SlidingWindowTest {

    static class Observer {

        List<Double> values = new ArrayList<>();

        public void observe(double value) {
            values.add(value);
        }

        void assertValues(double... expectedValues) {
            ArrayList<Double> expectedList = new ArrayList<>();
            for (double expectedValue : expectedValues) {
                expectedList.add(expectedValue);
            }
            Assertions.assertEquals(expectedList, values);
        }
    }

    private final AtomicLong currentTimeMillis = new AtomicLong();
    private SlidingWindow<Observer> ringBuffer;
    private final long maxAgeSeconds = 30;
    private final int ageBuckets = 5;
    private final long timeBetweenRotateMillis = maxAgeSeconds * 1000 / ageBuckets + 2;

    @BeforeEach
    void setUp() {
        currentTimeMillis.set(System.currentTimeMillis());
        ringBuffer = new SlidingWindow<>(Observer.class, Observer::new, Observer::observe, maxAgeSeconds, ageBuckets);
        ringBuffer.currentTimeMillis = currentTimeMillis::get;
    }

    @Test
    void testRotate() {
        for (int i=0; i<ageBuckets; i++) {
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
    void testMultiRotate() {
        ringBuffer.observe(1.0);
        currentTimeMillis.addAndGet(2 * timeBetweenRotateMillis); // 2/5 of max aqe
        ringBuffer.observe(2.0);
        ringBuffer.current().assertValues(1.0, 2.0);
        currentTimeMillis.addAndGet(3 * timeBetweenRotateMillis); // 5/5 of max age -> first observation evicted
        ringBuffer.current().assertValues(2.0);
        ringBuffer.observe(3.0);
        ringBuffer.current().assertValues(2.0, 3.0);
        currentTimeMillis.addAndGet(2 * timeBetweenRotateMillis); // 7/5 of max age
        ringBuffer.current().assertValues(3.0);
        currentTimeMillis.addAndGet(3 * timeBetweenRotateMillis); // 10/5 of max age
        ringBuffer.current().assertValues(); // empty
    }
}
