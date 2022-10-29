package io.prometheus.metrics.model;

public abstract class Snapshot {
    private final Labels labels;

    protected Snapshot(Labels labels) {
        this.labels = labels;
    }

    public Labels getLabels() {
        return labels;
    }
}
