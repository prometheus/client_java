package io.prometheus.client.exporter.common.formatter;

import io.prometheus.client.*;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static io.prometheus.client.exporter.common.TextFormat.*;

/**
 * Implement TextFormat#write004.
 */
public class PrometheusTextFormatter extends TextFormatter {

  public static final List<Collector.Type> SUPPORTED_TYPES =
      Arrays.asList(
          Collector.Type.COUNTER,
          Collector.Type.GAUGE,
          Collector.Type.SUMMARY,
          Collector.Type.HISTOGRAM);

  public PrometheusTextFormatter(Writer writer) {
    super(writer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void format(MetricSnapshotSamples samples) throws IOException {
    Collector.Type type = samples.type;
    if (!SUPPORTED_TYPES.contains(type)) {
      return;
    }

    this.write(samples.name, samples.help, type);
    final String createdName = samples.name + "_created";
    final String gcountName = samples.name + "_gcount";
    final String gsumName = samples.name + "_gsum";

    if (type.equals(Collector.Type.GAUGE)) {
      Map<List<String>, Gauge.Child> children = (Map<List<String>, Gauge.Child>) samples.children;
      for (Map.Entry<List<String>, Gauge.Child> entry : children.entrySet()) {
        this.write(
            samples.name, null, samples.labelNames, entry.getKey(), entry.getValue().get(), null);
      }
    } else if (type.equals(Collector.Type.COUNTER)) {
      Map<List<String>, Counter.Child> children =
          (Map<List<String>, Counter.Child>) samples.children;
      for (Map.Entry<List<String>, Counter.Child> entry : children.entrySet()) {
        this.write(
            samples.name,
            "_total",
            samples.labelNames,
            entry.getKey(),
            entry.getValue().get(),
            null);
      }
      // TODO
    } else if (type.equals(Collector.Type.HISTOGRAM)) {
      HistogramMetricSnapshotSamples samples1 = (HistogramMetricSnapshotSamples) samples;
      double[] buckets = samples1.buckets;
      Map<List<String>, Histogram.Child> children =
          (Map<List<String>, Histogram.Child>) samples1.children;
      for (Map.Entry<List<String>, Histogram.Child> entry : children.entrySet()) {}
      // TODO
    } else if (type.equals(Collector.Type.SUMMARY)) {
      SummaryMetricSnapshotSamples samples1 = (SummaryMetricSnapshotSamples) samples;
      List<CKMSQuantiles.Quantile> quantiles = samples1.quantiles;
      Map<List<String>, Summary.Child> children =
          (Map<List<String>, Summary.Child>) samples.children;
      for (Map.Entry<List<String>, Summary.Child> entry : children.entrySet()) {}
      // TODO
    }
  }

  @Override
  public void format(Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    TextFormat.write004(this.writer, mfs);
  }

  final String value = Double.toString(Double.MAX_VALUE);

  private void write(
      String name,
      String suffix,
      List<String> labelNames,
      List<String> labelValues,
      double value,
      Long timestampMs)
      throws IOException {
    writer.write(name);
    if (null != suffix) {
      writer.write(suffix);
    }
    if (labelNames.size() > 0) {
      writer.write('{');
      for (int i = 0; i < labelNames.size(); ++i) {
        writer.write(labelNames.get(i));
        writer.write("=\"");
        writeEscapedLabelValue(writer, labelValues.get(i));
        writer.write("\",");
      }
      writer.write('}');
    }
    writer.write(' ');

    if (value == Double.POSITIVE_INFINITY) {
      writer.write("+Inf");
    } else if (value == Double.NEGATIVE_INFINITY) {
      writer.write("-Inf");
    } else {
      //TODO doubleToStr
      writer.write(this.value);
    }

    if (timestampMs != null) {
      writer.write(' ');
      writer.write(timestampMs.toString());
    }
    writer.write('\n');
  }

  private void write(String name, String help, Collector.Type type) throws IOException {
    writer.write("# HELP ");
    writer.write(name);
    if (type == Collector.Type.COUNTER) {
      writer.write("_total");
    }
    if (type == Collector.Type.INFO) {
      writer.write("_info");
    }
    writer.write(' ');
    writeEscapedHelp(writer, help);
    writer.write('\n');

    writer.write("# TYPE ");
    writer.write(name);
    if (type == Collector.Type.COUNTER) {
      writer.write("_total");
    }
    if (type == Collector.Type.INFO) {
      writer.write("_info");
    }
    writer.write(' ');
    writer.write(typeString(type));
    writer.write('\n');
  }
}
