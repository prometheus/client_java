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
        List<? extends MetricData> dataCopy = new ArrayList<>(data);
        dataCopy.sort(Comparator.comparing(MetricData::getLabels));
        this.data = Collections.unmodifiableList(dataCopy);
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
