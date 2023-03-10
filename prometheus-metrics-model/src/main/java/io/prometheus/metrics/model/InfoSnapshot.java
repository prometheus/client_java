package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InfoSnapshot extends MetricSnapshot {

    /**
     * To create a new {@link InfoSnapshot}, you can either call the constructor directly or use
     * the builder with {@link InfoSnapshot#newBuilder()}.
     *
     * @param metadata the metric name in metadata must not include the {@code _info} suffix.
     *                 See {@link MetricMetadata} for more naming conventions.
     *                 The metadata must not have a unit.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public InfoSnapshot(MetricMetadata metadata, Collection<InfoData> data) {
        super(metadata, data);
        if (metadata.getName().endsWith("_info")) {
            throw new IllegalArgumentException("The name of an info snapshot must not include the _info suffix");
        }
        if (metadata.hasUnit()) {
            throw new IllegalArgumentException("An Info metric cannot have a unit.");
        }
    }

    @Override
    public List<InfoData> getData() {
        return (List<InfoData>) data;
    }

    public static class InfoData extends MetricData {

        /**
         * To create a new {@link InfoData}, you can either call the constructor directly or use the
         * Builder with {@link InfoData#newBuilder()}.
         *
         * @param labels                 must not be null. Use {@link Labels#EMPTY} if there are no labels.
         */
        public InfoData(Labels labels) {
            this(labels, 0L);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this.
         * This is only useful in rare cases as the timestamp of a Prometheus metric should usually be set by the
         * Prometheus server during scraping. Exceptions include mirroring metrics with given timestamps from other
         * metric sources.
         */
        public InfoData(Labels labels, long scrapeTimestampMillis) {
            super(labels, 0L, scrapeTimestampMillis);
            validate();
        }

        @Override
        protected void validate() {}

        public static class Builder extends MetricData.Builder<Builder> {

            public InfoData build() {
                return new InfoData(labels, scrapeTimestampMillis);
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

        private final List<InfoData> infoData = new ArrayList<>();

        private Builder() {}

        public Builder addInfoData(InfoData data) {
            infoData.add(data);
            return this;
        }

        @Override
        public Builder withUnit(Unit unit) {
            throw new IllegalArgumentException("Info metric cannot have a unit.");
        }

        public InfoSnapshot build() {
            return new InfoSnapshot(buildMetadata(), infoData);
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
