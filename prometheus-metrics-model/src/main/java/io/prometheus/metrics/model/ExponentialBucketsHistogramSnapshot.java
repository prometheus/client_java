package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.List;

public final class ExponentialBucketsHistogramSnapshot extends MetricSnapshot {
        private final Collection<ExponentialBucketsHistogramData> data;

        public ExponentialBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExponentialBucketsHistogramData> data) {
            super(metadata);
            this.data = data;
        }

        public static final class ExponentialBucketsHistogramData extends MetricData {

            private final int schema;
            private final double zeroThreshold;
            private final List<ExponentialBucket> bucketsForPositiveValues;
            private final List<ExponentialBucket> bucketsForNegativeValues;

            public ExponentialBucketsHistogramData(int schema, double zeroThreshold, List<ExponentialBucket> bucketsForPositiveValues, List<ExponentialBucket> bucketsForNegativeValues, Labels labels, long createdTimeMillis) {
                super(labels);
                this.schema = schema;
                this.zeroThreshold = zeroThreshold;
                this.bucketsForPositiveValues = bucketsForPositiveValues;
                this.bucketsForNegativeValues = bucketsForNegativeValues;
            }

            public double getZeroThreshold() {
                return zeroThreshold;
            }
        }
}
