package io.prometheus.client.exporter.common;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import io.prometheus.client.Collector;

public class TextFormat {
  /**
   * Content-type for Prometheus text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

  /**
   * Content-type for Openmetrics text version 1.0.0.
   *
   * @since 0.10.0
   */
  public final static String CONTENT_TYPE_OPENMETRICS_100 = "application/openmetrics-text; version=1.0.0; charset=utf-8";

  /**
   * Return the content type that should be used for a given Accept HTTP header.
   *
   * @since 0.10.0
   */
  public static String chooseContentType(String acceptHeader) {
    if (acceptHeader == null) {
      return CONTENT_TYPE_004;
    }

    for (String accepts : acceptHeader.split(",")) {
      if ("application/openmetrics-text".equals(accepts.split(";")[0].trim())) {
        return CONTENT_TYPE_OPENMETRICS_100;
      }
    }

    return CONTENT_TYPE_004;
  }

  /**
   * Write out the given MetricFamilySamples in a format per the contentType.
   *
   * @since 0.10.0
   */
  public static void writeFormat(String contentType, Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    if (CONTENT_TYPE_004.equals(contentType)) {
        write004(writer, mfs);
        return;
    }
    if (CONTENT_TYPE_OPENMETRICS_100.equals(contentType)) {
        writeOpenMetrics100(writer, mfs);
        return;
    }
    throw new IllegalArgumentException("Unknown contentType " + contentType);
  }

  /**
   * Write out the text version 0.0.4 of the given MetricFamilySamples.
   */
  public static void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    Map<String, Collector.MetricFamilySamples> omFamilies = new TreeMap<String, Collector.MetricFamilySamples>();
    /* See http://prometheus.io/docs/instrumenting/exposition_formats/
     * for the output format specification. */
    while(mfs.hasMoreElements()) {
      Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
      String name = metricFamilySamples.name;
      writer.write("# HELP ");
      writer.write(name);
      if (metricFamilySamples.type == Collector.Type.COUNTER) {
        writer.write("_total");
      }
      if (metricFamilySamples.type == Collector.Type.INFO) {
        writer.write("_info");
      }
      writer.write(' ');
      writeEscapedHelp(writer, metricFamilySamples.help);
      writer.write('\n');

      writer.write("# TYPE ");
      writer.write(name);
      if (metricFamilySamples.type == Collector.Type.COUNTER) {
        writer.write("_total");
      }
      if (metricFamilySamples.type == Collector.Type.INFO) {
        writer.write("_info");
      }
      writer.write(' ');
      writer.write(typeString(metricFamilySamples.type));
      writer.write('\n');

      String createdName = name + "_created";
      String gcountName = name + "_gcount";
      String gsumName = name + "_gsum";
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        /* OpenMetrics specific sample, put in a gauge at the end. */
        if (sample.name.equals(createdName)
            || sample.name.equals(gcountName)
            || sample.name.equals(gsumName)) {
          Collector.MetricFamilySamples omFamily = omFamilies.get(sample.name);
          if (omFamily == null) {
            omFamily = new Collector.MetricFamilySamples(sample.name, Collector.Type.GAUGE, metricFamilySamples.help, new ArrayList<Collector.MetricFamilySamples.Sample>());
            omFamilies.put(sample.name, omFamily);
          }
          omFamily.samples.add(sample);
          continue;
        }
        writer.write(sample.name);
        if (sample.labelNames.size() > 0) {
          writer.write('{');
          for (int i = 0; i < sample.labelNames.size(); ++i) {
            writer.write(sample.labelNames.get(i));
            writer.write("=\"");
            writeEscapedLabelValue(writer, sample.labelValues.get(i));
            if (i < sample.labelNames.size() - 1) {
              writer.write("\",");
            } else {
              writer.write("\"");
            }
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
    // Write out any OM-specific samples.
    if (!omFamilies.isEmpty()) {
      write004(writer, Collections.enumeration(omFamilies.values()));
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

  private static String typeString(Collector.Type t) {
    switch (t) {
      case GAUGE:
        return "gauge";
      case COUNTER:
        return "counter";
      case SUMMARY:
        return "summary";
      case HISTOGRAM:
        return "histogram";
      case GAUGE_HISTOGRAM:
        return "histogram";
      case STATE_SET:
        return "gauge";
      case INFO:
        return "gauge";
      default:
        return "untyped";
    }
  }

  /**
   * Write out the OpenMetrics text version 1.0.0 of the given MetricFamilySamples.
   *
   * @since 0.10.0
   */
  public static void writeOpenMetrics100(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    while(mfs.hasMoreElements()) {
      Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
      String name = metricFamilySamples.name;

      writer.write("# TYPE ");
      writer.write(name);
      writer.write(' ');
      writer.write(omTypeString(metricFamilySamples.type));
      writer.write('\n');

      if (!metricFamilySamples.unit.isEmpty()) {
        writer.write("# UNIT ");
        writer.write(name);
        writer.write(' ');
        writer.write(metricFamilySamples.unit);
        writer.write('\n');
      }

      writer.write("# HELP ");
      writer.write(name);
      writer.write(' ');
      writeEscapedLabelValue(writer, metricFamilySamples.help);
      writer.write('\n');
 
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        writer.write(sample.name);
        if (sample.labelNames.size() > 0) {
          writer.write('{');
          for (int i = 0; i < sample.labelNames.size(); ++i) {
            if (i > 0) {
              writer.write(",");
            }
            writer.write(sample.labelNames.get(i));
            writer.write("=\"");
            writeEscapedLabelValue(writer, sample.labelValues.get(i));
            writer.write("\"");
          }
          writer.write('}');
        }
        writer.write(' ');
        writer.write(Collector.doubleToGoString(sample.value));
        if (sample.timestampMs != null){
          writer.write(' ');
          omWriteTimestamp(writer, sample.timestampMs);
        }
        if (sample.exemplar != null) {
          writer.write(" # {");
          for (int i=0; i<sample.exemplar.getNumberOfLabels(); i++) {
            if (i > 0) {
              writer.write(",");
            }
            writer.write(sample.exemplar.getLabelName(i));
            writer.write("=\"");
            writeEscapedLabelValue(writer, sample.exemplar.getLabelValue(i));
            writer.write("\"");
          }
          writer.write("} ");
          writer.write(Collector.doubleToGoString(sample.exemplar.getValue()));
          if (sample.exemplar.getTimestampMs() != null) {
            writer.write(' ');
            omWriteTimestamp(writer, sample.exemplar.getTimestampMs());
          }
        }
        writer.write('\n');
      }
    }
    writer.write("# EOF\n");
  }

  static void omWriteTimestamp(Writer writer, long timestampMs) throws IOException {
    writer.write(Long.toString(timestampMs / 1000L));
    writer.write(".");
    long ms = timestampMs % 1000;
    if (ms < 100) {
      writer.write("0");
    }
    if (ms < 10) {
      writer.write("0");
    }
    writer.write(Long.toString(timestampMs % 1000));
  }

  private static String omTypeString(Collector.Type t) {
    switch (t) {
      case GAUGE:
        return "gauge";
      case COUNTER:
        return "counter";
      case SUMMARY:
        return "summary";
      case HISTOGRAM:
        return "histogram";
      case GAUGE_HISTOGRAM:
        return "gaugehistogram";
      case STATE_SET:
        return "stateset";
      case INFO:
        return "info";
      default:
        return "unknown";
    }
  }
}
