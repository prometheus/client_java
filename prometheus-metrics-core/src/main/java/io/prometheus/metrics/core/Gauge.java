package io.prometheus.metrics.core;

import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.GaugingObserver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.DoubleSupplier;

public class Gauge extends ObservingMetric<GaugingObserver, Gauge.GaugeData> implements GaugingObserver {

    private Gauge(Builder builder) {
        super(builder);
    }

    @Override
    public void inc(double amount) {
        getNoLabels().inc(amount);
    }

    @Override
    public void set(double value) {
        getNoLabels().set(value);
    }

    @Override
    protected GaugeData newMetricData() {
        return new GaugeData();
    }

    static class GaugeData implements GaugingObserver, MetricData<GaugingObserver> {

        private final DoubleAdder value = new DoubleAdder();

        @Override
        public void inc(double amount) {
            value.add(amount);
        }

        @Override
        public void set(double value) {
            // todo: quick hack when switching to Java 8's built-in DoubleAdder. Needs investigation.
            this.value.add(value - this.value.sum());
        }

        @Override
        public GaugeSnapshot snapshot(Labels labels) {
            return new GaugeSnapshot(value.sum(), labels);
        }

        @Override
        public GaugingObserver toObserver() {
            return this;
        }
    }

    public static class Builder extends ObservingMetric.Builder<Builder, Gauge> {

        private Builder() {
        }

        @Override
        public Gauge build() {
            return new Gauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static class FromCallback extends Metric {

        private final DoubleSupplier callback;

        private FromCallback(Gauge.FromCallback.Builder builder) {
            super(builder);
            this.callback = builder.callback;
        }

        @Override
        public Collection<GaugeSnapshot> snapshot() {
            return Collections.singletonList(new GaugeSnapshot(callback.getAsDouble(), constLabels));
        }

        public static class Builder extends Metric.Builder<Gauge.FromCallback.Builder, Gauge.FromCallback> {

            private DoubleSupplier callback;

            private Builder() {
            }

            public Gauge.FromCallback.Builder withCallback(DoubleSupplier callback) {
                this.callback = callback;
                return this;
            }

            @Override
            public Gauge.FromCallback build() {
                return new Gauge.FromCallback(this);
            }

            @Override
            protected Gauge.FromCallback.Builder self() {
                return this;
            }
        }
    }
}
