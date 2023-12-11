package io.prometheus.client.dropwizard;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;


public class DropwizardExportsTest {

    private CollectorRegistry registry = new CollectorRegistry();
    private MetricRegistry metricRegistry;

    private SampleBuilder sampleBuilder;

    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        sampleBuilder = Mockito.mock(SampleBuilder.class);
        new DropwizardExports(metricRegistry, sampleBuilder).register(registry);
    }

    @Test
    public void testCounter() {
        Mockito.when(sampleBuilder.createSample("foo.bar", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d)).thenReturn(new Collector.MetricFamilySamples.Sample("foo_bar", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d));
        metricRegistry.counter("foo.bar").inc();
        assertEquals(new Double(1),
                registry.getSampleValue("foo_bar")
        );
    }

    @Test
    public void testGauge() {
        Gauge<Integer> integerGauge = new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1234;
            }
        };
        Gauge<Double> doubleGauge = new Gauge<Double>() {
            @Override
            public Double getValue() {
                return 1.234D;
            }
        };
        Gauge<Long> longGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return 1234L;
            }
        };
        Gauge<Float> floatGauge = new Gauge<Float>() {
            @Override
            public Float getValue() {
                return 0.1234F;
            }
        };
        Gauge<Boolean> booleanGauge = new Gauge<Boolean>() {
            @Override
            public Boolean getValue() {
                return true;
            }
        };

        Mockito.when(sampleBuilder.createSample("integer.gauge", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234)).thenReturn(new Collector.MetricFamilySamples.Sample("integer_gauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));
        Mockito.when(sampleBuilder.createSample("long.gauge", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234)).thenReturn(new Collector.MetricFamilySamples.Sample("long_gauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));
        Mockito.when(sampleBuilder.createSample("double.gauge", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1.234)).thenReturn(new Collector.MetricFamilySamples.Sample("double_gauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1.234));
        Mockito.when(sampleBuilder.createSample("float.gauge", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 0.1234F)).thenReturn(new Collector.MetricFamilySamples.Sample("float_gauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 0.1234F));
        Mockito.when(sampleBuilder.createSample("boolean.gauge", "", Collections.<String>emptyList(), Collections.<String>emptyList(), 1)).thenReturn(new Collector.MetricFamilySamples.Sample("boolean_gauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1));

        metricRegistry.register("double.gauge", doubleGauge);
        metricRegistry.register("long.gauge", longGauge);
        metricRegistry.register("integer.gauge", integerGauge);
        metricRegistry.register("float.gauge", floatGauge);
        metricRegistry.register("boolean.gauge", booleanGauge);

        assertEquals(new Double(1234),
                registry.getSampleValue("integer_gauge", new String[]{}, new String[]{}));
        assertEquals(new Double(1234),
                registry.getSampleValue("long_gauge", new String[]{}, new String[]{}));
        assertEquals(new Double(1.234),
                registry.getSampleValue("double_gauge", new String[]{}, new String[]{}));
        assertEquals(new Double(0.1234F),
                registry.getSampleValue("float_gauge", new String[]{}, new String[]{}));
        assertEquals(new Double(1),
                registry.getSampleValue("boolean_gauge", new String[]{}, new String[]{}));
    }

    @Test
    public void testInvalidGaugeType() {
        Gauge<String> invalidGauge = new Gauge<String>() {
            @Override
            public String getValue() {
                return "foobar";
            }
        };

        metricRegistry.register("invalid_gauge", invalidGauge);
        assertEquals(null, registry.getSampleValue("invalid_gauge"));
        Mockito.verifyNoInteractions(sampleBuilder);
    }

    @Test
    public void testGaugeReturningNullValue() {
        Gauge<String> invalidGauge = new Gauge<String>() {
            @Override
            public String getValue() {
                return null;
            }
        };
        metricRegistry.register("invalid_gauge", invalidGauge);
        assertEquals(null, registry.getSampleValue("invalid_gauge"));
        Mockito.verifyNoInteractions(sampleBuilder);
    }

    void assertRegistryContainsMetrics(String... metrics) {
        for (String metric : metrics) {
            assertNotEquals(String.format("Metric %s should exist", metric), null,
                    registry.getSampleValue(metric, new String[]{}, new String[]{}));
        }
    }

    @Test
    public void testHistogram() throws IOException {
        // just test the standard mapper
        final MetricRegistry metricRegistry = new MetricRegistry();
        final CollectorRegistry registry = new CollectorRegistry();
        new DropwizardExports(metricRegistry).register(registry);
        Histogram hist = metricRegistry.histogram("hist");
        int i = 0;
        while (i < 100) {
            hist.update(i);
            i += 1;
        }
        assertEquals(new Double(100), registry.getSampleValue("hist_count"));
        for (Double d : Arrays.asList(0.75, 0.95, 0.98, 0.99)) {
            assertEquals(new Double((d - 0.01) * 100), registry.getSampleValue("hist",
                    new String[]{"quantile"}, new String[]{d.toString()}));
        }
        assertEquals(new Double(99), registry.getSampleValue("hist", new String[]{"quantile"},
                new String[]{"0.999"}));
    }

    @Test
    public void testMeter() throws IOException, InterruptedException {
        Mockito.when(sampleBuilder.createSample("meter", "_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 2)).thenReturn(new Collector.MetricFamilySamples.Sample("meter_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 2));
        Meter meter = metricRegistry.meter("meter");
        meter.mark();
        meter.mark();
        assertEquals(new Double(2), registry.getSampleValue("meter_total"));
    }

    @Test
    public void testTimer() throws IOException, InterruptedException {
        // just test the standard mapper
        final MetricRegistry metricRegistry = new MetricRegistry();
        final CollectorRegistry registry = new CollectorRegistry();
        new DropwizardExports(metricRegistry).register(registry);

        Timer t = metricRegistry.timer("timer");
        Timer.Context time = t.time();
        Thread.sleep(1L);
        time.stop();
        // We slept for 1Ms so we ensure that all timers are above 1ms:
        assertTrue(registry.getSampleValue("timer", new String[]{"quantile"}, new String[]{"0.99"}) > 0.001);
        assertEquals(new Double(1.0D), registry.getSampleValue("timer_count"));
    }

    @Test
    public void testThatMetricHelpUsesOriginalDropwizardName() {
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedTimer1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(new Collector.MetricFamilySamples.Sample("my_application_namedTimer1", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));

        Mockito.when(sampleBuilder.createSample(eq("my.application.namedCounter1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(new Collector.MetricFamilySamples.Sample("my_application_namedCounter1", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));

        Mockito.when(sampleBuilder.createSample(eq("my.application.namedMeter1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(new Collector.MetricFamilySamples.Sample("my_application_namedMeter1_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));

        Mockito.when(sampleBuilder.createSample(eq("my.application.namedHistogram1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(new Collector.MetricFamilySamples.Sample("my_application_namedHistogram1", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));

        Mockito.when(sampleBuilder.createSample(eq("my.application.namedGauge1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(new Collector.MetricFamilySamples.Sample("my_application_namedGauge1", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234));

        metricRegistry.timer("my.application.namedTimer1");
        metricRegistry.counter("my.application.namedCounter1");
        metricRegistry.meter("my.application.namedMeter1");
        metricRegistry.histogram("my.application.namedHistogram1");
        metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());

        Enumeration<Collector.MetricFamilySamples> metricFamilySamples = registry.metricFamilySamples();


        Map<String, Collector.MetricFamilySamples> elements = new HashMap<String, Collector.MetricFamilySamples>();

        while (metricFamilySamples.hasMoreElements()) {
            Collector.MetricFamilySamples element = metricFamilySamples.nextElement();
            elements.put(element.name, element);
        }
        assertEquals(5, elements.size());

        assertTrue(elements.keySet().contains("my_application_namedTimer1"));
        assertTrue(elements.keySet().contains("my_application_namedCounter1"));
        assertTrue(elements.keySet().contains("my_application_namedMeter1"));
        assertTrue(elements.keySet().contains("my_application_namedHistogram1"));
        assertTrue(elements.keySet().contains("my_application_namedGauge1"));

        assertThat(elements.get("my_application_namedTimer1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedTimer1, type=com.codahale.metrics.Timer)"));

        assertThat(elements.get("my_application_namedCounter1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedCounter1, type=com.codahale.metrics.Counter)"));

        assertThat(elements.get("my_application_namedMeter1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedMeter1, type=com.codahale.metrics.Meter)"));

        assertThat(elements.get("my_application_namedHistogram1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedHistogram1, type=com.codahale.metrics.Histogram)"));

        assertThat(elements.get("my_application_namedGauge1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedGauge1, type=io.prometheus.client.dropwizard.DropwizardExportsTest$ExampleDoubleGauge)"));

    }

    @Test
    public void testThatMetricsMappedToSameNameAreGroupedInSameFamily() {
        final Collector.MetricFamilySamples.Sample namedTimerSample1 = new Collector.MetricFamilySamples.Sample("my_application_namedTimer", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedTimer1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedTimerSample1);

        final Collector.MetricFamilySamples.Sample namedTimerSample2 = new Collector.MetricFamilySamples.Sample("my_application_namedTimer", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedTimer2"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedTimerSample2);

        final Collector.MetricFamilySamples.Sample namedCounter1 = new Collector.MetricFamilySamples.Sample("my_application_namedCounter", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedCounter1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedCounter1);

        final Collector.MetricFamilySamples.Sample namedCounter2 = new Collector.MetricFamilySamples.Sample("my_application_namedCounter", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedCounter2"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedCounter2);

        final Collector.MetricFamilySamples.Sample namedMeter1 = new Collector.MetricFamilySamples.Sample("my_application_namedMeter_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedMeter1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedMeter1);

        final Collector.MetricFamilySamples.Sample namedMeter2 = new Collector.MetricFamilySamples.Sample("my_application_namedMeter_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedMeter2"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedMeter2);

        final Collector.MetricFamilySamples.Sample namedHistogram1 = new Collector.MetricFamilySamples.Sample("my_application_namedHistogram", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedHistogram1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedHistogram1);

        final Collector.MetricFamilySamples.Sample namedHistogram2 = new Collector.MetricFamilySamples.Sample("my_application_namedHistogram", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedHistogram2"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedHistogram2);

        final Collector.MetricFamilySamples.Sample namedGauge1 = new Collector.MetricFamilySamples.Sample("my_application_namedGauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedGauge1"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedGauge1);

        final Collector.MetricFamilySamples.Sample namedGauge2 = new Collector.MetricFamilySamples.Sample("my_application_namedGauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
        Mockito.when(sampleBuilder.createSample(eq("my.application.namedGauge2"), anyString(), anyList(), anyList(), anyDouble()))
                .thenReturn(namedGauge2);

        metricRegistry.timer("my.application.namedTimer1");
        metricRegistry.timer("my.application.namedTimer2");
        metricRegistry.counter("my.application.namedCounter1");
        metricRegistry.counter("my.application.namedCounter2");
        metricRegistry.meter("my.application.namedMeter1");
        metricRegistry.meter("my.application.namedMeter2");
        metricRegistry.histogram("my.application.namedHistogram1");
        metricRegistry.histogram("my.application.namedHistogram2");
        metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());
        metricRegistry.register("my.application.namedGauge2", new ExampleDoubleGauge());

        Enumeration<Collector.MetricFamilySamples> metricFamilySamples = registry.metricFamilySamples();


        Map<String, Collector.MetricFamilySamples> elements = new HashMap<String, Collector.MetricFamilySamples>();

        while (metricFamilySamples.hasMoreElements()) {
            Collector.MetricFamilySamples element = metricFamilySamples.nextElement();
            elements.put(element.name, element);
        }
        assertEquals(5, elements.size());

        final Collector.MetricFamilySamples namedTimer = elements.get("my_application_namedTimer");
        assertNotNull(namedTimer);
        assertEquals(Collector.Type.SUMMARY, namedTimer.type);
        assertEquals(14, namedTimer.samples.size());

        final Collector.MetricFamilySamples namedCounter = elements.get("my_application_namedCounter");
        assertNotNull(namedCounter);
        assertEquals(Collector.Type.GAUGE, namedCounter.type);
        assertEquals(2, namedCounter.samples.size());
        assertTrue(namedCounter.samples.contains(namedCounter1));
        assertTrue(namedCounter.samples.contains(namedCounter2));

        final Collector.MetricFamilySamples namedMeter = elements.get("my_application_namedMeter");
        assertNotNull(namedMeter);
        assertEquals(Collector.Type.COUNTER, namedMeter.type);
        assertEquals(2, namedMeter.samples.size());
        assertTrue(namedMeter.samples.contains(namedMeter1));
        assertTrue(namedMeter.samples.contains(namedMeter2));

        final Collector.MetricFamilySamples namedHistogram = elements.get("my_application_namedHistogram");
        assertNotNull(namedHistogram);
        assertEquals(Collector.Type.SUMMARY, namedHistogram.type);
        assertEquals(Collector.Type.SUMMARY, namedHistogram.type);
        assertEquals(14, namedHistogram.samples.size());

        final Collector.MetricFamilySamples namedGauge = elements.get("my_application_namedGauge");
        assertNotNull(namedGauge);
        assertEquals(Collector.Type.GAUGE, namedGauge.type);
        assertEquals(2, namedGauge.samples.size());
        assertTrue(namedGauge.samples.contains(namedGauge1));
        assertTrue(namedGauge.samples.contains(namedGauge2));

    }

    private static class ExampleDoubleGauge implements Gauge<Double> {
        @Override
        public Double getValue() {
            return 0.0;
        }
    }
}
