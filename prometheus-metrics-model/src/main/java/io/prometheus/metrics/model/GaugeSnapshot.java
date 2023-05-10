package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Immutable snapshot of a Gauge.
 */
public final class GaugeSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link GaugeSnapshot}, you can either call the constructor directly or use
     * the builder with {@link GaugeSnapshot#newBuilder()}.
     *
     * @param metadata see {@link MetricMetadata} for naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public GaugeSnapshot(MetricMetadata metadata, Collection<GaugeData> data) {
        super(metadata, data);
    }

    @Override
    public List<GaugeData> getData() {
        return (List<GaugeData>) data;
    }

    public static final class GaugeData extends MetricData {

        private final double value;
        private final Exemplar exemplar; // may be null

        /**
         * To create a new {@link GaugeData}, you can either call the constructor directly or use the
         * Builder with {@link GaugeData#newBuilder()}.
         *
         * @param value    the gauge value.
         * @param labels   must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplar may be null.
         */
        public GaugeData(double value, Labels labels, Exemplar exemplar) {
            this(value, labels, exemplar, 0);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public GaugeData(double value, Labels labels, Exemplar exemplar, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
            this.value = value;
            this.exemplar = exemplar;
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

        public static class Builder extends MetricData.Builder<Builder> {

            private Exemplar exemplar = null;
            private Double value = null;

            private Builder() {
            }

            /**
             * Gauge value. This is required.
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

            public GaugeData build() {
                if (value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new GaugeData(value, labels, exemplar, scrapeTimestampMillis);
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

        private final List<GaugeData> gaugeData = new ArrayList<>();

        private Builder() {
        }

        public Builder addGaugeData(GaugeData data) {
            gaugeData.add(data);
            return this;
        }

        public GaugeSnapshot build() {
            return new GaugeSnapshot(buildMetadata(), gaugeData);
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
