package io.prometheus.metrics.core.registry;

import io.prometheus.metrics.model.MetricSnapshots;

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
    MetricSnapshots collect();
}
