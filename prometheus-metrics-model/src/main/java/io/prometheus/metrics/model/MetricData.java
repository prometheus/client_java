package io.prometheus.metrics.model;

public abstract class MetricData {
    private final Labels labels;

    protected MetricData(Labels labels) {
        this.labels = labels;
    }

    public Labels getLabels() {
        return labels;
    }
}
