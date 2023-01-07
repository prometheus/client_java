package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public abstract class MetricSnapshot {
    private final MetricMetadata metadata;
    protected final List<? extends MetricData> data;

    protected MetricSnapshot(MetricMetadata metadata, MetricData... data) {
        this(metadata, Arrays.asList(data));
    }

    protected MetricSnapshot(MetricMetadata metadata, Collection<? extends MetricData> data) {
        this.metadata = metadata;
        this.data = new ArrayList<>(data);
        this.data.sort(Comparator.comparing(MetricData::getLabels));
        validateLabels(this.data);
    }

    public MetricMetadata getMetadata() {
        return metadata;
    }

    public abstract List<? extends MetricData> getData();

    protected void validateLabels(List<? extends MetricData> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " cannot have empty data.");
        }
        for (int i=0; i<data.size(); i++) {
            for (int j=0; j<data.size(); j++) {
                if (i != j) {
                    if (!data.get(i).getLabels().hasSameNames(data.get(j).getLabels())) {
                        throw new IllegalArgumentException("All labels for a snapshot must have the same names.");
                    }
                    if (data.get(i).getLabels().hasSameValues(data.get(j).getLabels())) {
                        throw new IllegalArgumentException("Can't have different metric data with the same label values.");
                    }
                }
            }
        }
    }
}
