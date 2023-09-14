package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Immutable snapshot of a Counter.
 */
public class CounterSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link CounterSnapshot}, you can either call the constructor directly or use
     * the builder with {@link CounterSnapshot#builder()}.
     *
     * @param metadata   the metric name in metadata must not include the {@code _total} suffix.
     *                   See {@link MetricMetadata} for more naming conventions.
     * @param dataPoints the constructor will create a sorted copy of the collection.
     */
    public CounterSnapshot(MetricMetadata metadata, Collection<CounterDataPointSnapshot> dataPoints) {
        super(metadata, dataPoints);
    }

    @Override
    public List<CounterDataPointSnapshot> getDataPoints() {
        return (List<CounterDataPointSnapshot>) dataPoints;
    }

    public static class CounterDataPointSnapshot extends DataPointSnapshot {

        private final double value;
        private final Exemplar exemplar; // may be null

        /**
         * To create a new {@link CounterDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link CounterDataPointSnapshot#builder()}.
         *
         * @param value                  the counter value. Must not be negative.
         * @param labels                 must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplar               may be null.
         * @param createdTimestampMillis timestamp (as in {@link System#currentTimeMillis()}) when the time series
         *                               (this specific set of labels) was created (or reset to zero).
         *                               It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public CounterDataPointSnapshot(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis) {
            this(value, labels, exemplar, createdTimestampMillis, 0);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public CounterDataPointSnapshot(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis, long scrapeTimestampMillis) {
            super(labels, createdTimestampMillis, scrapeTimestampMillis);
            this.value = value;
            this.exemplar = exemplar;
            validate();
        }

        public double getValue() {
            return value;
        }

        /**
         * May be {@code null}.
         */
        public Exemplar getExemplar() {
            return exemplar;
        }

        protected void validate() {
            if (value < 0.0) {
                throw new IllegalArgumentException(value + ": counters cannot have a negative value");
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends DataPointSnapshot.Builder<Builder> {

            private Exemplar exemplar = null;
            private Double value = null;
            private long createdTimestampMillis = 0L;

            private Builder() {
            }

            /**
             * Counter value. This is required. The value must not be negative.
             */
            public Builder value(double value) {
                this.value = value;
                return this;
            }

            public Builder exemplar(Exemplar exemplar) {
                this.exemplar = exemplar;
                return this;
            }

            public Builder createdTimestampMillis(long createdTimestampMillis) {
                this.createdTimestampMillis = createdTimestampMillis;
                return this;
            }

            public CounterDataPointSnapshot build() {
                if (value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new CounterDataPointSnapshot(value, labels, exemplar, createdTimestampMillis, scrapeTimestampMillis);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<CounterDataPointSnapshot> dataPoints = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add a data point. Can be called multiple times to add multiple data points.
         */
        public Builder dataPoint(CounterDataPointSnapshot dataPoint) {
            dataPoints.add(dataPoint);
            return this;
        }

        public CounterSnapshot build() {
            return new CounterSnapshot(buildMetadata(), dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
