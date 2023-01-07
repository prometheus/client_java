package io.prometheus.metrics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class GaugeSnapshot extends MetricSnapshot {

    public GaugeSnapshot(String name, GaugeSnapshot.GaugeData... data) {
        this(new MetricMetadata(name), data);
    }

    public GaugeSnapshot(String name, String help, GaugeSnapshot.GaugeData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public GaugeSnapshot(String name, String help, Unit unit, GaugeSnapshot.GaugeData... data) {
        this(new MetricMetadata(name, help, unit), data);
    }

    public GaugeSnapshot(MetricMetadata metadata, GaugeSnapshot.GaugeData... data) {
        this(metadata, Arrays.asList(data));
    }

    public GaugeSnapshot(MetricMetadata metadata, Collection<GaugeSnapshot.GaugeData> data) {
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

        public GaugeData(double value) {
            this(value, Labels.EMPTY, null, 0, 0);
        }

        public GaugeData(double value, long createdTimestampMillis) {
            this(value, Labels.EMPTY, null, createdTimestampMillis, 0);
        }

        public GaugeData(double value, Labels labels) {
            this(value, labels, null, 0, 0);
        }

        public GaugeData(double value, Labels labels, long createdTimestampMillis) {
            this(value, labels, null, createdTimestampMillis, 0);
        }

        public GaugeData(double value, Exemplar exemplar) {
            this(value, Labels.EMPTY, exemplar, 0, 0);
        }

        public GaugeData(double value, Exemplar exemplar, long createdTimestampMillis) {
            this(value, Labels.EMPTY, exemplar, createdTimestampMillis, 0);
        }

        public GaugeData(double value, Labels labels, Exemplar exemplar) {
            this(value, labels, exemplar, 0, 0);
        }

        public GaugeData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis) {
            this(value, labels, exemplar, createdTimestampMillis, 0);
        }

        public GaugeData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis, long timestampMillis) {
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
        void validate() {}
    }
}
