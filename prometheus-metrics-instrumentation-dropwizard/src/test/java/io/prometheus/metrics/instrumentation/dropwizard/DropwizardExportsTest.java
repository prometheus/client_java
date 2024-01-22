package io.prometheus.metrics.instrumentation.dropwizard;

import io.dropwizard.metrics5.*;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DropwizardExportsTest {

    private PrometheusRegistry registry = new PrometheusRegistry();
    private MetricRegistry metricRegistry;

    private DropwizardExports dropwizardExports;

    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        dropwizardExports = new DropwizardExports(metricRegistry);//.register(registry);
        registry.register(dropwizardExports);
    }


    @Test
    public void testCounter()  {
        metricRegistry.counter("foo.bar").inc(1);
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
    public void testTimer() throws InterruptedException {
        final MetricRegistry metricRegistry = new MetricRegistry();
        DropwizardExports exports = new DropwizardExports(metricRegistry);
        Timer t = metricRegistry.timer("timer");
        Timer.Context time = t.time();
        Thread.sleep(100L);
        long timeSpentNanos = time.stop();
        double timeSpentMillis = TimeUnit.NANOSECONDS.toMillis(timeSpentNanos);
        System.out.println(timeSpentMillis);

        SummarySnapshot.SummaryDataPointSnapshot dataPointSnapshot = (SummarySnapshot.SummaryDataPointSnapshot) exports.collect().stream().flatMap(i -> i.getDataPoints().stream()).findFirst().get();
        // We slept for 1Ms so we ensure that all timers are above 1ms:
        assertTrue(dataPointSnapshot.getQuantiles().size() > 1);
        dataPointSnapshot.getQuantiles().forEach( i-> {
            System.out.println(i.getQuantile() + " : " + i.getValue());
            assertTrue(i.getValue() > timeSpentMillis/1000d);
        });
        assertEquals(1, dataPointSnapshot.getCount());
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
