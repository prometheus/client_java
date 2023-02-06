package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class MetricMetadataTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyName() {
        new MetricMetadata("");
    }

    @Test(expected = NullPointerException.class)
    public void testNullName() {
        new MetricMetadata(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalName() {
        new MetricMetadata("http.server.duration");
    }

    @Test
    public void testSanitization() {
        MetricMetadata metadata = new MetricMetadata(MetricMetadata.sanitizeMetricName("http.server.duration"), "help string", Unit.SECONDS);
        Assert.assertEquals("http_server_duration", metadata.getName());
        Assert.assertEquals("help string", metadata.getHelp());
        Assert.assertEquals("seconds", metadata.getUnit().toString());
    }
}
