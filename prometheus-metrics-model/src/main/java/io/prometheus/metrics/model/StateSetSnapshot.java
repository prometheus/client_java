package io.prometheus.metrics.model;

import java.util.Collection;

public final class StateSetSnapshot extends MetricSnapshot {

    private final Collection<StateSetData> data;

    public StateSetSnapshot(MetricMetadata metadata, Collection<StateSetData> data) {
        super(metadata);
        this.data = data;
    }

    public static class StateSetData extends MetricData {

        protected StateSetData(Labels labels) {
            super(labels);
        }
    }
}
