package io.prometheus.metrics.exporter.common;

import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;

public class PrometheusHttpScrapeRequest implements PrometheusScrapeRequest {
	
	protected PrometheusHttpRequest prometheusHttpRequest;
	
	public PrometheusHttpScrapeRequest(PrometheusHttpRequest prometheusHttpRequest) {
		super();
		this.prometheusHttpRequest = prometheusHttpRequest;
	}

	@Override
	public String getRequestURI() {
		return prometheusHttpRequest.getRequestPath();
	}

	@Override
	public String[] getParameterValues(String name) {
		return prometheusHttpRequest.getParameterValues(name);
	}

}
