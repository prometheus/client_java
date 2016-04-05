package io.prometheus.client.exporter.common;

import io.prometheus.client.Collector;

import java.util.Collections;
import java.util.Enumeration;
import java.io.IOException;
import java.io.Writer;

public class TextFormat {
  /**
   * Write out the text version 0.0.4 of the given MetricFamilySamples.
   */
  public static void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    /* See http://prometheus.io/docs/instrumenting/exposition_formats/
     * for the output format specification. */
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(mfs)) {
      writer.write("# HELP " + metricFamilySamples.name + " " + escapeHelp(metricFamilySamples.help) + "\n");
      writer.write("# TYPE " + metricFamilySamples.name + " " + typeString(metricFamilySamples.type) + "\n");
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        writer.write(sample.name);
        if (sample.labelNames.size() > 0) {
          writer.write("{");
          for (int i = 0; i < sample.labelNames.size(); ++i) {
            writer.write(String.format("%s=\"%s\",",
                sample.labelNames.get(i),  escapeLabelValue(sample.labelValues.get(i))));
          }
          writer.write("}");
        }
        writer.write(" " + Collector.doubleToGoString(sample.value) + "\n");
      }
    }
  }

  /**
   * Content-type for text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

  static String escapeHelp(String s) {
    return s.replace("\\", "\\\\").replace("\n", "\\n");
  }
  static String escapeLabelValue(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
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
