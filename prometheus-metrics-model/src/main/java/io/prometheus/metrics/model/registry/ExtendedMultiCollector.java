package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshots;

public abstract class ExtendedMultiCollector implements MultiCollector {

	@Override
	public MetricSnapshots collect() {
		return new MetricSnapshots();
	}

	@Override
	public MetricSnapshots collect(PrometheusScrapeRequest scrapeRequest) {
		return collectMetricSnapshots(scrapeRequest);
	}

	/**
	 * Override to implement multi-target exporter pattern
	 * @param scrapeRequest
	 *            the (http) request context triggering metrics collection
	 */
	protected abstract MetricSnapshots collectMetricSnapshots(PrometheusScrapeRequest scrapeRequest);

	
}
