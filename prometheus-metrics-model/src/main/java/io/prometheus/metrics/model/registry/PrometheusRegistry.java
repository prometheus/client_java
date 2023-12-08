package io.prometheus.metrics.model.registry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import io.prometheus.metrics.model.snapshots.MetricSnapshots;

/** Collector that allows dynamic registration/unregistration of scrape sources.
 *
 * Consider to use CollectorBuilder.CompositeCollector if no dynamic registration is needed.
  */
public class PrometheusRegistry implements Collector {

	public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

	private final List<Collector> collectors = new CopyOnWriteArrayList<>();

	public void register(Collector collector) {
		collectors.add(collector);
	}

	public void unregister(Collector collector) {
		collectors.remove(collector);
	}

	/** Use collect() instead. */
	public MetricSnapshots scrape(Predicate<String> includedNames) {
		return collect(includedNames, null);
	}

	/** Use collect() instead. */
	public MetricSnapshots scrape() {
		return collect(MetricNameFilter.ALLOW_ALL, null);
	}

	/** Use collect() instead. */
	public MetricSnapshots scrape(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
		return collect(includedNames, scrapeRequest);
	}

	public MetricSnapshots collect(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
		MetricSnapshots.Builder result = MetricSnapshots.builder();
		for (Collector collector : collectors) {
			result.metricSnapshots(collector.collect(includedNames, scrapeRequest));
		}
		return result.build();
	}
}
