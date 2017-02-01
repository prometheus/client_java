package io.prometheus.client.exporter.common;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;

import io.prometheus.client.Collector;

public class TextFormat {
  /**
   * Write out the text version 0.0.4 of the given MetricFamilySamples.
   */
  public static void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    /* See http://prometheus.io/docs/instrumenting/exposition_formats/
     * for the output format specification. */
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(mfs)) {
      writer.write("# HELP ");
      writer.write(metricFamilySamples.name);
      writer.write(' ');
      writer.write(escapeHelp(metricFamilySamples.help));
      writer.write('\n');

      writer.write("# TYPE ");
      writer.write(metricFamilySamples.name);
      writer.write(' ');
      writer.write(typeString(metricFamilySamples.type));
      writer.write('\n');

      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        writer.write(sample.name);
        if (sample.labelNames.size() > 0) {
          writer.write('{');
          for (int i = 0; i < sample.labelNames.size(); ++i) {
            writer.write(sample.labelNames.get(i));
            writer.write("=\"");
            writer.write(escapeLabelValue(sample.labelValues.get(i)));
            writer.write("\",");
          }
          writer.write('}');
        }
        writer.write(' ');
        writer.write(Collector.doubleToGoString(sample.value));
        writer.write('\n');
      }
    }
  }

  /**
   * Content-type for text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

  static String escapeHelp(String s) {
    StringBuilder sb = new StringBuilder(s.length() * 2);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (s.charAt(i)) {
        case '\\':
          sb.append("\\\\");
          break;
        case '\n':
          sb.append("\\n");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  static String escapeLabelValue(String s) {
    StringBuilder sb = new StringBuilder(s.length() * 2);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (s.charAt(i)) {
        case '\\':
          sb.append("\\\\");
          break;
        case '\"':
          sb.append("\\\"");
          break;
        case '\n':
          sb.append("\\n");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  static String typeString(Collector.Type t) {
    switch (t) {
      case GAUGE:
        return "gauge";
      case COUNTER:
        return "counter";
      case SUMMARY:
        return "summary";
      case HISTOGRAM:
        return "histogram";
      default:
        return "untyped";
    }
  }
}
