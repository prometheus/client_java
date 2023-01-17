package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CounterSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link CounterSnapshot}, you can either call the constructor directly or use
     * the builder with {@link CounterSnapshot#newBuilder()}.
     *
     * @param metadata the metric name in metadata must not include the {@code _total} suffix.
     *                 See {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public CounterSnapshot(MetricMetadata metadata, Collection<CounterData> data) {
        super(metadata, data);
        if (metadata.getName().endsWith("_total")) {
            throw new IllegalArgumentException("The name of a counter snapshot must not include the _total suffix");
        }
    }

    @Override
    public List<CounterData> getData() {
        return (List<CounterData>) data;
    }

    public static class CounterData extends MetricData {

        private final double value;
        private final Exemplar exemplar; // may be null

        /**
         * To create a new {@link CounterData}, you can either call the constructor directly or use the
         * Builder with {@link CounterData#newBuilder()}.
         *
         * @param value                  the counter value. Must not be negative.
         * @param labels                 must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplar               may be null.
         * @param createdTimestampMillis timestamp (as in {@link System#currentTimeMillis()}) when the time series
         *                               (this specific set of labels) was created (or reset to zero).
         *                               Note that this refers to the creation of the timeseries,
         *                               not the creation of the snapshot.
         *                               It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public CounterData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis) {
            this(value, labels, exemplar, createdTimestampMillis, 0);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this.
         * This is only useful in rare cases as the timestamp of a Prometheus metric should usually be set by the
         * Prometheus server during scraping. Exceptions include mirroring metrics with given timestamps from other
         * metric sources.
         */
        public CounterData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis, long timestampMillis) {
            super(labels, createdTimestampMillis, timestampMillis);
            this.value = value;
            this.exemplar = exemplar;
            validate();
        }

        public double getValue() {
            return value;
        }

        public Exemplar getExemplar() {
            return exemplar;
        }

        @Override
        protected void validate() {
            if (value < 0.0) {
                throw new IllegalArgumentException(value + ": counters cannot have a negative value");
            }
        }

        public static class Builder extends MetricData.Builder<Builder> {

            private Exemplar exemplar = null;
            private Double value = null;

            private Builder() {}

            public Builder withExemplar(Exemplar exemplar) {
                this.exemplar = exemplar;
                return this;
            }

            /**
             * Counter value. This is required. The value must not be negative.
             */
            public Builder withValue(double value) {
                this.value = value;
                return this;
            }

            public CounterData build() {
                if (value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new CounterData(value, getLabels(), exemplar, getCreatedTimestampMillis(), getTimestampMillis());
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

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<CounterData> counterData = new ArrayList<>();

        private Builder() {}

        public Builder addCounterData(CounterData data) {
            counterData.add(data);
            return this;
        }

        public CounterSnapshot build() {
            return new CounterSnapshot(buildMetadata(), counterData);
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
