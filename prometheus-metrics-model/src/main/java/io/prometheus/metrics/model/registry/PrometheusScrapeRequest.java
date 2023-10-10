package io.prometheus.metrics.model.registry;

/**
 * Infos extracted from the request received by the endpoint
 * 
 */
public interface PrometheusScrapeRequest {
	
	String getRequestURI();

	String[] getParameterValues(String name);

}
