package io.prometheus.metrics.model;

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
    protected final List<? extends MetricData> data;

    protected MetricSnapshot(MetricMetadata metadata, MetricData... data) {
        this(metadata, Arrays.asList(data));
    }

    protected MetricSnapshot(MetricMetadata metadata, Collection<? extends MetricData> data) {
        this.metadata = metadata;
        if (data == null) {
            throw new NullPointerException();
        }
        List<? extends MetricData> dataCopy = new ArrayList<>(data);
        dataCopy.sort(Comparator.comparing(MetricData::getLabels));
        this.data = Collections.unmodifiableList(dataCopy);
        validateLabels();
    }

    public MetricMetadata getMetadata() {
        return metadata;
    }

    public abstract List<? extends MetricData> getData();

    protected void validateLabels() {
        // Verify that labels are unique (the same set of names/values must not be used multiple times for the same metric).
        for (int i = 0; i < data.size() - 1; i++) {
            if (data.get(i).getLabels().equals(data.get(i + 1).getLabels())) {
                throw new IllegalArgumentException("Duplicate labels in metric data: " + data.get(i).getLabels());
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
         * See {@link MetricMetadata#isValidMetricName(String)} for info on valid metric names.
         */
        public T withName(String name) {
            this.name = name;
            return self();
        }

        public T withHelp(String help) {
            this.help = help;
            return self();
        }

        public T withUnit(Unit unit) {
            this.unit = unit;
            return self();
        }

        protected MetricMetadata buildMetadata() {
            return new MetricMetadata(name, help, unit);
        }

        protected abstract T self();
    }
}
