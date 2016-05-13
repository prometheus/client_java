package io.prometheus.client.exporter.common;

import static org.junit.Assert.assertEquals;

import io.prometheus.client.Counter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;


public class TextFormatTest {
  CollectorRegistry registry;
  StringWriter writer;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    writer = new StringWriter();
  }

  @Test
  public void testGaugeOutput() throws IOException {
    Gauge noLabels = (Gauge) Gauge.build().name("nolabels").help("help").register(registry);
    noLabels.inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels gauge\n"
                 + "nolabels 1.0\n", writer.toString());
  }

  @Test
  public void testValueInfinity() throws IOException {
    Gauge noLabels = (Gauge) Gauge.build().name("nolabels").help("help").register(registry);
    noLabels.set(Double.POSITIVE_INFINITY);
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels gauge\n"
                 + "nolabels +Inf\n", writer.toString());
  }

  @Test
  public void testCounterOutput() throws IOException {
    Counter noLabels = (Counter) Counter.build().name("nolabels").help("help").register(registry);
    noLabels.inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels counter\n"
                 + "nolabels 1.0\n", writer.toString());
  }

  @Test
  public void testSummaryOutput() throws IOException {
    Summary noLabels = (Summary) Summary.build().name("nolabels").help("help").register(registry);
    noLabels.observe(2);
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels summary\n"
                 + "nolabels_count 1.0\n"
                 + "nolabels_sum 2.0\n", writer.toString());
  }

  @Test
  public void testLabelsOutput() throws IOException {
    Gauge labels = (Gauge) Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("a").inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP labels help\n"
                 + "# TYPE labels gauge\n"
                 + "labels{l=\"a\",} 1.0\n", writer.toString());
  }

  @Test
  public void testSortedLabelsOutput() throws IOException {
    Gauge labels = (Gauge) Gauge.build().name("labels").help("help").labelNames("m", "s", "l").register(registry);
    labels.labels("a", "b", "c").inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP labels help\n"
            + "# TYPE labels gauge\n"
            + "labels{l=\"c\",m=\"a\",s=\"b\",} 1.0\n", writer.toString());
  }

  @Test
  public void testLabelValuesEscaped() throws IOException {
    Gauge labels = (Gauge) Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("a\nb\\c\"d").inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP labels help\n"
                 + "# TYPE labels gauge\n"
                 + "labels{l=\"a\\nb\\\\c\\\"d\",} 1.0\n", writer.toString());
  }

  @Test
  public void testHelpEscaped() throws IOException {
    Gauge noLabels = (Gauge) Gauge.build().name("nolabels").help("h\"e\\l\np").register(registry);
    noLabels.inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels h\"e\\\\l\\np\n"
                 + "# TYPE nolabels gauge\n"
                 + "nolabels 1.0\n", writer.toString());
  }
}
