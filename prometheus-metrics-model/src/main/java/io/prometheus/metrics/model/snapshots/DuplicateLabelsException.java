package io.prometheus.metrics.model.snapshots;

/**
 * Thrown when a collector tries to create a {@link MetricSnapshot}
 * where multiple data points have the same labels (same label names and label values).
 */
public class DuplicateLabelsException extends IllegalArgumentException {

    private final MetricMetadata metadata;
    private final Labels labels;

    public DuplicateLabelsException(MetricMetadata metadata, Labels labels) {
        super("Duplicate labels for metric \"" + metadata.getName() + "\": " + labels);
        this.metadata = metadata;
        this.labels = labels;
    }

    public MetricMetadata getMetadata() {
        return metadata;
    }

    public Labels getLabels() {
        return labels;
    }
}