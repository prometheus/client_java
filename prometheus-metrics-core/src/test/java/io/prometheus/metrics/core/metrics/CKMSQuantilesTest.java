package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.core.metrics.CKMSQuantiles.Quantile;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CKMSQuantilesTest {

    private final Quantile qMin = new Quantile(0.0, 0.00);
    private final Quantile q50 = new Quantile(0.5, 0.01);
    private final Quantile q95 = new Quantile(0.95, 0.005);
    private final Quantile q99 = new Quantile(0.99, 0.001);
    private final Quantile qMax = new Quantile(1.0, 0.00);

    @Test
    public void testGetOnEmptyValues() {
        CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, q99);
        assertTrue(Double.isNaN(ckms.get(q95.quantile)));
    }

    @Test
    public void testGet() {
        Random random = new Random(0);
        CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, q99);
        List<Double> input = shuffledValues(100, random);
        for (double value : input) {
            ckms.insert(value);
        }
        validateResults(ckms);
    }

    @Test
    public void testBatchInsert() {
        Random random = new Random(1);
        testInsertBatch(1, 1, 100, random);
        testInsertBatch(1, 10, 100, random);
        testInsertBatch(2, 10, 100, random);
        testInsertBatch(2, 110, 100, random); // compress never called, because compress interval > number of inserts
        testInsertBatch(3, 10, 100, random);
        testInsertBatch(10, 10, 100, random);
        testInsertBatch(128, 128, 1, random);
        testInsertBatch(128, 128, 1000, random);
        testInsertBatch(128, 128, 10*1000, random);
        testInsertBatch(128, 128, 100*1000, random);
        testInsertBatch(128, 128, 1000*1000, random);
    }

    private void testInsertBatch(int batchSize, int compressInterval, int totalNumber, Random random) {
        System.out.println("testInsertBatch(batchSize=" + batchSize + ", compressInterval=" + compressInterval + ", totalNumber=" + totalNumber + ")");
        CKMSQuantiles ckms = new CKMSQuantiles(q50, q95);
        int insertsSinceCompress = 0;
        List<Double> input = shuffledValues(totalNumber, random);
        for (int i=0; i<input.size(); i+=batchSize) {
            double[] batch = new double[batchSize];
            int j;
            for (j=0; j<batchSize && i+j < input.size(); j++) {
                batch[j] = input.get(i+j);
            }
            Arrays.sort(batch, 0, j);
            ckms.insertBatch(batch, j);
            validateSamples(ckms); // after each insert the samples should still be valid
            insertsSinceCompress += j;
            if (insertsSinceCompress >= compressInterval) {
                ckms.compress();
                validateSamples(ckms); // after each compress the samples should still be valid
                insertsSinceCompress=0;
            }
        }
        validateResults(ckms);
    }

    @Test
    public void testGetWithAMillionElements() {
        Random random = new Random(2);
        List<Double> input = shuffledValues(1000*1000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, q99);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        assertTrue("sample size should be way below 1_000_000", ckms.samples.size() < 1000);
    }

    @Test
    public void testMin() {
        Random random = new Random(3);
        List<Double> input = shuffledValues(1000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(qMin);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        ckms.compress();
        assertEquals(2, ckms.samples.size());
    }

    @Test
    public void testMax() {
        Random random = new Random(4);
        List<Double> input = shuffledValues(1000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(qMax);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        ckms.compress();
        assertEquals(2, ckms.samples.size());
    }

    @Test
    public void testMinMax() {
        Random random = new Random(5);
        List<Double> input = shuffledValues(1000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(qMin, qMax);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        ckms.compress();
        assertEquals(2, ckms.samples.size());
    }

    @Test
    public void testMinAndOthers() {
        Random random = new Random(6);
        List<Double> input = shuffledValues(1000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(q95, qMin);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        assertTrue(ckms.samples.size() < 200); // should be a lot less than input.size()
    }

    @Test
    public void testMaxAndOthers() {
        Random random = new Random(7);
        List<Double> input = shuffledValues(10000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(q50, q95, qMax);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        assertTrue(ckms.samples.size() < 200); // should be a lot less than input.size()
    }

    @Test
    public void testMinMaxAndOthers() {
        Random random = new Random(8);
        List<Double> input = shuffledValues(10000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(qMin, q50, q95, q99, qMax);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        assertTrue(ckms.samples.size() < 200); // should be a lot less than input.size()
    }

    @Test
    public void testExactQuantile() {
        Random random = new Random(9);
        List<Double> input = shuffledValues(10000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.95, 0));
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        // With epsilon == 0 we need to keep all inputs in samples.
        assertEquals(input.size(), ckms.samples.size());
    }

    @Test
    public void testExactAndOthers() {
        Random random = new Random(10);
        List<Double> input = shuffledValues(10000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(q50, new Quantile(0.95, 0), q99);
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        // With epsilon == 0 we need to keep all inputs in samples.
        assertEquals(input.size(), ckms.samples.size());
    }

    @Test
    public void testExactAndMin() {
        Random random = new Random(11);
        List<Double> input = shuffledValues(10000, random);
        CKMSQuantiles ckms = new CKMSQuantiles(qMin, q50, new Quantile(0.95, 0));
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
        // With epsilon == 0 we need to keep all inputs in samples.
        assertEquals(input.size(), ckms.samples.size());
    }

    @Test
    public void testMaxEpsilon() {
        Random random = new Random(12);
        List<Double> input = shuffledValues(10000, random);
        // epsilon == 1 basically gives you random results, but it should still not throw an exception.
        CKMSQuantiles ckms = new CKMSQuantiles(new Quantile(0.95, 1));
        for (double v : input) {
            ckms.insert(v);
        }
        validateResults(ckms);
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

        CKMSQuantiles ckms = new CKMSQuantiles(quantiles.toArray(new Quantile[]{}));

        final int elemCount = 1000*1000;
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

    double errorBoundsNormalDistribution(double p, double epsilon, NormalDistribution nd) {
        //(φ+ε)n
        double upperBound = nd.inverseCumulativeProbability(p + epsilon);
        //(φ−ε)n
        double lowerBound = nd.inverseCumulativeProbability(p - epsilon);
        // subtract and divide by 2, assuming that the increase is linear in this small epsilon.
        return Math.abs(upperBound - lowerBound) / 2;
    }

    @Test
    public void testIllegalArgumentException() {
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

    private List<Double> shuffledValues(int n, Random random) {
        List<Double> result = new ArrayList<Double>(n);
        for (int i=0; i<n; i++) {
            result.add(i+1.0);
        }
        Collections.shuffle(result, random);
        return result;
    }

    /**
     * The following invariant must be true for each sample: g + delta <= f(r)
     */
    private void validateSamples(CKMSQuantiles ckms) {
        double prev = -1.0;
        int r = 0; // sum of all g's left of the current sample
        for (CKMSQuantiles.Sample sample : ckms.samples) {
            String msg = "invalid sample " + sample + ": count=" + ckms.n + " r=" + r + " f(r)=" + ckms.f(r);
            assertTrue(msg, sample.g + sample.delta <= ckms.f(r));
            assertTrue("Samples not ordered. Keep in mind that insertBatch() takes a sorted array as parameter.", prev <= sample.value);
            prev = sample.value;
            r += sample.g;
        }
        assertEquals("the sum of all g's must be the total number of observations", r, ckms.n);
    }

    /**
     * The values that we insert in these tests are always the numbers from 1 to n, in random order.
     * So we can trivially calculate the range of acceptable results for each quantile.
     * We check if the value returned by get() is within the range of acceptable results.
     */
    private void validateResults(CKMSQuantiles ckms) {
        for (Quantile q : ckms.quantiles) {
            double actual = ckms.get(q.quantile);
            double lowerBound, upperBound;
            if (q.quantile == 0) {
                lowerBound = 1;
                upperBound = 1;
            } else if (q.quantile == 1) {
                lowerBound = ckms.n;
                upperBound = ckms.n;
            } else {
                lowerBound = Math.floor(ckms.n * (q.quantile - 2 * q.epsilon));
                upperBound = Math.ceil(ckms.n * (q.quantile + 2 * q.epsilon));
            }
            boolean ok = actual >= lowerBound && actual <= upperBound;
            if (!ok) {
                for (CKMSQuantiles.Sample sample : ckms.samples) {
                    System.err.println(sample);
                }
            }
            String errorMessage = q + ": " + actual + " not in [" + lowerBound + ", " + upperBound + "], n=" + ckms.n + ", " +  q.quantile + "*" + ckms.n + "=" + (q.quantile*ckms.n);
            assertTrue(errorMessage, ok);
        }
    }
}
