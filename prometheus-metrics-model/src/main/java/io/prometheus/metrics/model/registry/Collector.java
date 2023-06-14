package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.List;
import java.util.function.Predicate;

/**
 * To be registered with the Prometheus collector registry.
 * See <i>Overall Structure</i> on
 * <a href="https://prometheus.io/docs/instrumenting/writing_clientlibs/">https://prometheus.io/docs/instrumenting/writing_clientlibs/</a>.
 */
@FunctionalInterface
public interface Collector {

    /**
     * Called when the Prometheus server scrapes metrics.
     */
    MetricSnapshot collect();

    /**
     * Like {@link #collect()}, but returns {@code null} if {@code excludedNames.test(name)} is {@code true}.
     * <p>
     * Override this if there is a more efficient way than first collecting the snapshot and then discarding it.
     */
    default MetricSnapshot collect(Predicate<String> excludedNames) {
        MetricSnapshot result = collect();
        if (excludedNames.test(result.getMetadata().getName())) {
            return null;
        } else {
            return result;
        }
    }

    /**
     * Override this and return {@code null} if a collector does not have a constant name (name may change between scrapes).
     */
    default String getName() {
        return collect().getMetadata().getName();
    }
}
