package io.prometheus.metrics.expositionformats;

import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeDouble;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeEscapedString;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLabels;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLong;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeName;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeOpenMetricsTimestamp;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getExpositionBaseMetadataName;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getSnapshotLabelName;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.config.OpenMetrics2Properties;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
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
import javax.annotation.Nullable;

/**
 * Write the OpenMetrics 2.0 text format. Unlike the OM1 writer, this writer outputs metric names as
 * provided by the user — no {@code _total} or unit suffix appending. The {@code _info} suffix is
 * enforced per the OM2 spec (MUST). This is experimental and subject to change as the <a
 * href="https://github.com/prometheus/docs/blob/main/docs/specs/om/open_metrics_spec_2_0.md">OpenMetrics
 * 2.0 specification</a> evolves.
 */
public class OpenMetrics2TextFormatWriter implements ExpositionFormatWriter {

  public static class Builder {
    private OpenMetrics2Properties openMetrics2Properties =
        OpenMetrics2Properties.builder().build();
    boolean createdTimestampsEnabled;
    boolean exemplarsOnAllMetricTypesEnabled;

    private Builder() {}

    /**
     * @param openMetrics2Properties OpenMetrics 2.0 feature flags
     */
    public Builder setOpenMetrics2Properties(OpenMetrics2Properties openMetrics2Properties) {
      this.openMetrics2Properties = openMetrics2Properties;
      return this;
    }

    /**
     * @param createdTimestampsEnabled whether delegated OM1 output includes _created metrics
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

    public OpenMetrics2TextFormatWriter build() {
      return new OpenMetrics2TextFormatWriter(
          openMetrics2Properties, createdTimestampsEnabled, exemplarsOnAllMetricTypesEnabled);
    }
  }

  public static final String CONTENT_TYPE =
      "application/openmetrics-text; version=2.0.0; charset=utf-8";
  private final OpenMetrics2Properties openMetrics2Properties;
  private final boolean exemplarsOnAllMetricTypesEnabled;
  private final OpenMetricsTextFormatWriter om1Writer;

  /**
   * @param openMetrics2Properties OpenMetrics 2.0 feature flags
   * @param createdTimestampsEnabled whether delegated OM1 output includes _created metrics
   * @param exemplarsOnAllMetricTypesEnabled whether to include exemplars on all metric types
   */
  public OpenMetrics2TextFormatWriter(
      OpenMetrics2Properties openMetrics2Properties,
      boolean createdTimestampsEnabled,
      boolean exemplarsOnAllMetricTypesEnabled) {
    this.openMetrics2Properties = openMetrics2Properties;
    this.exemplarsOnAllMetricTypesEnabled = exemplarsOnAllMetricTypesEnabled;
    this.om1Writer =
        new OpenMetricsTextFormatWriter(createdTimestampsEnabled, exemplarsOnAllMetricTypesEnabled);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static OpenMetrics2TextFormatWriter create() {
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
    // When contentNegotiation=false (default), masquerade as OM1 for compatibility.
    // When contentNegotiation=true, use proper OM2 version.
    if (openMetrics2Properties.getContentNegotiation()) {
      return CONTENT_TYPE;
    } else {
      return OpenMetricsTextFormatWriter.CONTENT_TYPE;
    }
  }

  public OpenMetrics2Properties getOpenMetrics2Properties() {
    return openMetrics2Properties;
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
    // OM2: use the name as provided by the user, no _total appending
    String counterName = getExpositionBaseMetadataName(metadata, scheme);
    writeMetadataWithName(writer, counterName, "counter", metadata);
    for (CounterSnapshot.CounterDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, counterName, null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      if (data.hasScrapeTimestamp()) {
        writer.write(' ');
        writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
      }
      if (data.hasCreatedTimestamp()) {
        writer.write(" st@");
        writeOpenMetricsTimestamp(writer, data.getCreatedTimestampMillis());
      }
      writeExemplar(writer, data.getExemplar(), scheme);
      writer.write('\n');
    }
  }

  private void writeGauge(Writer writer, GaugeSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    String name = getExpositionBaseMetadataName(metadata, scheme);
    writeMetadataWithName(writer, name, "gauge", metadata);
    for (GaugeSnapshot.GaugeDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, name, null, data.getLabels(), scheme);
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
    boolean compositeHistogram =
        openMetrics2Properties.getCompositeValues() || openMetrics2Properties.getNativeHistograms();
    if (!compositeHistogram && !openMetrics2Properties.getExemplarCompliance()) {
      om1Writer.writeHistogram(writer, snapshot, scheme);
      return;
    }
    MetricMetadata metadata = snapshot.getMetadata();
    String name = getExpositionBaseMetadataName(metadata, scheme);
    if (snapshot.isGaugeHistogram()) {
      writeMetadataWithName(writer, name, "gaugehistogram", metadata);
      for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
        if (openMetrics2Properties.getNativeHistograms() && data.hasNativeHistogramData()) {
          writeNativeHistogramDataPoint(writer, name, "gcount", "gsum", data, scheme, false);
        } else {
          writeCompositeHistogramDataPoint(writer, name, "gcount", "gsum", data, scheme, false);
        }
      }
    } else {
      writeMetadataWithName(writer, name, "histogram", metadata);
      for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
        if (openMetrics2Properties.getNativeHistograms() && data.hasNativeHistogramData()) {
          writeNativeHistogramDataPoint(writer, name, "count", "sum", data, scheme, true);
        } else {
          writeCompositeHistogramDataPoint(writer, name, "count", "sum", data, scheme, true);
        }
      }
    }
  }

