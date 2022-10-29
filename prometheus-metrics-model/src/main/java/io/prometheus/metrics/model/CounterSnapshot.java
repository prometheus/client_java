package io.prometheus.metrics.model;

public final class CounterSnapshot extends Snapshot {

    private final double value;
    private final Exemplar exemplar;
    private final long createdTimeMillis;

    public CounterSnapshot(double value, Labels labels, Exemplar exemplar, long createdTimeMillis) {
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

}
