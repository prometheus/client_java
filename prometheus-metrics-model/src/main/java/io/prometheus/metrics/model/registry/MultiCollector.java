package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Like {@link Collector}, but collecting multiple Snapshots at once.
 */
@FunctionalInterface
public interface MultiCollector {

    /**
     * Called when the Prometheus server scrapes metrics.
     */
    MetricSnapshots collect();

    /**
     * Provides Collector with the details of the request issued by Prometheus to allow multi-target pattern implementation
     * Override to implement request dependent logic to provide MetricSnapshot
     */
	default MetricSnapshots collect(PrometheusScrapeRequest scrapeRequest) {
		return collect();
	}
    
    
    /**
     * Like {@link #collect()}, but returns only the snapshots where {@code includedNames.test(name)} is {@code true}.
     * <p>
     * Override this if there is a more efficient way than first collecting all snapshot and then discarding the excluded ones.
     */
    default MetricSnapshots collect(Predicate<String> includedNames) {
    	return collect(includedNames, null);
    }

    /**
     * Like {@link #collect(Predicate)}, but with support for multi-target pattern.
     * <p>
     * Override this if there is a more efficient way than first collecting the snapshot and then discarding it.
     */
    default MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
    	MetricSnapshots allSnapshots = scrapeRequest == null ? collect(): collect(scrapeRequest);
        MetricSnapshots.Builder result = MetricSnapshots.builder();
        for (MetricSnapshot snapshot : allSnapshots) {
            if (includedNames.test(snapshot.getMetadata().getPrometheusName())) {
                result.metricSnapshot(snapshot);
            }
        }
        return result.build();
    }


    /**
     * This is called in two places:
     * <ol>
     * <li>During registration to check if a metric with that name already exists.</li>
     * <li>During scrape to check if the collector can be skipped because a name filter is present and all names are excluded.</li>
     * </ol>
     * Returning an empty list means checks are omitted (registration metric always succeeds),
     * and the collector is always scraped (if a name filter is present and all names are excluded the result is dropped).
     * <p>
     * If your collector returns a constant list of metrics that have names that do not change at runtime
     * it is a good idea to overwrite this and return the names.
     */
    default List<String> getPrometheusNames() {
        return Collections.emptyList();
    }
}
