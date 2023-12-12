package io.prometheus.metrics.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class PrometheusPropertiesTest {

    @Test
    void testPrometheusConfig() {
        PrometheusProperties result = PrometheusProperties.get();
        Assertions.assertEquals(11, result.getDefaultMetricProperties().getHistogramClassicUpperBounds().size());
        Assertions.assertEquals(4, result.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds().size());
    }

    @Test
    void testEmptyUpperBounds() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("emptyUpperBounds.properties")) {
            properties.load(stream);
        }
        Assertions.assertEquals(1, properties.size());
        MetricsProperties.load("io.prometheus.metrics", properties);
        Assertions.assertEquals(0, properties.size());
    }
}
