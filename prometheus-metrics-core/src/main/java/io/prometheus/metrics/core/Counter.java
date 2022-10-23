package io.prometheus.metrics.core;

import io.prometheus.metrics.exemplars.CounterExemplarSampler;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.DiscreteEventObserver;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.DoubleSupplier;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Counter extends ObservingMetric<DiscreteEventObserver, Counter.CounterData> implements DiscreteEventObserver {
    /**
     * null means default from ExemplarConfig applies.
     */
    private final Boolean exemplarsEnabled;
    private final CounterExemplarSampler exemplarSampler;

    private Counter(Builder builder) {
        super(builder);
        this.exemplarsEnabled = builder.exemplarsEnabled;
        this.exemplarSampler = builder.exemplarSampler;
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

    public static Builder builder() {
        return new Builder();
    }

    class CounterData implements DiscreteEventObserver, MetricData<DiscreteEventObserver> {

        private final DoubleAdder value = new DoubleAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private final AtomicReference<Exemplar> exemplar = new AtomicReference<Exemplar>();

        @Override
        public void inc(double amount) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                Exemplar prev, next;
                do {
                    prev = exemplar.get();
                    next = sampleNextExemplar(amount, prev);
                } while (next != null && !exemplar.compareAndSet(prev, next));
            }
        }

        @Override
        public void incWithExemplar(double amount, Labels labels) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                exemplar.set(new Exemplar(amount, labels, System.currentTimeMillis()));
            }
        }

        private void validateAndAdd(double amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Amount " + amount + " is negative.");
            }
            value.add(amount);
        }

        private boolean isExemplarsEnabled() {
            if (exemplarsEnabled != null) {
                return exemplarsEnabled;
            } else {
                return ExemplarConfig.isExemplarsEnabled();
            }
        }

        private Exemplar sampleNextExemplar(double amt, Exemplar prev) {
            if (exemplarSampler != null) {
                return exemplarSampler.sample(amt, prev);
            } else {
                CounterExemplarSampler exemplarSampler = ExemplarConfig.getCounterExemplarSampler();
                if (exemplarSampler != null) {
                    return exemplarSampler.sample(amt, prev);
                } else {
                    return null;
                }
            }
        }

        @Override
        public CounterSnapshot snapshot(Labels labels) {
            return new Snapshot(this, labels);
        }

        @Override
        public DiscreteEventObserver toObserver() {
            return this;
        }
    }

    private static class Snapshot extends CounterSnapshot {

        private final double value;
        private final long createdTimeMillis;
        private final Labels labels;
        private final io.prometheus.metrics.model.Exemplar exemplar;

        private Snapshot(CounterData data, Labels labels) {
            // Important: Read the exemplar first. Otherwise, there is a race condition where you might
            // see an Exemplar for a value that's not represented in getValue() yet.
            this.exemplar = data.exemplar.get();
            this.value = data.value.sum();
            this.createdTimeMillis = data.createdTimeMillis;
            this.labels = labels;
        }

        @Override
        public double getValue() {
            return value;
        }

        @Override
        public Labels getLabels() {
            return labels;
        }

        @Override
        public io.prometheus.metrics.model.Exemplar getExemplar() {
            return exemplar;
        }

        @Override
        public long getCreatedTimeMillis() {
            return createdTimeMillis;
        }

    }

    public static class Builder extends ObservingMetric.Builder<Builder, Counter> {

        private Boolean exemplarsEnabled;
        private CounterExemplarSampler exemplarSampler;

        private Builder() {
        }

        @Override
        public Counter build() {
            return new Counter(this);
        }

        public Builder withExemplars() {
            this.exemplarsEnabled = TRUE;
            return this;
        }

        public Builder withoutExemplars() {
            this.exemplarsEnabled = FALSE;
            return this;
        }

        /**
         * Enable exemplars and provide a custom {@link CounterExemplarSampler}.
         */
        public Builder withExemplarSampler(CounterExemplarSampler exemplarSampler) {
            this.exemplarSampler = exemplarSampler;
            return withExemplars();
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
        public Collection<CounterSnapshot> snapshot() {
            return Arrays.asList(new CounterSnapshot[]{new CounterSnapshot() {
                @Override
                public double getValue() {
                    return callback.getAsDouble();
                }

                @Override
                public Exemplar getExemplar() {
                    return null;
                }

                @Override
                public long getCreatedTimeMillis() {
                    return createdTimeMillis;
                }

                @Override
                public Labels getLabels() {
                    return constLabels;
                }
            }});
        }

        public static class Builder extends Metric.Builder<io.prometheus.metrics.core.Counter.FromCallback.Builder, io.prometheus.metrics.core.Counter.FromCallback> {

            private DoubleSupplier callback;

            private Builder() {
            }

            public io.prometheus.metrics.core.Counter.FromCallback.Builder withCallback(DoubleSupplier callback) {
                this.callback = callback;
                return this;
            }

            @Override
            public io.prometheus.metrics.core.Counter.FromCallback build() {
                return new io.prometheus.metrics.core.Counter.FromCallback(this);
            }

            @Override
            protected io.prometheus.metrics.core.Counter.FromCallback.Builder self() {
                return this;
            }
        }
    }
}
