package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.shaded.com_google_protobuf_3_25_3.TextFormat;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_25_3.Metrics;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfigTestUtil;
import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.initializer.SpanContextSupplier;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;

import static io.prometheus.metrics.core.metrics.TestUtil.assertExemplarEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

public class CounterTest {

    Counter noLabels;
    Counter labels;
    private static final long exemplarSampleIntervalMillis = 10;
    private static final long exemplarMinAgeMillis = 100;
    SpanContext origSpanContext;

    @Rule
    public final ExpectedException thrown = none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        noLabels = Counter.builder().name("nolabels").build();
        labels = Counter.builder().name("labels")
                .help("help")
                .unit(Unit.SECONDS)
                .labelNames("l")
                .build();
        origSpanContext = SpanContextSupplier.getSpanContext();
        ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(noLabels, exemplarSampleIntervalMillis);
        ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(noLabels, exemplarMinAgeMillis);
        ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(labels, exemplarSampleIntervalMillis);
        ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(labels, exemplarMinAgeMillis);
    }

    @After
    public void tearDown() {
        SpanContextSupplier.setSpanContext(origSpanContext);
    }

    private CounterSnapshot.CounterDataPointSnapshot getData(Counter counter, String... labels) {
        return counter.collect().getDataPoints().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("counter with labels " + labels + " not found"));
    }

    private double getValue(Counter counter, String... labels) {
        return getData(counter, labels).getValue();
    }


    private int getNumberOfLabels(Counter counter) {
        return ((CounterSnapshot) counter.collect()).getDataPoints().size();
    }

    @Test
    public void testIncrement() {
        noLabels.inc();
        assertEquals(1.0, getValue(noLabels), .001);
        noLabels.inc(2);
        assertEquals(3.0, getValue(noLabels), .001);
        noLabels.labelValues().inc(4);
        assertEquals(7.0, getValue(noLabels), .001);
        noLabels.labelValues().inc();
        assertEquals(8.0, getValue(noLabels), .001);
    }

    @Test
    public void testNegativeIncrementFails() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Negative increment -1 is illegal for Counter metrics.");
        noLabels.inc(-1);
    }

    @Test
    public void testEmptyCountersHaveNoLabels() {
        assertEquals(1, getNumberOfLabels(noLabels));
        assertEquals(0, getNumberOfLabels(labels));
    }

    @Test
    public void testLabels() {
        assertEquals(0, getNumberOfLabels(labels));
        labels.labelValues("a").inc();
        assertEquals(1, getNumberOfLabels(labels));
        assertEquals(1.0, getValue(labels, "l", "a"), .001);
        labels.labelValues("b").inc(3);
        assertEquals(2, getNumberOfLabels(labels));
        assertEquals(1.0, getValue(labels, "l", "a"), .001);
        assertEquals(3.0, getValue(labels, "l", "b"), .001);
    }

    @Test
    public void testTotalStrippedFromName() {
        for (String name : new String[]{
                "my_counter_total", "my.counter.total",
                "my_counter_seconds_total", "my.counter.seconds.total",
                "my_counter", "my.counter",
                "my_counter_seconds", "my.counter.seconds"}) {
            Counter counter = Counter.builder()
                    .name(name)
                    .unit(Unit.SECONDS)
                    .build();
            Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(counter.collect());
            assertEquals("name: \"my_counter_seconds_total\" type: COUNTER metric { counter { value: 0.0 } }", TextFormat.printer().shortDebugString(protobufData));
        }
    }

    @Test
    public void testSnapshotComplete() {
        long before = System.currentTimeMillis();
        Counter counter = Counter.builder()
                .name("test_seconds_total")
                .unit(Unit.SECONDS)
                .help("help message")
                .constLabels(Labels.of("const1name", "const1value", "const2name", "const2value"))
                .labelNames("path", "status")
                .build();
        counter.labelValues("/", "200").inc(2);
        counter.labelValues("/", "500").inc();
        CounterSnapshot snapshot = (CounterSnapshot) counter.collect();
        Assert.assertEquals("test_seconds", snapshot.getMetadata().getName());
        Assert.assertEquals("seconds", snapshot.getMetadata().getUnit().toString());
        Assert.assertEquals("help message", snapshot.getMetadata().getHelp());
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        Iterator<CounterSnapshot.CounterDataPointSnapshot> iter = snapshot.getDataPoints().iterator();
        // data is ordered by labels, so 200 comes before 500
        CounterSnapshot.CounterDataPointSnapshot data = iter.next();
        Assert.assertEquals(Labels.of("const1name", "const1value", "const2name", "const2value", "path", "/", "status", "200"), data.getLabels());
        Assert.assertEquals(2, data.getValue(), 0.0001);
        Assert.assertTrue(data.getCreatedTimestampMillis() >= before);
        Assert.assertTrue(data.getCreatedTimestampMillis() <= System.currentTimeMillis());
        // 500
        data = iter.next();
        Assert.assertEquals(Labels.of("const1name", "const1value", "const2name", "const2value", "path", "/", "status", "500"), data.getLabels());
        Assert.assertEquals(1, data.getValue(), 0.0001);
        Assert.assertTrue(data.getCreatedTimestampMillis() >= before);
        Assert.assertTrue(data.getCreatedTimestampMillis() <= System.currentTimeMillis());
    }

    @Test
    public void testIncWithExemplar() throws Exception {
        noLabels.incWithExemplar(Labels.of("key", "value"));
        assertExemplar(noLabels, 1.0, "key", "value");

        Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

        noLabels.incWithExemplar(Labels.EMPTY);
        assertExemplar(noLabels, 1.0);

        Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

        noLabels.incWithExemplar(3, Labels.of("key1", "value1", "key2", "value2"));
        assertExemplar(noLabels, 3, "key1", "value1", "key2", "value2");
    }

    private void assertExemplar(Counter counter, double value, String... labels) {
        Exemplar exemplar = getData(counter).getExemplar();
        Assert.assertEquals(value, exemplar.getValue(), 0.0001);
        assertEquals(Labels.of(labels), exemplar.getLabels());
    }

    @Test
    public void testExemplarSampler() throws Exception {
        final Exemplar exemplar1 = Exemplar.builder()
                .value(2.0)
                .traceId("abc")
                .spanId("123")
                .build();
        final Exemplar exemplar2 = Exemplar.builder()
                .value(1.0)
                .traceId("def")
                .spanId("456")
                .build();
        final Exemplar exemplar3 = Exemplar.builder()
                .value(1.0)
                .traceId("123")
                .spanId("abc")
                .build();
        final Exemplar customExemplar = Exemplar.builder()
                .value(1.0)
                .traceId("bab")
                .spanId("cdc")
                .labels(Labels.of("test", "test"))
                .build();
        SpanContext spanContext = new SpanContext() {
            private int callNumber = 0;

            @Override
            public String getCurrentTraceId() {
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
            public String getCurrentSpanId() {
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
            public boolean isCurrentSpanSampled() {
                callNumber++;
                if (callNumber == 2) {
                    return false;
                }
                return true;
            }

            @Override
            public void markCurrentSpanAsExemplar() {
            }
        };
        Counter counter = Counter.builder()
                .name("count_total")
                .build();

        SpanContextSupplier.setSpanContext(spanContext);
        ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(counter, exemplarMinAgeMillis);
        ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(counter, exemplarSampleIntervalMillis);

        counter.inc(2.0);
        assertExemplarEquals(exemplar1, getData(counter).getExemplar());

        Thread.sleep(2 * exemplarSampleIntervalMillis);

        counter.inc(3.0); // min age not reached -> keep the previous exemplar, exemplar sampler not called
        assertExemplarEquals(exemplar1, getData(counter).getExemplar());

        Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

        counter.inc(2.0); // 2nd call: isSampled() returns false -> not sampled
        assertExemplarEquals(exemplar1, getData(counter).getExemplar());

        Thread.sleep(2 * exemplarSampleIntervalMillis);

        counter.inc(1.0); // sampled
        assertExemplarEquals(exemplar2, getData(counter).getExemplar());

        Thread.sleep(exemplarMinAgeMillis + 2 * exemplarSampleIntervalMillis);

        counter.inc(1.0); // sampled
        assertExemplarEquals(exemplar3, getData(counter).getExemplar());

        Thread.sleep(2 * exemplarSampleIntervalMillis);

        counter.incWithExemplar(Labels.of("test", "test")); // custom exemplar sampled even though the automatic exemplar hasn't reached min age yet
        assertExemplarEquals(customExemplar, getData(counter).getExemplar());
    }

    @Test
    public void testExemplarSamplerDisabled() {
        Counter counter = Counter.builder()
                //.withExemplarSampler((inc, prev) -> {throw new RuntimeException("unexpected call to exemplar sampler");})
                .name("count_total")
                .withoutExemplars()
                .build();
        counter.incWithExemplar(3.0, Labels.of("a", "b"));
        Assert.assertNull(getData(counter).getExemplar());
        counter.inc(2.0);
        Assert.assertNull(getData(counter).getExemplar());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstLabelsFirst() {
        Counter.builder()
                .name("test_total")
                .constLabels(Labels.of("const_a", "const_b"))
                .labelNames("const.a")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstLabelsSecond() {
        Counter.builder()
                .name("test_total")
                .labelNames("const.a")
                .constLabels(Labels.of("const_a", "const_b"))
                .build();
    }
}
