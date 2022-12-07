package io.prometheus.metrics.model;

import java.util.Collection;

public final class InfoSnapshot extends MetricSnapshot {

    private final Collection<InfoData> data;
    public InfoSnapshot(MetricMetadata metadata, Collection<InfoData> data) {
        super(metadata);
        this.data = data;
    }

    public Collection<InfoData> getData() {
        return data;
    }

    public static class InfoData extends MetricData {
        public InfoData(Labels labels) {
            super(labels);
        }

        @Override
        void validate() {}
    }
}
