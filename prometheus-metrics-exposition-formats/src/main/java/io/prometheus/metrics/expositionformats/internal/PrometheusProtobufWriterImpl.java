package io.prometheus.metrics.expositionformats.internal;

import static io.prometheus.metrics.expositionformats.internal.ProtobufUtil.timestampFromMillis;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getSnapshotLabelName;

import com.google.protobuf.TextFormat;
import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_32_1.Metrics;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SnapshotEscaper;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

public class PrometheusProtobufWriterImpl implements ExpositionFormatWriter {

  @Override
  public boolean accepts(@Nullable String acceptHeader) {
    throw new IllegalStateException("use PrometheusProtobufWriter instead");
  }

  @Override
  public String getContentType() {
    throw new IllegalStateException("use PrometheusProtobufWriter instead");
  }

  @Override
  public String toDebugString(MetricSnapshots metricSnapshots, EscapingScheme escapingScheme) {
    StringBuilder stringBuilder = new StringBuilder();
    for (MetricSnapshot s : metricSnapshots) {
      MetricSnapshot snapshot = SnapshotEscaper.escapeMetricSnapshot(s, escapingScheme);
      if (!snapshot.getDataPoints().isEmpty()) {
        stringBuilder.append(TextFormat.printer().printToString(convert(snapshot, escapingScheme)));
      }
    }
    return stringBuilder.toString();
  }

  @Override
  public void write(
      OutputStream out, MetricSnapshots metricSnapshots, EscapingScheme escapingScheme)
      throws IOException {
    for (MetricSnapshot s : metricSnapshots) {
      MetricSnapshot snapshot = SnapshotEscaper.escapeMetricSnapshot(s, escapingScheme);
      if (!snapshot.getDataPoints().isEmpty()) {
        convert(snapshot, escapingScheme).writeDelimitedTo(out);
      }
    }
  }

  public Metrics.MetricFamily convert(MetricSnapshot snapshot, EscapingScheme scheme) {
    Metrics.MetricFamily.Builder builder = Metrics.MetricFamily.newBuilder();
    if (snapshot instanceof CounterSnapshot) {
      for (CounterDataPointSnapshot data : ((CounterSnapshot) snapshot).getDataPoints()) {
        builder.addMetric(convert(data, scheme));
      }
      setMetadataUnlessEmpty(
          builder, snapshot.getMetadata(), "_total", Metrics.MetricType.COUNTER, scheme);
    } else if (snapshot instanceof GaugeSnapshot) {
      for (GaugeSnapshot.GaugeDataPointSnapshot data : ((GaugeSnapshot) snapshot).getDataPoints()) {
        builder.addMetric(convert(data, scheme));
      }
      setMetadataUnlessEmpty(
          builder, snapshot.getMetadata(), null, Metrics.MetricType.GAUGE, scheme);
    } else if (snapshot instanceof HistogramSnapshot) {
      HistogramSnapshot histogram = (HistogramSnapshot) snapshot;
      for (HistogramSnapshot.HistogramDataPointSnapshot data : histogram.getDataPoints()) {
        builder.addMetric(convert(data, scheme));
      }
      Metrics.MetricType type =
          histogram.isGaugeHistogram()
              ? Metrics.MetricType.GAUGE_HISTOGRAM
              : Metrics.MetricType.HISTOGRAM;
      setMetadataUnlessEmpty(builder, snapshot.getMetadata(), null, type, scheme);
    } else if (snapshot instanceof SummarySnapshot) {
      for (SummarySnapshot.SummaryDataPointSnapshot data :
          ((SummarySnapshot) snapshot).getDataPoints()) {
        if (data.hasCount() || data.hasSum() || data.getQuantiles().size() > 0) {
          builder.addMetric(convert(data, scheme));
        }
      }
      setMetadataUnlessEmpty(
          builder, snapshot.getMetadata(), null, Metrics.MetricType.SUMMARY, scheme);
    } else if (snapshot instanceof InfoSnapshot) {
      for (InfoSnapshot.InfoDataPointSnapshot data : ((InfoSnapshot) snapshot).getDataPoints()) {
        builder.addMetric(convert(data, scheme));
      }
      setMetadataUnlessEmpty(
          builder, snapshot.getMetadata(), "_info", Metrics.MetricType.GAUGE, scheme);
    } else if (snapshot instanceof StateSetSnapshot) {
      for (StateSetSnapshot.StateSetDataPointSnapshot data :
          ((StateSetSnapshot) snapshot).getDataPoints()) {
        for (int i = 0; i < data.size(); i++) {
          builder.addMetric(convert(data, snapshot.getMetadata().getPrometheusName(), i, scheme));
        }
      }
      setMetadataUnlessEmpty(
          builder, snapshot.getMetadata(), null, Metrics.MetricType.GAUGE, scheme);
    } else if (snapshot instanceof UnknownSnapshot) {
      for (UnknownSnapshot.UnknownDataPointSnapshot data :
          ((UnknownSnapshot) snapshot).getDataPoints()) {
        builder.addMetric(convert(data, scheme));
      }
      setMetadataUnlessEmpty(
          builder, snapshot.getMetadata(), null, Metrics.MetricType.UNTYPED, scheme);
    }
    return builder.build();
  }

