package io.prometheus.metrics.model;

import java.util.Collection;

public final class SummarySnapshot extends MetricSnapshot {
    private final Collection<SummaryData> data;

    public SummarySnapshot(MetricMetadata metadata, Collection<SummaryData> data) {
        super(metadata);
        this.data = data;
    }

    public Collection<SummaryData> getData() {
        return data;
    }

    public static final class SummaryData extends MetricData {

        private final long count;
        private final double sum;
        private final Quantiles quantiles;
        private final long createdTimeMillis;

        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, long createdTimeMillis) {
            super(labels);
            this.count = count;
            this.sum = sum;
            this.quantiles = quantiles;
            this.createdTimeMillis = createdTimeMillis;
        }

        public long getCount() {
            return count;
        }

        public double getSum() {
            return sum;
        }

        public Quantiles getQuantiles() {
            return quantiles;
        }

        public long getCreatedTimeMillis() {
            return createdTimeMillis;
        }
    }

}
