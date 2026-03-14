package io.prometheus.metrics.expositionformats;

import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeDouble;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeEscapedString;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLabels;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLong;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeName;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeOpenMetricsTimestamp;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getExpositionBaseMetadataName;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getMetadataName;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getSnapshotLabelName;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.DistributionDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.SnapshotEscaper;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Write the OpenMetrics text format as defined on <a
 * href="https://openmetrics.io/">https://openmetrics.io</a>.
 */
public class OpenMetricsTextFormatWriter implements ExpositionFormatWriter {

  public static class Builder {
    boolean createdTimestampsEnabled;
    boolean exemplarsOnAllMetricTypesEnabled;

    private Builder() {}

    /**
     * @param createdTimestampsEnabled whether to include the _created timestamp in the output
     */
    public Builder setCreatedTimestampsEnabled(boolean createdTimestampsEnabled) {
      this.createdTimestampsEnabled = createdTimestampsEnabled;
      return this;
    }

    /**
     * @param exemplarsOnAllMetricTypesEnabled whether to include exemplars in the output for all
     *     metric types
     */
    public Builder setExemplarsOnAllMetricTypesEnabled(boolean exemplarsOnAllMetricTypesEnabled) {
      this.exemplarsOnAllMetricTypesEnabled = exemplarsOnAllMetricTypesEnabled;
      return this;
    }

    public OpenMetricsTextFormatWriter build() {
      return new OpenMetricsTextFormatWriter(
          createdTimestampsEnabled, exemplarsOnAllMetricTypesEnabled);
    }
  }

  public static final String CONTENT_TYPE =
      "application/openmetrics-text; version=1.0.0; charset=utf-8";
  private final boolean createdTimestampsEnabled;
  private final boolean exemplarsOnAllMetricTypesEnabled;

  /**
   * @param createdTimestampsEnabled whether to include the _created timestamp in the output - This
   *     will produce an invalid OpenMetrics output, but is kept for backwards compatibility.
   */
  public OpenMetricsTextFormatWriter(
      boolean createdTimestampsEnabled, boolean exemplarsOnAllMetricTypesEnabled) {
    this.createdTimestampsEnabled = createdTimestampsEnabled;
    this.exemplarsOnAllMetricTypesEnabled = exemplarsOnAllMetricTypesEnabled;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static OpenMetricsTextFormatWriter create() {
    return builder().build();
  }

  @Override
  public boolean accepts(@Nullable String acceptHeader) {
    if (acceptHeader == null) {
      return false;
    }
    return acceptHeader.contains("application/openmetrics-text");
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void write(OutputStream out, MetricSnapshots metricSnapshots, EscapingScheme scheme)
      throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    MetricSnapshots merged = TextFormatUtil.mergeDuplicates(metricSnapshots);
    for (MetricSnapshot s : merged) {
      MetricSnapshot snapshot = SnapshotEscaper.escapeMetricSnapshot(s, scheme);
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
    writer.write("# EOF\n");
    writer.flush();
  }

  private void writeCounter(Writer writer, CounterSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    String counterName = resolveExpositionName(metadata, "_total", scheme);
    String baseName = resolveBaseName(counterName, "_total");
    writeMetadataWithName(writer, baseName, "counter", metadata);
    for (CounterSnapshot.CounterDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, counterName, null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      writeScrapeTimestampAndExemplar(writer, data, data.getExemplar(), scheme);
      writeCreated(writer, baseName, data, scheme);
    }
  }

  private void writeGauge(Writer writer, GaugeSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "gauge", metadata, scheme);
    for (GaugeSnapshot.GaugeDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, getMetadataName(metadata, scheme), null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      if (exemplarsOnAllMetricTypesEnabled) {
        writeScrapeTimestampAndExemplar(writer, data, data.getExemplar(), scheme);
      } else {
        writeScrapeTimestampAndExemplar(writer, data, null, scheme);
      }
    }
  }

  private void writeHistogram(Writer writer, HistogramSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    if (snapshot.isGaugeHistogram()) {
      writeMetadata(writer, "gaugehistogram", metadata, scheme);
      writeClassicHistogramBuckets(
          writer, metadata, "_gcount", "_gsum", snapshot.getDataPoints(), scheme);
    } else {
      writeMetadata(writer, "histogram", metadata, scheme);
      writeClassicHistogramBuckets(
          writer, metadata, "_count", "_sum", snapshot.getDataPoints(), scheme);
    }
  }

  private void writeClassicHistogramBuckets(
      Writer writer,
      MetricMetadata metadata,
      String countSuffix,
      String sumSuffix,
      List<HistogramSnapshot.HistogramDataPointSnapshot> dataList,
      EscapingScheme scheme)
      throws IOException {
    for (HistogramSnapshot.HistogramDataPointSnapshot data : dataList) {
      ClassicHistogramBuckets buckets = getClassicBuckets(data);
      Exemplars exemplars = data.getExemplars();
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
        Exemplar exemplar;
        if (i == 0) {
          exemplar = exemplars.get(Double.NEGATIVE_INFINITY, buckets.getUpperBound(i));
        } else {
          exemplar = exemplars.get(buckets.getUpperBound(i - 1), buckets.getUpperBound(i));
        }
        writeScrapeTimestampAndExemplar(writer, data, exemplar, scheme);
      }
      // In OpenMetrics format, histogram _count and _sum are either both present or both absent.
      if (data.hasCount() && data.hasSum()) {
        writeCountAndSum(writer, metadata, data, countSuffix, sumSuffix, exemplars, scheme);
      }
      writeCreated(writer, metadata, data, scheme);
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

  private void writeSummary(Writer writer, SummarySnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    boolean metadataWritten = false;
    MetricMetadata metadata = snapshot.getMetadata();
    for (SummarySnapshot.SummaryDataPointSnapshot data : snapshot.getDataPoints()) {
      if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) {
        continue;
      }
      if (!metadataWritten) {
        writeMetadata(writer, "summary", metadata, scheme);
        metadataWritten = true;
      }
      Exemplars exemplars = data.getExemplars();
      // Exemplars for summaries are new, and there's no best practice yet which Exemplars to choose
      // for which
      // time series. We select exemplars[0] for _count, exemplars[1] for _sum, and exemplars[2...]
      // for the
      // quantiles, all indexes modulo exemplars.length.
      int exemplarIndex = 1;
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
        if (exemplars.size() > 0 && exemplarsOnAllMetricTypesEnabled) {
          exemplarIndex = (exemplarIndex + 1) % exemplars.size();
          writeScrapeTimestampAndExemplar(writer, data, exemplars.get(exemplarIndex), scheme);
        } else {
          writeScrapeTimestampAndExemplar(writer, data, null, scheme);
        }
      }
      // Unlike histograms, summaries can have only a count or only a sum according to OpenMetrics.
      writeCountAndSum(writer, metadata, data, "_count", "_sum", exemplars, scheme);
      writeCreated(writer, metadata, data, scheme);
    }
  }

