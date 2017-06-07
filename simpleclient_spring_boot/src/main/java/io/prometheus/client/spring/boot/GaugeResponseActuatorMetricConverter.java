package io.prometheus.client.spring.boot;

import io.prometheus.client.Collector.MetricFamilySamples;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GaugeResponseActuatorMetricConverter {

	private List<String> labelsMerged;
	private List<String> labelsPerHttpMethod;

	private final static String GAUGE_RESPONSE_NAME = "gauge_response";

	public GaugeResponseActuatorMetricConverter() {
		this.labelsMerged = new ArrayList<String>();
		this.labelsMerged.add("endpoint");

		this.labelsPerHttpMethod = new ArrayList<String>(labelsMerged);
		this.labelsPerHttpMethod.add("method");
	}

	public MetricFamilySamples.Sample convert(Metric<?> metric) {
		MetricFamilySamples.Sample sample;
		if (containsHttpMethod(metric.getName())) {
			List<String> labelValues  = retrieveLabelValues("gauge.response.", metric.getName());
			return new MetricFamilySamples.Sample(GAUGE_RESPONSE_NAME, labelsPerHttpMethod, labelValues, metric.getValue().doubleValue());
		} else {
			List<String> labelValues  = retrieveJustEndpoint("gauge.response.", metric.getName());
			return new MetricFamilySamples.Sample(GAUGE_RESPONSE_NAME, labelsMerged, labelValues, metric.getValue().doubleValue());
		}
	}

	private List<String> retrieveJustEndpoint(String keyToRemove, String name) {
		return Arrays.asList(name.replace(keyToRemove , ".").replaceAll("\\.","/"));
	}

	private List<String> retrieveLabelValues(String keyToRemove, String name) {
		String method = name.split("\\.")[2];
		String endpoint = name.substring(name.lastIndexOf(method) + method.length()).replaceAll("\\.","/");
		return Arrays.asList(endpoint, method);
	}

	private boolean containsHttpMethod(String name) {
		return name.contains(".GET.")
				|| name.contains(".POST.")
				|| name.contains(".PUT.")
				|| name.contains(".DELETE.")
				|| name.contains(".OPTIONS.")
				|| name.contains(".HEAD.")
				|| name.contains(".CONNECT.");
	}
}
