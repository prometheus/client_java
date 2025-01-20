package io.prometheus.metrics.instrumentation.dropwizard;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.nameEscapingScheme;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codahale.metrics.*;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DropwizardExportsTest {

  private final PrometheusRegistry registry = new PrometheusRegistry();
  private MetricRegistry metricRegistry;

  @BeforeEach
  public void setUp() {
    metricRegistry = new MetricRegistry();
    DropwizardExports.builder()
        .dropwizardRegistry(metricRegistry)
        .metricFilter(MetricFilter.ALL)
        .register(registry);
  }

  @Test
  public void testBuilderThrowsErrorOnNullRegistry() {
    assertThatThrownBy(
            () -> DropwizardExports.builder().dropwizardRegistry(null).register(registry))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testBuilderCreatesOkay() {
    assertThatCode(
            () -> DropwizardExports.builder().dropwizardRegistry(metricRegistry).register(registry))
        .doesNotThrowAnyException();
  }

  @Test
  public void testCounter() {
    metricRegistry.counter("foo.bar").inc(1);
    String expected =
        """
        # TYPE foo_bar counter
        # HELP foo_bar Generated from Dropwizard metric import (metric=foo.bar, type=com.codahale.metrics.Counter)
        foo_bar_total 1.0
        # EOF
        """;

    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testGauge() {
    // don't convert to lambda, as we need to test the type
    Gauge<Integer> integerGauge =
        new Gauge<Integer>() {
          @Override
          public Integer getValue() {
            return 1234;
          }
        };
    Gauge<Double> doubleGauge =
        new Gauge<Double>() {
          @Override
          public Double getValue() {
            return 1.234D;
          }
        };
    Gauge<Long> longGauge =
        new Gauge<Long>() {
          @Override
          public Long getValue() {
            return 1234L;
          }
        };
    Gauge<Float> floatGauge =
        new Gauge<Float>() {
          @Override
          public Float getValue() {
            return 0.1234F;
          }
        };
    Gauge<Boolean> booleanGauge =
        new Gauge<Boolean>() {
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

    String expected =
        """
        # TYPE boolean_gauge gauge
        # HELP boolean_gauge Generated from Dropwizard metric import (metric=boolean.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$5)
        boolean_gauge 1.0
        # TYPE double_gauge gauge
        # HELP double_gauge Generated from Dropwizard metric import (metric=double.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$2)
        double_gauge 1.234
        # TYPE float_gauge gauge
        # HELP float_gauge Generated from Dropwizard metric import (metric=float.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$4)
        float_gauge 0.1234000027179718
        # TYPE integer_gauge gauge
        # HELP integer_gauge Generated from Dropwizard metric import (metric=integer.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$1)
        integer_gauge 1234.0
        # TYPE long_gauge gauge
        # HELP long_gauge Generated from Dropwizard metric import (metric=long.gauge, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$3)
        long_gauge 1234.0
        # EOF
        """;

    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testInvalidGaugeType() {
    Gauge<String> invalidGauge = () -> "foobar";

    metricRegistry.register("invalid_gauge", invalidGauge);

    String expected = "# EOF\n";
    assertThat(convertToOpenMetricsFormat()).isEqualTo(expected);
  }

  @Test
  public void testGaugeReturningNullValue() {
    Gauge<String> invalidGauge = () -> null;
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
    String expected1 =
        """
        # TYPE hist summary
        # HELP hist Generated from Dropwizard metric import (metric=hist, type=com.codahale.metrics.Histogram)
        hist{quantile="0.5"} 49.0
        hist{quantile="0.75"} 74.0
        hist{quantile="0.95"} 94.0
        hist{quantile="0.98"} 97.0
        hist{quantile="0.99"} 98.0
        hist{quantile="0.999"} 99.0
        hist_count 100
        # EOF
        """;

    // However, Dropwizard uses a random reservoir sampling algorithm, so the values could as well
    // be off-by-one
    String expected2 =
        """
        # TYPE hist summary
        # HELP hist Generated from Dropwizard metric import (metric=hist, type=com.codahale.metrics.Histogram)
        hist{quantile="0.5"} 50.0
        hist{quantile="0.75"} 75.0
        hist{quantile="0.95"} 95.0
        hist{quantile="0.98"} 98.0
        hist{quantile="0.99"} 99.0
        hist{quantile="0.999"} 99.0
        hist_count 100
        # EOF
        """;

    // The following asserts the values matches either of the expected value.
    String textFormat = convertToOpenMetricsFormat(pmRegistry);
    assertThat(textFormat)
        .satisfiesAnyOf(
            text -> assertThat(text).isEqualTo(expected1),
            text -> assertThat(text).isEqualTo(expected2));
  }

  @Test
  public void testMeter() {
    Meter meter = metricRegistry.meter("meter");
    meter.mark();
    meter.mark();

    String expected =
        """
        # TYPE meter counter
        # HELP meter Generated from Dropwizard metric import (metric=meter_total, type=com.codahale.metrics.Meter)
        meter_total 2.0
        # EOF
        """;
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

    assertThat(exports.collect().stream().flatMap(i1 -> i1.getDataPoints().stream()).findFirst())
        .containsInstanceOf(SummarySnapshot.SummaryDataPointSnapshot.class)
        .hasValueSatisfying(
            snapshot -> {
              var dataPointSnapshot = (SummarySnapshot.SummaryDataPointSnapshot) snapshot;
              // We slept for 1Ms so we ensure that all timers are above 1ms:
              assertThat(dataPointSnapshot.getQuantiles().size()).isGreaterThan(1);
              dataPointSnapshot
                  .getQuantiles()
                  .forEach(i -> assertThat(i.getValue()).isGreaterThan(timeSpentMillis / 1000d));
              assertThat(dataPointSnapshot.getCount()).isOne();
            });
  }

  @Test
  public void testThatMetricHelpUsesOriginalDropwizardName() {
    metricRegistry.timer("my.application.namedTimer1");
    metricRegistry.counter("my.application.namedCounter1");
    metricRegistry.meter("my.application.namedMeter1");
    metricRegistry.histogram("my.application.namedHistogram1");
    metricRegistry.register("my.application.namedGauge1", new ExampleDoubleGauge());

    String expected =
        """
        # TYPE my_application_namedCounter1 counter
        # HELP my_application_namedCounter1 Generated from Dropwizard metric import (metric=my.application.namedCounter1, type=com.codahale.metrics.Counter)
        my_application_namedCounter1_total 0.0
        # TYPE my_application_namedGauge1 gauge
        # HELP my_application_namedGauge1 Generated from Dropwizard metric import (metric=my.application.namedGauge1, type=io.prometheus.metrics.instrumentation.dropwizard.DropwizardExportsTest$ExampleDoubleGauge)
        my_application_namedGauge1 0.0
        # TYPE my_application_namedHistogram1 summary
        # HELP my_application_namedHistogram1 Generated from Dropwizard metric import (metric=my.application.namedHistogram1, type=com.codahale.metrics.Histogram)
        my_application_namedHistogram1{quantile="0.5"} 0.0
        my_application_namedHistogram1{quantile="0.75"} 0.0
        my_application_namedHistogram1{quantile="0.95"} 0.0
        my_application_namedHistogram1{quantile="0.98"} 0.0
        my_application_namedHistogram1{quantile="0.99"} 0.0
        my_application_namedHistogram1{quantile="0.999"} 0.0
        my_application_namedHistogram1_count 0
        # TYPE my_application_namedMeter1 counter
        # HELP my_application_namedMeter1 Generated from Dropwizard metric import (metric=my.application.namedMeter1_total, type=com.codahale.metrics.Meter)
        my_application_namedMeter1_total 0.0
        # TYPE my_application_namedTimer1 summary
        # HELP my_application_namedTimer1 Generated from Dropwizard metric import (metric=my.application.namedTimer1, type=com.codahale.metrics.Timer)
        my_application_namedTimer1{quantile="0.5"} 0.0
        my_application_namedTimer1{quantile="0.75"} 0.0
        my_application_namedTimer1{quantile="0.95"} 0.0
        my_application_namedTimer1{quantile="0.98"} 0.0
        my_application_namedTimer1{quantile="0.99"} 0.0
        my_application_namedTimer1{quantile="0.999"} 0.0
        my_application_namedTimer1_count 0
        # EOF
        """;
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
      nameEscapingScheme = EscapingScheme.NO_ESCAPING;
      writer.write(out, _registry.scrape());
      return out.toString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String convertToOpenMetricsFormat() {
    return convertToOpenMetricsFormat(registry);
  }
}
