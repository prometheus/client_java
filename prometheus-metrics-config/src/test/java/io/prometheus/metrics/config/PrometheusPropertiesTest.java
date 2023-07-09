package io.prometheus.metrics.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PrometheusPropertiesTest {

    @Test
    public void testPrometheusConfig() {
        PrometheusProperties result = PrometheusProperties.get();
        Assert.assertEquals(11, result.getDefaultMetricProperties().getHistogramClassicUpperBounds().size());
        Assert.assertEquals(4, result.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds().size());
    }

    @Test
    public void testEmptyUpperBounds() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("emptyUpperBounds.properties")) {
            properties.load(stream);
        }
        Assert.assertEquals(1, properties.size());
        MetricsProperties.load("io.prometheus.metrics", properties);
        Assert.assertEquals(0, properties.size());
    }
}
