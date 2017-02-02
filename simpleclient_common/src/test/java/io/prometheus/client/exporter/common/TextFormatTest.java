package io.prometheus.client.exporter.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;


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
    Gauge noLabels = Gauge.build().name("nolabels").help("help").register(registry);
    noLabels.inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels gauge\n"
                 + "nolabels 1.0\n", writer.toString());
  }

  @Test
  public void testValueInfinity() throws IOException {
    Gauge noLabels = Gauge.build().name("nolabels").help("help").register(registry);
    noLabels.set(Double.POSITIVE_INFINITY);
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels gauge\n"
                 + "nolabels +Inf\n", writer.toString());
  }

  @Test
  public void testCounterOutput() throws IOException {
    Counter noLabels = Counter.build().name("nolabels").help("help").register(registry);
    noLabels.inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels counter\n"
                 + "nolabels 1.0\n", writer.toString());
  }

  @Test
  public void testSummaryOutput() throws IOException {
    Summary noLabels = Summary.build().name("nolabels").help("help").register(registry);
    noLabels.observe(2);
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels help\n"
                 + "# TYPE nolabels summary\n"
                 + "nolabels_count 1.0\n"
                 + "nolabels_sum 2.0\n", writer.toString());
  }

  @Test
  public void testSummaryOutputWithQuantiles() throws IOException {
    Summary labelsAndQuantiles = Summary.build()
            .quantile(0.5, 0.05).quantile(0.9, 0.01).quantile(0.99, 0.001)
            .labelNames("l").name("labelsAndQuantiles").help("help").register(registry);
    labelsAndQuantiles.labels("a").observe(2);
    writer = new StringWriter();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP labelsAndQuantiles help\n"
            + "# TYPE labelsAndQuantiles summary\n"
            + "labelsAndQuantiles{l=\"a\",quantile=\"0.5\",} 2.0\n"
            + "labelsAndQuantiles{l=\"a\",quantile=\"0.9\",} 2.0\n"
            + "labelsAndQuantiles{l=\"a\",quantile=\"0.99\",} 2.0\n"
            + "labelsAndQuantiles_count{l=\"a\",} 1.0\n"
            + "labelsAndQuantiles_sum{l=\"a\",} 2.0\n", writer.toString());
  }

  @Test
  public void testLabelsOutput() throws IOException {
    Gauge labels = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("a").inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP labels help\n"
                 + "# TYPE labels gauge\n"
                 + "labels{l=\"a\",} 1.0\n", writer.toString());
  }

  @Test
  public void testLabelValuesEscaped() throws IOException {
    Gauge labels = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("a\nb\\c\"d").inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP labels help\n"
                 + "# TYPE labels gauge\n"
                 + "labels{l=\"a\\nb\\\\c\\\"d\",} 1.0\n", writer.toString());
  }

  @Test
  public void testHelpEscaped() throws IOException {
    Gauge noLabels = Gauge.build().name("nolabels").help("h\"e\\l\np").register(registry);
    noLabels.inc();
    TextFormat.write004(writer, registry.metricFamilySamples());
    assertEquals("# HELP nolabels h\"e\\\\l\\np\n"
                 + "# TYPE nolabels gauge\n"
                 + "nolabels 1.0\n", writer.toString());
  }
}
