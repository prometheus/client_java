package io.prometheus.metrics.model;

import java.util.Collection;

public final class GaugeSnapshot extends MetricSnapshot {

    private final Collection<GaugeData> data;

    public GaugeSnapshot(MetricMetadata metadata, Collection<GaugeData> data) {
        super(metadata);
        this.data = data;
    }

    public static final class GaugeData extends MetricData {

        private final double value;

        public GaugeData(double value, Labels labels) {
            super(labels);
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }
}
