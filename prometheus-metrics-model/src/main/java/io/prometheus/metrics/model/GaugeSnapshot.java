package io.prometheus.metrics.model;

public final class GaugeSnapshot extends Snapshot {

    private final double value;

    public GaugeSnapshot(double value, Labels labels) {
        super(labels);
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
