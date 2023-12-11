package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Example:
 * <pre>{@code
 * MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
 *
 * GaugeWithCallback.builder()
 *     .name("jvm_memory_bytes_used")
 *     .help("Used bytes of a given JVM memory area.")
 *     .unit(Unit.BYTES)
 *     .labelNames("area")
 *     .callback(callback -> {
 *         callback.call(memoryBean.getHeapMemoryUsage().getUsed(), "heap");
 *         callback.call(memoryBean.getNonHeapMemoryUsage().getUsed(), "nonheap");
 *     })
 *     .register();
 * }</pre>
 */
public class GaugeWithCallback extends CallbackMetric {

    @FunctionalInterface
    public interface Callback {
        void call(double value, String... labelValues);
    }

    private final Consumer<Callback> callback;

    private GaugeWithCallback(Builder builder) {
        super(builder);
        this.callback = builder.callback;
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    @Override
    public GaugeSnapshot collect() {
        List<GaugeSnapshot.GaugeDataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept((value, labelValues) -> {
            dataPoints.add(new GaugeSnapshot.GaugeDataPointSnapshot(value, makeLabels(labelValues), null, 0L));
        });
        return new GaugeSnapshot(getMetadata(), dataPoints);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties properties) {
        return new Builder(properties);
    }

    public static class Builder extends CallbackMetric.Builder<GaugeWithCallback.Builder, GaugeWithCallback> {

        private Consumer<Callback> callback;

        public Builder callback(Consumer<Callback> callback) {
            this.callback = callback;
            return self();
        }

        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        @Override
        public GaugeWithCallback build() {
            return new GaugeWithCallback(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
