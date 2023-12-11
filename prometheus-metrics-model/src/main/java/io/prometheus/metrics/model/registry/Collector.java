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
		return collect();
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
     * Like {@link #collect(Predicate)}, but with support for multi-target pattern.
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
     * This is called in two places:
     * <ol>
     * <li>During registration to check if a metric with that name already exists.</li>
     * <li>During scrape to check if this collector can be skipped because a name filter is present and the metric name is excluded.</li>
     * </ol>
     * Returning {@code null} means checks are omitted (registration the metric always succeeds),
     * and the collector is always scraped (the result is dropped after scraping if a name filter is present and
     * the metric name is excluded).
     * <p>
     * If your metric has a name that does not change at runtime it is a good idea to overwrite this and return the name.
     * <p>
     * All metrics in {@code prometheus-metrics-core} override this.
     */
    default String getPrometheusName() {
        return null;
    }
}
