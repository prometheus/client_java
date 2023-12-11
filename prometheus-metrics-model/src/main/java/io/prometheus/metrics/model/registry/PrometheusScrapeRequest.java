package io.prometheus.metrics.model.registry;

/**
 * Infos extracted from the request received by the endpoint
 */
public interface PrometheusScrapeRequest {

	/**
	 * Absolute path of the HTTP request.
	 */
	String getRequestPath();

	/**
	 * See {@code jakarta.servlet.ServletRequest.getParameterValues(String name)}
	 */
	String[] getParameterValues(String name);
}