  private void writeCompositeHistogramDataPoint(
      Writer writer,
      String name,
      String countKey,
      String sumKey,
      HistogramSnapshot.HistogramDataPointSnapshot data,
      EscapingScheme scheme,
      boolean includeStartTimestamp)
      throws IOException {
    writeNameAndLabels(writer, name, null, data.getLabels(), scheme);
    writer.write('{');
    writer.write(countKey);
    writer.write(':');
    writeLong(writer, data.getCount());
    writer.write(',');
    writer.write(sumKey);
    writer.write(':');
    writeDouble(writer, data.getSum());
    writeClassicBucketsField(writer, data);
    writer.write('}');
    if (data.hasScrapeTimestamp()) {
      writer.write(' ');
      writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
    }
    if (includeStartTimestamp && data.hasCreatedTimestamp()) {
      writer.write(" st@");
      writeOpenMetricsTimestamp(writer, data.getCreatedTimestampMillis());
    }
    writeExemplars(writer, data.getExemplars(), scheme);
    writer.write('\n');
  }

  private void writeNativeHistogramDataPoint(
      Writer writer,
      String name,
      String countKey,
      String sumKey,
      HistogramSnapshot.HistogramDataPointSnapshot data,
      EscapingScheme scheme,
      boolean includeStartTimestamp)
      throws IOException {
    writeNameAndLabels(writer, name, null, data.getLabels(), scheme);
    writer.write('{');
    writer.write(countKey);
    writer.write(':');
    writeLong(writer, data.getCount());
    writer.write(',');
    writer.write(sumKey);
    writer.write(':');
    writeDouble(writer, data.getSum());
    writer.write(",schema:");
    writer.write(Integer.toString(data.getNativeSchema()));
    writer.write(",zero_threshold:");
    writeDouble(writer, data.getNativeZeroThreshold());
    writer.write(",zero_count:");
    writeLong(writer, data.getNativeZeroCount());
    writeNativeBucketFields(writer, "negative", data.getNativeBucketsForNegativeValues());
    writeNativeBucketFields(writer, "positive", data.getNativeBucketsForPositiveValues());
    if (data.hasClassicHistogramData()) {
      writeClassicBucketsField(writer, data);
    }
    writer.write('}');
    if (data.hasScrapeTimestamp()) {
      writer.write(' ');
      writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
    }
    if (includeStartTimestamp && data.hasCreatedTimestamp()) {
      writer.write(" st@");
      writeOpenMetricsTimestamp(writer, data.getCreatedTimestampMillis());
    }
    writeExemplars(writer, data.getExemplars(), scheme);
    writer.write('\n');
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

  private void writeClassicBucketsField(
      Writer writer, HistogramSnapshot.HistogramDataPointSnapshot data) throws IOException {
    writer.write(",bucket:[");
    ClassicHistogramBuckets buckets = getClassicBuckets(data);
    long cumulativeCount = 0;
    for (int i = 0; i < buckets.size(); i++) {
      if (i > 0) {
        writer.write(',');
      }
      cumulativeCount += buckets.getCount(i);
      writeDouble(writer, buckets.getUpperBound(i));
      writer.write(':');
      writeLong(writer, cumulativeCount);
    }
    writer.write(']');
  }

  private void writeNativeBucketFields(Writer writer, String prefix, NativeHistogramBuckets buckets)
      throws IOException {
    if (buckets.size() == 0) {
      return;
    }
    writer.write(',');
    writer.write(prefix);
    writer.write("_spans:[");
    writeNativeBucketSpans(writer, buckets);
    writer.write("],");
    writer.write(prefix);
    writer.write("_buckets:[");
    for (int i = 0; i < buckets.size(); i++) {
      if (i > 0) {
        writer.write(',');
      }
      writeLong(writer, buckets.getCount(i));
    }
    writer.write(']');
  }

  private void writeNativeBucketSpans(Writer writer, NativeHistogramBuckets buckets)
      throws IOException {
    int spanOffset = buckets.getBucketIndex(0);
    int spanLength = 1;
    int previousIndex = buckets.getBucketIndex(0);
    boolean firstSpan = true;
    for (int i = 1; i < buckets.size(); i++) {
      int bucketIndex = buckets.getBucketIndex(i);
      if (bucketIndex == previousIndex + 1) {
        spanLength++;
      } else {
        firstSpan = writeNativeBucketSpan(writer, spanOffset, spanLength, firstSpan);
        spanOffset = bucketIndex - previousIndex - 1;
        spanLength = 1;
      }
      previousIndex = bucketIndex;
    }
    writeNativeBucketSpan(writer, spanOffset, spanLength, firstSpan);
  }

  private boolean writeNativeBucketSpan(Writer writer, int offset, int length, boolean firstSpan)
      throws IOException {
    if (!firstSpan) {
      writer.write(',');
    }
    writer.write(Integer.toString(offset));
    writer.write(':');
    writer.write(Integer.toString(length));
    return false;
  }

  private void writeSummary(Writer writer, SummarySnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    if (!openMetrics2Properties.getCompositeValues()
        && !openMetrics2Properties.getExemplarCompliance()) {
      om1Writer.writeSummary(writer, snapshot, scheme);
      return;
    }
    boolean metadataWritten = false;
    MetricMetadata metadata = snapshot.getMetadata();
    String name = getExpositionBaseMetadataName(metadata, scheme);
    for (SummarySnapshot.SummaryDataPointSnapshot data : snapshot.getDataPoints()) {
      if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) {
        continue;
      }
      if (!metadataWritten) {
        writeMetadataWithName(writer, name, "summary", metadata);
        metadataWritten = true;
      }
      writeCompositeSummaryDataPoint(writer, name, data, scheme);
    }
  }

