package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.List;

public final class UnknownSnapshot extends MetricSnapshot {
    public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownData> data) {
        super(metadata, data);
    }

    @Override
    public List<UnknownData> getData() {
        return (List<UnknownData>) data;
    }

    public static class UnknownData extends MetricData {
        protected UnknownData(Labels labels, long timestampMillis) {
            super(labels, 0L, timestampMillis);
            validate();
        }

        @Override
        void validate() {}
    }
}
