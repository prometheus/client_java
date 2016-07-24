package io.prometheus.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Test;

public class UniformSamplingTest {

    @Test
    public void testMaxSize() {
        UniformSampling s = new UniformSampling(1);

        double[] values = s.getValues();
        assertEquals(0, values.length);

        s.add(1.0);
        values = s.getValues();
        assertEquals(1, values.length);

        s.add(2.0);
        values = s.getValues();
        assertEquals(1, values.length);
    }

    @Test
    public void testRepeatedValues() {
        UniformSampling s = new UniformSampling(2);

        s.add(1.0);
        s.add(1.0);
        double[] values = s.getValues();
        assertEquals(2, values.length);
    }

    @Test
    public void testReplacement() {
        Random rand = mock(Random.class);
        when(rand.nextDouble()).thenReturn(0.0);

        UniformSampling s = new UniformSampling(1, rand);

        s.add(1.0);
        double[] values = s.getValues();
        assertEquals(1, values.length);

        s.add(2.0);
        values = s.getValues();
        assertEquals(1, values.length);
        assertEquals(2.0, values[0], .0);
    }

}
