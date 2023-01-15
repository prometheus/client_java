package io.prometheus.metrics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class InfoSnapshot extends MetricSnapshot {

    public InfoSnapshot(String name, InfoData... data) {
        this(new MetricMetadata(name), data);
    }

    public InfoSnapshot(String name, String help, InfoData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public InfoSnapshot(MetricMetadata metadata, InfoData... data) {
        this(metadata, Arrays.asList(data));
    }

    public InfoSnapshot(MetricMetadata metadata, Collection<InfoData> data) {
        super(metadata, data);
        if (metadata.getName().endsWith("_info")) {
            throw new IllegalArgumentException("The name of an info snapshot must not include the _info suffix");
        }
    }

    @Override
    public List<InfoData> getData() {
        return (List<InfoData>) data;
    }

    public static class InfoData extends MetricData {
        public InfoData(Labels labels) {
            this(labels, 0L);
        }

        public InfoData(Labels labels, long timestampMillis) {
            super(labels, 0L, timestampMillis);
            validate();
        }

        @Override
        void validate() {}
    }
}
