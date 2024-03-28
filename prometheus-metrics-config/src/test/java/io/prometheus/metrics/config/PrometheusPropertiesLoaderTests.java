package io.prometheus.metrics.config;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link PrometheusPropertiesLoader}.
 */
public class PrometheusPropertiesLoaderTests {

	@Test
	public void propertiesShouldBeLoadedFromPropertiesFile() {
		PrometheusProperties prometheusProperties = PrometheusPropertiesLoader.load();
		Assert.assertEquals(11, prometheusProperties.getDefaultMetricProperties().getHistogramClassicUpperBounds().size());
		Assert.assertEquals(4, prometheusProperties.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds().size());
		Assert.assertTrue(prometheusProperties.getExporterProperties().getExemplarsOnAllMetricTypes());
	}

	@Test
	public void externalPropertiesShouldOverridePropertiesFile() {
		Properties properties = new Properties();
		properties.setProperty("io.prometheus.metrics.histogramClassicUpperBounds", ".005, .01");
		properties.setProperty("io.prometheus.metrics.http_duration_seconds.histogramClassicUpperBounds", ".005, .01, .015");
		properties.setProperty("io.prometheus.exporter.exemplarsOnAllMetricTypes", "false");

		PrometheusProperties prometheusProperties = PrometheusPropertiesLoader.load(properties);
		Assert.assertEquals(2, prometheusProperties.getDefaultMetricProperties().getHistogramClassicUpperBounds().size());
		Assert.assertEquals(3, prometheusProperties.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds().size());
		Assert.assertFalse(prometheusProperties.getExporterProperties().getExemplarsOnAllMetricTypes());
	}
}
