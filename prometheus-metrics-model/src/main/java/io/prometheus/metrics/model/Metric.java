package io.prometheus.metrics.model;

import java.util.Collection;

public final class Metric {

    private final MetricMetadata metadata;
    private final Collection<? extends Snapshot> snapshots;

    public Metric(MetricMetadata metadata, Collection<? extends Snapshot> snapshots) {
        this.metadata = metadata;
        this.snapshots = snapshots;
    }

    public MetricMetadata getMetadata() {
        return metadata;
    }

    public Collection<? extends Snapshot> getSnapshots() {
        return snapshots;
    }
}
