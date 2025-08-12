package io.prometheus.metrics.expositionformats;

import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeDouble;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeEscapedString;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLabels;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLong;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeName;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writePrometheusTimestamp;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.escapeMetricSnapshot;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getMetadataName;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getSnapshotLabelName;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Write the Prometheus text format. This is the default if you view a Prometheus endpoint with your
 * Web browser.
 */
public class PrometheusTextFormatWriter implements ExpositionFormatWriter {

  public static final String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";

  private final boolean writeCreatedTimestamps;
  private final boolean timestampsInMs;

  public static class Builder {
    boolean includeCreatedTimestamps;
    boolean timestampsInMs = true;

    private Builder() {}

    /**
     * @param includeCreatedTimestamps whether to include the _created timestamp in the output
     */
    public Builder setIncludeCreatedTimestamps(boolean includeCreatedTimestamps) {
      this.includeCreatedTimestamps = includeCreatedTimestamps;
      return this;
    }

    @Deprecated
    public Builder setTimestampsInMs(boolean timestampsInMs) {
      this.timestampsInMs = timestampsInMs;
      return this;
    }

    public PrometheusTextFormatWriter build() {
      return new PrometheusTextFormatWriter(includeCreatedTimestamps, timestampsInMs);
    }
  }

  /**
   * @param writeCreatedTimestamps whether to include the _created timestamp in the output - This
   *     will produce an invalid OpenMetrics output, but is kept for backwards compatibility.
   * @deprecated this constructor is deprecated and will be removed in the next major version -
   *     {@link #builder()} or {@link #create()} instead
   */
  @Deprecated
  public PrometheusTextFormatWriter(boolean writeCreatedTimestamps) {
    this(writeCreatedTimestamps, false);
  }

  private PrometheusTextFormatWriter(boolean writeCreatedTimestamps, boolean timestampsInMs) {
    this.writeCreatedTimestamps = writeCreatedTimestamps;
    this.timestampsInMs = timestampsInMs;
  }

  public static PrometheusTextFormatWriter.Builder builder() {
    return new Builder();
  }

  public static PrometheusTextFormatWriter create() {
    return builder().build();
  }

  @Override
  public boolean accepts(String acceptHeader) {
    if (acceptHeader == null) {
      return false;
    } else {
      return acceptHeader.contains("text/plain");
    }
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void write(OutputStream out, MetricSnapshots metricSnapshots, EscapingScheme scheme)
      throws IOException {
    // See https://prometheus.io/docs/instrumenting/exposition_formats/
    // "unknown", "gauge", "counter", "stateset", "info", "histogram", "gaugehistogram", and
    // "summary".
    Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    for (MetricSnapshot s : metricSnapshots) {
      MetricSnapshot snapshot = escapeMetricSnapshot(s, scheme);
      if (!snapshot.getDataPoints().isEmpty()) {
        if (snapshot instanceof CounterSnapshot) {
          writeCounter(writer, (CounterSnapshot) snapshot, scheme);
        } else if (snapshot instanceof GaugeSnapshot) {
          writeGauge(writer, (GaugeSnapshot) snapshot, scheme);
        } else if (snapshot instanceof HistogramSnapshot) {
          writeHistogram(writer, (HistogramSnapshot) snapshot, scheme);
        } else if (snapshot instanceof SummarySnapshot) {
          writeSummary(writer, (SummarySnapshot) snapshot, scheme);
        } else if (snapshot instanceof InfoSnapshot) {
          writeInfo(writer, (InfoSnapshot) snapshot, scheme);
        } else if (snapshot instanceof StateSetSnapshot) {
          writeStateSet(writer, (StateSetSnapshot) snapshot, scheme);
        } else if (snapshot instanceof UnknownSnapshot) {
          writeUnknown(writer, (UnknownSnapshot) snapshot, scheme);
        }
      }
    }
    if (writeCreatedTimestamps) {
      for (MetricSnapshot s : metricSnapshots) {
        MetricSnapshot snapshot = escapeMetricSnapshot(s, scheme);
        if (!snapshot.getDataPoints().isEmpty()) {
          if (snapshot instanceof CounterSnapshot) {
            writeCreated(writer, snapshot, scheme);
          } else if (snapshot instanceof HistogramSnapshot) {
            writeCreated(writer, snapshot, scheme);
          } else if (snapshot instanceof SummarySnapshot) {
            writeCreated(writer, snapshot, scheme);
          }
        }
      }
    }
    writer.flush();
  }

  public void writeCreated(Writer writer, MetricSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    boolean metadataWritten = false;
    MetricMetadata metadata = snapshot.getMetadata();
    for (DataPointSnapshot data : snapshot.getDataPoints()) {
      if (data.hasCreatedTimestamp()) {
        if (!metadataWritten) {
          writeMetadata(writer, "_created", "gauge", metadata, scheme);
          metadataWritten = true;
        }
        writeNameAndLabels(
            writer, getMetadataName(metadata, scheme), "_created", data.getLabels(), scheme);
        writePrometheusTimestamp(writer, data.getCreatedTimestampMillis(), timestampsInMs);
        writeScrapeTimestampAndNewline(writer, data);
      }
    }
  }

  private void writeCounter(Writer writer, CounterSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    if (!snapshot.getDataPoints().isEmpty()) {
      MetricMetadata metadata = snapshot.getMetadata();
      writeMetadata(writer, "_total", "counter", metadata, scheme);
      for (CounterSnapshot.CounterDataPointSnapshot data : snapshot.getDataPoints()) {
        writeNameAndLabels(writer, getMetadataName(metadata, scheme), "_total", data.getLabels(), scheme);
        writeDouble(writer, data.getValue());
        writeScrapeTimestampAndNewline(writer, data);
      }
    }
  }

  private void writeGauge(Writer writer, GaugeSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "", "gauge", metadata, scheme);
    for (GaugeSnapshot.GaugeDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, getMetadataName(metadata, scheme), null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      writeScrapeTimestampAndNewline(writer, data);
    }
  }

