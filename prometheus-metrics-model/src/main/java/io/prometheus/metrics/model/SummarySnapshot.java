package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SummarySnapshot extends MetricSnapshot {

    /**
     * To create a new {@link SummarySnapshot}, you can either call the constructor directly or use
     * the builder with {@link SummarySnapshot#newBuilder()}.
     *
     * @param metadata See {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public SummarySnapshot(MetricMetadata metadata, Collection<SummaryData> data) {
        super(metadata, data);
    }

    @Override
    public List<SummaryData> getData() {
        return (List<SummaryData>) data;
    }

    public static final class SummaryData extends DistributionMetricData {

        private final Quantiles quantiles;


        /**
         * To create a new {@link SummaryData}, you can either call the constructor directly
         * or use the Builder with {@link SummaryData#newBuilder()}.
         *
         * @param count                  total number of observations. Optional, pass -1 if not available.
         * @param sum                    sum of all observed values. Optional, pass {@link Double#NaN} if not available.
         * @param quantiles              must not be {@code null}. Use {@link Quantiles#EMPTY} if there are no quantiles.
         * @param labels                 must not be {@code null}. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars              must not be {@code null}. Use {@link Exemplars#EMPTY} if there are no exemplars.
         * @param createdTimestampMillis timestamp (as in {@link System#currentTimeMillis()}) when this summary
         *                               data (this specific set of labels) was created.
         *                               Note that this refers to the creation of the timeseries,
         *                               not the creation of the snapshot.
         *                               The created timestamp optional. Use {@code 0L} if there is no created timestamp.
         */
        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, quantiles, labels, exemplars, createdTimestampMillis, 0);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this,
         * as the timestamp of a Prometheus metric is set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public SummaryData(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(count, sum, exemplars, labels, createdTimestampMillis, timestampMillis);
            this.quantiles = quantiles;
            validate();
        }

        public Quantiles getQuantiles() {
            return quantiles;
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("quantile")) {
                    throw new IllegalArgumentException("quantile is a reserved label name for summaries");
                }
            }
        }

        public static class Builder extends DistributionMetricData.Builder<Builder> {

            private Quantiles quantiles = Quantiles.EMPTY;

            private Builder() {
            }

            @Override
            protected Builder self() {
                return this;
            }

            public Builder withQuantiles(Quantiles quantiles) {
                this.quantiles = quantiles;
                return this;
            }

            public SummaryData build() {
                return new SummaryData(count, sum, quantiles, labels, exemplars, createdTimestampMillis, timestampMillis);
            }
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<SummaryData> summaryData = new ArrayList<>();

        private Builder() {
        }

        public Builder addData(SummaryData data) {
            summaryData.add(data);
            return this;
        }

        public SummarySnapshot build() {
            return new SummarySnapshot(buildMetadata(), summaryData);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
