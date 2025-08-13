package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.SnapshotEscaper;
import java.io.IOException;
import java.io.Writer;

public class TextFormatUtil {

  static void writeLong(Writer writer, long value) throws IOException {
    writer.append(Long.toString(value));
  }

  static void writeDouble(Writer writer, double d) throws IOException {
    if (d == Double.POSITIVE_INFINITY) {
      writer.write("+Inf");
    } else if (d == Double.NEGATIVE_INFINITY) {
      writer.write("-Inf");
    } else {
      writer.write(Double.toString(d));
      // FloatingDecimal.getBinaryToASCIIConverter(d).appendTo(writer);
    }
  }

  static void writePrometheusTimestamp(Writer writer, long timestampMs, boolean timestampsInMs)
      throws IOException {
    if (timestampsInMs) {
      // correct for prometheus exposition format
      // https://prometheus.io/docs/instrumenting/exposition_formats/#text-format-details
      writer.write(Long.toString(timestampMs));
    } else {
      // incorrect for prometheus exposition format -
      // but we need to support it for backwards compatibility
      writeOpenMetricsTimestamp(writer, timestampMs);
    }
  }

  static void writeOpenMetricsTimestamp(Writer writer, long timestampMs) throws IOException {
    writer.write(Long.toString(timestampMs / 1000L));
    writer.write(".");
    long ms = timestampMs % 1000;
    if (ms < 100) {
      writer.write("0");
    }
    if (ms < 10) {
      writer.write("0");
    }
    writer.write(Long.toString(ms));
  }

  static void writeEscapedString(Writer writer, String s) throws IOException {
    // optimize for the common case where no escaping is needed
    int start = 0;
    // #indexOf is a vectorized intrinsic
    int backslashIndex = s.indexOf('\\', start);
    int quoteIndex = s.indexOf('\"', start);
    int newlineIndex = s.indexOf('\n', start);

    int allEscapesIndex = backslashIndex & quoteIndex & newlineIndex;
    while (allEscapesIndex != -1) {
      int escapeStart = Integer.MAX_VALUE;
      if (backslashIndex != -1) {
        escapeStart = backslashIndex;
      }
      if (quoteIndex != -1) {
        escapeStart = Math.min(escapeStart, quoteIndex);
      }
      if (newlineIndex != -1) {
        escapeStart = Math.min(escapeStart, newlineIndex);
      }

      // bulk write up to the first character that needs to be escaped
      if (escapeStart > start) {
        writer.write(s, start, escapeStart - start);
      }
      char c = s.charAt(escapeStart);
      start = escapeStart + 1;
      switch (c) {
        case '\\':
          writer.write("\\\\");
          backslashIndex = s.indexOf('\\', start);
          break;
        case '\"':
          writer.write("\\\"");
          quoteIndex = s.indexOf('\"', start);
          break;
        case '\n':
          writer.write("\\n");
          newlineIndex = s.indexOf('\n', start);
          break;
      }

      allEscapesIndex = backslashIndex & quoteIndex & newlineIndex;
    }
    // up until the end nothing needs to be escaped anymore
    int remaining = s.length() - start;
    if (remaining > 0) {
      writer.write(s, start, remaining);
    }
  }

  static void writeLabels(
      Writer writer,
      Labels labels,
      String additionalLabelName,
      double additionalLabelValue,
      boolean metricInsideBraces,
      EscapingScheme scheme)
      throws IOException {
    if (!metricInsideBraces) {
      writer.write('{');
    }
    for (int i = 0; i < labels.size(); i++) {
      if (i > 0 || metricInsideBraces) {
        writer.write(",");
      }
      writeName(writer, SnapshotEscaper.getSnapshotLabelName(labels, i, scheme), NameType.Label);
      writer.write("=\"");
      writeEscapedString(writer, labels.getValue(i));
      writer.write("\"");
    }
    if (additionalLabelName != null) {
      if (!labels.isEmpty()) {
        writer.write(",");
      }
      writer.write(additionalLabelName);
      writer.write("=\"");
      writeDouble(writer, additionalLabelValue);
      writer.write("\"");
    }
    writer.write('}');
  }

  static void writeName(Writer writer, String name, NameType nameType) throws IOException {
    switch (nameType) {
      case Metric:
        if (PrometheusNaming.isValidLegacyMetricName(name)) {
          writer.write(name);
          return;
        }
        break;
      case Label:
        if (PrometheusNaming.isValidLegacyLabelName(name)) {
          writer.write(name);
          return;
        }
        break;
      default:
        throw new RuntimeException("Invalid name type requested: " + nameType);
    }
    writer.write('"');
    writeEscapedString(writer, name);
    writer.write('"');
  }
}
