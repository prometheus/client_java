package io.prometheus.metrics.model;

import java.util.Collection;

public final class GaugeSnapshot extends MetricSnapshot {

    private final Collection<GaugeData> data;

    public GaugeSnapshot(MetricMetadata metadata, Collection<GaugeData> data) {
        super(metadata);
        this.data = data;
    }

    public Collection<GaugeData> getData() {
        return data;
    }

    public static final class GaugeData extends MetricData {

        private final double value;
        private final Exemplar exemplar;

        public GaugeData(double value, Labels labels, Exemplar exemplar) {
            super(labels);
            this.value = value;
            this.exemplar = exemplar;
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
