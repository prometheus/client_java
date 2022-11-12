package io.prometheus.metrics.model;

import java.util.Collection;

// or CounterModel, or CounterData
public final class CounterSnapshot extends MetricSnapshot {

    private final Collection<CounterData> data;

    public CounterSnapshot(MetricMetadata metadata, Collection<CounterData> data) {
        super(metadata);
        this.data = data;
    }

    public Collection<CounterData> getData() {
        return data;
    }

    public static final class CounterData extends MetricData {

        private final double value;
        private final Exemplar exemplar;
        private final long createdTimeMillis;

        public CounterData(double value, Labels labels, Exemplar exemplar, long createdTimeMillis) {
            super(labels);
            this.value = value;
            this.exemplar = exemplar;
            this.createdTimeMillis = createdTimeMillis;
        }

        public double getValue() {
            return value;
        }

        public Exemplar getExemplar() {
            return exemplar;
        }

        public long getCreatedTimeMillis() {
            return createdTimeMillis;
        }

        @Override
        void validate() {
            if (value < 0) {
                throw new IllegalArgumentException(value + ": counters cannot have negative values");
            }
        }
    }
}
