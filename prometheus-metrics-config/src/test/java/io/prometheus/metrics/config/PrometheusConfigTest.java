package io.prometheus.metrics.config;

import org.junit.Assert;
import org.junit.Test;

public class PrometheusConfigTest {

    @Test
    public void testPrometheusConfig() {
        PrometheusConfig result = PrometheusConfig.getInstance();
        Assert.assertEquals(11, result.getDefaultMetricsConfig().getClassicHistogramUpperBounds().length);
        Assert.assertEquals(4, result.getMetricsConfig("http_duration_seconds").getClassicHistogramUpperBounds().length);
    }
}
