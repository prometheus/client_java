package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;

public abstract class ExtendedCollector implements Collector {

	@Override
	public MetricSnapshot collect() {
		return null;
	}

	@Override
	public MetricSnapshot collect(PrometheusScrapeRequest scrapeRequest) {
		return collectMetricSnapshot(scrapeRequest);
	}

	/**
	 * Override to implement multi-target exporter pattern
	 * @param scrapeRequest
	 *            the (http) request context triggering metrics collection
	 */
	protected abstract MetricSnapshot collectMetricSnapshot(PrometheusScrapeRequest scrapeRequest);

	@Override
	public String getPrometheusName() {
		return null;
	}

	
}
