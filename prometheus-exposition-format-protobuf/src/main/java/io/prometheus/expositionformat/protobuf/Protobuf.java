package io.prometheus.expositionformat.protobuf;

import io.prometheus.expositionformat.protobuf.generated.Metrics;
import io.prometheus.metrics.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class Protobuf {

    public static void writeProtobuf(ByteArrayOutputStream response, Collection<MetricSnapshot> metricSnapshots) throws IOException {
        for (MetricSnapshot snapshot : metricSnapshots) {
            convert(snapshot).writeDelimitedTo(response);
        }
    }

    public static Metrics.MetricFamily convert(MetricSnapshot snapshot) {
        MetricMetadata metadata = snapshot.getMetadata();
        Metrics.MetricFamily.Builder builder = Metrics.MetricFamily.newBuilder();
        builder.setName(metadata.getName());
        if (metadata.getHelp() != null) {
            builder.setHelp(metadata.getHelp());
        }
        if (snapshot instanceof CounterSnapshot) {
            builder.setName(metadata.getName() + "_total");
            CounterSnapshot counter = (CounterSnapshot) snapshot;
            builder.setType(Metrics.MetricType.COUNTER);
            for (CounterSnapshot.CounterData data : counter.getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof GaugeSnapshot) {
            GaugeSnapshot gauge = (GaugeSnapshot) snapshot;
            builder.setType(Metrics.MetricType.GAUGE);
            for (GaugeSnapshot.GaugeData data : gauge.getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof FixedBucketsHistogramSnapshot) {
            FixedBucketsHistogramSnapshot histogram = (FixedBucketsHistogramSnapshot) snapshot;
            builder.setType(Metrics.MetricType.HISTOGRAM);
            for (FixedBucketsHistogramSnapshot.FixedBucketsHistogramData data : histogram.getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof ExponentialBucketsHistogramSnapshot) {
            ExponentialBucketsHistogramSnapshot histogram = (ExponentialBucketsHistogramSnapshot) snapshot;
            builder.setType(Metrics.MetricType.HISTOGRAM);
            for (ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData data : histogram.getData()) {
                builder.addMetric(convert(data));
            }
        } else if (snapshot instanceof SummarySnapshot) {
            SummarySnapshot summary = (SummarySnapshot) snapshot;
            builder.setType(Metrics.MetricType.SUMMARY);
            for (SummarySnapshot.SummaryData data : summary.getData()) {
                builder.addMetric(convert(data));
            }
        }
        return builder.build();
    }

    private static Metrics.Metric convert(CounterSnapshot.CounterData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Counter.Builder counterBuilder = Metrics.Counter.newBuilder();
        counterBuilder.setValue(data.getValue());
        if (data.getExemplar() != null) {
            counterBuilder.setExemplar(convert(data.getExemplar()));
        }
        for (Label label : data.getLabels()) {
            metricBuilder.addLabel(convert(label));
        }
        metricBuilder.setCounter(counterBuilder.build());
        return metricBuilder.build();
    }

    private static Metrics.Metric convert(GaugeSnapshot.GaugeData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Gauge.Builder gaugeBuilder = Metrics.Gauge.newBuilder();
        gaugeBuilder.setValue(data.getValue());
        for (Label label : data.getLabels()) {
            metricBuilder.addLabel(convert(label));
        }
        metricBuilder.setGauge(gaugeBuilder.build());
        return metricBuilder.build();
    }

    private static Metrics.Metric convert(FixedBucketsHistogramSnapshot.FixedBucketsHistogramData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
        for (FixedBucket bucket : data.getBuckets()) {
            histogramBuilder.addBucket(convert(bucket));
        }
        histogramBuilder.setSampleCount(data.getCount());
        histogramBuilder.setSampleSum(data.getSum());
        for (Label label : data.getLabels()) {
            metricBuilder.addLabel(convert(label));
        }
        metricBuilder.setHistogram(histogramBuilder.build());
        return metricBuilder.build();
    }

    private static Metrics.Metric convert(ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder()
            .setSchema(data.getSchema())
                    .setSampleCount(data.getCount())
                            .setSampleSum(data.getSum())
                .setZeroCount(data.getZeroCount())
                .setZeroThreshold(data.getZeroThreshold());
        addBuckets(histogramBuilder, data.getBucketsForPositiveValues(), +1);
        addBuckets(histogramBuilder, data.getBucketsForNegativeValues(), -1);
        // TODO
        for (Label label : data.getLabels()) {
            metricBuilder.addLabel(convert(label));
        }
        metricBuilder.setHistogram(histogramBuilder.build());
        return metricBuilder.build();
    }

    private static void addBuckets(Metrics.Histogram.Builder histogramBuilder, List<ExponentialBucket> buckets, int sgn) {
        if (!buckets.isEmpty()) {
            Metrics.BucketSpan.Builder currentSpan = Metrics.BucketSpan.newBuilder();
            currentSpan.setOffset(buckets.get(0).getBucketIndex());
            currentSpan.setLength(0);
            int previousIndex = currentSpan.getOffset();
            long previousCount = 0;
            for (ExponentialBucket bucket : buckets) {
                if (bucket.getBucketIndex() > previousIndex + 1) {
                    // If the gap between bucketIndex and previousIndex is just 1 or 2,
                    // we don't start a new span but continue the existing span and add 1 or 2 empty buckets.
                    if (bucket.getBucketIndex() < previousIndex + 3) {
                        while (bucket.getBucketIndex() > previousIndex + 1) {
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
                        currentSpan.setOffset(bucket.getBucketIndex() - (previousIndex + 1));
                    }
                }
                currentSpan.setLength(currentSpan.getLength() + 1);
                previousIndex = bucket.getBucketIndex();
                if (sgn > 0) {
                    histogramBuilder.addPositiveDelta(bucket.getCumulativeCount() - previousCount);
                } else {
                    histogramBuilder.addNegativeDelta(bucket.getCumulativeCount() - previousCount);
                }
                previousCount = bucket.getCumulativeCount();
            }
            if (sgn > 0) {
                histogramBuilder.addPositiveSpan(currentSpan.build());
            } else {
                histogramBuilder.addNegativeSpan(currentSpan.build());
            }
        }
    }

    private static Metrics.Metric convert(SummarySnapshot.SummaryData data) {
        Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
        Metrics.Summary.Builder summaryBuilder = Metrics.Summary.newBuilder();
        summaryBuilder.setSampleCount(data.getCount());
        summaryBuilder.setSampleSum(data.getSum());
        for (Quantile quantile : data.getQuantiles()) {
            summaryBuilder.addQuantile(convert(quantile));
        }
        for (Label label : data.getLabels()) {
            metricBuilder.addLabel(convert(label));
        }
        metricBuilder.setSummary(summaryBuilder.build());
        return metricBuilder.build();
    }

    private static Metrics.LabelPair convert(Label label) {
        return Metrics.LabelPair.newBuilder()
                .setName(label.getName())
                .setValue(label.getValue())
                .build();
    }

    private static Metrics.Exemplar convert(Exemplar exemplar) {
        Metrics.Exemplar.Builder builder = Metrics.Exemplar.newBuilder();
        builder.setValue(exemplar.getValue());
        for (Label label : exemplar.getLabels()) {
            builder.addLabel(convert(label));
        }
        return builder.build();
    }

    private static Metrics.Bucket convert(FixedBucket bucket) {
        Metrics.Bucket.Builder builder = Metrics.Bucket.newBuilder();
        if (bucket.getExemplar() != null) {
            builder.setExemplar(convert(bucket.getExemplar()));
        }
        builder.setCumulativeCount(bucket.getCumulativeCount());
        builder.setUpperBound(bucket.getUpperBound());
        return builder.build();
    }

    private static Metrics.Quantile convert(Quantile quantile) {
        return Metrics.Quantile.newBuilder()
                .setQuantile(quantile.getQuantile())
                .setValue(quantile.getValue())
                .build();
    }
}
