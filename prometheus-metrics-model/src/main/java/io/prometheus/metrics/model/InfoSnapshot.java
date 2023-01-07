package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.List;

public final class InfoSnapshot extends MetricSnapshot {

    public InfoSnapshot(MetricMetadata metadata, Collection<InfoData> data) {
        super(metadata, data);
    }

    @Override
    public List<InfoData> getData() {
        return (List<InfoData>) data;
    }

    public static class InfoData extends MetricData {
        public InfoData(Labels labels, long timestampMillis) {
            super(labels, 0L, timestampMillis);
            validate();
        }

        @Override
        void validate() {}
    }
}
