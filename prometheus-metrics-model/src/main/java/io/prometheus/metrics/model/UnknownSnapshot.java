package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Immutable snapshot of an Unknown (Untyped) metric.
 */
public final class UnknownSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link UnknownSnapshot}, you can either call the constructor directly or use
     * the builder with {@link UnknownSnapshot#newBuilder()}.
     *
     * @param metadata required name and optional help and unit.
     *                 See {@link MetricMetadata} for naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownData> data) {
        super(metadata, data);
    }

    @Override
    public List<UnknownData> getData() {
        return (List<UnknownData>) data;
    }

    public static final class UnknownData extends MetricData {

        private final double value;
        private final Exemplar exemplar; // may be null

        /**
         * To create a new {@link UnknownData}, you can either call the constructor directly or use the
         * Builder with {@link UnknownData#newBuilder()}.
         *
         * @param value    the value.
         * @param labels   must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplar may be null.
         */
        public UnknownData(double value, Labels labels, Exemplar exemplar) {
            this(value, Labels.EMPTY, exemplar, 0);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public UnknownData(double value, Labels labels, Exemplar exemplar, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
            this.value = value;
            this.exemplar = exemplar;
        }

        public double getValue() {
            return value;
        }

        /**
         * May return {@code null}.
         */
        public Exemplar getExemplar() {
            return exemplar;
        }

        public static class Builder extends MetricData.Builder<Builder> {

            private Exemplar exemplar = null;
            private Double value = null;

            private Builder() {
            }

            /**
             * required.
             */
            public Builder withValue(double value) {
                this.value = value;
                return this;
            }

            /**
             * Optional
             */
            public Builder withExemplar(Exemplar exemplar) {
                this.exemplar = exemplar;
                return this;
            }

            public UnknownData build() {
                if (value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new UnknownData(value, labels, exemplar, scrapeTimestampMillis);
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

        private final List<UnknownData> unknownData = new ArrayList<>();

        private Builder() {
        }

        public Builder addUnknownData(UnknownData data) {
            unknownData.add(data);
            return this;
        }

        public UnknownSnapshot build() {
            return new UnknownSnapshot(buildMetadata(), unknownData);
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
