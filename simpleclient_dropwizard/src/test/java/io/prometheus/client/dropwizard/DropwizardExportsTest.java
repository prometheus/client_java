package io.prometheus.client.dropwizard;


import com.codahale.metrics.*;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class DropwizardExportsTest {

    private CollectorRegistry registry = new CollectorRegistry();
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        new DropwizardExports(metricRegistry).register(registry);
    }

    @Test
    public void testCounter() {
        metricRegistry.counter("foo_bar").inc();
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

        metricRegistry.register("double_gauge", doubleGauge);
        metricRegistry.register("long_gauge", longGauge);
        metricRegistry.register("integer_gauge", integerGauge);
        metricRegistry.register("float_gauge", floatGauge);
        metricRegistry.register("boolean_gauge", booleanGauge);

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
    }

    void assertRegistryContainsMetrics(String... metrics) {
        for (String metric : metrics) {
            assertNotEquals(String.format("Metric %s should exist", metric), null,
                    registry.getSampleValue(metric, new String[]{}, new String[]{}));
        }
    }

    @Test
    public void testHistogram() throws IOException {
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
        Meter meter = metricRegistry.meter("meter");
        meter.mark();
        meter.mark();
        assertEquals(new Double(2), registry.getSampleValue("meter_total"));
    }

    @Test
    public void testTimer() throws IOException, InterruptedException {
        Timer t = metricRegistry.timer("timer");
        Timer.Context time = t.time();
        Thread.sleep(1L);
        time.stop();
        // We slept for 1Ms so we ensure that all timers are above 1ms:
        assertTrue(registry.getSampleValue("timer", new String[]{"quantile"}, new String[]{"0.99"}) > 0.001);
        assertEquals(new Double(1.0D), registry.getSampleValue("timer_count"));
    }

    @Test
    public void testSanitizeMetricName() {
        assertEquals("Foo_Bar_metric_mame", DropwizardExports.sanitizeMetricName("Foo.Bar-metric,mame"));
    }

    @Test
    public void testThatMetricHelpUsesOriginalDropwizardName() {
        metricRegistry.timer("my.application.namedTimer1");
        metricRegistry.counter("my.application.namedCounter1");
        metricRegistry.meter("my.application.namedMeter1");
        metricRegistry.histogram("my.application.namedHistogram1");
        metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());

        Enumeration<Collector.MetricFamilySamples> metricFamilySamples = registry.metricFamilySamples();


        Map<String, Collector.MetricFamilySamples> elements = new HashMap<String, Collector.MetricFamilySamples>();

        while (metricFamilySamples.hasMoreElements()) {
            Collector.MetricFamilySamples element =  metricFamilySamples.nextElement();
            elements.put(element.name, element);
        }
        assertEquals(5, elements.size());

        assertTrue(elements.keySet().contains("my_application_namedTimer1"));
        assertTrue(elements.keySet().contains("my_application_namedCounter1"));
        assertTrue(elements.keySet().contains("my_application_namedMeter1_total"));
        assertTrue(elements.keySet().contains("my_application_namedHistogram1"));
        assertTrue(elements.keySet().contains("my_application_namedGauge1"));

        assertThat(elements.get("my_application_namedTimer1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedTimer1, type=com.codahale.metrics.Timer)"));

        assertThat(elements.get("my_application_namedCounter1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedCounter1, type=com.codahale.metrics.Counter)"));

        assertThat(elements.get("my_application_namedMeter1_total").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedMeter1, type=com.codahale.metrics.Meter)"));

        assertThat(elements.get("my_application_namedHistogram1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedHistogram1, type=com.codahale.metrics.Histogram)"));

        assertThat(elements.get("my_application_namedGauge1").help,
                is("Generated from Dropwizard metric import (metric=my.application.namedGauge1, type=io.prometheus.client.dropwizard.DropwizardExportsTest$ExampleDoubleGauge)"));

    }


    private static class ExampleDoubleGauge implements Gauge<Double> {
        @Override
        public Double getValue() {
            return 0.0;
        }
    }
}
