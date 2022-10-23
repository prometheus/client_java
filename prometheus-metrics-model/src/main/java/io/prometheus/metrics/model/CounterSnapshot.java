package io.prometheus.metrics.model;

public abstract class CounterSnapshot extends Snapshot {

    public abstract double getValue();

    public abstract Exemplar getExemplar();

    public abstract long getCreatedTimeMillis();
}
