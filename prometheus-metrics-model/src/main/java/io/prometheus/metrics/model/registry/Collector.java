package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.function.Predicate;

/**
 * Basic interface for fetching metrics.
 * Accepts name filter (allowing to avoid fetching of unnecessary data)
 * as well as scrape request (for multi target).
 *
 * Used to be named MultiCollector in v1.1.
 */
@FunctionalInterface
public interface Collector {
    /**
     * Called when the Prometheus server scrapes metrics.
     * <p>
     * Should return only the snapshots where {@code includedNames.test(name)} is {@code true}.
     *
     * @param includedNames prometheusName filter (non-null, use MetricNameFilter.ALLOW_ALL to disable filtering)
     * @param scrapeRequest null allowed
     */
    MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest);
}
