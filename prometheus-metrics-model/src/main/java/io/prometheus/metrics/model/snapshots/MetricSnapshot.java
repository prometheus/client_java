package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for metric snapshots.
 */
public abstract class MetricSnapshot {

    private final MetricMetadata metadata;
    protected final List<? extends DataPointSnapshot> dataPoints;

    protected MetricSnapshot(MetricMetadata metadata, DataPointSnapshot... dataPoints) {
        this(metadata, Arrays.asList(dataPoints));
    }

    protected MetricSnapshot(MetricMetadata metadata, Collection<? extends DataPointSnapshot> dataPoints) {
        if (metadata == null) {
            throw new NullPointerException("metadata");
        }
        if (dataPoints == null) {
            throw new NullPointerException("dataPoints");
        }
        this.metadata = metadata;
        List<? extends DataPointSnapshot> dataCopy = new ArrayList<>(dataPoints);
        dataCopy.sort(Comparator.comparing(DataPointSnapshot::getLabels));
        this.dataPoints = Collections.unmodifiableList(dataCopy);
        validateLabels();
    }

    public MetricMetadata getMetadata() {
        return metadata;
    }

    public abstract List<? extends DataPointSnapshot> getDataPoints();

    protected void validateLabels() {
        // Verify that labels are unique (the same set of names/values must not be used multiple times for the same metric).
        for (int i = 0; i < dataPoints.size() - 1; i++) {
            if (dataPoints.get(i).getLabels().equals(dataPoints.get(i + 1).getLabels())) {
                throw new DuplicateLabelsException(metadata, dataPoints.get(i).getLabels());
            }
        }
        // Should we verify that all entries in data have the same label names?
        // No. They should have the same label names, but according to OpenMetrics this is not a MUST.
    }

    public static abstract class Builder<T extends Builder<T>> {

        private String name;
        private String help;
        private Unit unit;

        /**
         * The name is required.
         * If the name is missing or invalid, {@code build()} will throw an {@link IllegalArgumentException}.
         * See {@link PrometheusNaming#isValidMetricName(String)} for info on valid metric names.
         */
        public T name(String name) {
            this.name = name;
            return self();
        }

        public T help(String help) {
            this.help = help;
            return self();
        }

        public T unit(Unit unit) {
            this.unit = unit;
            return self();
        }

        protected MetricMetadata buildMetadata() {
            return new MetricMetadata(name, help, unit);
        }

        protected abstract T self();
    }
}
