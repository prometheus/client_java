package io.prometheus.metrics.model;

import java.util.Collection;

public final class InfoSnapshot extends MetricSnapshot {

    private final Collection<InfoData> data;
    public InfoSnapshot(MetricMetadata metadata, Collection<InfoData> data) {
        super(metadata);
        this.data = data;
    }

    public static class InfoData extends MetricData {
        protected InfoData(Labels labels) {
            super(labels);
        }

        @Override
        void validate() {}
    }
}
