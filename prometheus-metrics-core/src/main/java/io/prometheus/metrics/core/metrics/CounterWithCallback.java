package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Example:
 * <pre>{@code
 * ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
 *
 * CounterWithCallback.builder()
 *         .name("classes_loaded_total")
 *         .help("The total number of classes that have been loaded since the JVM has started execution")
 *         .callback(callback -> callback.call(classLoadingMXBean.getLoadedClassCount()))
 *         .register();
 * }</pre>
 */
public class CounterWithCallback extends CallbackMetric {

    @FunctionalInterface
    public interface Callback {
        void call(double value, String... labelValues);
    }

    private final Consumer<Callback> callback;

    private CounterWithCallback(Builder builder) {
        super(builder);
        this.callback = builder.callback;
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    @Override
    public CounterSnapshot collect() {
        List<CounterSnapshot.CounterDataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept((value, labelValues) -> {
            dataPoints.add(new CounterSnapshot.CounterDataPointSnapshot(value, makeLabels(labelValues), null, 0L));
        });
        return new CounterSnapshot(getMetadata(), dataPoints);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties properties) {
        return new Builder(properties);
    }

    public static class Builder extends CallbackMetric.Builder<CounterWithCallback.Builder, CounterWithCallback> {

        private Consumer<Callback> callback;

        public Builder callback(Consumer<Callback> callback) {
            this.callback = callback;
            return self();
        }

        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        /**
         * The {@code _total} suffix will automatically be appended if it's missing.
         * <pre>{@code
         * CounterWithCallback c1 = CounterWithCallback.builder()
         *     .name("events_total")
         *     .build();
         * CounterWithCallback c2 = CounterWithCallback.builder()
         *     .name("events")
         *     .build();
         * }</pre>
         * In the example above both {@code c1} and {@code c2} would be named {@code "events_total"} in Prometheus.
         * <p>
         * Throws an {@link IllegalArgumentException} if
         * {@link io.prometheus.metrics.model.snapshots.PrometheusNaming#isValidMetricName(String) MetricMetadata.isValidMetricName(name)}
         * is {@code false}.
         */
        @Override
        public Builder name(String name) {
            return super.name(Counter.stripTotalSuffix(name));
        }

        @Override
        public CounterWithCallback build() {
            return new CounterWithCallback(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
