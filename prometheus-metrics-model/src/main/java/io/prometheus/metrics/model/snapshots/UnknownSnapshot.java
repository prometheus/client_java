package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable snapshot of an Unknown (Untyped) metric.
 */
public final class UnknownSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link UnknownSnapshot}, you can either call the constructor directly or use
     * the builder with {@link UnknownSnapshot#builder()}.
     *
     * @param metadata required name and optional help and unit.
     *                 See {@link MetricMetadata} for naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownDataPointSnapshot> data) {
        super(metadata, data);
    }


    @Override
    public UnknownSnapshot merge(MetricSnapshot snapshot) {
        if (!(getMetadata().equals(snapshot.getMetadata())))
            throw new IllegalArgumentException("Unable to merge - metadata mismatch.");
        if (snapshot instanceof UnknownSnapshot s) {
            var result = new ArrayList<UnknownSnapshot.UnknownDataPointSnapshot>();
            result.addAll(this.getDataPoints());
            result.addAll(s.getDataPoints());
            return new UnknownSnapshot(getMetadata(), result);
        } else {
            throw new IllegalArgumentException("Unable to merge - invalid snapshot type");
        }
    }


    @Override
    public UnknownSnapshot withNamePrefix(String prefix) {
        return new UnknownSnapshot(getMetadata().withNamePrefix(prefix), getDataPoints());
    }

    /** Merge additional labels to all the data points. */
    public UnknownSnapshot withLabels(Labels labels) {
        var points = getDataPoints()
                .stream()
                .map(point -> new UnknownSnapshot.UnknownDataPointSnapshot(point.value, point.getLabels().merge(labels), point.exemplar, point.getScrapeTimestampMillis()))
                .collect(Collectors.toList());
        return new UnknownSnapshot(getMetadata(), points);
    }

    @Override
    public List<UnknownDataPointSnapshot> getDataPoints() {
        return (List<UnknownDataPointSnapshot>) dataPoints;
    }

    public static final class UnknownDataPointSnapshot extends DataPointSnapshot {

        private final double value;
        private final Exemplar exemplar; // may be null

        /**
         * To create a new {@link UnknownDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link UnknownDataPointSnapshot#builder()}.
         *
         * @param value    the value.
         * @param labels   must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplar may be null.
         */
        public UnknownDataPointSnapshot(double value, Labels labels, Exemplar exemplar) {
            this(value, Labels.EMPTY, exemplar, 0);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public UnknownDataPointSnapshot(double value, Labels labels, Exemplar exemplar, long scrapeTimestampMillis) {
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

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends DataPointSnapshot.Builder<Builder> {

            private Exemplar exemplar = null;
            private Double value = null;

            private Builder() {
            }

            /**
             * required.
             */
            public Builder value(double value) {
                this.value = value;
                return this;
            }

            /**
             * Optional
             */
            public Builder exemplar(Exemplar exemplar) {
                this.exemplar = exemplar;
                return this;
            }

            public UnknownDataPointSnapshot build() {
                if (value == null) {
                    throw new IllegalArgumentException("Missing required field: value is null.");
                }
                return new UnknownDataPointSnapshot(value, labels, exemplar, scrapeTimestampMillis);
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

        private final List<UnknownDataPointSnapshot> dataPoints = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add a data point. Call multiple times to add multiple data points.
         */
        public Builder dataPoint(UnknownDataPointSnapshot data) {
            dataPoints.add(data);
            return this;
        }

        public UnknownSnapshot build() {
            return new UnknownSnapshot(buildMetadata(), dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
