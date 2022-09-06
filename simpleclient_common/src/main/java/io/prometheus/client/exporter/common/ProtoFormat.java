package io.prometheus.client.exporter.common;

import io.prometheus.client.Collector;
import io.prometheus.client.Metrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

// THIS CODE WILL BE THROWN AWAY. IT WILL NEVER GET MERGED.
// Prometheus has no intention to re-introduce the Protobuf format.
// This is just a temporary solution before a proper text representation for sparse histograms is defined.
public class ProtoFormat {

    public static void writeProtobuf(ByteArrayOutputStream response, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
        while (mfs.hasMoreElements()) {
            convert(mfs.nextElement()).writeDelimitedTo(response);
        }
    }

    private static Metrics.MetricFamily convert(Collector.MetricFamilySamples metricFamily) {
        Metrics.MetricFamily.Builder builder = Metrics.MetricFamily.newBuilder()
                .setName(getName(metricFamily))
                .setHelp(getHelp(metricFamily));
        switch (metricFamily.type) {
            case COUNTER:
                builder.setType(Metrics.MetricType.COUNTER);
                builder.addAllMetric(makeCounters(metricFamily));
                break;
            case GAUGE:
                builder.setType(Metrics.MetricType.GAUGE);
                builder.addAllMetric(makeGauges(metricFamily));
                break;
            case HISTOGRAM:
                builder.setType(Metrics.MetricType.HISTOGRAM);
                builder.addAllMetric(makeHistograms(metricFamily));
                break;
            case SUMMARY:
                builder.setType(Metrics.MetricType.SUMMARY);
                builder.addAllMetric(makeSummaries(metricFamily));
                break;
            default: // GAUGE_HISTOGRAM, INFO, STATE_SET
                builder.addAllMetric(makeUntyped(metricFamily));
        }
        return builder.build();
    }

    private static String getName(Collector.MetricFamilySamples metricFamily) {
        switch (metricFamily.type) {
            case INFO:
                return metricFamily.name + "_info";
            case COUNTER:
                return metricFamily.name + "_total";
            default:
                return metricFamily.name;
        }
    }

    private static String getHelp(Collector.MetricFamilySamples metricFamily) {
        if (metricFamily.help == null) {
            return getName(metricFamily);
        } else {
            return getName(metricFamily) + " " + metricFamily.help;
        }
    }

    private static List<Metrics.Metric> makeCounters(Collector.MetricFamilySamples metricFamily) {
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (Collector.MetricFamilySamples.Sample sample : metricFamily.samples) {
            if (sample.name.endsWith("_created")) {
                continue;
            }
            result.add(Metrics.Metric.newBuilder()
                    .addAllLabel(makeLabels(sample))
                    .setCounter(Metrics.Counter.newBuilder()
                            .setValue(sample.value)
                            //.setExemplar()
                            .build())
                    .build());
        }
        return result;
    }

    private static List<Metrics.Metric> makeGauges(Collector.MetricFamilySamples metricFamily) {
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (Collector.MetricFamilySamples.Sample sample : metricFamily.samples) {
            if (sample.name.endsWith("_created")) {
                continue;
            }
            result.add(Metrics.Metric.newBuilder()
                    .addAllLabel(makeLabels(sample))
                    .setGauge(Metrics.Gauge.newBuilder()
                            .setValue(sample.value)
                            .build())
                    .build());
        }
        return result;
    }

    private static List<Metrics.Metric> makeUntyped(Collector.MetricFamilySamples metricFamily) {
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (Collector.MetricFamilySamples.Sample sample : metricFamily.samples) {
            if (sample.name.endsWith("_created")) {
                continue;
            }
            result.add(Metrics.Metric.newBuilder()
                    .addAllLabel(makeLabels(sample))
                    .setUntyped(Metrics.Untyped.newBuilder()
                            .setValue(sample.value)
                            .build())
                    .build());
        }
        return result;
    }

    private static List<Metrics.Metric> makeHistograms(Collector.MetricFamilySamples metricFamily) {
        // We might later merge sparse histograms and explicit histograms to provide a unified API,
        // but for the initial evaluation we keep them separate.
        Collector.MetricFamilySamples.Sample bucket = metricFamily.samples.get(0); // this is always a bucket
        if (bucket.labelNames.size() >= 2 && bucket.labelNames.get(bucket.labelNames.size() - 2).equals("bucket_index")) {
            return makeSparseHistograms(metricFamily);
        } else {
            return makeExplicitHistograms(metricFamily);
        }
    }

