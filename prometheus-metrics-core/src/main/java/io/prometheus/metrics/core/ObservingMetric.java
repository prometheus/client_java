package io.prometheus.metrics.core;

import io.prometheus.metrics.exemplars.DefaultExemplarConfig;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.observer.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public abstract class ObservingMetric<O extends Observer, V extends MetricData<O>> extends Metric {
    private final String[] labelNames;
    private final Boolean exemplarsEnabled;
    protected final ExemplarConfig exemplarConfig;

    /**
     * Map from variableLabelValues to MetricData.
     */
    private final ConcurrentHashMap<List<String>, V> data = new ConcurrentHashMap<>();

    /**
     * Shortcut to data.get(Collections.emptyList())
     */
    private volatile V noLabels;

    protected ObservingMetric(Builder<?, ?> builder) {
        super(builder);
        this.labelNames = Arrays.copyOf(builder.labelNames, builder.labelNames.length);
        this.exemplarsEnabled = builder.exemplarsEnabled;
        this.exemplarConfig = builder.exemplarConfig;
    }

    /**
     * labels and metricData have the same size. labels.get(i) are the labels for metricData.get(i).
     */
    protected abstract MetricSnapshot collect(List<Labels> labels, List<V> metricData);

    public io.prometheus.metrics.model.MetricSnapshot collect() {
        if (labelNames.length == 0 && data.size() == 0) {
            // This is a metric without labels that has not been used yet. Initialize the data on the fly.
            withLabels();
        }
        List<Labels> labels = new ArrayList<>(data.size());
        List<V> metricData = new ArrayList<>(data.size());
        for (Map.Entry<List<String>, V> entry : data.entrySet()) {
            String[] variableLabelValues = entry.getKey().toArray(new String[labelNames.length]);
            labels.add(constLabels.merge(labelNames, variableLabelValues));
            metricData.add(entry.getValue());
        }
        return collect(labels, metricData);
    }

    public O withLabels(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            throw new IllegalArgumentException("Expected " + labelNames.length + " label values, but got " + labelValues.length + ".");
        }
        return data.computeIfAbsent(Arrays.asList(labelValues), l -> newMetricData()).toObserver();
    }

    // TODO: Remove automatically if label values have not been used in a while?
    public void remove(String... labelValues) {
        data.remove(Arrays.asList(labelValues));
    }

    protected abstract V newMetricData();

    protected V getNoLabels() {
        if (noLabels == null) {
            // Note that this will throw an IllegalArgumentException if labelNames is not empty.
            noLabels = (V) withLabels();
        }
        return noLabels;
    }

    protected boolean isExemplarsEnabled() {
        if (exemplarsEnabled != null) {
            return exemplarsEnabled;
        } else {
            return DefaultExemplarConfig.isEnabledByDefault();
        }
    }

    protected boolean hasSpanContextSupplier() {
        return exemplarConfig != null ? exemplarConfig.hasSpanContextSupplier() : DefaultExemplarConfig.hasSpanContextSupplier();
    }

    static abstract class Builder<B extends Builder<B, M>, M extends ObservingMetric<?,?>> extends Metric.Builder<B, M> {
        private String[] labelNames = new String[0];
        private Boolean exemplarsEnabled;
        private ExemplarConfig exemplarConfig;

        protected Builder(List<String> illegalLabelNames) {
            super(illegalLabelNames);
        }

        public B withLabelNames(String... labelNames) {
            for (String labelName : labelNames) {
                if (!Labels.isValidLabelName(labelName)) {
                    throw new IllegalArgumentException(labelName + ": illegal label name");
                }
                if (illegalLabelNames.contains(labelName)) {
                    throw new IllegalArgumentException(labelName + ": illegal label name for this metric type");
                }
            }
            this.labelNames = labelNames;
            return self();
        }

        public B withExemplars() {
            this.exemplarsEnabled = TRUE;
            return self();
        }

        public B withoutExemplars() {
            this.exemplarsEnabled = FALSE;
            return self();
        }

        /**
         * Enable exemplars and provide an {@link ExemplarConfig}.
         */
        public B withExemplarConfig(ExemplarConfig exemplarConfig) {
            this.exemplarConfig = exemplarConfig;
            return withExemplars();
        }
    }
}
