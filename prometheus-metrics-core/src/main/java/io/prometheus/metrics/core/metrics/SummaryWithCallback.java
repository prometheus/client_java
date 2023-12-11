package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Example:
 * <pre>{@code
 * double MILLISECONDS_PER_SECOND = 1E3;
 *
 * SummaryWithCallback.builder()
 *         .name("jvm_gc_collection_seconds")
 *         .help("Time spent in a given JVM garbage collector in seconds.")
 *         .unit(Unit.SECONDS)
 *         .labelNames("gc")
 *         .callback(callback -> {
 *             for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
 *                 callback.call(
 *                         gc.getCollectionCount(),
 *                         gc.getCollectionTime() / MILLISECONDS_PER_SECOND,
 *                         Quantiles.EMPTY,
 *                         gc.getName()
 *                 );
 *             }
 *         })
 *         .register();
 * }</pre>
 */
public class SummaryWithCallback extends CallbackMetric {

    @FunctionalInterface
    public interface Callback {
        void call(long count, double sum, Quantiles quantiles, String... labelValues);
    }

    private final Consumer<Callback> callback;

    private SummaryWithCallback(Builder builder) {
        super(builder);
        this.callback = builder.callback;
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    @Override
    public SummarySnapshot collect() {
        List<SummarySnapshot.SummaryDataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept((count, sum, quantiles, labelValues) -> {
            dataPoints.add(new SummarySnapshot.SummaryDataPointSnapshot(count, sum, quantiles, makeLabels(labelValues), Exemplars.EMPTY, 0L));
        });
        return new SummarySnapshot(getMetadata(), dataPoints);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties properties) {
        return new Builder(properties);
    }

    public static class Builder extends CallbackMetric.Builder<SummaryWithCallback.Builder, SummaryWithCallback> {

        private Consumer<Callback> callback;

        public Builder callback(Consumer<Callback> callback) {
            this.callback = callback;
            return self();
        }

        private Builder(PrometheusProperties properties) {
            super(Collections.singletonList("quantile"), properties);
        }

        @Override
        public SummaryWithCallback build() {
            return new SummaryWithCallback(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
