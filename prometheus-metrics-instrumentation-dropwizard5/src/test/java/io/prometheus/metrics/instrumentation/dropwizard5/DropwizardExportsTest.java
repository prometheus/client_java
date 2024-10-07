package io.prometheus.metrics.instrumentation.dropwizard5;

import io.dropwizard.metrics5.*;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class DropwizardExportsTest {

  private PrometheusRegistry registry = new PrometheusRegistry();
  private MetricRegistry metricRegistry;

  @Before
  public void setUp() {
    metricRegistry = new MetricRegistry();
    DropwizardExports.builder().dropwizardRegistry(metricRegistry).register(registry);
  }

  @Test
  public void testCounter() {
    metricRegistry.counter("foo.bar").inc(1);
    String expected =
        "# TYPE foo_bar counter\n"
            + "# HELP foo_bar Generated from Dropwizard metric import (metric=foo.bar, type=io.dropwizard.metrics5.Counter)\n"
            + "foo_bar_total 1.0\n"
            + "# EOF\n";

    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testGauge() {
    Gauge<Integer> integerGauge =
            () -> 1234;
    Gauge<Double> doubleGauge =
            () -> 1.234D;
    Gauge<Long> longGauge =
            () -> 1234L;
    Gauge<Float> floatGauge =
            () -> 0.1234F;
    Gauge<Boolean> booleanGauge =
            () -> true;

    metricRegistry.register("double.gauge", doubleGauge);
    metricRegistry.register("long.gauge", longGauge);
    metricRegistry.register("integer.gauge", integerGauge);
    metricRegistry.register("float.gauge", floatGauge);
    metricRegistry.register("boolean.gauge", booleanGauge);

    String expected =
        "# TYPE boolean_gauge gauge\n"
            + "# HELP boolean_gauge Generated from Dropwizard metric import (metric=boolean.gauge, type=io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExportsTest$5)\n"
            + "boolean_gauge 1.0\n"
            + "# TYPE double_gauge gauge\n"
            + "# HELP double_gauge Generated from Dropwizard metric import (metric=double.gauge, type=io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExportsTest$2)\n"
            + "double_gauge 1.234\n"
            + "# TYPE float_gauge gauge\n"
            + "# HELP float_gauge Generated from Dropwizard metric import (metric=float.gauge, type=io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExportsTest$4)\n"
            + "float_gauge 0.1234000027179718\n"
            + "# TYPE integer_gauge gauge\n"
            + "# HELP integer_gauge Generated from Dropwizard metric import (metric=integer.gauge, type=io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExportsTest$1)\n"
            + "integer_gauge 1234.0\n"
            + "# TYPE long_gauge gauge\n"
            + "# HELP long_gauge Generated from Dropwizard metric import (metric=long.gauge, type=io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExportsTest$3)\n"
            + "long_gauge 1234.0\n"
            + "# EOF\n";

    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testInvalidGaugeType() {
    Gauge<String> invalidGauge =
            () -> "foobar";

    metricRegistry.register("invalid_gauge", invalidGauge);

    String expected = "# EOF\n";
    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testGaugeReturningNullValue() {
    Gauge<String> invalidGauge =
            () -> null;
    metricRegistry.register("invalid_gauge", invalidGauge);
    String expected = "# EOF\n";
    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testHistogram() {
    // just test the standard mapper
    final MetricRegistry metricRegistry = new MetricRegistry();
    PrometheusRegistry pmRegistry = new PrometheusRegistry();
    DropwizardExports.builder().dropwizardRegistry(metricRegistry).register(pmRegistry);

    Histogram hist = metricRegistry.histogram("hist");
    int i = 0;
    while (i < 100) {
      hist.update(i);
      i += 1;
    }

    // The result should look like this
    //
    // # TYPE hist summary
    // # HELP hist Generated from Dropwizard metric import (metric=hist,
    // type=io.dropwizard.metrics5.Histogram)
    // hist{quantile="0.5"} 49.0
    // hist{quantile="0.75"} 74.0
    // hist{quantile="0.95"} 94.0
    // hist{quantile="0.98"} 97.0
    // hist{quantile="0.99"} 98.0
    // hist{quantile="0.999"} 99.0
    // hist_count 100
    // # EOF
    //
    // However, Dropwizard uses a random reservoir sampling algorithm, so the values could as well
    // be off-by-one
    //
    // # TYPE hist summary
    // # HELP hist Generated from Dropwizard metric import (metric=hist,
    // type=io.dropwizard.metrics5.Histogram)
    // hist{quantile="0.5"} 50.0
    // hist{quantile="0.75"} 75.0
    // hist{quantile="0.95"} 95.0
    // hist{quantile="0.98"} 98.0
    // hist{quantile="0.99"} 99.0
    // hist{quantile="0.999"} 99.0
    // hist_count 100
    // # EOF
    //
    // The following asserts the values, but allows an error of 1.0 for quantile values.

    MetricSnapshots snapshots = pmRegistry.scrape(name -> name.equals("hist"));
    assertThat(snapshots.size()).isOne();
    SummarySnapshot snapshot = (SummarySnapshot) snapshots.get(0);
    assertThat(snapshot.getMetadata().getName()).isEqualTo("hist");
    assertThat(snapshot.getMetadata().getHelp()).isEqualTo("Generated from Dropwizard metric import (metric=hist, type=io.dropwizard.metrics5.Histogram)");
    assertThat(snapshot.getDataPoints().size()).isOne();
    SummarySnapshot.SummaryDataPointSnapshot dataPoint = snapshot.getDataPoints().get(0);
    assertThat(dataPoint.hasCount()).isTrue();
    assertThat(dataPoint.getCount()).isEqualTo(100);
    assertThat(dataPoint.hasSum()).isFalse();
    Quantiles quantiles = dataPoint.getQuantiles();
    assertThat(quantiles.size()).isEqualTo(6);
    assertThat(quantiles.get(0).getQuantile()).isCloseTo(0.5, offset(0.0));
    assertThat(quantiles.get(0).getValue()).isCloseTo(49.0, offset(1.0));
    assertThat(quantiles.get(1).getQuantile()).isCloseTo(0.75, offset(0.0));
    assertThat(quantiles.get(1).getValue()).isCloseTo(74.0, offset(1.0));
    assertThat(quantiles.get(2).getQuantile()).isCloseTo(0.95, offset(0.0));
    assertThat(quantiles.get(2).getValue()).isCloseTo(94.0, offset(1.0));
    assertThat(quantiles.get(3).getQuantile()).isCloseTo(0.98, offset(0.0));
    assertThat(quantiles.get(3).getValue()).isCloseTo(97.0, offset(1.0));
    assertThat(quantiles.get(4).getQuantile()).isCloseTo(0.99, offset(0.0));
    assertThat(quantiles.get(4).getValue()).isCloseTo(98.0, offset(1.0));
    assertThat(quantiles.get(5).getQuantile()).isCloseTo(0.999, offset(0.0));
    assertThat(quantiles.get(5).getValue()).isCloseTo(99.0, offset(1.0));
  }

  @Test
  public void testMeter() {
    Meter meter = metricRegistry.meter("meter");
    meter.mark();
    meter.mark();

    String expected =
        "# TYPE meter counter\n"
            + "# HELP meter Generated from Dropwizard metric import (metric=meter_total, type=io.dropwizard.metrics5.Meter)\n"
            + "meter_total 2.0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
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

    SummarySnapshot.SummaryDataPointSnapshot dataPointSnapshot =
        (SummarySnapshot.SummaryDataPointSnapshot)
            exports.collect().stream().flatMap(i -> i.getDataPoints().stream()).findFirst().get();
    // We slept for 1Ms so we ensure that all timers are above 1ms:
    assertThat(dataPointSnapshot.getQuantiles().size()).isGreaterThan(1);
    dataPointSnapshot
        .getQuantiles()
        .forEach(
            i -> {
              System.out.println(i.getQuantile() + " : " + i.getValue());
              assertThat(i.getValue()).isGreaterThan(timeSpentMillis / 1000d);
            });
    assertThat(dataPointSnapshot.getCount()).isOne();
  }

  @Test
  public void testThatMetricHelpUsesOriginalDropwizardName() {

    metricRegistry.timer("my.application.namedTimer1");
    metricRegistry.counter("my.application.namedCounter1");
    metricRegistry.meter("my.application.namedMeter1");
    metricRegistry.histogram("my.application.namedHistogram1");
    metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());

    String expected =
        "# TYPE my_application_namedCounter1 counter\n"
            + "# HELP my_application_namedCounter1 Generated from Dropwizard metric import (metric=my.application.namedCounter1, type=io.dropwizard.metrics5.Counter)\n"
            + "my_application_namedCounter1_total 0.0\n"
            + "# TYPE my_application_namedGauge1 gauge\n"
            + "# HELP my_application_namedGauge1 Generated from Dropwizard metric import (metric=my.application.namedGauge1, type=io.prometheus.metrics.instrumentation.dropwizard5.DropwizardExportsTest$ExampleDoubleGauge)\n"
            + "my_application_namedGauge1 0.0\n"
            + "# TYPE my_application_namedHistogram1 summary\n"
            + "# HELP my_application_namedHistogram1 Generated from Dropwizard metric import (metric=my.application.namedHistogram1, type=io.dropwizard.metrics5.Histogram)\n"
            + "my_application_namedHistogram1{quantile=\"0.5\"} 0.0\n"
            + "my_application_namedHistogram1{quantile=\"0.75\"} 0.0\n"
            + "my_application_namedHistogram1{quantile=\"0.95\"} 0.0\n"
            + "my_application_namedHistogram1{quantile=\"0.98\"} 0.0\n"
            + "my_application_namedHistogram1{quantile=\"0.99\"} 0.0\n"
            + "my_application_namedHistogram1{quantile=\"0.999\"} 0.0\n"
            + "my_application_namedHistogram1_count 0\n"
            + "# TYPE my_application_namedMeter1 counter\n"
            + "# HELP my_application_namedMeter1 Generated from Dropwizard metric import (metric=my.application.namedMeter1_total, type=io.dropwizard.metrics5.Meter)\n"
            + "my_application_namedMeter1_total 0.0\n"
            + "# TYPE my_application_namedTimer1 summary\n"
            + "# HELP my_application_namedTimer1 Generated from Dropwizard metric import (metric=my.application.namedTimer1, type=io.dropwizard.metrics5.Timer)\n"
            + "my_application_namedTimer1{quantile=\"0.5\"} 0.0\n"
            + "my_application_namedTimer1{quantile=\"0.75\"} 0.0\n"
            + "my_application_namedTimer1{quantile=\"0.95\"} 0.0\n"
            + "my_application_namedTimer1{quantile=\"0.98\"} 0.0\n"
            + "my_application_namedTimer1{quantile=\"0.99\"} 0.0\n"
            + "my_application_namedTimer1{quantile=\"0.999\"} 0.0\n"
            + "my_application_namedTimer1_count 0\n"
            + "# EOF\n";
    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  private static class ExampleDoubleGauge implements Gauge<Double> {
    @Override
    public Double getValue() {
      return 0.0;
    }
  }

  private String convertToOpenMetricsFormat(PrometheusRegistry _registry) {
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
