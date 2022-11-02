package io.prometheus.metrics.model;

import java.util.Collection;

public final class GaugeHistogramSnapshot extends MetricSnapshot {
    private final Collection<GaugeHistogramData> data;

    public GaugeHistogramSnapshot(MetricMetadata metadata, Collection<GaugeHistogramData> data) {
        super(metadata);
        this.data = data;
    }
    public static class GaugeHistogramData extends MetricData {
        protected GaugeHistogramData(Labels labels) {
            super(labels);
        }
        // TODO: Define data model, or re-use histogram model.
    }
}
