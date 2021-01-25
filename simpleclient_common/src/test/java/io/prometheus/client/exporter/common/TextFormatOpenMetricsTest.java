package io.prometheus.client.exporter.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Info;
import io.prometheus.client.Summary;


public class TextFormatOpenMetricsTest {
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
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels gauge\n"
                 + "# HELP nolabels help\n"
                 + "nolabels 1.0\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testValueInfinity() throws IOException {
    Gauge noLabels = Gauge.build().name("nolabels").help("help").register(registry);
    noLabels.set(Double.POSITIVE_INFINITY);
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels gauge\n"
                 + "# HELP nolabels help\n"
                 + "nolabels +Inf\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testCounterOutput() throws IOException {
    Counter noLabels = Counter.build().name("nolabels").help("help").register(registry);
    noLabels.inc();
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels counter\n"
                 + "# HELP nolabels help\n"
                 + "nolabels_total 1.0\n"
                 + "nolabels_created 1234.0\n"
                 + "# EOF\n", writer.toString().replaceAll("_created [0-9E.]+", "_created 1234.0"));
  }

  @Test
  public void testInfoOutput() throws IOException {
    Info noLabels = Info.build().name("nolabels").help("help").register(registry);
    noLabels.info("foo", "bar");
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels info\n"
                 + "# HELP nolabels help\n"
                 + "nolabels_info{foo=\"bar\"} 1.0\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testCounterSamplesMissingTotal() throws IOException {

    class CustomCollector extends Collector {
      public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        ArrayList<String> labelNames = new ArrayList<String>();
        ArrayList<String> labelValues = new ArrayList<String>();
        ArrayList<MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample("nolabels", labelNames, labelValues, 1.0);
        samples.add(sample);
        mfs.add(new MetricFamilySamples("nolabels", Collector.Type.COUNTER, "help", samples));
        return mfs;
      }
    }

    new CustomCollector().register(registry);
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels counter\n"
                 + "# HELP nolabels help\n"
                 + "nolabels_total 1.0\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testMetricOutputWithTimestamp() throws IOException {

    class CustomCollector extends Collector {
      public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        ArrayList<String> labelNames = new ArrayList<String>();
        ArrayList<String> labelValues = new ArrayList<String>();
        ArrayList<MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample("nolabels", labelNames, labelValues, 1.0, 1518123006L);
        samples.add(sample);
        mfs.add(new MetricFamilySamples("nolabels", Collector.Type.UNKNOWN, "help", samples));
        return mfs;
      }
    }

    new CustomCollector().register(registry);
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels unknown\n"
                 + "# HELP nolabels help\n"
                 + "nolabels 1.0 1518123.006\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testSummaryOutput() throws IOException {
    Summary noLabels = Summary.build().name("nolabels").help("help").register(registry);
    noLabels.observe(2);
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels summary\n"
                 + "# HELP nolabels help\n"
                 + "nolabels_count 1.0\n"
                 + "nolabels_sum 2.0\n"
                 + "nolabels_created 1234.0\n"
                 + "# EOF\n", writer.toString().replaceAll("_created [0-9E.]+", "_created 1234.0"));
  }

  @Test
  public void testSummaryOutputWithQuantiles() throws IOException {
    Summary labelsAndQuantiles = Summary.build()
            .quantile(0.5, 0.05).quantile(0.9, 0.01).quantile(0.99, 0.001)
            .labelNames("l").name("labelsAndQuantiles").help("help").register(registry);
    labelsAndQuantiles.labels("a").observe(2);
    writer = new StringWriter();
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE labelsAndQuantiles summary\n"
            + "# HELP labelsAndQuantiles help\n"
            + "labelsAndQuantiles{l=\"a\",quantile=\"0.5\"} 2.0\n"
            + "labelsAndQuantiles{l=\"a\",quantile=\"0.9\"} 2.0\n"
            + "labelsAndQuantiles{l=\"a\",quantile=\"0.99\"} 2.0\n"
            + "labelsAndQuantiles_count{l=\"a\"} 1.0\n"
            + "labelsAndQuantiles_sum{l=\"a\"} 2.0\n"
            + "labelsAndQuantiles_created{l=\"a\"} 1234.0\n"
            + "# EOF\n", writer.toString().replaceAll("(_created\\{.*\\}) [0-9E.]+", "$1 1234.0"));
  }

  @Test
  public void testLabelsOutput() throws IOException {
    Gauge labels = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("a").inc();
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE labels gauge\n"
                 + "# HELP labels help\n"
                 + "labels{l=\"a\"} 1.0\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testLabelValuesEscaped() throws IOException {
    Gauge labels = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    labels.labels("ąćčęntěd a\nb\\c\"d").inc();
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE labels gauge\n"
                 + "# HELP labels help\n"
                 + "labels{l=\"ąćčęntěd a\\nb\\\\c\\\"d\"} 1.0\n"
                 + "# EOF\n", writer.toString());
  }

  @Test
  public void testHelpEscaped() throws IOException {
    Gauge noLabels = Gauge.build().name("nolabels").help("ąćčęntěd h\"e\\l\np").register(registry);
    noLabels.inc();
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    assertEquals("# TYPE nolabels gauge\n"
                 + "# HELP nolabels ąćčęntěd h\\\"e\\\\l\\np\n"
                 + "nolabels 1.0\n"
                 + "# EOF\n", writer.toString());
  }
}