  private void writeHistogram(Writer writer, HistogramSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "", "histogram", metadata, scheme);
    for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
      ClassicHistogramBuckets buckets = getClassicBuckets(data);
      long cumulativeCount = 0;
      for (int i = 0; i < buckets.size(); i++) {
        cumulativeCount += buckets.getCount(i);
        writeNameAndLabels(
            writer,
            getMetadataName(metadata, scheme),
            "_bucket",
            data.getLabels(),
            scheme,
            "le",
            buckets.getUpperBound(i));
        writeLong(writer, cumulativeCount);
        writeScrapeTimestampAndNewline(writer, data);
      }
      if (!snapshot.isGaugeHistogram()) {
        if (data.hasCount()) {
          writeNameAndLabels(
              writer, getMetadataName(metadata, scheme), "_count", data.getLabels(), scheme);
          writeLong(writer, data.getCount());
          writeScrapeTimestampAndNewline(writer, data);
        }
        if (data.hasSum()) {
          writeNameAndLabels(
              writer, getMetadataName(metadata, scheme), "_sum", data.getLabels(), scheme);
          writeDouble(writer, data.getSum());
          writeScrapeTimestampAndNewline(writer, data);
        }
      }
    }
    if (snapshot.isGaugeHistogram()) {
      writeGaugeCountSum(writer, snapshot, metadata, scheme);
    }
  }

  private ClassicHistogramBuckets getClassicBuckets(
      HistogramSnapshot.HistogramDataPointSnapshot data) {
    if (data.getClassicBuckets().isEmpty()) {
      return ClassicHistogramBuckets.of(
          new double[] {Double.POSITIVE_INFINITY}, new long[] {data.getCount()});
    } else {
      return data.getClassicBuckets();
    }
  }

  private void writeGaugeCountSum(
      Writer writer, HistogramSnapshot snapshot, MetricMetadata metadata, EscapingScheme scheme)
      throws IOException {
    // Prometheus text format does not support gaugehistogram's _gcount and _gsum.
    // So we append _gcount and _gsum as gauge metrics.
    boolean metadataWritten = false;
    for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
      if (data.hasCount()) {
        if (!metadataWritten) {
          writeMetadata(writer, "_gcount", "gauge", metadata, scheme);
          metadataWritten = true;
        }
        writeNameAndLabels(
            writer, getMetadataName(metadata, scheme), "_gcount", data.getLabels(), scheme);
        writeLong(writer, data.getCount());
        writeScrapeTimestampAndNewline(writer, data);
      }
    }
    metadataWritten = false;
    for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
      if (data.hasSum()) {
        if (!metadataWritten) {
          writeMetadata(writer, "_gsum", "gauge", metadata, scheme);
          metadataWritten = true;
        }
        writeNameAndLabels(
            writer, getMetadataName(metadata, scheme), "_gsum", data.getLabels(), scheme);
        writeDouble(writer, data.getSum());
        writeScrapeTimestampAndNewline(writer, data);
      }
    }
  }

  private void writeSummary(Writer writer, SummarySnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    boolean metadataWritten = false;
    MetricMetadata metadata = snapshot.getMetadata();
    for (SummarySnapshot.SummaryDataPointSnapshot data : snapshot.getDataPoints()) {
      if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) {
        continue;
      }
      if (!metadataWritten) {
        writeMetadata(writer, "", "summary", metadata, scheme);
        metadataWritten = true;
      }
      for (Quantile quantile : data.getQuantiles()) {
        writeNameAndLabels(
            writer,
            getMetadataName(metadata, scheme),
            null,
            data.getLabels(),
            scheme,
            "quantile",
            quantile.getQuantile());
        writeDouble(writer, quantile.getValue());
        writeScrapeTimestampAndNewline(writer, data);
      }
      if (data.hasCount()) {
        writeNameAndLabels(
            writer, getMetadataName(metadata, scheme), "_count", data.getLabels(), scheme);
        writeLong(writer, data.getCount());
        writeScrapeTimestampAndNewline(writer, data);
      }
      if (data.hasSum()) {
        writeNameAndLabels(
            writer, getMetadataName(metadata, scheme), "_sum", data.getLabels(), scheme);
        writeDouble(writer, data.getSum());
        writeScrapeTimestampAndNewline(writer, data);
      }
    }
  }

  private void writeInfo(Writer writer, InfoSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "_info", "gauge", metadata, scheme);
    for (InfoSnapshot.InfoDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(
          writer, getMetadataName(metadata, scheme), "_info", data.getLabels(), scheme);
      writer.write("1");
      writeScrapeTimestampAndNewline(writer, data);
    }
  }

  private void writeStateSet(Writer writer, StateSetSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "", "gauge", metadata, scheme);
    for (StateSetSnapshot.StateSetDataPointSnapshot data : snapshot.getDataPoints()) {
      for (int i = 0; i < data.size(); i++) {
        writer.write(getMetadataName(metadata, scheme));
        writer.write('{');
        for (int j = 0; j < data.getLabels().size(); j++) {
          if (j > 0) {
            writer.write(",");
          }
          writer.write(getSnapshotLabelName(data.getLabels(), j, scheme));
          writer.write("=\"");
          writeEscapedString(writer, data.getLabels().getValue(j));
          writer.write("\"");
        }
        if (!data.getLabels().isEmpty()) {
          writer.write(",");
        }
        writer.write(getMetadataName(metadata, scheme));
        writer.write("=\"");
        writeEscapedString(writer, data.getName(i));
        writer.write("\"} ");
        if (data.isTrue(i)) {
          writer.write("1");
        } else {
          writer.write("0");
        }
        writeScrapeTimestampAndNewline(writer, data);
      }
    }
  }

  private void writeUnknown(Writer writer, UnknownSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "", "untyped", metadata, scheme);
    for (UnknownSnapshot.UnknownDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, getMetadataName(metadata, scheme), null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      writeScrapeTimestampAndNewline(writer, data);
    }
  }

  private void writeNameAndLabels(
      Writer writer, String name, String suffix, Labels labels, EscapingScheme escapingScheme)
      throws IOException {
    writeNameAndLabels(writer, name, suffix, labels, escapingScheme, null, 0.0);
  }

  private void writeNameAndLabels(
      Writer writer,
      String name,
      String suffix,
      Labels labels,
      EscapingScheme scheme,
      String additionalLabelName,
      double additionalLabelValue)
      throws IOException {
    boolean metricInsideBraces = false;
    // If the name does not pass the legacy validity check, we must put the
    // metric name inside the braces.
    if (!PrometheusNaming.isValidLegacyLabelName(name)) {
      metricInsideBraces = true;
      writer.write('{');
    }
    writeName(writer, name + (suffix != null ? suffix : ""), NameType.Metric);
    if (!labels.isEmpty() || additionalLabelName != null) {
      writeLabels(
          writer,
          labels,
          additionalLabelName,
          additionalLabelValue,
          metricInsideBraces,
          scheme);
    } else if (metricInsideBraces) {
      writer.write('}');
    }
    writer.write(' ');
  }

  private void writeMetadata(
      Writer writer,
      String suffix,
      String typeString,
      MetricMetadata metadata,
      EscapingScheme scheme)
      throws IOException {
    String name = getMetadataName(metadata, scheme) + (suffix != null ? suffix : "");
    if (metadata.getHelp() != null && !metadata.getHelp().isEmpty()) {
      writer.write("# HELP ");
      writeName(writer, name, NameType.Metric);
      writer.write(' ');
      writeEscapedHelp(writer, metadata.getHelp());
      writer.write('\n');
    }
    writer.write("# TYPE ");
    writeName(writer, name, NameType.Metric);
    writer.write(' ');
    writer.write(typeString);
    writer.write('\n');
  }

  private void writeEscapedHelp(Writer writer, String s) throws IOException {
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

  private void writeScrapeTimestampAndNewline(Writer writer, DataPointSnapshot data)
      throws IOException {
    if (data.hasScrapeTimestamp()) {
      writer.write(' ');
      writePrometheusTimestamp(writer, data.getScrapeTimestampMillis(), timestampsInMs);
    }
    writer.write('\n');
  }
}
