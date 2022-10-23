package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.Snapshot;
import io.prometheus.metrics.observer.Observer;
import io.prometheus.metrics.registry.PrometheusRegistry;

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

    @Override
    public Collection<Snapshot> snapshot() {
        List<Snapshot> result = new ArrayList<>(data.size());
        for (Map.Entry<List<String>, V> entry : data.entrySet()) {
            String[] variableLabelValues = entry.getKey().toArray(new String[labelNames.length]);
            result.add(entry.getValue().snapshot(constLabels.merge(labelNames, variableLabelValues)));
        }
        return result;
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
        private String[] labelNames;

        protected Builder() {}

        public B withLabelNames(String... labelNames) {
            this.labelNames = labelNames;
            return self();
        }
    }
}
