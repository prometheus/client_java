package io.prometheus.metrics.model;

public abstract class GaugeSnapshot extends Snapshot {
    public abstract double getValue();
}