    private static List<Metrics.Metric> makeSparseHistograms(Collector.MetricFamilySamples metricFamily) {
        // We assume that the samples are in order _bucket..., _count, _sum, _schema, _zero_count, _zero_threshold
        // because that's how we write them in Histogram.collect()
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (int i = 0; i < metricFamily.samples.size();) {
            Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
            Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
            Collector.MetricFamilySamples.Sample sample;
            List<Bucket> positiveBuckets = new ArrayList<Bucket>();
            List<Bucket> negativeBuckets = new ArrayList<Bucket>();
            double prevValue = 0;
            while ((sample = metricFamily.samples.get(i)).name.endsWith("_bucket")) {
                Bucket bucket = new Bucket(sample, prevValue);
                if (sample.labelValues.get(sample.labelValues.size() - 1).startsWith("-")) {
                    // le label starts with "-", i.e. it's a negative upper bound
                    negativeBuckets.add(bucket);
                } else {
                    positiveBuckets.add(bucket);
                }
                prevValue = sample.value;
                i++;
            }
            addBuckets(histogramBuilder, positiveBuckets, +1);
            addBuckets(histogramBuilder, negativeBuckets, -1);
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_count")) {
                metricBuilder.addAllLabel(makeLabels(sample));
                histogramBuilder.setSampleCount((long) sample.value);
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_sum")) {
                histogramBuilder.setSampleSum(sample.value);
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_schema")) {
                histogramBuilder.setSchema(Double.valueOf(sample.value).intValue());
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_zero_count")) {
                histogramBuilder.setZeroCount(Double.valueOf(sample.value).longValue());
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_zero_threshold")) {
                histogramBuilder.setZeroThreshold(sample.value);
                i++;
            }
            if (i < metricFamily.samples.size() && metricFamily.samples.get(i).name.endsWith("_created")) {
                i++;
            }
            result.add(metricBuilder.setHistogram(histogramBuilder.build()).build());
        }
        return result;
    }

    private static void addBuckets(Metrics.Histogram.Builder histogramBuilder, List<Bucket> buckets, int sgn) {
        if (buckets.size() > 1) { // more than just the Inf bucket
            Collections.sort(buckets, new Comparator<Bucket>() {
                @Override
                public int compare(Bucket bucket1, Bucket bucket2) {
                    return Integer.valueOf(bucket1.index).compareTo(bucket2.index);
                }
            });
            Metrics.BucketSpan.Builder currentSpan = Metrics.BucketSpan.newBuilder();
            currentSpan.setOffset(buckets.get(0).index);
            currentSpan.setLength(0);
            int previousIndex = currentSpan.getOffset();
            long previousCount = 0;
            for (Bucket bucket : buckets) {
                if (bucket.index == Integer.MAX_VALUE) {
                    continue; // don't know how to represent the Inf bucket
                }
                if (bucket.index > previousIndex + 1) {
                    if (sgn > 0) {
                        histogramBuilder.addPositiveSpan(currentSpan.build());
                    }  else {
                        histogramBuilder.addNegativeSpan(currentSpan.build());
                    }
                    currentSpan = Metrics.BucketSpan.newBuilder();
                    currentSpan.setOffset(bucket.index - previousIndex);
                }
                currentSpan.setLength(currentSpan.getLength() + 1);
                previousIndex = bucket.index;
                if (sgn > 0) {
                    histogramBuilder.addPositiveDelta(Double.valueOf(bucket.count).longValue() - previousCount);
                } else {
                    histogramBuilder.addNegativeDelta(Double.valueOf(bucket.count).longValue() - previousCount);
                }
                previousCount = Double.valueOf(bucket.count).longValue();
            }
            if (sgn > 0) {
                histogramBuilder.addPositiveSpan(currentSpan.build());
            } else {
                histogramBuilder.addNegativeSpan(currentSpan.build());
            }
        }
    }

    private static class Bucket {
        final int index;
        final double count;

        private Bucket(Collector.MetricFamilySamples.Sample sample, double prevCount) {
            this.index = findIndex(sample);
            this.count = sample.value - prevCount;
        }

        private int findIndex(Collector.MetricFamilySamples.Sample sample) {
            String indexString = sample.labelValues.get(sample.labelValues.size()-2);
            String le = sample.labelValues.get(sample.labelValues.size()-1);
            if (indexString.endsWith("Inf")) {
                return Integer.MAX_VALUE;
            }
            int index = Integer.parseInt(indexString);
            return le.startsWith("-") ? -index : index;
        }
    }

    private static List<Metrics.Metric> makeExplicitHistograms(Collector.MetricFamilySamples metricFamily) {
        // We assume that the samples are in order _bucket..., _count, _sum,
        // because that's how we write them in Histogram.collect()
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (int i = 0; i < metricFamily.samples.size(); i++) {
            Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
            Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
            Collector.MetricFamilySamples.Sample sample;
            while ((sample = metricFamily.samples.get(i)).name.endsWith("_bucket")) {
                histogramBuilder.addBucket(Metrics.Bucket.newBuilder()
                        //.setCumulativeCountFloat(sample.value)
                        .setCumulativeCount((long) sample.value)
                        // We assume that the 'le' label is tha last one,
                        // because that's how we write the label values in Histogram.collect()
                        .setUpperBound(doubleFromGoString(sample.labelValues.get(sample.labelValues.size() - 1)))
                        // .setExemplar()
                        .build());
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_count")) {
                metricBuilder.addAllLabel(makeLabels(sample));
                histogramBuilder.setSampleCount((long) sample.value);
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_sum")) {
                histogramBuilder.setSampleSum(sample.value);
            }
            if (i < metricFamily.samples.size() - 1 && metricFamily.samples.get(i + 1).name.endsWith("_created")) {
                i++;
            }
            result.add(metricBuilder.setHistogram(histogramBuilder.build()).build());
        }
        return result;
    }

    private static List<Metrics.Metric> makeSummaries(Collector.MetricFamilySamples metricFamily) {
        // We assume that the samples are in order quantile..., _count, _sum,
        // because that's how we write them in Summary.collect()
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (int i = 0; i < metricFamily.samples.size(); i++) {
            Metrics.Metric.Builder metricBuilder = Metrics.Metric.newBuilder();
            Metrics.Summary.Builder summaryBuilder = Metrics.Summary.newBuilder();
            Collector.MetricFamilySamples.Sample sample;
            while (!(sample = metricFamily.samples.get(i)).name.endsWith("_count")) {
                summaryBuilder.addQuantile(Metrics.Quantile.newBuilder()
                        // We assume that the 'quantile' label is tha last one,
                        // because that's how we write the label values in Summary.collect()
                        .setQuantile(doubleFromGoString(sample.labelValues.get(sample.labelValues.size() - 1)))
                        .setValue(sample.value)
                        .build());
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_count")) {
                metricBuilder.addAllLabel(makeLabels(sample));
                summaryBuilder.setSampleCount((long) sample.value);
                i++;
            }
            if ((sample = metricFamily.samples.get(i)).name.endsWith("_sum")) {
                summaryBuilder.setSampleSum(sample.value);
            }
            if (i < metricFamily.samples.size() - 1 && metricFamily.samples.get(i + 1).name.endsWith("_created")) {
                i++;
            }
            result.add(metricBuilder.setSummary(summaryBuilder.build()).build());
        }
        return result;
    }

    private static Double doubleFromGoString(String s) {
        if (s.equals("+Inf")) {
            return Double.POSITIVE_INFINITY;
        }
        if (s.equals("-Inf")) {
            return Double.NEGATIVE_INFINITY;
        }
        return Double.valueOf(s);
    }

    private static Iterable<Metrics.LabelPair> makeLabels(Collector.MetricFamilySamples.Sample sample) {
        List<Metrics.LabelPair> labels = new ArrayList<Metrics.LabelPair>();
        for (int i = 0; i < sample.labelNames.size(); i++) {
            labels.add(Metrics.LabelPair.newBuilder()
                    .setName(sample.labelNames.get(i))
                    .setValue(sample.labelValues.get(i))
                    .build());
        }
        return labels;
    }
}
