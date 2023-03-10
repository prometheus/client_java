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
    public void testSanitizationIllegalCharacters() {
        MetricMetadata metadata = new MetricMetadata(MetricMetadata.sanitizeMetricName("http.server.duration"), "help string", Unit.SECONDS);
        Assert.assertEquals("http_server_duration", metadata.getName());
        Assert.assertEquals("help string", metadata.getHelp());
        Assert.assertEquals("seconds", metadata.getUnit().toString());
    }

    @Test
    public void testSanitizationCounter() {
        MetricMetadata metadata = new MetricMetadata(MetricMetadata.sanitizeMetricName("my_events_total"));
        Assert.assertEquals("my_events", metadata.getName());
    }

    @Test
    public void testSanitizationInfo() {
        MetricMetadata metadata = new MetricMetadata(MetricMetadata.sanitizeMetricName("target_info"));
        Assert.assertEquals("target", metadata.getName());
    }

    @Test
    public void testSanitizationWeirdCornerCase() {
        MetricMetadata metadata = new MetricMetadata(MetricMetadata.sanitizeMetricName("_total_created"));
        Assert.assertEquals("total", metadata.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSanitizeEmptyString() {
        MetricMetadata.sanitizeMetricName("");
    }
}
