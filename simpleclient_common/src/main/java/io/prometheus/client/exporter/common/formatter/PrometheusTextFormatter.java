package io.prometheus.client.exporter.common.formatter;

import io.prometheus.client.*;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static io.prometheus.client.exporter.common.TextFormat.*;

/** Implement TextFormat#write004. */
public class PrometheusTextFormatter extends TextFormatter {

  public static final List<Collector.Type> SUPPORTED_TYPES =
      Arrays.asList(
          Collector.Type.COUNTER,
          Collector.Type.GAUGE,
          Collector.Type.SUMMARY,
          Collector.Type.HISTOGRAM);

  private final MetricsWriter subWriter;

  public PrometheusTextFormatter(MetricsWriter writer, MetricsWriter subWriter) {
    super(writer);
    this.subWriter = subWriter;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void format(MetricSnapshotSamples samples) throws IOException {
    Collector.Type type = samples.type;
    if (!SUPPORTED_TYPES.contains(type)) {
      return;
    }

    outputHeader(writer, samples.name, samples.help, type);

    if (type.equals(Collector.Type.GAUGE)) {
      Map<List<String>, Gauge.Child> children = (Map<List<String>, Gauge.Child>) samples.children;
      for (Map.Entry<List<String>, Gauge.Child> entry : children.entrySet()) {
        outputValue(
            writer, samples.name, null, samples.labelNames, entry.getKey(), entry.getValue().get());
      }
    } else if (type.equals(Collector.Type.COUNTER)) {
      Map<List<String>, Counter.Child> children =
          (Map<List<String>, Counter.Child>) samples.children;
      outputHeader(subWriter, samples.name + "_created", samples.help, Collector.Type.GAUGE);
      for (Map.Entry<List<String>, Counter.Child> entry : children.entrySet()) {
        List<String> labelValues = entry.getKey();
        Counter.Child child = entry.getValue();
        outputValue(writer, samples.name, "_total", samples.labelNames, labelValues, child.get());
        outputValue(
            subWriter,
            samples.name,
            "_created",
            samples.labelNames,
            labelValues,
            child.created() / 1000.0);
      }
    } else if (type.equals(Collector.Type.HISTOGRAM)) {
      HistogramMetricSnapshotSamples samples1 = (HistogramMetricSnapshotSamples) samples;
      double[] buckets = samples1.buckets;
      Map<List<String>, Histogram.Child> children =
          (Map<List<String>, Histogram.Child>) samples1.children;
      outputHeader(subWriter, samples.name + "_created", samples.help, Collector.Type.GAUGE);
      for (Map.Entry<List<String>, Histogram.Child> entry : children.entrySet()) {
        List<String> labelValues = entry.getKey();
        Histogram.Child.Value value = entry.getValue().get();
        for (int i = 0; i < value.buckets.length; ++i) {
          outputValue(
              writer,
              samples.name,
              "_bucket",
              samples.labelNames,
              "le",
              labelValues,
              buckets[i],
              value.buckets[i]);
        }

        outputValue(
            writer,
            samples.name,
            "_count",
            samples.labelNames,
            labelValues,
            value.buckets[buckets.length - 1]);
        outputValue(writer, samples.name, "_sum", samples.labelNames, labelValues, value.sum);
        outputValue(
            subWriter,
            samples.name,
            "_created",
            samples.labelNames,
            labelValues,
            value.created / 1000.0);
      }
    } else if (type.equals(Collector.Type.SUMMARY)) {
      Map<List<String>, Summary.Child> children =
          (Map<List<String>, Summary.Child>) samples.children;
      outputHeader(subWriter, samples.name + "_created", samples.help, Collector.Type.GAUGE);
      for (Map.Entry<List<String>, Summary.Child> entry : children.entrySet()) {
        List<String> labelValues = entry.getKey();
        Summary.Child.Value value = entry.getValue().get();
        for (Map.Entry<Double, Double> q : value.quantiles.entrySet()) {
          outputValue(
              writer,
              samples.name,
              null,
              samples.labelNames,
              "quantile",
              labelValues,
              q.getValue(),
              q.getValue());
        }

        outputValue(writer, samples.name, "_count", samples.labelNames, labelValues, value.count);
        outputValue(writer, samples.name, "_sum", samples.labelNames, labelValues, value.sum);
        outputValue(
            subWriter,
            samples.name,
            "_created",
            samples.labelNames,
            labelValues,
            value.created / 1000.0);
      }
    }
  }

  @Override
  public void format(Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    TextFormat.write004(this.writer, mfs);
  }

  @Override
  protected void flush() throws IOException {
    this.writer.append(this.subWriter);
  }

  private static void outputValue(
      MetricsWriter writer,
      String name,
      String suffix,
      List<String> labelNames,
      List<String> labelValues,
      double value)
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
    DoubleUtil.append(writer, value);
    writer.write('\n');
  }

  private static void outputValue(
      MetricsWriter writer,
      String name,
      String suffix,
      List<String> labelNames,
      String extraLabelName,
      List<String> labelValues,
      double extraLabelValue,
      double value)
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
      writer.write(extraLabelName);
      writer.write("=\"");
      DoubleUtil.append(writer, extraLabelValue);
      writer.write("\",}");
    }
    writer.write(' ');
    DoubleUtil.append(writer, value);
    writer.write('\n');
  }

  private static void outputHeader(
      MetricsWriter writer, String name, String help, Collector.Type type) throws IOException {
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
