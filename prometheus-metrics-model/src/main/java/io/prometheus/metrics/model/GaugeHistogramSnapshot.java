package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.List;

public final class GaugeHistogramSnapshot extends MetricSnapshot {

    public GaugeHistogramSnapshot(MetricMetadata metadata, Collection<GaugeHistogramData> data) {
        super(metadata, data);
    }

    @Override
    public List<GaugeHistogramData> getData() {
        return (List<GaugeHistogramData>) data;
    }

    public static class GaugeHistogramData extends MetricData {
        protected GaugeHistogramData(Labels labels, long timestampMillis) {
            super(labels, 0L, timestampMillis);
            validate();
            // TODO: Define data model, or re-use histogram model.
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
        }
    }
}
