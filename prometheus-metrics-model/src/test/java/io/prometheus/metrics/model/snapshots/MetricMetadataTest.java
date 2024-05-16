package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;

public class MetricMetadataTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyName() {
        new MetricMetadata("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullName() {
        new MetricMetadata(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalName() {
        new MetricMetadata("my_namespace/http_server_duration"); // let's see when we decide to allow slashes :)
    }

    @Test
    public void testSanitizationIllegalCharacters() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_namespace/http.server.duration", Unit.SECONDS), "help string", Unit.SECONDS);
        Assert.assertEquals("my_namespace_http.server.duration_seconds", metadata.getName());
        Assert.assertEquals("my_namespace_http_server_duration_seconds", metadata.getPrometheusName());
        Assert.assertEquals("help string", metadata.getHelp());
        Assert.assertEquals("seconds", metadata.getUnit().toString());
    }

    @Test
    public void testSanitizationCounter() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_total"));
        Assert.assertEquals("my_events", metadata.getName());
    }

    @Test
    public void testSanitizationInfo() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("target_info"));
        Assert.assertEquals("target", metadata.getName());
    }

    @Test
    public void testSanitizationWeirdCornerCase() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("_total_created"));
        Assert.assertEquals("total", metadata.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSanitizeEmptyString() {
        sanitizeMetricName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnitSuffixRequired() {
        new MetricMetadata("my_counter", "help", Unit.SECONDS);
    }

    @Test
    public void testUnitSuffixAdded() {
        new MetricMetadata(sanitizeMetricName("my_counter", Unit.SECONDS), "help", Unit.SECONDS);
    }

    @Test
    public void testUnitNotDuplicated() {
        Assert.assertEquals("my_counter_bytes", sanitizeMetricName("my_counter_bytes", Unit.BYTES));
    }
}
