package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Immutable snapshot of a Summary metric.
 */
public final class SummarySnapshot extends MetricSnapshot {

    /**
     * To create a new {@link SummarySnapshot}, you can either call the constructor directly or use
     * the builder with {@link SummarySnapshot#builder()}.
     *
     * @param metadata See {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public SummarySnapshot(MetricMetadata metadata, Collection<SummaryDataPointSnapshot> data) {
        super(metadata, data);
    }

    @Override
    public List<SummaryDataPointSnapshot> getDataPoints() {
        return (List<SummaryDataPointSnapshot>) dataPoints;
    }

    public static final class SummaryDataPointSnapshot extends DistributionDataPointSnapshot {

        private final Quantiles quantiles;


        /**
         * To create a new {@link SummaryDataPointSnapshot}, you can either call the constructor directly
         * or use the Builder with {@link SummaryDataPointSnapshot#builder()}.
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
        public SummaryDataPointSnapshot(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, quantiles, labels, exemplars, createdTimestampMillis, 0);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public SummaryDataPointSnapshot(long count, double sum, Quantiles quantiles, Labels labels, Exemplars exemplars, long createdTimestampMillis, long scrapeTimestampMillis) {
            super(count, sum, exemplars, labels, createdTimestampMillis, scrapeTimestampMillis);
            this.quantiles = quantiles;
            validate();
        }

        public Quantiles getQuantiles() {
            return quantiles;
        }

        private void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("quantile")) {
                    throw new IllegalArgumentException("quantile is a reserved label name for summaries");
                }
            }
            if (quantiles == null) {
                throw new NullPointerException();
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends DistributionDataPointSnapshot.Builder<Builder> {

            private Quantiles quantiles = Quantiles.EMPTY;

            private Builder() {
            }

            @Override
            protected Builder self() {
                return this;
            }

            public Builder quantiles(Quantiles quantiles) {
                this.quantiles = quantiles;
                return this;
            }

            @Override
            public Builder count(long count) {
                super.count(count);
                return this;
            }

            public SummaryDataPointSnapshot build() {
                return new SummaryDataPointSnapshot(count, sum, quantiles, labels, exemplars, createdTimestampMillis, scrapeTimestampMillis);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<SummaryDataPointSnapshot> dataPoints = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add a data point. Call multiple times to add multiple data points.
         */
        public Builder dataPoint(SummaryDataPointSnapshot data) {
            dataPoints.add(data);
            return this;
        }

        public SummarySnapshot build() {
            return new SummarySnapshot(buildMetadata(), dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