  private void writeCompositeSummaryDataPoint(
      Writer writer,
      String name,
      SummarySnapshot.SummaryDataPointSnapshot data,
      EscapingScheme scheme)
      throws IOException {
    writeNameAndLabels(writer, name, null, data.getLabels(), scheme);
    writer.write('{');
    boolean first = true;
    if (data.hasCount()) {
      writer.write("count:");
      writeLong(writer, data.getCount());
      first = false;
    }
    if (data.hasSum()) {
      if (!first) {
        writer.write(',');
      }
      writer.write("sum:");
      writeDouble(writer, data.getSum());
      first = false;
    }
    if (!first) {
      writer.write(',');
    }
    writer.write("quantile:[");
    for (int i = 0; i < data.getQuantiles().size(); i++) {
      if (i > 0) {
        writer.write(',');
      }
      Quantile q = data.getQuantiles().get(i);
      writeDouble(writer, q.getQuantile());
      writer.write(':');
      writeDouble(writer, q.getValue());
    }
    writer.write(']');
    writer.write('}');
    if (data.hasScrapeTimestamp()) {
      writer.write(' ');
      writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
    }
    if (data.hasCreatedTimestamp()) {
      writer.write(" st@");
      writeOpenMetricsTimestamp(writer, data.getCreatedTimestampMillis());
    }
    writeExemplars(writer, data.getExemplars(), scheme);
    writer.write('\n');
  }

