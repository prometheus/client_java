package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.shaded.com_google_protobuf_3_25_3.TextFormat;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_25_3.Metrics;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;

import java.io.IOException;
import java.io.OutputStream;

import static io.prometheus.metrics.expositionformats.ProtobufUtil.timestampFromMillis;

/**
 * Write the Prometheus protobuf format as defined in
 * <a href="https://github.com/prometheus/client_model/tree/master/io/prometheus/client">github.com/prometheus/client_model</a>.
 * <p>
 * As of today, this is the only exposition format that supports native histograms.
 */
public class PrometheusProtobufWriter implements ExpositionFormatWriter {

    public static final String CONTENT_TYPE = "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited";

    @Override
    public boolean accepts(String acceptHeader) {
        if (acceptHeader == null) {
            return false;
        } else {
            return acceptHeader.contains("application/vnd.google.protobuf")
                    && acceptHeader.contains("proto=io.prometheus.client.MetricFamily");
        }
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    public String toDebugString(MetricSnapshots metricSnapshots) {
        StringBuilder stringBuilder = new StringBuilder();
        for (MetricSnapshot snapshot : metricSnapshots) {
            if (snapshot.getDataPoints().size() > 0) {
                stringBuilder.append(TextFormat.printer().printToString(convert(snapshot)));
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
        for (MetricSnapshot snapshot : metricSnapshots) {
            if (snapshot.getDataPoints().size() > 0) {
                convert(snapshot).writeDelimitedTo(out);
            }
        }
    }

    public Metrics.MetricFamily convert(MetricSnapshot snapshot) {
        Metrics.MetricFamily.Builder builder = Metrics.MetricFamily.newBuilder();
        if (snapshot instanceof CounterSnapshot) {
            for (CounterDataPointSnapshot data : ((CounterSnapshot) snapshot).getDataPoints()) {
                builder.addMetric(convert(data));
            }
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), "_total", Metrics.MetricType.COUNTER);
        } else if (snapshot instanceof GaugeSnapshot) {
            for (GaugeSnapshot.GaugeDataPointSnapshot data : ((GaugeSnapshot) snapshot).getDataPoints()) {
                builder.addMetric(convert(data));
            }
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), null, Metrics.MetricType.GAUGE);
        } else if (snapshot instanceof HistogramSnapshot) {
            HistogramSnapshot histogram = (HistogramSnapshot) snapshot;
            for (HistogramSnapshot.HistogramDataPointSnapshot data : histogram.getDataPoints()) {
                builder.addMetric(convert(data));
            }
            Metrics.MetricType type = histogram.isGaugeHistogram() ? Metrics.MetricType.GAUGE_HISTOGRAM : Metrics.MetricType.HISTOGRAM;
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), null, type);
        } else if (snapshot instanceof SummarySnapshot) {
            for (SummarySnapshot.SummaryDataPointSnapshot data : ((SummarySnapshot) snapshot).getDataPoints()) {
                if (data.hasCount() || data.hasSum() || data.getQuantiles().size() > 0) {
                    builder.addMetric(convert(data));
                }
            }
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), null, Metrics.MetricType.SUMMARY);
        } else if (snapshot instanceof InfoSnapshot) {
            for (InfoSnapshot.InfoDataPointSnapshot data : ((InfoSnapshot) snapshot).getDataPoints()) {
                builder.addMetric(convert(data));
            }
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), "_info", Metrics.MetricType.GAUGE);
        } else if (snapshot instanceof StateSetSnapshot) {
            for (StateSetSnapshot.StateSetDataPointSnapshot data : ((StateSetSnapshot) snapshot).getDataPoints()) {
                for (int i = 0; i < data.size(); i++) {
                    builder.addMetric(convert(data, snapshot.getMetadata().getPrometheusName(), i));
                }
            }
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), null, Metrics.MetricType.GAUGE);
        } else if (snapshot instanceof UnknownSnapshot) {
            for (UnknownSnapshot.UnknownDataPointSnapshot data : ((UnknownSnapshot) snapshot).getDataPoints()) {
                builder.addMetric(convert(data));
            }
            setMetadataUnlessEmpty(builder, snapshot.getMetadata(), null, Metrics.MetricType.UNTYPED);
        }
        return builder.build();
    }

    private void setMetadataUnlessEmpty(Metrics.MetricFamily.Builder builder, MetricMetadata metadata, String nameSuffix, Metrics.MetricType type) {
        if (builder.getMetricCount() == 0) {
            return;
        }
        if (nameSuffix == null) {
            builder.setName(metadata.getPrometheusName());
        } else {
            builder.setName(metadata.getPrometheusName() + nameSuffix);
        }
        if (metadata.getHelp() != null) {
            builder.setHelp(metadata.getHelp());
        }
        builder.setType(type);
    }

    private Metrics.Metric.Builder convert(CounterDataPointSnapshot data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Counter.Builder counterBuilder = Metrics.Counter.newBuilder();
        counterBuilder.setValue(data.getValue());
        if (data.getExemplar() != null) {
            counterBuilder.setExemplar(convert(data.getExemplar()));
        }
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setCounter(counterBuilder.build());
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(GaugeSnapshot.GaugeDataPointSnapshot data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
        gaugeBuilder.setValue(data.getValue());
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setGauge(gaugeBuilder);
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(HistogramSnapshot.HistogramDataPointSnapshot data) {
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
                    Metrics.Bucket.Builder bucketBuilder = Metrics.Bucket.newBuilder()
                            .setCumulativeCount(getNativeCount(data))
                            .setUpperBound(Double.POSITIVE_INFINITY);
                    bucketBuilder.setExemplar(convert(exemplar));
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
                Metrics.Bucket.Builder bucketBuilder = Metrics.Bucket.newBuilder()
                        .setCumulativeCount(cumulativeCount)
                        .setUpperBound(upperBound);
                Exemplar exemplar = data.getExemplars().get(lowerBound, upperBound);
                if (exemplar != null) {
                    bucketBuilder.setExemplar(convert(exemplar));
                }
                histogramBuilder.addBucket(bucketBuilder);
                lowerBound = upperBound;
            }
        }
        addLabels(metricBuilder, data.getLabels());
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

    private void addBuckets(Metrics.Histogram.Builder histogramBuilder, NativeHistogramBuckets buckets, int sgn) {
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

    private Metrics.Metric.Builder convert(SummarySnapshot.SummaryDataPointSnapshot data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Summary.Builder summaryBuilder = Metrics.Summary.newBuilder();
        if (data.hasCount()) {
            summaryBuilder.setSampleCount(data.getCount());
        }
        if (data.hasSum()) {
            summaryBuilder.setSampleSum(data.getSum());
        }
        Quantiles quantiles = data.getQuantiles();
        for (int i = 0; i < quantiles.size(); i++) {
            summaryBuilder.addQuantile(Metrics.Quantile.newBuilder()
                    .setQuantile(quantiles.get(i).getQuantile())
                    .setValue(quantiles.get(i).getValue())
                    .build());
        }
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setSummary(summaryBuilder.build());
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(InfoSnapshot.InfoDataPointSnapshot data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
        gaugeBuilder.setValue(1);
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setGauge(gaugeBuilder);
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(StateSetSnapshot.StateSetDataPointSnapshot data, String name, int i) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.addLabel(Metrics.LabelPair.newBuilder()
                .setName(name)
                .setValue(data.getName(i))
                .build());
        if (data.isTrue(i)) {
            gaugeBuilder.setValue(1);
        } else {
            gaugeBuilder.setValue(0);
        }
        metricBuilder.setGauge(gaugeBuilder);
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(UnknownSnapshot.UnknownDataPointSnapshot data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Untyped.Builder untypedBuilder = Metrics.Untyped.newBuilder();
        untypedBuilder.setValue(data.getValue());
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setUntyped(untypedBuilder);
        return metricBuilder;
    }

    private void addLabels(Metrics.Metric.Builder metricBuilder, Labels labels) {
        for (int i = 0; i < labels.size(); i++) {
            metricBuilder.addLabel(Metrics.LabelPair.newBuilder()
                    .setName(labels.getPrometheusName(i))
                    .setValue(labels.getValue(i))
                    .build());
        }
    }

    private void addLabels(Metrics.Exemplar.Builder metricBuilder, Labels labels) {
        for (int i = 0; i < labels.size(); i++) {
            metricBuilder.addLabel(Metrics.LabelPair.newBuilder()
                    .setName(labels.getPrometheusName(i))
                    .setValue(labels.getValue(i))
                    .build());
        }
    }

    private Metrics.Exemplar.Builder convert(Exemplar exemplar) {
        Metrics.Exemplar.Builder builder = Metrics.Exemplar.newBuilder();
        builder.setValue(exemplar.getValue());
        addLabels(builder, exemplar.getLabels());
        if (exemplar.hasTimestamp()) {
            builder.setTimestamp(timestampFromMillis(exemplar.getTimestampMillis()));
        }
        return builder;
    }

    private void setScrapeTimestamp(Metrics.Metric.Builder metricBuilder, DataPointSnapshot data) {
        if (data.hasScrapeTimestamp()) {
            metricBuilder.setTimestampMs(data.getScrapeTimestampMillis());
        }
    }
}
