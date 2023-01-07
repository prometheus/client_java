package io.prometheus.metrics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// or CounterModel, or CounterData
public class CounterSnapshot extends MetricSnapshot {

    public CounterSnapshot(String name, CounterData... data) {
        this(new MetricMetadata(name), data);
    }

    public CounterSnapshot(String name, String help, CounterData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public CounterSnapshot(String name, String help, Unit unit, CounterData... data) {
        this(new MetricMetadata(name, help, unit), data);
    }

    public CounterSnapshot(MetricMetadata metadata, CounterData... data) {
        this(metadata, Arrays.asList(data));
    }

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
        private final Exemplar exemplar;

        public CounterData(double value) {
            this(value, Labels.EMPTY, null, 0, 0);
        }

        public CounterData(double value, long createdTimestampMillis) {
            this(value, Labels.EMPTY, null, createdTimestampMillis, 0);
        }

        public CounterData(double value, Labels labels) {
            this(value, labels, null, 0, 0);
        }

        public CounterData(double value, Labels labels, long createdTimestampMillis) {
            this(value, labels, null, createdTimestampMillis, 0);
        }

        public CounterData(double value, Exemplar exemplar) {
            this(value, Labels.EMPTY, exemplar, 0, 0);
        }

        public CounterData(double value, Exemplar exemplar, long createdTimestampMillis) {
            this(value, Labels.EMPTY, exemplar, createdTimestampMillis, 0);
        }

        public CounterData(double value, Labels labels, Exemplar exemplar) {
            this(value, labels, exemplar, 0, 0);
        }

        public CounterData(double value, Labels labels, Exemplar exemplar, long createdTimestampMillis) {
            this(value, labels, exemplar, createdTimestampMillis, 0);
        }

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
        void validate() {
            if (value < 0.0) {
                throw new IllegalArgumentException(value + ": counters cannot have a negative value");
            }
        }
    }
}