  private void writeInfo(Writer writer, InfoSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    // OM2 spec: Info MetricFamily name MUST end in _info.
    // In OM2, TYPE/HELP use the same name as the data lines.
    String infoName = ensureSuffix(getExpositionBaseMetadataName(metadata, scheme), "_info");
    writeMetadataWithName(writer, infoName, "info", metadata);
    for (InfoSnapshot.InfoDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, infoName, null, data.getLabels(), scheme);
      writer.write("1");
      writeScrapeTimestampAndExemplar(writer, data, null, scheme);
    }
  }

  private void writeStateSet(Writer writer, StateSetSnapshot snapshot, EscapingScheme scheme)
      throws IOException {
    MetricMetadata metadata = snapshot.getMetadata();
    String name = getExpositionBaseMetadataName(metadata, scheme);
    writeMetadataWithName(writer, name, "stateset", metadata);
    for (StateSetSnapshot.StateSetDataPointSnapshot data : snapshot.getDataPoints()) {
      for (int i = 0; i < data.size(); i++) {
        writer.write(name);
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
        writer.write(name);
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
    String name = getExpositionBaseMetadataName(metadata, scheme);
    writeMetadataWithName(writer, name, "unknown", metadata);
    for (UnknownSnapshot.UnknownDataPointSnapshot data : snapshot.getDataPoints()) {
      writeNameAndLabels(writer, name, null, data.getLabels(), scheme);
      writeDouble(writer, data.getValue());
      if (exemplarsOnAllMetricTypesEnabled) {
        writeScrapeTimestampAndExemplar(writer, data, data.getExemplar(), scheme);
      } else {
        writeScrapeTimestampAndExemplar(writer, data, null, scheme);
      }
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
    if (!openMetrics2Properties.getExemplarCompliance()) {
      om1Writer.writeScrapeTimestampAndExemplar(writer, data, exemplar, scheme);
      return;
    }
    if (data.hasScrapeTimestamp()) {
      writer.write(' ');
      writeOpenMetricsTimestamp(writer, data.getScrapeTimestampMillis());
    }
    writeExemplar(writer, exemplar, scheme);
    writer.write('\n');
  }

  private void writeExemplar(Writer writer, @Nullable Exemplar exemplar, EscapingScheme scheme)
      throws IOException {
    if (exemplar == null) {
      return;
    }
    if (!openMetrics2Properties.getExemplarCompliance()) {
      om1Writer.writeExemplar(writer, exemplar, scheme);
      return;
    }
    // exemplarCompliance=true: exemplars MUST have a timestamp per the OM2 spec.
    if (exemplar.hasTimestamp()) {
      om1Writer.writeExemplar(writer, exemplar, scheme);
    }
  }

  private void writeExemplars(Writer writer, Exemplars exemplars, EscapingScheme scheme)
      throws IOException {
    for (Exemplar exemplar : exemplars) {
      writeExemplar(writer, exemplar, scheme);
    }
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

  private static String ensureSuffix(String name, String suffix) {
    if (name.endsWith(suffix)) {
      return name;
    }
    return name + suffix;
  }
}
