package io.prometheus.metrics.core;

import io.prometheus.metrics.config.MetricProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exemplars.ExemplarSampler;
import io.prometheus.metrics.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.DiscreteEventObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.DoubleSupplier;

public class Counter extends ObservingMetric<DiscreteEventObserver, Counter.CounterData> implements DiscreteEventObserver {

    private final boolean exemplarsEnabled;
    private final ExemplarSamplerConfig exemplarSamplerConfig;

    private Counter(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricProperties[] properties = getMetricProperties(builder, prometheusProperties);
        exemplarsEnabled = getConfigProperty(properties, MetricProperties::getExemplarsEnabled);
        if (exemplarsEnabled) {
            exemplarSamplerConfig = new ExemplarSamplerConfig(prometheusProperties.getExemplarConfig(), 1);
        } else {
            exemplarSamplerConfig = null;
        }
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return exemplarsEnabled;
    }

    @Override
    public void inc(double amount) {
        getNoLabels().inc(amount);
    }

    @Override
    public void incWithExemplar(double amount, Labels labels) {
        getNoLabels().incWithExemplar(amount, labels);
    }


    @Override
    protected CounterData newMetricData() {
        if (isExemplarsEnabled()) {
            return new CounterData(new ExemplarSampler(exemplarSamplerConfig));
        } else {
            return new CounterData(null);
        }
    }

    @Override
    protected CounterSnapshot collect(List<Labels> labels, List<CounterData> metricData) {
        List<CounterSnapshot.CounterData> data = new ArrayList<>(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new CounterSnapshot(getMetadata(), data);
    }

    @Override
    public CounterSnapshot collect() {
        return (CounterSnapshot) super.collect();
    }

    public static Builder newBuilder() {
        return new Builder(PrometheusProperties.getInstance());
    }

    public static Builder newBuilder(PrometheusProperties config) {
        return new Builder(config);
    }

    private static String normalizeName(String name) {
        if (name != null && name.endsWith("_total")) {
            name = name.substring(0, name.length() - 6);
        }
        return name;
    }

    class CounterData extends MetricData<DiscreteEventObserver> implements DiscreteEventObserver {

        private final DoubleAdder value = new DoubleAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private final ExemplarSampler exemplarSampler; // null if isExemplarsEnabled() is false

        private CounterData(ExemplarSampler exemplarSampler) {
            this.exemplarSampler = exemplarSampler;
        }

        @Override
        public void inc(double amount) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplarSampler.observe(amount);
            }
        }

        @Override
        public void incWithExemplar(double amount, Labels labels) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        private void validateAndAdd(double amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Negative increment " + amount + " is illegal for Counter metrics.");
            }
            value.add(amount);
        }

        private CounterSnapshot.CounterData collect(Labels labels) {
            // Read the exemplar first. Otherwise, there is a race condition where you might
            // see an Exemplar for a value that's not represented in getValue() yet.
            // If there are multiple Exemplars (by default it's just one), use the oldest
            // so that we don't violate min age.
            Exemplar oldest = null;
            if (exemplarSampler != null) {
                for (Exemplar exemplar : exemplarSampler.collect()) {
                    if (oldest == null || exemplar.getTimestampMillis() < oldest.getTimestampMillis()) {
                        oldest = exemplar;
                    }
                }
            }
            return new CounterSnapshot.CounterData(value.sum(), labels, oldest, createdTimeMillis);
        }

        @Override
        public DiscreteEventObserver toObserver() {
            return this;
        }
    }

    public static class Builder extends ObservingMetric.Builder<Builder, Counter> {

        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        @Override
        public Builder withName(String name) {
            return super.withName(normalizeName(name));
        }

        @Override
        public Counter build() {
            return new Counter(this, properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static class FromCallback extends Metric {

        private final DoubleSupplier callback;
        private final long createdTimeMillis = System.currentTimeMillis();

        private FromCallback(io.prometheus.metrics.core.Counter.FromCallback.Builder builder) {
            super(builder);
            this.callback = builder.callback;
        }

        @Override
        public CounterSnapshot collect() {
            return new CounterSnapshot(getMetadata(), Collections.singletonList(new CounterSnapshot.CounterData(
                    callback.getAsDouble(),
                    constLabels,
                    null,
                    createdTimeMillis
            )));
        }

        public static class Builder extends Metric.Builder<io.prometheus.metrics.core.Counter.FromCallback.Builder, io.prometheus.metrics.core.Counter.FromCallback> {

            private DoubleSupplier callback;

            private Builder(PrometheusProperties config) {
                super(Collections.emptyList(), config);
            }

            public io.prometheus.metrics.core.Counter.FromCallback.Builder withCallback(DoubleSupplier callback) {
                this.callback = callback;
                return this;
            }

            @Override
            public io.prometheus.metrics.core.Counter.FromCallback build() {
                return new io.prometheus.metrics.core.Counter.FromCallback(withName(normalizeName(name)));
            }

            @Override
            protected io.prometheus.metrics.core.Counter.FromCallback.Builder self() {
                return this;
            }
        }
    }
}
