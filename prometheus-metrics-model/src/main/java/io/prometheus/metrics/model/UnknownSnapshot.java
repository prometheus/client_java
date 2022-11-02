package io.prometheus.metrics.model;

import java.util.Collection;

public final class UnknownSnapshot extends MetricSnapshot {
    private final Collection<UnknownData> data;
    public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownData> data) {
        super(metadata);
        this.data = data;
    }

    public static class UnknownData extends MetricData {
        protected UnknownData(Labels labels) {
            super(labels);
        }
    }
}
