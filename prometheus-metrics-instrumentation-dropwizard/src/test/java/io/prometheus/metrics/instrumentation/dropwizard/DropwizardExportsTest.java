package io.prometheus.metrics.instrumentation.dropwizard;

import  io.dropwizard.metrics5.*;
import  io.dropwizard.metrics5.Timer;

import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;


public class DropwizardExportsTest {

    private PrometheusRegistry registry = new PrometheusRegistry();
    private MetricRegistry metricRegistry;
//
//    private SampleBuilder sampleBuilder;

    private DropwizardExports dropwizardExports;

    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
//        sampleBuilder = Mockito.mock(SampleBuilder.class);
        dropwizardExports = new DropwizardExports(metricRegistry);//.register(registry);
        registry.register(dropwizardExports);
    }


    @Test
    public void testCounter()  {
        metricRegistry.counter("foo.bar").inc(1);
        System.out.println(convertToOpenMetricsFormat());
        String expected = "# TYPE foo_bar counter\n" +
                "# HELP foo_bar Generated from Dropwizard metric import (metric=foo.bar, type=io.dropwizard.metrics5.Counter)\n" +
                "foo_bar_total 1.0\n" +
                "# EOF\n";

        assertEquals(expected, convertToOpenMetricsFormat());
    }

    @Test
    public void testGauge()  {
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

        metricRegistry.register("double.gauge", doubleGauge);
        metricRegistry.register("long.gauge", longGauge);
        metricRegistry.register("integer.gauge", integerGauge);
        metricRegistry.register("float.gauge", floatGauge);
        metricRegistry.register("boolean.gauge", booleanGauge);

        String expected = "# TYPE boolean_gauge gauge\n" +
                "# HELP boolean_gauge Generated from Dropwizard metric import (metric=boolean.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$5)\n" +
                "boolean_gauge 1.0\n" +
                "# TYPE double_gauge gauge\n" +
                "# HELP double_gauge Generated from Dropwizard metric import (metric=double.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$2)\n" +
                "double_gauge 1.234\n" +
                "# TYPE float_gauge gauge\n" +
                "# HELP float_gauge Generated from Dropwizard metric import (metric=float.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$4)\n" +
                "float_gauge 0.1234000027179718\n" +
                "# TYPE integer_gauge gauge\n" +
                "# HELP integer_gauge Generated from Dropwizard metric import (metric=integer.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$1)\n" +
                "integer_gauge 1234.0\n" +
                "# TYPE long_gauge gauge\n" +
                "# HELP long_gauge Generated from Dropwizard metric import (metric=long.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$3)\n" +
                "long_gauge 1234.0\n" +
                "# EOF\n";

        assertEquals(expected, convertToOpenMetricsFormat());
    }

    @Test
    public void testInvalidGaugeType()  {
        Gauge<String> invalidGauge = new Gauge<String>() {
            @Override
            public String getValue() {
                return "foobar";
            }
        };

        metricRegistry.register("invalid_gauge", invalidGauge);

        String expected = "# EOF\n";
        assertEquals(expected, convertToOpenMetricsFormat());
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
        String expected = "# EOF\n";
        assertEquals(expected, convertToOpenMetricsFormat());
    }

    @Test
    public void testHistogram() throws IOException {
        // just test the standard mapper
        final MetricRegistry metricRegistry = new MetricRegistry();
        PrometheusRegistry pmRegistry = new PrometheusRegistry();
        DropwizardExports exports = new DropwizardExports(metricRegistry);
        pmRegistry.register(exports);

        Histogram hist = metricRegistry.histogram("hist");
        int i = 0;
        while (i < 100) {
            hist.update(i);
            i += 1;
        }

        String expected = "# TYPE hist summary\n" +
                "# HELP hist Generated from Dropwizard metric import (metric=hist, type=io.dropwizard.metrics5.Histogram)\n" +
                "hist{quantile=\"0.5\"} 49.0\n" +
                "hist{quantile=\"0.75\"} 74.0\n" +
                "hist{quantile=\"0.95\"} 94.0\n" +
                "hist{quantile=\"0.98\"} 97.0\n" +
                "hist{quantile=\"0.99\"} 98.0\n" +
                "hist{quantile=\"0.999\"} 99.0\n" +
                "hist_count 100\n" +
                "# EOF\n";
        assertEquals(expected, convertToOpenMetricsFormat(pmRegistry));
    }

    @Test
    public void testMeter()  {
        Meter meter = metricRegistry.meter("meter");
        meter.mark();
        meter.mark();

        String expected = "# TYPE meter counter\n" +
                "# HELP meter Generated from Dropwizard metric import (metric=meter_total, type=io.dropwizard.metrics5.Meter)\n" +
                "meter_total 2.0\n" +
                "# EOF\n";
        assertEquals(expected, convertToOpenMetricsFormat());

    }

    @Test
    public void testTimer() throws IOException, InterruptedException {

        final MetricRegistry metricRegistry = new MetricRegistry();
        PrometheusRegistry pmRegistry = new PrometheusRegistry();
        DropwizardExports exports = new DropwizardExports(metricRegistry);
        pmRegistry.register(exports);

        Timer t = metricRegistry.timer("timer");
        Timer.Context time = t.time();
        Thread.sleep(1L);
        time.stop();

//        System.out.println( convertToOpenMetricsFormat(pmRegistry));

        // We slept for 1Ms so we ensure that all timers are above 1ms:
//        assertTrue(registry.getSampleValue("timer", new String[]{"quantile"}, new String[]{"0.99"}) > 0.001);
        SummarySnapshot.SummaryDataPointSnapshot aa = (SummarySnapshot.SummaryDataPointSnapshot) pmRegistry.scrape().stream().findFirst().get().getDataPoints().get(0);
        System.out.println(aa);;

//        aa.getQuantiles().iterator().
//        assertEquals();

        String expected = "# TYPE timer summary\n" +
                "# HELP timer Generated from Dropwizard metric import (metric=timer, type=io.dropwizard.metrics5.Timer)\n" +
                "timer{quantile=\"0.5\"} 0.0013355420000000001\n" +
                "timer{quantile=\"0.75\"} 0.0013355420000000001\n" +
                "timer{quantile=\"0.95\"} 0.0013355420000000001\n" +
                "timer{quantile=\"0.98\"} 0.0013355420000000001\n" +
                "timer{quantile=\"0.99\"} 0.0013355420000000001\n" +
                "timer{quantile=\"0.999\"} 0.0013355420000000001\n" +
                "timer_count 1\n" +
                "# EOF";
        assertEquals(expected, convertToOpenMetricsFormat(pmRegistry));
        //Doesn't work.
        //TODO fix this

//        assertEquals(new Double(1.0D), registry.getSampleValue("timer_count"));
    }

    @Test
    public void testThatMetricHelpUsesOriginalDropwizardName() {

        metricRegistry.timer("my.application.namedTimer1");
        metricRegistry.counter("my.application.namedCounter1");
        metricRegistry.meter("my.application.namedMeter1");
        metricRegistry.histogram("my.application.namedHistogram1");
        metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());

        String expected  = "# TYPE my_application_namedCounter1 counter\n" +
                "# HELP my_application_namedCounter1 Generated from Dropwizard metric import (metric=my.application.namedCounter1, type=io.dropwizard.metrics5.Counter)\n" +
                "my_application_namedCounter1_total 0.0\n" +
                "# TYPE my_application_namedGauge1 gauge\n" +
                "# HELP my_application_namedGauge1 Generated from Dropwizard metric import (metric=my.application.namedGauge1, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$ExampleDoubleGauge)\n" +
                "my_application_namedGauge1 0.0\n" +
                "# TYPE my_application_namedHistogram1 summary\n" +
                "# HELP my_application_namedHistogram1 Generated from Dropwizard metric import (metric=my.application.namedHistogram1, type=io.dropwizard.metrics5.Histogram)\n" +
                "my_application_namedHistogram1{quantile=\"0.5\"} 0.0\n" +
                "my_application_namedHistogram1{quantile=\"0.75\"} 0.0\n" +
                "my_application_namedHistogram1{quantile=\"0.95\"} 0.0\n" +
                "my_application_namedHistogram1{quantile=\"0.98\"} 0.0\n" +
                "my_application_namedHistogram1{quantile=\"0.99\"} 0.0\n" +
                "my_application_namedHistogram1{quantile=\"0.999\"} 0.0\n" +
                "my_application_namedHistogram1_count 0\n" +
                "# TYPE my_application_namedMeter1 counter\n" +
                "# HELP my_application_namedMeter1 Generated from Dropwizard metric import (metric=my.application.namedMeter1_total, type=io.dropwizard.metrics5.Meter)\n" +
                "my_application_namedMeter1_total 0.0\n" +
                "# TYPE my_application_namedTimer1 summary\n" +
                "# HELP my_application_namedTimer1 Generated from Dropwizard metric import (metric=my.application.namedTimer1, type=io.dropwizard.metrics5.Timer)\n" +
                "my_application_namedTimer1{quantile=\"0.5\"} 0.0\n" +
                "my_application_namedTimer1{quantile=\"0.75\"} 0.0\n" +
                "my_application_namedTimer1{quantile=\"0.95\"} 0.0\n" +
                "my_application_namedTimer1{quantile=\"0.98\"} 0.0\n" +
                "my_application_namedTimer1{quantile=\"0.99\"} 0.0\n" +
                "my_application_namedTimer1{quantile=\"0.999\"} 0.0\n" +
                "my_application_namedTimer1_count 0\n" +
                "# EOF\n";
        assertEquals(expected, convertToOpenMetricsFormat());
    }

    @Test
    public void testThatMetricsMappedToSameNameAreGroupedInSameFamily() {
//        final Collector.MetricFamilySamples.Sample namedTimerSample1 = new Collector.MetricFamilySamples.Sample("my_application_namedTimer", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedTimer1"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedTimerSample1);
//
//        final Collector.MetricFamilySamples.Sample namedTimerSample2 = new Collector.MetricFamilySamples.Sample("my_application_namedTimer", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedTimer2"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedTimerSample2);
//
//        final Collector.MetricFamilySamples.Sample namedCounter1 = new Collector.MetricFamilySamples.Sample("my_application_namedCounter", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedCounter1"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedCounter1);
//
//        final Collector.MetricFamilySamples.Sample namedCounter2 = new Collector.MetricFamilySamples.Sample("my_application_namedCounter", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedCounter2"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedCounter2);
//
//        final Collector.MetricFamilySamples.Sample namedMeter1 = new Collector.MetricFamilySamples.Sample("my_application_namedMeter_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedMeter1"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedMeter1);
//
//        final Collector.MetricFamilySamples.Sample namedMeter2 = new Collector.MetricFamilySamples.Sample("my_application_namedMeter_total", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedMeter2"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedMeter2);
//
//        final Collector.MetricFamilySamples.Sample namedHistogram1 = new Collector.MetricFamilySamples.Sample("my_application_namedHistogram", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedHistogram1"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedHistogram1);
//
//        final Collector.MetricFamilySamples.Sample namedHistogram2 = new Collector.MetricFamilySamples.Sample("my_application_namedHistogram", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedHistogram2"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedHistogram2);
//
//        final Collector.MetricFamilySamples.Sample namedGauge1 = new Collector.MetricFamilySamples.Sample("my_application_namedGauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1234);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedGauge1"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedGauge1);
//
//        final Collector.MetricFamilySamples.Sample namedGauge2 = new Collector.MetricFamilySamples.Sample("my_application_namedGauge", Collections.<String>emptyList(), Collections.<String>emptyList(), 1235);
//        Mockito.when(sampleBuilder.createSample(eq("my.application.namedGauge2"), anyString(), anyList(), anyList(), anyDouble()))
//                .thenReturn(namedGauge2);
//
//        metricRegistry.timer("my.application.namedTimer1");
//        metricRegistry.timer("my.application.namedTimer2");
//        metricRegistry.counter("my.application.namedCounter1");
//        metricRegistry.counter("my.application.namedCounter2");
//        metricRegistry.meter("my.application.namedMeter1");
//        metricRegistry.meter("my.application.namedMeter2");
//        metricRegistry.histogram("my.application.namedHistogram1");
//        metricRegistry.histogram("my.application.namedHistogram2");
//        metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());
//        metricRegistry.register("my.application.namedGauge2", new ExampleDoubleGauge());
//
//        Enumeration<Collector.MetricFamilySamples> metricFamilySamples = registry.metricFamilySamples();
//
//
//        Map<String, Collector.MetricFamilySamples> elements = new HashMap<String, Collector.MetricFamilySamples>();
//
//        while (metricFamilySamples.hasMoreElements()) {
//            Collector.MetricFamilySamples element = metricFamilySamples.nextElement();
//            elements.put(element.name, element);
//        }
//        assertEquals(5, elements.size());
//
//        final Collector.MetricFamilySamples namedTimer = elements.get("my_application_namedTimer");
//        assertNotNull(namedTimer);
//        assertEquals(Collector.Type.SUMMARY, namedTimer.type);
//        assertEquals(14, namedTimer.samples.size());
//
//        final Collector.MetricFamilySamples namedCounter = elements.get("my_application_namedCounter");
//        assertNotNull(namedCounter);
//        assertEquals(Collector.Type.GAUGE, namedCounter.type);
//        assertEquals(2, namedCounter.samples.size());
//        assertTrue(namedCounter.samples.contains(namedCounter1));
//        assertTrue(namedCounter.samples.contains(namedCounter2));
//
//        final Collector.MetricFamilySamples namedMeter = elements.get("my_application_namedMeter");
//        assertNotNull(namedMeter);
//        assertEquals(Collector.Type.COUNTER, namedMeter.type);
//        assertEquals(2, namedMeter.samples.size());
//        assertTrue(namedMeter.samples.contains(namedMeter1));
//        assertTrue(namedMeter.samples.contains(namedMeter2));
//
//        final Collector.MetricFamilySamples namedHistogram = elements.get("my_application_namedHistogram");
//        assertNotNull(namedHistogram);
//        assertEquals(Collector.Type.SUMMARY, namedHistogram.type);
//        assertEquals(Collector.Type.SUMMARY, namedHistogram.type);
//        assertEquals(14, namedHistogram.samples.size());
//
//        final Collector.MetricFamilySamples namedGauge = elements.get("my_application_namedGauge");
//        assertNotNull(namedGauge);
//        assertEquals(Collector.Type.GAUGE, namedGauge.type);
//        assertEquals(2, namedGauge.samples.size());
//        assertTrue(namedGauge.samples.contains(namedGauge1));
//        assertTrue(namedGauge.samples.contains(namedGauge2));
//
    }

    private static class ExampleDoubleGauge implements Gauge<Double> {
        @Override
        public Double getValue() {
            return 0.0;
        }
    }

    private String convertToOpenMetricsFormat(PrometheusRegistry _registry)  {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
        try {
            writer.write(out, _registry.scrape());
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToOpenMetricsFormat() {
        return convertToOpenMetricsFormat(registry);
    }
}
