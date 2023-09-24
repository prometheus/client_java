package io.prometheus.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleTimerTest {
    @Test
    public void elapsedSeconds() throws Exception {
        SimpleTimer.TimeProvider provider = new SimpleTimer.TimeProvider() {
            long value = (long)(30 * 1e9);
            long nanoTime() {
                value += (long)(10 * 1e9);
                return value;
            }
        };

        SimpleTimer timer = new SimpleTimer(provider);
        assertEquals(10, timer.elapsedSeconds(), .001);

    }

}
