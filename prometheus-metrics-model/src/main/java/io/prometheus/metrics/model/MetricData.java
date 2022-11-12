package io.prometheus.metrics.model;

public abstract class MetricData {
    private final Labels labels;

    protected MetricData(Labels labels) {
        this.labels = labels;
        validate();
    }

    public Labels getLabels() {
        return labels;
    }

    abstract void validate();
}
