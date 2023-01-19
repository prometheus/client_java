package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class GaugeSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link GaugeSnapshot}, you can either call the constructor directly or use
     * the builder with {@link GaugeSnapshot#newBuilder()}.
     *
     * @param metadata required name and optional help and unit.
     *                 See {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public GaugeSnapshot(MetricMetadata metadata, Collection<GaugeData> data) {
        super(metadata, data);
        if (metadata.getName().endsWith("_total")) {
            throw new IllegalArgumentException("The name of a counter snapshot must not include the _total suffix");
        }
    }

    @Override
    public List<GaugeData> getData() {
        return (List<GaugeData>) data;
    }

    public static final class GaugeData extends MetricData {

        private final double value;
        private final Exemplar exemplar;

        /**
         * To create a new {@link GaugeData}, you can either call the constructor directly or use the
         * Builder with {@link GaugeData#newBuilder()}.
         *
         * @param value                  the gauge value.
         * @param labels                 must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplar               may be null.
         */
        public GaugeData(double value, Labels labels, Exemplar exemplar) {
            this(value, labels, exemplar, 0);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this.
         * This is only useful in rare cases as the timestamp of a Prometheus metric should usually be set by the
         * Prometheus server during scraping. Exceptions include mirroring metrics with given timestamps from other
         * metric sources.
         */
        public GaugeData(double value, Labels labels, Exemplar exemplar, long timestampMillis) {
            super(labels, 0L, timestampMillis);
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
        protected void validate() {}

        public static class Builder extends MetricData.Builder<Builder> {

            private Exemplar exemplar = null;
            private Double value = null;

            private Builder() {}

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
                return new GaugeData(value, getLabels(), exemplar, getTimestampMillis());
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

        private Builder() {}

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
