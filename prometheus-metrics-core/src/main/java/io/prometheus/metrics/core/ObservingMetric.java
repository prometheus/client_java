package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Label;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.observer.Observer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ObservingMetric<O extends Observer, V extends MetricData<O>> extends Metric {
    private final String[] labelNames;

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
    }

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

    static abstract class Builder<B extends Builder<B, M>, M extends ObservingMetric<?,?>> extends Metric.Builder<B, M> {
        private String[] labelNames = new String[0];

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
    }
}