  private Metrics.Metric.Builder convert(CounterDataPointSnapshot data, EscapingScheme scheme) {
    Metrics.Counter.Builder counterBuilder = Metrics.Counter.newBuilder();
    counterBuilder.setValue(data.getValue());
    if (data.getExemplar() != null) {
      counterBuilder.setExemplar(convert(data.getExemplar(), scheme));
    }
    if (data.hasCreatedTimestamp()) {
      counterBuilder.setCreatedTimestamp(
          ProtobufUtil.timestampFromMillis(data.getCreatedTimestampMillis()));
    }
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    addLabels(metricBuilder, data.getLabels(), scheme);
    metricBuilder.setCounter(counterBuilder.build());
    setScrapeTimestamp(metricBuilder, data);
    return metricBuilder;
  }

  private Metrics.Metric.Builder convert(
      GaugeSnapshot.GaugeDataPointSnapshot data, EscapingScheme scheme) {
    Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
    gaugeBuilder.setValue(data.getValue());
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    addLabels(metricBuilder, data.getLabels(), scheme);
    metricBuilder.setGauge(gaugeBuilder);
    setScrapeTimestamp(metricBuilder, data);
    return metricBuilder;
  }

  private Metrics.Metric.Builder convert(
      HistogramSnapshot.HistogramDataPointSnapshot data, EscapingScheme scheme) {
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
    if (data.hasNativeHistogramData()) {
      histogramBuilder.setSchema(data.getNativeSchema());
      histogramBuilder.setZeroCount(data.getNativeZeroCount());
      histogramBuilder.setZeroThreshold(data.getNativeZeroThreshold());
      addBuckets(histogramBuilder, data.getNativeBucketsForPositiveValues(), +1);
      addBuckets(histogramBuilder, data.getNativeBucketsForNegativeValues(), -1);

      if (!data.hasClassicHistogramData()) { // native only
        // Add a single +Inf bucket for the exemplar.
        Exemplar exemplar = data.getExemplars().getLatest();
        if (exemplar != null) {
          Metrics.Bucket.Builder bucketBuilder =
              Metrics.Bucket.newBuilder()
                  .setCumulativeCount(getNativeCount(data))
                  .setUpperBound(Double.POSITIVE_INFINITY);
          bucketBuilder.setExemplar(convert(exemplar, scheme));
          histogramBuilder.addBucket(bucketBuilder);
        }
      }
    }
    if (data.hasClassicHistogramData()) {

      ClassicHistogramBuckets buckets = data.getClassicBuckets();
      double lowerBound = Double.NEGATIVE_INFINITY;
      long cumulativeCount = 0;
      for (int i = 0; i < buckets.size(); i++) {
        cumulativeCount += buckets.getCount(i);
        double upperBound = buckets.getUpperBound(i);
        Metrics.Bucket.Builder bucketBuilder =
            Metrics.Bucket.newBuilder()
                .setCumulativeCount(cumulativeCount)
                .setUpperBound(upperBound);
        Exemplar exemplar = data.getExemplars().get(lowerBound, upperBound);
        if (exemplar != null) {
          bucketBuilder.setExemplar(convert(exemplar, scheme));
        }
        histogramBuilder.addBucket(bucketBuilder);
        lowerBound = upperBound;
      }
    }
    addLabels(metricBuilder, data.getLabels(), scheme);
    setScrapeTimestamp(metricBuilder, data);
    if (data.hasCount()) {
      histogramBuilder.setSampleCount(data.getCount());
    }
    if (data.hasSum()) {
      histogramBuilder.setSampleSum(data.getSum());
    }
    metricBuilder.setHistogram(histogramBuilder.build());
    return metricBuilder;
  }

