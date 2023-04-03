package io.prometheus.expositionformat.text;

import io.prometheus.expositionformat.protobuf.generated.com_google_protobuf_3_21_7.Metrics;
import io.prometheus.metrics.model.ClassicHistogramBuckets;
import io.prometheus.metrics.model.ClassicHistogramSnapshot;
import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.CounterSnapshot.CounterData;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricData;
import io.prometheus.metrics.model.MetricMetadata;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.NativeHistogramBuckets;
import io.prometheus.metrics.model.NativeHistogramSnapshot;
import io.prometheus.metrics.model.Quantiles;
import io.prometheus.metrics.model.StateSetSnapshot;
import io.prometheus.metrics.model.SummarySnapshot;
import io.prometheus.metrics.model.UnknownSnapshot;

import java.io.IOException;
import java.io.OutputStream;

public class PrometheusProtobufFormatWriter {

    public final String CONTENT_TYPE = "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited";

    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
        for (MetricSnapshot snapshot : metricSnapshots) {
            if (snapshot.getData().size() > 0) {
                convert(snapshot).writeDelimitedTo(out);
            }
        }
    }

    public Metrics.MetricFamily convert(MetricSnapshot snapshot) {
        // "unknown", "gauge", "counter", "stateset", "info", "histogram", "gaugehistogram", and "summary".
        Metrics.MetricFamily.Builder builder = Metrics.MetricFamily.newBuilder();
        if (snapshot instanceof CounterSnapshot) {
            setMetadata(builder, snapshot.getMetadata(), "_total", Metrics.MetricType.COUNTER);
            builder.setType(Metrics.MetricType.COUNTER);
            for (CounterData data : ((CounterSnapshot) snapshot).getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof GaugeSnapshot) {
            setMetadata(builder, snapshot.getMetadata(), null, Metrics.MetricType.GAUGE);
            for (GaugeSnapshot.GaugeData data : ((GaugeSnapshot) snapshot).getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof ClassicHistogramSnapshot) {
            ClassicHistogramSnapshot histogram = (ClassicHistogramSnapshot) snapshot;
            Metrics.MetricType type = histogram.isGaugeHistogram() ? Metrics.MetricType.GAUGE_HISTOGRAM : Metrics.MetricType.HISTOGRAM;
            setMetadata(builder, snapshot.getMetadata(), null, type);
            for (ClassicHistogramSnapshot.ClassicHistogramData data : histogram.getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof NativeHistogramSnapshot) {
            NativeHistogramSnapshot histogram = (NativeHistogramSnapshot) snapshot;
            Metrics.MetricType type = histogram.isGaugeHistogram() ? Metrics.MetricType.GAUGE_HISTOGRAM : Metrics.MetricType.HISTOGRAM;
            setMetadata(builder, snapshot.getMetadata(), null, type);
            for (NativeHistogramSnapshot.NativeHistogramData data : histogram.getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof SummarySnapshot) {
            setMetadata(builder, snapshot.getMetadata(), null, Metrics.MetricType.SUMMARY);
            for (SummarySnapshot.SummaryData data : ((SummarySnapshot) snapshot).getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof InfoSnapshot) {
            setMetadata(builder, snapshot.getMetadata(), "_info", Metrics.MetricType.GAUGE);
            for (InfoSnapshot.InfoData data : ((InfoSnapshot) snapshot).getData()) {
                builder.addMetric(convert(data));
            }

        } else if (snapshot instanceof StateSetSnapshot) {
            setMetadata(builder, snapshot.getMetadata(), null, Metrics.MetricType.GAUGE);
            for (StateSetSnapshot.StateSetData data : ((StateSetSnapshot) snapshot).getData()) {
                for (int i=0; i<data.size(); i++) {
                builder.addMetric(convert(data, snapshot.getMetadata().getName(), i));
                }
            }

        } else if (snapshot instanceof UnknownSnapshot) {
            setMetadata(builder, snapshot.getMetadata(), null, Metrics.MetricType.UNTYPED);
            for (UnknownSnapshot.UnknownData data : ((UnknownSnapshot) snapshot).getData()) {
                builder.addMetric(convert(data));
            }
        }
        return builder.build();
    }

    private void setMetadata(Metrics.MetricFamily.Builder builder, MetricMetadata metadata, String nameSuffix, Metrics.MetricType type) {
        if (nameSuffix == null) {
            builder.setName(metadata.getName());
        } else {
            builder.setHelp(metadata.getName() + nameSuffix);
        }
        if (metadata.getHelp() != null) {
            builder.setHelp(metadata.getHelp());
        }
        builder.setType(type);
    }

    private Metrics.Metric.Builder convert(CounterData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Counter.Builder counterBuilder = Metrics.Counter.newBuilder();
        counterBuilder.setValue(data.getValue());
        if (data.getExemplar() != null) {
            counterBuilder.setExemplar(convert(data.getExemplar()));
        }
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setCounter(counterBuilder.build());
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(GaugeSnapshot.GaugeData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
        gaugeBuilder.setValue(data.getValue());
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setGauge(gaugeBuilder);
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(ClassicHistogramSnapshot.ClassicHistogramData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
        ClassicHistogramBuckets buckets = data.getBuckets();
        double lowerBound = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < buckets.size(); i++) {
            double upperBound = buckets.getUpperBound(i);
            Metrics.Bucket.Builder bucketBuilder = Metrics.Bucket.newBuilder()
                    .setCumulativeCount(buckets.getCumulativeCount(i))
                    .setUpperBound(upperBound);
            Exemplar exemplar = data.getExemplars().get(lowerBound, upperBound);
            if (exemplar != null) {
                bucketBuilder.setExemplar(convert(exemplar));
            }
            histogramBuilder.addBucket(bucketBuilder);
            lowerBound = upperBound;
        }
        if (data.hasCount()) {
            histogramBuilder.setSampleCount(data.getCount());
        }
        if (data.hasSum()) {
            histogramBuilder.setSampleSum(data.getSum());
        }
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setHistogram(histogramBuilder.build());
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(NativeHistogramSnapshot.NativeHistogramData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
        histogramBuilder.setSchema(data.getSchema());
        if (data.hasCount()) {
            histogramBuilder.setSampleCount(data.getCount());
        }
        if (data.hasSum()) {
            histogramBuilder.setSampleSum(data.getSum());
        }
        histogramBuilder.setZeroCount(data.getZeroCount());
        histogramBuilder.setZeroThreshold(data.getZeroThreshold());
        addBuckets(histogramBuilder, data.getBucketsForPositiveValues(), +1);
        addBuckets(histogramBuilder, data.getBucketsForNegativeValues(), -1);
        // TODO: Add classic buckets for exemplars
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setHistogram(histogramBuilder.build());
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
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
                    if (buckets.getBucketIndex(i) < previousIndex + 3) {
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
                    histogramBuilder.addPositiveDelta(buckets.getCumulativeCount(i) - previousCount);
                } else {
                    histogramBuilder.addNegativeDelta(buckets.getCumulativeCount(i) - previousCount);
                }
                previousCount = buckets.getCumulativeCount(i);
            }
            if (sgn > 0) {
                histogramBuilder.addPositiveSpan(currentSpan.build());
            } else {
                histogramBuilder.addNegativeSpan(currentSpan.build());
            }
        }
    }

    private Metrics.Metric.Builder convert(SummarySnapshot.SummaryData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Summary.Builder summaryBuilder = Metrics.Summary.newBuilder();
        if (data.hasCount()) {
            summaryBuilder.setSampleCount(data.getCount());
        }
        if (data.hasSum()) {
            summaryBuilder.setSampleSum(data.getSum());
        }
        Quantiles quantiles = data.getQuantiles();
        for (int i=0; i<quantiles.size(); i++) {
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

    private Metrics.Metric.Builder convert(InfoSnapshot.InfoData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
        gaugeBuilder.setValue(1);
        addLabels(metricBuilder, data.getLabels());
        metricBuilder.setGauge(gaugeBuilder);
        setScrapeTimestamp(metricBuilder, data);
        return metricBuilder;
    }

    private Metrics.Metric.Builder convert(StateSetSnapshot.StateSetData data, String name, int i) {
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

    private Metrics.Metric.Builder convert(UnknownSnapshot.UnknownData data) {
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
                    .setName(labels.getName(i))
                    .setValue(labels.getValue(i))
                    .build());
        }
    }

    private void addLabels(Metrics.Exemplar.Builder metricBuilder, Labels labels) {
        for (int i = 0; i < labels.size(); i++) {
            metricBuilder.addLabel(Metrics.LabelPair.newBuilder()
                    .setName(labels.getName(i))
                    .setValue(labels.getValue(i))
                    .build());
        }
    }

    private Metrics.Exemplar.Builder convert(Exemplar exemplar) {
        Metrics.Exemplar.Builder builder = Metrics.Exemplar.newBuilder();
        builder.setValue(exemplar.getValue());
        addLabels(builder, exemplar.getLabels());
        return builder;
    }

    private void setScrapeTimestamp(Metrics.Metric.Builder metricBuilder, MetricData data) {
        if (data.hasScrapeTimestamp()) {
            metricBuilder.setTimestampMs(data.getScrapeTimestampMillis());
        }
    }
}
