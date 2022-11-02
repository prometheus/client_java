package io.prometheus.metrics.model;

import java.util.Collection;

public final class Metric {

    private final MetricMetadata metadata;
    private final Collection<? extends MetricSnapshot> snapshots;

    public Metric(MetricMetadata metadata, Collection<? extends MetricSnapshot> snapshots) {
        this.metadata = metadata;
        this.snapshots = snapshots;
    }

    public MetricMetadata getMetadata() {
        return metadata;
    }

    public Collection<? extends MetricSnapshot> getSnapshots() {
        return snapshots;
    }
}