  private Metrics.Metric.Builder convert(
      SummarySnapshot.SummaryDataPointSnapshot data, EscapingScheme scheme) {
    Metrics.Summary.Builder summaryBuilder = Metrics.Summary.newBuilder();
    if (data.hasCount()) {
      summaryBuilder.setSampleCount(data.getCount());
    }
    if (data.hasSum()) {
      summaryBuilder.setSampleSum(data.getSum());
    }
    Quantiles quantiles = data.getQuantiles();
    for (int i = 0; i < quantiles.size(); i++) {
      summaryBuilder.addQuantile(
          Metrics.Quantile.newBuilder()
              .setQuantile(quantiles.get(i).getQuantile())
              .setValue(quantiles.get(i).getValue())
              .build());
    }
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    addLabels(metricBuilder, data.getLabels(), scheme);
    metricBuilder.setSummary(summaryBuilder.build());
    setScrapeTimestamp(metricBuilder, data);
    return metricBuilder;
  }

  private Metrics.Metric.Builder convert(
      InfoSnapshot.InfoDataPointSnapshot data, EscapingScheme scheme) {
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
    gaugeBuilder.setValue(1);
    addLabels(metricBuilder, data.getLabels(), scheme);
    metricBuilder.setGauge(gaugeBuilder);
    setScrapeTimestamp(metricBuilder, data);
    return metricBuilder;
  }

  private Metrics.Metric.Builder convert(
      StateSetSnapshot.StateSetDataPointSnapshot data, String name, int i, EscapingScheme scheme) {
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
    addLabels(metricBuilder, data.getLabels(), scheme);
    metricBuilder.addLabel(
        Metrics.LabelPair.newBuilder().setName(name).setValue(data.getName(i)).build());
    if (data.isTrue(i)) {
      gaugeBuilder.setValue(1);
    } else {
      gaugeBuilder.setValue(0);
    }
    metricBuilder.setGauge(gaugeBuilder);
    setScrapeTimestamp(metricBuilder, data);
    return metricBuilder;
  }

  private Metrics.Metric.Builder convert(
      UnknownSnapshot.UnknownDataPointSnapshot data, EscapingScheme scheme) {
    Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
    Metrics.Untyped.Builder untypedBuilder = Metrics.Untyped.newBuilder();
    untypedBuilder.setValue(data.getValue());
    addLabels(metricBuilder, data.getLabels(), scheme);
    metricBuilder.setUntyped(untypedBuilder);
    return metricBuilder;
  }

  private Metrics.Exemplar.Builder convert(Exemplar exemplar, EscapingScheme scheme) {
    Metrics.Exemplar.Builder builder = Metrics.Exemplar.newBuilder();
    builder.setValue(exemplar.getValue());
    addLabels(builder, exemplar.getLabels(), scheme);
    if (exemplar.hasTimestamp()) {
      builder.setTimestamp(timestampFromMillis(exemplar.getTimestampMillis()));
    }
    return builder;
  }

  private void setMetadataUnlessEmpty(
      Metrics.MetricFamily.Builder builder,
      MetricMetadata metadata,
      @Nullable String nameSuffix,
      Metrics.MetricType type,
      EscapingScheme scheme) {
    if (builder.getMetricCount() == 0) {
      return;
    }
    if (nameSuffix == null) {
      builder.setName(SnapshotEscaper.getMetadataName(metadata, scheme));
    } else {
      builder.setName(SnapshotEscaper.getMetadataName(metadata, scheme) + nameSuffix);
    }
    if (metadata.getHelp() != null) {
      builder.setHelp(metadata.getHelp());
    }
    builder.setType(type);
  }

