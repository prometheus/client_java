package io.prometheus.metrics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class FixedBucketsHistogramSnapshot extends MetricSnapshot {

    public FixedBucketsHistogramSnapshot(String name, FixedBucketsHistogramData... data) {
        this(new MetricMetadata(name), data);
    }

    public FixedBucketsHistogramSnapshot(String name, String help, FixedBucketsHistogramData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public FixedBucketsHistogramSnapshot(String name, String help, Unit unit, FixedBucketsHistogramData... data) {
        this(new MetricMetadata(name, help, unit), data);
    }

    public FixedBucketsHistogramSnapshot(MetricMetadata metadata, FixedBucketsHistogramData... data) {
        this(metadata, Arrays.asList(data));
    }

    public FixedBucketsHistogramSnapshot(MetricMetadata metadata, Collection<FixedBucketsHistogramData> data) {
        super(metadata, data);
    }

    @Override
    public List<FixedBucketsHistogramData> getData() {
        return (List<FixedBucketsHistogramData>) data;
    }

    public static final class FixedBucketsHistogramData extends MetricData {
        private final long count;
        private final double sum;
        private final FixedBuckets buckets;
        private final Exemplars exemplars;

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets) {
            this(count, sum, buckets, Labels.EMPTY, Exemplars.EMPTY, 0, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Exemplars exemplars) {
            this(count, sum, buckets, Labels.EMPTY, exemplars, 0, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, long createdTimestampMillis) {
            this(count, sum, buckets, Labels.EMPTY, Exemplars.EMPTY, createdTimestampMillis, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, buckets, Labels.EMPTY, exemplars, createdTimestampMillis, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels) {
            this(count, sum, buckets, labels, Exemplars.EMPTY, 0, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars) {
            this(count, sum, buckets, labels, exemplars, 0, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, long createdTimestampMillis) {
            this(count, sum, buckets, labels, Exemplars.EMPTY, createdTimestampMillis, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, buckets, labels, exemplars, createdTimestampMillis, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(labels, createdTimestampMillis, timestampMillis);
            this.count = count;
            this.sum = sum;
            this.buckets = buckets;
            this.exemplars = exemplars;
            validate();
        }

        public long getCount() {
            return count;
        }

        public double getSum() {
            return sum;
        }

        public FixedBuckets getBuckets() {
            return buckets;
        }

        public Exemplars getExemplars() {
            return exemplars;
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
            if (buckets.getCumulativeCount(buckets.size()-1) != count) {
                throw new IllegalArgumentException("The +Inf bucket must have the same value as the count.");
            }
        }
    }
}
