package io.prometheus.metrics.core;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.Timer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.prometheus.metrics.core.TestUtil.assertExemplarEquals;
import static org.junit.Assert.assertEquals;

public class GaugeTest {

    private static final long exemplarSampleIntervalMillis = 10;
    private static final long exemplarMinAgeMillis = 100;

    private Gauge noLabels, labels;

    @BeforeClass
    public static void beforeClass() {
        ExemplarConfig.setDefaultSampleIntervalMillis(exemplarSampleIntervalMillis);
        ExemplarConfig.setDefaultMinAgeMillis(exemplarMinAgeMillis);
    }

    @Before
    public void setUp() {
        noLabels = Gauge.newBuilder().withName("nolabels").build();
        labels = Gauge.newBuilder().withName("labels").withLabelNames("l").build();
    }

    private GaugeSnapshot.GaugeData getData(Gauge gauge, String... labels) {
        return ((GaugeSnapshot) gauge.collect()).getData().stream()
                .filter(data -> data.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    private double getValue(Gauge gauge, String... labels) {
        return getData(gauge, labels).getValue();
    }

    @Test
    public void testIncrement() {
        noLabels.inc();
        assertEquals(1.0, getValue(noLabels), .001);
        noLabels.inc(2);
        assertEquals(3.0, getValue(noLabels), .001);
        noLabels.inc(4);
        assertEquals(7.0, getValue(noLabels), .001);
        noLabels.inc();
        assertEquals(8.0, getValue(noLabels), .001);
    }

    @Test
    public void testDecrement() {
        noLabels.dec();
        assertEquals(-1.0, getValue(noLabels), .001);
        noLabels.dec(2);
        assertEquals(-3.0, getValue(noLabels), .001);
        noLabels.dec(4);
        assertEquals(-7.0, getValue(noLabels), .001);
        noLabels.dec();
        assertEquals(-8.0, getValue(noLabels), .001);
    }

    @Test
    public void testSet() {
        noLabels.set(42);
        assertEquals(42, getValue(noLabels), .001);
        noLabels.set(7);
        assertEquals(7.0, getValue(noLabels), .001);
    }

    @Test
    public void testTimer() throws InterruptedException {
        try (Timer timer = noLabels.startTimer()) {
            Thread.sleep(12);
        }
        assertEquals(0.012, getValue(noLabels), 0.005); // 5ms delta should be enough so this isn't flaky
    }

    @Test
    public void noLabelsDefaultZeroValue() {
        assertEquals(0.0, getValue(noLabels), .001);
    }

    @Test
    public void testLabels() {
        labels.withLabels("a").inc();
        labels.withLabels("b").inc(3);
        assertEquals(1.0, getValue(labels, "l", "a"), .001);
        assertEquals(3.0, getValue(labels, "l", "b"), .001);
    }

    @Test
    public void testExemplarSampler() throws Exception {
        final Exemplar exemplar1 = Exemplar.newBuilder()
                .withValue(2.0)
                .withTraceId("abc")
                .withSpanId("123")
                .build();
        final Exemplar exemplar2 = Exemplar.newBuilder()
                .withValue(6.5)
                .withTraceId("def")
                .withSpanId("456")
                .build();
        final Exemplar exemplar3 = Exemplar.newBuilder()
                .withValue(7.0)
                .withTraceId("123")
                .withSpanId("abc")
                .build();
        final Exemplar customExemplar = Exemplar.newBuilder()
                .withValue(8.0)
                .withTraceId("bab")
                .withSpanId("cdc")
                .withLabels(Labels.of("test", "test"))
                .build();
        SpanContextSupplier scs = new SpanContextSupplier() {
            private int callNumber = 0;

            @Override
            public String getTraceId() {
                switch (callNumber) {
                    case 1:
                        return "abc";
                    case 3:
                        return "def";
                    case 4:
                        return "123";
                    case 5:
                        return "bab";
                    default:
                        throw new RuntimeException("unexpected call");
                }
            }

            @Override
            public String getSpanId() {
                switch (callNumber) {
                    case 1:
                        return "123";
                    case 3:
                        return "456";
                    case 4:
                        return "abc";
                    case 5:
                        return "cdc";
                    default:
                        throw new RuntimeException("unexpected call");
                }
            }

            @Override
            public boolean isSampled() {
                callNumber++;
                if (callNumber == 2) {
                    return false;
                }
                return true;
            }
        };
        Gauge gauge = Gauge.newBuilder()
                .withExemplarConfig(ExemplarConfig.newBuilder().withSpanContextSupplier(scs).build())
                .withName("my_gauge")
                .build();

        gauge.inc(2.0);
        assertExemplarEquals(exemplar1, getData(gauge).getExemplar());

        Thread.sleep(2 * exemplarSampleIntervalMillis);

        gauge.inc(3.0); // min age not reached -> keep the previous exemplar, exemplar sampler not called
        assertExemplarEquals(exemplar1, getData(gauge).getExemplar());

        Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

        gauge.inc(2.0); // 2nd call: isSampled() returns false -> not sampled
        assertExemplarEquals(exemplar1, getData(gauge).getExemplar());

        Thread.sleep(2 * exemplarSampleIntervalMillis);

        gauge.dec(0.5); // sampled
        assertExemplarEquals(exemplar2, getData(gauge).getExemplar());

        Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

        gauge.set(7.0); // sampled
        assertExemplarEquals(exemplar3, getData(gauge).getExemplar());

        Thread.sleep(2 * exemplarSampleIntervalMillis);

        gauge.incWithExemplar(Labels.of("test", "test")); // custom exemplar sampled even though the automatic exemplar hasn't reached min age yet
        assertExemplarEquals(customExemplar, getData(gauge).getExemplar());
    }

    @Test
    public void testExemplarSamplerDisabled() {
        Gauge gauge = Gauge.newBuilder()
                .withName("test")
                .withoutExemplars()
                .build();
        gauge.setWithExemplar(3.0, Labels.of("a", "b"));
        Assert.assertNull(getData(gauge).getExemplar());
        gauge.inc(2.0);
        Assert.assertNull(getData(gauge).getExemplar());
    }
}
