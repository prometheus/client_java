package io.prometheus.client;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;


public class ExtremumTest {

    private Extremum collector;
    private TestClock clock;



    @Before
    public void init() {
        clock = new TestClock();
        clock.setNanos(1);
        collector = Extremum.build("test", "extremumCollectorTest").
                samplingPeriod(100, TimeUnit.NANOSECONDS).
                withClock(clock).
                create();
    }

    @Test
    public void testTakeBiggestSample() {
        collector.set(1);
        collector.set(2);
        collector.set(3);

        assertEquals(3d, collector.get());
    }

    @Test
    public void testTakeSmallestSample() {
        collector = Extremum.build("test", "extremumCollectorTest").
                direction(Extremum.Direction.MIN).
                samplingPeriod(100, TimeUnit.MILLISECONDS).
                create();
        collector.set(1);
        collector.set(2);
        collector.set(3);

        assertEquals(1d, collector.get());
    }

    @Test
    public void testNegativeNumbers() {
        collector.set(-1);
        collector.set(-2);
        collector.set(-3);

        assertEquals(-1d, collector.get());
    }

    @Test
    public void testZero() {
        collector.set(-1);
        collector.set(0);
        collector.set(1);

        assertEquals(1d, collector.get());
    }

    @Test
    public void testIgnoreOldValues() {
        collector.set(1);
        clock.setNanos(10);
        collector.set(2);
        clock.setNanos(20);
        collector.set(3);
        clock.setNanos(121);

        assertEquals(0d, collector.get());
    }

    @Test
    public void testDelaySameSample() {
        collector.set(3);
        clock.setNanos(9);
        collector.set(1);
        clock.setNanos(111);

        assertEquals(0d, collector.get());
    }

    @Test
    public void testDelayDifferentSample() {
        collector.set(3);
        clock.setNanos(51);
        collector.set(1);
        clock.setNanos(102);
        assertEquals(1d, collector.get());
    }

    static class TestClock implements CollectorClock {

        private long millis = 0;
        private long nanos = 0;

        public void setMillis(long millis) {
            this.millis = millis;
        }

        public void setNanos(long nanos) {
            this.nanos = nanos;
        }

        @Override
        public long millis() {
            return millis;
        }

        @Override
        public long nanos() {
            return nanos;
        }
    }
}
