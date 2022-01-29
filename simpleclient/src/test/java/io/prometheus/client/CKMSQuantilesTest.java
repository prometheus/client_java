package io.prometheus.client;

import io.prometheus.client.CKMSQuantiles.Quantile;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CKMSQuantilesTest {

    @Test
    public void testGetOnEmptyValues() {
        List<Quantile> quantiles = new ArrayList<Quantile>();
        quantiles.add(new Quantile(0.50, 0.01));
        quantiles.add(new Quantile(0.90, 0.01));
        quantiles.add(new Quantile(0.95, 0.01));
        quantiles.add(new Quantile(0.99, 0.01));

        CKMSQuantiles ckms = new CKMSQuantiles(
                quantiles.toArray(new Quantile[]{}));
        assertEquals(Double.NaN, ckms.get(0), 0);
    }

    @Test
    public void testGetWhenNoQuantilesAreDefined() {
        CKMSQuantiles ckms = new CKMSQuantiles(new Quantile[]{});
        assertEquals(Double.NaN, ckms.get(0), 0);
    }

    @Test
    public void testInsertWhenNoQuantilesAreDefined() {
        CKMSQuantiles ckms = new CKMSQuantiles(new Quantile[]{});
        ckms.insert(1.0);
        ckms.insert(2.0);
        ckms.insert(3.0);
        assertEquals(1.0, ckms.get(0), 0);
        assertEquals(2.0, ckms.get(0.5), 0);
        assertEquals(3.0, ckms.get(1), 0);
    }

    @Test
    public void testCompressWhenBufferSize500Reached() {
        CKMSQuantiles ckms = new CKMSQuantiles(new Quantile[]{});
        List<Double> input = makeSequence(1, 499);

        for (double v : input) {
            ckms.insert(v);
        }
        assertEquals("No compress should be triggered", 0, ckms.samples.size());
        
        ckms.insert(500);
        assertEquals(500, ckms.samples.size());
    }

    @Test
    public void testGet() {
        List<Quantile> quantiles = new ArrayList<Quantile>();
        quantiles.add(new Quantile(0.50, 0.01));
        quantiles.add(new Quantile(0.90, 0.01));
        quantiles.add(new Quantile(0.95, 0.01));
        quantiles.add(new Quantile(0.99, 0.01));

        List<Double> input = makeSequence(1, 100);
        CKMSQuantiles ckms = new CKMSQuantiles(
                quantiles.toArray(new Quantile[]{}));
        for (double v : input) {
            ckms.insert(v);
        }
        assertEquals(10.0, ckms.get(0.1), 1);
        assertEquals(50.0, ckms.get(0.5), 1);
        assertEquals(90.0, ckms.get(0.9), 1);
        assertEquals(95.0, ckms.get(0.95), 1);
        assertEquals(99.0, ckms.get(0.99), 1);
    }

    @Test
    public void testGetWithAMillionElements() {
        List<Quantile> quantiles = new ArrayList<Quantile>();
        quantiles.add(new Quantile(0.0, 0.01));
        quantiles.add(new Quantile(0.10, 0.01));
        quantiles.add(new Quantile(0.90, 0.001));
        quantiles.add(new Quantile(0.95, 0.02));
        quantiles.add(new Quantile(0.99, 0.001));

        final int elemCount = 1000000;
        double[] shuffle = new double[elemCount];
        for (int i = 0; i < shuffle.length; i++) {
            shuffle[i] = i + 1;
        }
        Random rand = new Random(0);

        Collections.shuffle(Arrays.asList(shuffle), rand);

        CKMSQuantiles ckms = new CKMSQuantiles(
                quantiles.toArray(new Quantile[]{}));

        for (double v : shuffle) {
            ckms.insert(v);
        }
        // given the linear distribution, we set the delta equal to the εn value for this quantile
        assertEquals(0.1 * elemCount, ckms.get(0.1), 0.01 * elemCount);
        assertEquals(0.9 * elemCount, ckms.get(0.9), 0.001 * elemCount);
        assertEquals(0.95 * elemCount, ckms.get(0.95), 0.02 * elemCount);
        assertEquals(0.99 * elemCount, ckms.get(0.99), 0.001 * elemCount);

        assertTrue("sample size should be way below 1_000_000", ckms.samples.size() < 1000);
    }


    @Test
    public void testGetGaussian() {
        RandomGenerator rand = new JDKRandomGenerator();
        rand.setSeed(0);

        double mean = 0.0;
        double stddev = 1.0;
        NormalDistribution normalDistribution = new NormalDistribution(rand, mean, stddev, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        List<Quantile> quantiles = new ArrayList<Quantile>();
        quantiles.add(new Quantile(0.10, 0.001));
        quantiles.add(new Quantile(0.50, 0.01));
        quantiles.add(new Quantile(0.90, 0.001));
        quantiles.add(new Quantile(0.95, 0.001));
        quantiles.add(new Quantile(0.99, 0.001));

        CKMSQuantiles ckms = new CKMSQuantiles(
                quantiles.toArray(new Quantile[]{}));

        final int elemCount = 1000000;
        double[] shuffle = normalDistribution.sample(elemCount);

        // insert a million samples
        for (double v : shuffle) {
            ckms.insert(v);
        }

        // give the actual values for the quantiles we test
        double p10 = normalDistribution.inverseCumulativeProbability(0.1);
        double p90 = normalDistribution.inverseCumulativeProbability(0.9);
        double p95 = normalDistribution.inverseCumulativeProbability(0.95);
        double p99 = normalDistribution.inverseCumulativeProbability(0.99);

        //ε-approximate quantiles relaxes the requirement
        //to finding an item with rank between (φ−ε)n and (φ+ε)n.
        assertEquals(p10, ckms.get(0.1), errorBoundsNormalDistribution(0.1, 0.001, normalDistribution));
        assertEquals(mean, ckms.get(0.5), errorBoundsNormalDistribution(0.5, 0.01, normalDistribution));
        assertEquals(p90, ckms.get(0.9), errorBoundsNormalDistribution(0.9, 0.001, normalDistribution));
        assertEquals(p95, ckms.get(0.95), errorBoundsNormalDistribution(0.95, 0.001, normalDistribution));
        assertEquals(p99, ckms.get(0.99), errorBoundsNormalDistribution(0.99, 0.001, normalDistribution));

        assertTrue("sample size should be below 1000", ckms.samples.size() < 1000);
    }

    @Test
    public void checkBounds() {
        try {
            new Quantile(-1, 0);
        } catch (IllegalArgumentException e) {
            assertEquals("Quantile must be between 0 and 1", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception thrown" + e);
        }

        try {
            new Quantile(0.95, 2);
        } catch (IllegalArgumentException e) {
            assertEquals("Epsilon must be between 0 and 1", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception thrown" + e);
        }
    }

    double errorBoundsNormalDistribution(double p, double epsilon, NormalDistribution nd) {
        //(φ+ε)n
        double upperBound = nd.inverseCumulativeProbability(p + epsilon);
        //(φ−ε)n
        double lowerBound = nd.inverseCumulativeProbability(p - epsilon);
        // subtract and divide by 2, assuming that the increase is linear in this small epsilon.
        return Math.abs(upperBound - lowerBound) / 2;
    }

    /**
     * In Java 8 we could use IntStream
     */
    List<Double> makeSequence(int begin, int end) {
        List<Double> ret = new ArrayList<Double>(end - begin + 1);
        for (int i = begin; i <= end; i++) {
            ret.add((double) i);
        }
        return ret;
    }
}
