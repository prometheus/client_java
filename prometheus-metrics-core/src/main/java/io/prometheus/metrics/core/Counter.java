package io.prometheus.metrics.core;

import io.prometheus.metrics.exemplars.CounterExemplarSampler;
import io.prometheus.metrics.exemplars.ExemplarSampler;
import io.prometheus.metrics.exemplars.ExemplarConfig;
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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Counter extends ObservingMetric<DiscreteEventObserver, Counter.CounterData> implements DiscreteEventObserver {
    /**
     * null means default from ExemplarConfig applies.
     */
    private final Boolean exemplarsEnabled;
    private final ExemplarConfig exemplarConfig;

    private Counter(Builder builder) {
        super(builder);
        this.exemplarsEnabled = builder.exemplarsEnabled;
        this.exemplarConfig = builder.exemplarConfig;
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
            data.add(metricData.get(i).snapshot(labels.get(i)));
        }
        return new CounterSnapshot(getMetadata(), data);
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

    class CounterData implements DiscreteEventObserver, MetricData<DiscreteEventObserver> {

        private final DoubleAdder value = new DoubleAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private volatile ExemplarSampler exemplarSampler;

        @Override
        public void inc(double amount) {
            validateAndAdd(amount);
            if (isExemplarsEnabled() && hasSpanContextSupplier()) {
                lazyInitExemplarSampler();
                exemplarSampler.observe(amount);
            }
        }

        private boolean hasSpanContextSupplier() {
            return exemplarConfig != null ? exemplarConfig.hasSpanContextSupplier() : ExemplarConfig.hasDefaultSpanContextSupplier();
        }

        // Some metrics might be statically initialized (static final Counter myCounter = ...).
        // However, some tracers (like Micrometer) may not be available at static initialization time.
        // Therefore, exemplarSampler must be lazily configured so that it will still be initialized if
        // a tracer is added later at runtime.
        // However, if no tracing is used or exemplars are disabled, this code should have almost zero overhead.
        // TODO: This is partly copy-and-past
        private void lazyInitExemplarSampler() {
                if (exemplarSampler == null) {
                    synchronized (this) {
                        if (exemplarSampler == null) {
                            ExemplarConfig config = exemplarConfig;
                            if (config == null) {
                                config = ExemplarConfig.newBuilder()
                                        .withNumberOfExemplars(1)
                                        .build();
                            } else {
                                ExemplarConfig.Builder builder = config.toBuilder();
                                if (!builder.hasBuckets() && !builder.hasNumberOfExemplars()) {
                                    config = builder.withNumberOfExemplars(1).build();
                                }
                            }
                            exemplarSampler = ExemplarSampler.newInstance(config);
                        }
                    }
                }
        }

        @Override
        public void incWithExemplar(double amount, Labels labels) {
            validateAndAdd(amount);
            if (isExemplarsEnabled()) {
                lazyInitExemplarSampler();
                exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        private void validateAndAdd(double amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("Negative increment " + amount + " is illegal for Counter metrics.");
            }
            value.add(amount);
        }

        private boolean isExemplarsEnabled() {
            if (exemplarsEnabled != null) {
                return exemplarsEnabled;
            } else {
                return ExemplarConfig.isEnabled();
            }
        }

        private CounterSnapshot.CounterData snapshot(Labels labels) {
            // Read the exemplar first. Otherwise, there is a race condition where you might
            // see an Exemplar for a value that's not represented in getValue() yet.
            Exemplar exemplar = null;
            if (exemplarSampler != null) {
                Collection<Exemplar> exemplars = exemplarSampler.collect();
                if (!exemplars.isEmpty()) {
                    exemplar = exemplars.iterator().next();
                }
            }
            return new CounterSnapshot.CounterData(value.sum(), labels, exemplar, createdTimeMillis);
        }

        @Override
        public DiscreteEventObserver toObserver() {
            return this;
        }
    }

    public static class Builder extends ObservingMetric.Builder<Builder, Counter> {

        private Boolean exemplarsEnabled;
        private ExemplarConfig exemplarConfig;

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
        public Builder withExemplarConfig(ExemplarConfig exemplarConfig) {
            this.exemplarConfig = exemplarConfig;
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
