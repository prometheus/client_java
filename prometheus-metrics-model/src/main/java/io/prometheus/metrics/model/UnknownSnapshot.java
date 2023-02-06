package io.prometheus.metrics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class UnknownSnapshot extends MetricSnapshot {

    public UnknownSnapshot(String name, UnknownData... data) {
        this(new MetricMetadata(name), data);
    }

    public UnknownSnapshot(String name, String help, UnknownData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public UnknownSnapshot(String name, String help, Unit unit, UnknownData... data) {
        this(new MetricMetadata(name, help, unit), data);
    }

    public UnknownSnapshot(MetricMetadata metadata, UnknownData... data) {
        this(metadata, Arrays.asList(data));
    }

    public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownData> data) {
        super(metadata, data);
    }

    @Override
    public List<UnknownData> getData() {
        return (List<UnknownData>) data;
    }

    public static final class UnknownData extends MetricData {

        private final double value;
        private final Exemplar exemplar;

        public UnknownData(double value) {
            this(value, Labels.EMPTY, null, 0, 0);
        }

        public UnknownData(double value, long createdTimestampMillis) {
            this(value, Labels.EMPTY, null, createdTimestampMillis, 0);
        }

        public UnknownData(double value, Labels labels) {
            this(value, labels, null, 0, 0);
        }

        public UnknownData(double value, Labels labels, long createdTimestampMillis) {
            this(value, labels, null, createdTimestampMillis, 0);
        }

        public UnknownData(double value, Exemplar exemplar) {
            this(value, Labels.EMPTY, exemplar, 0, 0);
        }

        public UnknownData(double value, Exemplar exemplar, long createdTimestampMillis) {
            this(value, Labels.EMPTY, exemplar, createdTimestampMillis, 0);
        }

        public UnknownData(double value, Labels labels, Exemplar exemplar) {
            this(value, labels, exemplar, 0, 0);
        }

        public UnknownData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis) {
            this(value, labels, exemplar, createdTimestampMillis, 0);
        }

        public UnknownData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis, long scrapeTimestampMillis) {
            super(labels, createdTimestampMillis, scrapeTimestampMillis);
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
    }
}
