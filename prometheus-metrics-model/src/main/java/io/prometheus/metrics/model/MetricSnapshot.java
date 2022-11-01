package io.prometheus.metrics.model;

public abstract class MetricSnapshot {
    private final MetricMetadata metadata;

    protected MetricSnapshot(MetricMetadata metadata) {
        this.metadata = metadata;
    }
}
