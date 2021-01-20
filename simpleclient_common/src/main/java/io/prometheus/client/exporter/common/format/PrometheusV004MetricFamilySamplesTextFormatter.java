package io.prometheus.client.exporter.common.format;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.format.AbstractMetricFamilySamplesTextFormatter;
import io.prometheus.client.exporter.common.format.MetricFamilySamplesTextFormatter;

/**
 * Writer for the text version 0.0.4 of the given MetricFamilySamples.
 *
 * @see {@link http://prometheus.io/docs/instrumenting/exposition_formats/}
 */
public class PrometheusV004MetricFamilySamplesTextFormatter extends AbstractMetricFamilySamplesTextFormatter {
  /**
   * {@inheritDoc}
   */
  @Override
  public void write(final Writer writer, final Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    while(mfs.hasMoreElements()) {
      Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
      writer.write("# HELP ");
      writer.write(metricFamilySamples.name);
      writer.write(' ');
      writeEscapedHelp(writer, metricFamilySamples.help);
      writer.write('\n');

      writer.write("# TYPE ");
      writer.write(metricFamilySamples.name);
      writer.write(' ');
      writer.write(formatTypeToString(metricFamilySamples.type));
      writer.write('\n');

      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        writer.write(sample.name);
        if (sample.labelNames.size() > 0) {
          writer.write('{');
          for (int i = 0; i < sample.labelNames.size(); ++i) {
            writer.write(sample.labelNames.get(i));
            writer.write("=\"");
            writeEscapedLabelValue(writer, sample.labelValues.get(i));
            writer.write("\",");
          }
          writer.write('}');
        }
        writer.write(' ');
        writer.write(Collector.doubleToGoString(sample.value));
        if (sample.timestampMs != null){
          writer.write(' ');
          writer.write(sample.timestampMs.toString());
        }
        writer.write('\n');
      }
    }
  }

  private static void writeEscapedHelp(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.append("\\\\");
          break;
        case '\n':
          writer.append("\\n");
          break;
        default:
          writer.append(c);
      }
    }
  }

  private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.append("\\\\");
          break;
        case '\"':
          writer.append("\\\"");
          break;
        case '\n':
          writer.append("\\n");
          break;
        default:
          writer.append(c);
      }
    }
  }
}
