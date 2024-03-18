package io.prometheus.metrics.core.exemplars;

import io.prometheus.metrics.tracer.initializer.SpanContextSupplier;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.core.util.Scheduler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExemplarSamplerTest {

    private final int tick = 10; // Time step in milliseconds. Make this larger if the test is flaky.
    private final int sampleInterval = 10 * tick; // do not change this
    private final int minAge = 50 * tick; // do not change this
    private final int maxAge = 200 * tick; // do not change this

    private ExemplarSamplerConfig makeConfig(double... buckets) {
        return new ExemplarSamplerConfig(
                minAge,
                maxAge,
                sampleInterval,
                buckets.length == 0 ? 4 : buckets.length, // number of exemplars
                buckets.length == 0 ? null : buckets
        );
    }


    private static class SpanContext implements io.prometheus.metrics.tracer.common.SpanContext {

        int callCount = 0;
        boolean isSampled = true;
        boolean isExemplar = false;

        @Override
        public String getCurrentTraceId() {
            return "" + (callCount++);
        }

        @Override
        public String getCurrentSpanId() {
            return "" + callCount;
        }

        @Override
        public boolean isCurrentSpanSampled() {
            return isSampled;
        }

        @Override
        public void markCurrentSpanAsExemplar() {
            isExemplar = true;
        }
    }

    @Test
    public void testCustomExemplarsBuckets() throws Exception {
        // TODO
    }

    private io.prometheus.metrics.tracer.common.SpanContext origContext;

    @Before
    public void setUp() {
        origContext = SpanContextSupplier.getSpanContext();
    }

    @After
    public void tearDown() {
        SpanContextSupplier.setSpanContext(origContext);
    }

    @Test
    public void testIsSampled() throws Exception {
        SpanContext context = new SpanContext();
        context.isSampled = false;
        ExemplarSampler sampler = new ExemplarSampler(makeConfig(), context);
        Thread.sleep(tick); // t = 1 tick
        sampler.observe(0.3); // no sampled, because isSampled() returns false
        assertExemplars(sampler); // empty
    }

    @Test
    public void testDefaultConfigHasFourExemplars() throws Exception {
        ExemplarSampler sampler = new ExemplarSampler(makeConfig(), new SpanContext());
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
        ExemplarSampler sampler = new ExemplarSampler(makeConfig(Double.POSITIVE_INFINITY), new SpanContext());
        Thread.sleep(tick); // t = 1 tick
        sampler.observe(0.8); // observed in the +Inf bucket
        Thread.sleep(sampleInterval + tick); // t = 12 tick
        sampler.observe(0.5); // not observed, because +Inf is the only bucket
        assertExemplars(sampler, 0.8);
        print(sampler.collect());
    }

    @Test
    public void testDefaultExemplarsBuckets() throws Exception {
        ExemplarSampler sampler = new ExemplarSampler(makeConfig(0.2, 0.4, 0.6, 0.8, 1.0, Double.POSITIVE_INFINITY), new SpanContext());
        Scheduler.awaitInitialization();
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
        ExemplarSampler sampler = new ExemplarSampler(makeConfig(), new SpanContext());
        Scheduler.awaitInitialization();
        Thread.sleep(tick);           // t = 1 tick
        sampler.observe(1);    // observed
        assertExemplars(sampler, 1);
        sampler.observe(2);    // not observed, previous observation is less than sample interval ms ago
        Thread.sleep(sampleInterval + tick); // t = 12 ticks
        sampler.observe(3);    // observed
        assertExemplars(sampler, 1, 3);
        Thread.sleep(2 * tick);    // t = 14 ticks
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
        Exemplars exemplars = sampler.collect();
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

    private void print(Exemplars exemplars) {
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
