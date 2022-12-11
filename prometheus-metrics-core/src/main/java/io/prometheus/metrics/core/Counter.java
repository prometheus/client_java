package io.prometheus.metrics.core;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricType;
import io.prometheus.metrics.observer.DiscreteEventObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.DoubleSupplier;

public class Counter extends ObservingMetric<DiscreteEventObserver, Counter.CounterData> implements DiscreteEventObserver {

    private Counter(Builder builder) {
        super(builder);
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
        return new CounterData();
    }

    @Override
    protected CounterSnapshot collect(List<Labels> labels, List<CounterData> metricData) {
        List<CounterSnapshot.CounterData> data = new ArrayList<>(labels.size());
        for (int i=0; i<labels.size(); i++) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new CounterSnapshot(getMetadata(), data);
    }

    @Override
    public CounterSnapshot collect() {
        return (CounterSnapshot) super.collect();
    }

    public static Builder newBuilder() {
        return new Builder();
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

        @Override
        public void inc(double amount) {
            validateAndAdd(amount);
            if (isExemplarsEnabled() && hasSpanContextSupplier()) {
                lazyInitExemplarSampler(exemplarConfig, 1, null);
                exemplarSampler.observe(amount);
            }
        }

        @Override
        public void incWithExemplar(double amount, Labels labels) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                lazyInitExemplarSampler(exemplarConfig, 1, null);
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

        private Builder() {
            super(Collections.emptyList());
        }

        @Override
        protected MetricType getType() {
            return MetricType.COUNTER;
        }

        @Override
        public Counter build() {
            return new Counter(withName(normalizeName(name)));
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

            private Builder() {
                super(Collections.emptyList());
            }

            @Override
            protected MetricType getType() {
                return MetricType.COUNTER;
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
