package io.prometheus.metrics.exemplars;

import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Label;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class ExemplarSamplerTest {

    private final int tick = 10; // Time step in milliseconds. Make this larger if the test is flaky.
    private final int sampleInterval = 10 * tick; // do not change this
    private final int minAge = 50 * tick; // do not change this
    private final int maxAge = 200 * tick; // do not change this

    private static class SpanContextSupplier implements io.prometheus.client.exemplars.tracer.common.SpanContextSupplier {

        int callCount = 0;
        boolean isSampled = true;

        @Override
        public String getTraceId() {
            return "" + (callCount++);
        }

        @Override
        public String getSpanId() {
            return "" + callCount;
        }

        @Override
        public boolean isSampled() {
            return isSampled;
        }
    }

    @Test
    public void testCustomExemplarsBuckets() throws Exception {
        // TODO
    }

    @Test
    public void testIsSampled() throws Exception {
        SpanContextSupplier scs = new SpanContextSupplier();
        scs.isSampled = false;
        ExemplarSampler sampler = DefaultExemplarSampler.newInstance(ExemplarConfig.newBuilder()
                .withSpanContextSupplier(scs)
                .withMinAgeMillis(minAge)
                .withMaxAgeMillis(maxAge)
                .withSampleIntervalMillis(sampleInterval)
                .build());
        Thread.sleep(tick); // t = 1 tick
        sampler.observe(0.3); // no sampled, because isSampled() returns false
        assertExemplars(sampler); // empty
    }

    @Test
    public void testDefaultConfigHasFourExemplars() throws Exception {
        SpanContextSupplier scs = new SpanContextSupplier();
        ExemplarSampler sampler = DefaultExemplarSampler.newInstance(ExemplarConfig.newBuilder()
                .withSpanContextSupplier(scs)
                .withMinAgeMillis(minAge)
                .withMaxAgeMillis(maxAge)
                .withSampleIntervalMillis(sampleInterval)
                .build());
        Thread.sleep(tick); // t = 1 tick
        sampler.observe(0.3);
        Thread.sleep(sampleInterval + tick); // t = 12 tick
        sampler.observe(0.8);
        Thread.sleep(sampleInterval + tick); // t = 23 tick
        sampler.observe(0.4);
        Thread.sleep(sampleInterval + tick); // t = 34 tick
        sampler.observe(0.6);
        Thread.sleep(sampleInterval + tick); // t = 45 tick
        sampler.observe(0.2); // not observed, we got 4 Exemplars already and non reached min age
        assertExemplars(sampler, 0.3, 0.8, 0.4, 0.6);
        print(sampler.collect());
    }

    @Test
    public void testEmptyBuckets() throws Exception {
        SpanContextSupplier scs = new SpanContextSupplier();
        ExemplarSampler sampler = DefaultExemplarSampler.newInstance(ExemplarConfig.newBuilder()
                .withSpanContextSupplier(scs)
                .withMinAgeMillis(minAge)
                .withMaxAgeMillis(maxAge)
                .withSampleIntervalMillis(sampleInterval)
                .withBuckets()
                .build());
        Thread.sleep(tick); // t = 1 tick
        sampler.observe(0.8); // observed in the +Inf bucket
        Thread.sleep(sampleInterval + tick); // t = 12 tick
        sampler.observe(0.5); // not observed, because +Inf is the only bucket
        assertExemplars(sampler, 0.8);
        print(sampler.collect());
    }

    @Test
    public void testDefaultExemplarsBuckets() throws Exception {
        SpanContextSupplier scs = new SpanContextSupplier();
        ExemplarSampler sampler = DefaultExemplarSampler.newInstance(ExemplarConfig.newBuilder()
                .withSpanContextSupplier(scs)
                .withBuckets(0.2, 0.4, 0.6, 0.8, 1.0)
                .withMinAgeMillis(minAge)
                .withMaxAgeMillis(maxAge)
                .withSampleIntervalMillis(sampleInterval)
                .build());

        ((DefaultExemplarSampler) sampler).awaitInitialization();
        Thread.sleep(tick); // t = 1 tick
        sampler.observe(0.3);
        sampler.observe(0.5); // not observed, previous observation is less than sample interval ms ago
        assertExemplars(sampler, 0.3);
        Thread.sleep(sampleInterval + tick); // t = 12 ticks
        sampler.observe(0.5); // observed
        assertExemplars(sampler, 0.3, 0.5);
        Thread.sleep(sampleInterval + tick); // t = 23 ticks
        sampler.observe(0.4); // not observed, because 0.3 hasn't reached min age yet
        assertExemplars(sampler, 0.3, 0.5);
        Thread.sleep(sampleInterval + tick); // t = 34 ticks
        sampler.observe(1.1); // observed
        assertExemplars(sampler, 0.3, 0.5, 1.1);
        Thread.sleep(20 * tick); // t = 54 ticks
        assertExemplars(sampler, 0.3, 0.5, 1.1);
        sampler.observe(0.4); // observed
        assertExemplars(sampler, 0.4, 0.5, 1.1);
        Thread.sleep(159 * tick); // t = 213 ticks
        assertExemplars(sampler, 0.4, 1.1); // 0.5 evicted because it has reached max age
        print(sampler.collect());
    }

    @Test
    public void testCustomExemplarsNoBuckets() throws Exception {
        // TODO
    }

    @Test
    public void testDefaultExemplarsNoBuckets() throws Exception {
        SpanContextSupplier scs = new SpanContextSupplier();
        ExemplarSampler sampler = DefaultExemplarSampler.newInstance(ExemplarConfig.newBuilder()
                .withSpanContextSupplier(scs)
                .withNumberOfExemplars(4)
                .withMinAgeMillis(minAge)
                .withMaxAgeMillis(maxAge)
                .withSampleIntervalMillis(sampleInterval)
                .build());
        ((DefaultExemplarSampler) sampler).awaitInitialization();
        Thread.sleep(tick);           // t = 1 tick
        sampler.observe(1);    // observed
        assertExemplars(sampler, 1);
        sampler.observe(2);    // not observed, previous observation is less than sample interval ms ago
        Thread.sleep(sampleInterval + tick); // t = 12 ticks
        sampler.observe(3);    // observed
        assertExemplars(sampler, 1, 3);
        Thread.sleep(2*tick);    // t = 14 ticks
        sampler.observe(4);    // not observed, previous observation is less than sample interval ms ago
        Thread.sleep(sampleInterval + tick); // t = 25 ticks
        sampler.observe(5);    // observed
        assertExemplars(sampler, 1, 3, 5);
        Thread.sleep(sampleInterval + tick); // t = 36 ticks
        sampler.observe(6);    // observed
        assertExemplars(sampler, 1, 3, 5, 6);
        Thread.sleep(sampleInterval + tick);  // t = 47 ticks
        sampler.observe(7);    // not observed, because no Exemplar has reached the minimum age yet
        Thread.sleep(5 * tick); // t = 52 ticks
        sampler.observe(2); // not observed. 1 is older than min age, but kept because it's the minimum
        assertExemplars(sampler, 1, 3, 5, 6);
        Thread.sleep(sampleInterval + tick); // t = 63 ticks
        sampler.observe(2); // observed
        assertExemplars(sampler, 1, 2, 5, 6);
        Thread.sleep(27 * tick); // t = 90 ticks
        sampler.observe(7); // observed, replaces 6 because 7 > 6 even though 5 is older
        assertExemplars(sampler, 1, 2, 5, 7);
        sampler.observe(8); // not observed, sample interval not done
        assertExemplars(sampler, 1, 2, 5, 7);
        Thread.sleep(sampleInterval + tick); // t = 101 ticks
        sampler.observe(8); // observed
        assertExemplars(sampler, 1, 2, 8, 7);
        Thread.sleep(101 * tick); // t = 202 ticks
        sampler.observe(5); // observed, replaces 1 because 1 reached the max age
        assertExemplars(sampler, 5, 2, 8, 7);
        print(sampler.collect());
    }

    private void assertExemplars(ExemplarSampler sampler, double... values) {
        Collection<Exemplar> exemplars = sampler.collect();
        Assert.assertEquals(values.length, exemplars.size());
        for (double value : values) {
            boolean found = false;
            for (Exemplar exemplar : exemplars) {
                if (exemplar.getValue() == value) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(value + " not found", found);
        }
    }

    private void print(Collection<Exemplar> exemplars) {
        System.out.print("[");
        boolean farst = true;
        for (Exemplar exemplar : exemplars) {
            if (!farst) {
                System.out.print(",");
            }
            farst = false;
            System.out.print(exemplar.getValue() + "{");
            boolean first = true;
            for (Label label : exemplar.getLabels()) {
                if (!first) {
                    System.out.print(",");
                }
                System.out.print(label.getName() + "=" + label.getValue());
                first = false;
            }
            if (!first) {
                System.out.print(",");
            }
            System.out.print("age=" + (System.currentTimeMillis() - exemplar.getTimestampMillis()));
            System.out.print("}");
        }
        System.out.println("]");
    }
}
