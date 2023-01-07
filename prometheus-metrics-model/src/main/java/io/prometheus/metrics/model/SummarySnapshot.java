package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class SummarySnapshot extends MetricSnapshot {

    public SummarySnapshot(String name, SummaryData... data) {
        this(new MetricMetadata(name), data);
    }

    public SummarySnapshot(String name, String help, SummaryData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public SummarySnapshot(String name, String help, Unit unit, SummaryData... data) {
        this(new MetricMetadata(name, help, unit), data);
    }

    public SummarySnapshot(MetricMetadata metadata, SummaryData... data) {
        this(metadata, Arrays.asList(data));
    }

    public SummarySnapshot(MetricMetadata metadata, Collection<SummaryData> data) {
        super(metadata, data);
    }

    @Override
    public List<SummaryData> getData() {
        return (List<SummaryData>) data;
    }

    public static final class SummaryData extends MetricData {

        private final long count;
        private final double sum;
        private final Quantiles quantiles;
        private final Exemplars exemplars;

        public SummaryData(long count, double sum) {
            this(count, sum, Quantiles.EMPTY, Labels.EMPTY, Exemplars.EMPTY, 0, 0);
        }

        public SummaryData(long count, double sum, Exemplars exemplars) {
            this(count, sum, Quantiles.EMPTY, Labels.EMPTY, exemplars, 0, 0);
        }

        public SummaryData(long count, double sum, Labels labels) {
            this(count, sum, Quantiles.EMPTY, labels, Exemplars.EMPTY, 0, 0);
        }

        public SummaryData(long count, double sum, Labels labels, Exemplars exemplars) {
            this(count, sum, Quantiles.EMPTY, labels, exemplars, 0, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles) {
            this(count, sum, quantiles, Labels.EMPTY, Exemplars.EMPTY, 0, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Exemplars exemplars) {
            this(count, sum, quantiles, Labels.EMPTY, exemplars, 0, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels) {
            this(count, sum, quantiles, labels, Exemplars.EMPTY, 0, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars) {
            this(count, sum, quantiles, labels, exemplars, 0, 0);
        }

        public SummaryData(long count, double sum, long createdTimestampMillis) {
            this(count, sum, Quantiles.EMPTY, Labels.EMPTY, Exemplars.EMPTY, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, Quantiles.EMPTY, Labels.EMPTY, exemplars, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Labels labels, long createdTimestampMillis) {
            this(count, sum, Quantiles.EMPTY, labels, Exemplars.EMPTY, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, Quantiles.EMPTY, labels, exemplars, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, long createdTimestampMillis) {
            this(count, sum, quantiles, Labels.EMPTY, Exemplars.EMPTY, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, quantiles, Labels.EMPTY, exemplars, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, long createdTimestampMillis) {
            this(count, sum, quantiles, labels, Exemplars.EMPTY, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, quantiles, labels, exemplars, createdTimestampMillis, 0);
        }

        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(labels, createdTimestampMillis, timestampMillis);
            this.count = count;
            this.sum = sum;
            this.quantiles = quantiles;
            this.exemplars = exemplars;
            validate();
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

        public Exemplars getExemplars() {
            return exemplars;
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("quantile")) {
                    throw new IllegalArgumentException("quantile is a reserved label name for summaries");
                }
            }
        }
    }
}
