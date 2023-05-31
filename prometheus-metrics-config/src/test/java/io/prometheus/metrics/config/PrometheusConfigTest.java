package io.prometheus.metrics.config;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PrometheusConfigTest {

    @Test
    public void testPrometheusConfig() {
        PrometheusConfig result = PrometheusConfig.getInstance();
        Assert.assertEquals(11, result.getDefaultMetricsConfig().getClassicHistogramUpperBounds().length);
        Assert.assertEquals(4, result.getMetricsConfig("http_duration_seconds").getClassicHistogramUpperBounds().length);
    }

    @Test
    public void testEmptyUpperBounds() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("emptyUpperBounds.properties")) {
            properties.load(stream);
        }
        MetricsConfig config = MetricsConfig.load("io.prometheus.metrics", properties);
        Assert.assertNull(config.getClassicHistogramUpperBounds());
    }
}
