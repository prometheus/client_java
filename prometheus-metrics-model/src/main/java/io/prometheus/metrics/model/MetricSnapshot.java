package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
        // According to the OpenMetrics standard the metric data SHOULD have the same label names.
        // However, this is a SHOULD and not a MUST, so this is not enforced.
        // So let's just make sure that labels (including name and value) are unique.
        // Data is already sorted by labels, so if there are duplicates they will be next to each other.
        for (int i=0; i<data.size()-1; i++) {
            if (data.get(i).getLabels().equals(data.get(i+1).getLabels())) {
                throw new IllegalArgumentException("Duplicate labels in metric data.");
            }
        }
    }

    public static abstract class Builder<T extends Builder<T>> {

        private String name;
        private String help;
        private Unit unit;

        /**
         * The name is required. If a {@link #withUnit(Unit) unit} is present, the metric name should include the
         * unit as a suffix (in <a href="https://openmetrics.io/">OpenMetrics</a> the unit suffix is required,
         * but this library does not enforce this). The name must not include the {@code _total} suffix for counters or
         * the {@code _info} suffix for info metrics.
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
