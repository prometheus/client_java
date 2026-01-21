package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.SnapshotEscaper;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Utility methods for writing Prometheus text exposition formats.
 *
 * <p>This class provides low-level formatting utilities used by both Prometheus text format and
 * OpenMetrics format writers. It handles escaping, label formatting, timestamp conversion, and
 * merging of duplicate metric names.
 */
public class TextFormatUtil {
  /**
   * Merges snapshots with duplicate Prometheus names by combining their data points. This ensures
   * only one HELP/TYPE declaration per metric family.
   */
  public static MetricSnapshots mergeDuplicates(MetricSnapshots metricSnapshots) {
    if (metricSnapshots.size() <= 1) {
      return metricSnapshots;
    }

    Map<String, List<MetricSnapshot>> grouped = new LinkedHashMap<>();

    for (MetricSnapshot snapshot : metricSnapshots) {
      String prometheusName = snapshot.getMetadata().getPrometheusName();
      List<MetricSnapshot> list = grouped.get(prometheusName);
      if (list == null) {
        list = new ArrayList<>();
        grouped.put(prometheusName, list);
      }
      list.add(snapshot);
    }

    MetricSnapshots.Builder builder = MetricSnapshots.builder();
    for (List<MetricSnapshot> group : grouped.values()) {
      if (group.size() == 1) {
        builder.metricSnapshot(group.get(0));
      } else {
        MetricSnapshot merged = mergeSnapshots(group);
        builder.metricSnapshot(merged);
      }
    }

    return builder.build();
  }

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
      @Nullable String additionalLabelName,
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
      if (!labels.isEmpty() || metricInsideBraces) {
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

  /**
   * Merges multiple snapshots of the same type into a single snapshot with combined data points.
   */
  @SuppressWarnings("unchecked")
  private static MetricSnapshot mergeSnapshots(List<MetricSnapshot> snapshots) {
    MetricSnapshot first = snapshots.get(0);

    // Validate all snapshots are the same type and calculate total size
    int totalDataPoints = 0;
    for (MetricSnapshot snapshot : snapshots) {
      if (snapshot.getClass() != first.getClass()) {
        throw new IllegalArgumentException(
            "Cannot merge snapshots of different types: "
                + first.getClass().getName()
                + " and "
                + snapshot.getClass().getName());
      }
      totalDataPoints += snapshot.getDataPoints().size();
    }

    // Pre-size the list to avoid resizing
    List<DataPointSnapshot> allDataPoints = new ArrayList<>(totalDataPoints);
    for (MetricSnapshot snapshot : snapshots) {
      allDataPoints.addAll(snapshot.getDataPoints());
    }

    // Create merged snapshot based on type
    if (first instanceof CounterSnapshot) {
      return new CounterSnapshot(
          first.getMetadata(),
          (Collection<CounterSnapshot.CounterDataPointSnapshot>) (Object) allDataPoints);
    } else if (first instanceof GaugeSnapshot) {
      return new GaugeSnapshot(
          first.getMetadata(),
          (Collection<GaugeSnapshot.GaugeDataPointSnapshot>) (Object) allDataPoints);
    } else if (first instanceof HistogramSnapshot) {
      return new HistogramSnapshot(
          first.getMetadata(),
          (Collection<HistogramSnapshot.HistogramDataPointSnapshot>) (Object) allDataPoints);
    } else if (first instanceof SummarySnapshot) {
      return new SummarySnapshot(
          first.getMetadata(),
          (Collection<SummarySnapshot.SummaryDataPointSnapshot>) (Object) allDataPoints);
    } else if (first instanceof InfoSnapshot) {
      return new InfoSnapshot(
          first.getMetadata(),
          (Collection<InfoSnapshot.InfoDataPointSnapshot>) (Object) allDataPoints);
    } else if (first instanceof StateSetSnapshot) {
      return new StateSetSnapshot(
          first.getMetadata(),
          (Collection<StateSetSnapshot.StateSetDataPointSnapshot>) (Object) allDataPoints);
    } else if (first instanceof UnknownSnapshot) {
      return new UnknownSnapshot(
          first.getMetadata(),
          (Collection<UnknownSnapshot.UnknownDataPointSnapshot>) (Object) allDataPoints);
    } else {
      throw new IllegalArgumentException("Unknown snapshot type: " + first.getClass().getName());
    }
  }
}
