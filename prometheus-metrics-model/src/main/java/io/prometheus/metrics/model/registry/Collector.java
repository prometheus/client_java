package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;

import java.util.function.Predicate;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

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
     * Provides Collector with the details of the request issued by Prometheus to allow multi-target pattern implementation
     * Override to implement request dependent logic to provide MetricSnapshot
     */
	default MetricSnapshot collect(PrometheusScrapeRequest scrapeRequest) {
		MetricSnapshot result = collect();
		return result;
	}
    
    /**
     * Like {@link #collect()}, but returns {@code null} if {@code includedNames.test(name)} is {@code false}.
     * <p>
     * Override this if there is a more efficient way than first collecting the snapshot and then discarding it.
     */
    default MetricSnapshot collect(Predicate<String> includedNames) {
        MetricSnapshot result = collect();
        if (includedNames.test(result.getMetadata().getPrometheusName())) {
            return result;
        } else {
            return null;
        }
    }

    /**
     * Like {@link #collect(includedNames)}, but with support for multi-target pattern.
     * <p>
     * Override this if there is a more efficient way than first collecting the snapshot and then discarding it.
     */
    default MetricSnapshot collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
        MetricSnapshot result = collect(scrapeRequest);
        if (includedNames.test(result.getMetadata().getPrometheusName())) {
            return result;
        } else {
            return null;
        }
    }

    
    /**
     * Override this and return {@code null} if a collector does not have a constant name,
     * or if you don't want this library to call {@link #collect()} during registration of this collector.
     */
    default String getPrometheusName() {
        return collect().getMetadata().getPrometheusName();
    }
}