  private void writeInfo(Writer writer, InfoSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    String infoName = resolveExpositionName(metadata, "_info", scheme);
    String baseName = resolveBaseName(infoName, "_info");
    writeMetadataWithName(writer, baseName, "info", metadata);
    for (InfoSnapshot.InfoDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, infoName, null, data.getLabels(), scheme);
      writer.write("1");
      writeScrapeTimestampAndExemplar(writer, data, null, scheme);
    }
  }

  private void writeStateSet(Writer writer, StateSetSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "stateset", metadata, scheme);
    for (StateSetSnapshot.StateSetDataPointSnapshot data : snapshot.getDataPoints()) {
      for (int i = 0; i < data.size(); i++) {
        writer.write(getMetadataName(metadata, scheme));
        writer.write('{');
        Labels labels = data.getLabels();
        for (int j = 0; j < labels.size(); j++) {
          if (j > 0) {
            writer.write(",");
          }
          writer.write(getSnapshotLabelName(labels, j, scheme));
          writer.write("=\"");
          writeEscapedString(writer, labels.getValue(j));
          writer.write("\"");
        }
        if (!labels.isEmpty()) {
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
        writeScrapeTimestampAndExemplar(writer, data, null, scheme);
      }
    }
  }

  private void writeUnknown(Writer writer, UnknownSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    writeMetadata(writer, "unknown", metadata, scheme);
    for (UnknownSnapshot.UnknownDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, getMetadataName(metadata, scheme), null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      if (exemplarsOnAllMetricTypesEnabled) {
        writeScrapeTimestampAndExemplar(writer, data, data.getExemplar(), scheme);
      } else {
        writeScrapeTimestampAndExemplar(writer, data, null, scheme);
      }
    }
  }

  private void writeCountAndSum(
      Writer writer,
      MetricMetadata metadata,
      DistributionDataPointSnapshot data,
      String countSuffix,
      String sumSuffix,
      Exemplars exemplars,
      EscapingScheme scheme)
      throws IOException {
    if (data.hasCount()) {
      writeNameAndLabels(
          writer, getMetadataName(metadata, scheme), countSuffix, data.getLabels(), scheme);
      writeLong(writer, data.getCount());
      if (exemplarsOnAllMetricTypesEnabled) {
        writeScrapeTimestampAndExemplar(writer, data, exemplars.getLatest(), scheme);
      } else {
        writeScrapeTimestampAndExemplar(writer, data, null, scheme);
      }
    }
    if (data.hasSum()) {
      writeNameAndLabels(
          writer, getMetadataName(metadata, scheme), sumSuffix, data.getLabels(), scheme);
      writeDouble(writer, data.getSum());
      writeScrapeTimestampAndExemplar(writer, data, null, scheme);
    }
  }

  private void writeCreated(
      Writer writer, MetricMetadata metadata, DataPointSnapshot data, EscapingScheme scheme)
      throws IOException {
    writeCreated(writer, getMetadataName(metadata, scheme), data, scheme);
  }

  private void writeCreated(
      Writer writer, String baseName, DataPointSnapshot data, EscapingScheme scheme)
      throws IOException {
    if (createdTimestampsEnabled && data.hasCreatedTimestamp()) {
      writeNameAndLabels(writer, baseName, "_created", data.getLabels(), scheme);
      writeOpenMetricsTimestamp(writer, data.getCreatedTimestampMillis());
      if (data.hasScrapeTimestamp()) {
        writer.write(' ');
        writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
      }
      writer.write('\n');
    }
  }

  private void writeNameAndLabels(
      Writer writer,
      String name,
      @Nullable String suffix,
      Labels labels,
      EscapingScheme escapingScheme)
      throws IOException {
    writeNameAndLabels(writer, name, suffix, labels, escapingScheme, null, 0.0);
  }

  private void writeNameAndLabels(
      Writer writer,
      String name,
      @Nullable String suffix,
      Labels labels,
      EscapingScheme escapingScheme,
      @Nullable String additionalLabelName,
      double additionalLabelValue)
      throws IOException {
    boolean metricInsideBraces = false;
    // If the name does not pass the legacy validity check, we must put the
    // metric name inside the braces.
    if (!PrometheusNaming.isValidLegacyMetricName(name)) {
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
          escapingScheme);
    } else if (metricInsideBraces) {
      writer.write('}');
    }
    writer.write(' ');
  }

  private void writeScrapeTimestampAndExemplar(
      Writer writer, DataPointSnapshot data, @Nullable Exemplar exemplar, EscapingScheme scheme)
      throws IOException {
    if (data.hasScrapeTimestamp()) {
      writer.write(' ');
      writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
    }
    if (exemplar != null) {
      writer.write(" # ");
      writeLabels(writer, exemplar.getLabels(), null, 0, false, scheme);
      writer.write(' ');
      writeDouble(writer, exemplar.getValue());
      if (exemplar.hasTimestamp()) {
        writer.write(' ');
        writeOpenMetricsTimestamp(writer, exemplar.getTimestampMillis());
      }
    }
    writer.write('\n');
  }

  /**
   * Returns the full exposition name for a metric. If the original name already ends with the given
   * suffix (e.g. "_total" for counters), uses the original name directly. Otherwise, appends the
   * suffix to the base name.
   */
  private static String resolveExpositionName(
      MetricMetadata metadata, String suffix, EscapingScheme scheme) {
    String expositionBaseName = getExpositionBaseMetadataName(metadata, scheme);
    if (expositionBaseName.endsWith(suffix)) {
      return expositionBaseName;
    }
    return getMetadataName(metadata, scheme) + suffix;
  }

  private void writeMetadata(
      Writer writer, String typeName, MetricMetadata metadata, EscapingScheme scheme)
      throws IOException {
    writeMetadataWithName(writer, getMetadataName(metadata, scheme), typeName, metadata);
  }

  private void writeMetadataWithName(
      Writer writer, String name, String typeName, MetricMetadata metadata) throws IOException {
    writer.write("# TYPE ");
    writeName(writer, name, NameType.Metric);
    writer.write(' ');
    writer.write(typeName);
    writer.write('\n');
    if (metadata.getUnit() != null) {
      writer.write("# UNIT ");
      writeName(writer, name, NameType.Metric);
      writer.write(' ');
      writeEscapedString(writer, metadata.getUnit().toString());
      writer.write('\n');
    }
    if (metadata.getHelp() != null && !metadata.getHelp().isEmpty()) {
      writer.write("# HELP ");
      writeName(writer, name, NameType.Metric);
      writer.write(' ');
      writeEscapedString(writer, metadata.getHelp());
      writer.write('\n');
    }
  }

  private static String resolveBaseName(String fullName, String suffix) {
    if (fullName.endsWith(suffix)) {
      return fullName.substring(0, fullName.length() - suffix.length());
    }
    return fullName;
  }
}
