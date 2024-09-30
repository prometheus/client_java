package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmMetricsTest {

    @Test
    public void testRegisterIdempotent() {
        PrometheusRegistry registry = new PrometheusRegistry();
        assertEquals(0, registry.scrape().size());
        JvmMetrics.builder().register(registry);
        assertTrue(registry.scrape().size() > 0);
        JvmMetrics.builder().register(registry);
    }
}