  private long getNativeCount(HistogramSnapshot.HistogramDataPointSnapshot data) {
    if (data.hasCount()) {
      return data.getCount();
    } else {
      long count = data.getNativeZeroCount();
      for (int i = 0; i < data.getNativeBucketsForPositiveValues().size(); i++) {
        count += data.getNativeBucketsForPositiveValues().getCount(i);
      }
      for (int i = 0; i < data.getNativeBucketsForNegativeValues().size(); i++) {
        count += data.getNativeBucketsForNegativeValues().getCount(i);
      }
      return count;
    }
  }

  private void addBuckets(
      Metrics.Histogram.Builder histogramBuilder, NativeHistogramBuckets buckets, int sgn) {
    if (buckets.size() > 0) {
      Metrics.BucketSpan.Builder currentSpan = Metrics.BucketSpan.newBuilder();
      currentSpan.setOffset(buckets.getBucketIndex(0));
      currentSpan.setLength(0);
      int previousIndex = currentSpan.getOffset();
      long previousCount = 0;
      for (int i = 0; i < buckets.size(); i++) {
        if (buckets.getBucketIndex(i) > previousIndex + 1) {
          // If the gap between bucketIndex and previousIndex is just 1 or 2,
          // we don't start a new span but continue the existing span and add 1 or 2 empty buckets.
          if (buckets.getBucketIndex(i) <= previousIndex + 3) {
            while (buckets.getBucketIndex(i) > previousIndex + 1) {
              currentSpan.setLength(currentSpan.getLength() + 1);
              previousIndex++;
              if (sgn > 0) {
                histogramBuilder.addPositiveDelta(-previousCount);
              } else {
                histogramBuilder.addNegativeDelta(-previousCount);
              }
              previousCount = 0;
            }
          } else {
            if (sgn > 0) {
              histogramBuilder.addPositiveSpan(currentSpan.build());
            } else {
              histogramBuilder.addNegativeSpan(currentSpan.build());
            }
            currentSpan = Metrics.BucketSpan.newBuilder();
            currentSpan.setOffset(buckets.getBucketIndex(i) - (previousIndex + 1));
          }
        }
        currentSpan.setLength(currentSpan.getLength() + 1);
        previousIndex = buckets.getBucketIndex(i);
        if (sgn > 0) {
          histogramBuilder.addPositiveDelta(buckets.getCount(i) - previousCount);
        } else {
          histogramBuilder.addNegativeDelta(buckets.getCount(i) - previousCount);
        }
        previousCount = buckets.getCount(i);
      }
      if (sgn > 0) {
        histogramBuilder.addPositiveSpan(currentSpan.build());
      } else {
        histogramBuilder.addNegativeSpan(currentSpan.build());
      }
    }
  }

  private void addLabels(
      Metrics.Metric.Builder metricBuilder, Labels labels, EscapingScheme scheme) {
    for (int i = 0; i < labels.size(); i++) {
      metricBuilder.addLabel(
          Metrics.LabelPair.newBuilder()
              .setName(getSnapshotLabelName(labels, i, scheme))
              .setValue(labels.getValue(i))
              .build());
    }
  }

  private void addLabels(
      Metrics.Exemplar.Builder metricBuilder, Labels labels, EscapingScheme scheme) {
    for (int i = 0; i < labels.size(); i++) {
      metricBuilder.addLabel(
          Metrics.LabelPair.newBuilder()
              .setName(getSnapshotLabelName(labels, i, scheme))
              .setValue(labels.getValue(i))
              .build());
    }
  }

  private void setScrapeTimestamp(Metrics.Metric.Builder metricBuilder, DataPointSnapshot data) {
    if (data.hasScrapeTimestamp()) {
      metricBuilder.setTimestampMs(data.getScrapeTimestampMillis());
    }
  }
}
