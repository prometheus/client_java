package io.prometheus.metrics.model.snapshots;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;

class MetricMetadataTest {

    @Test
    void testEmptyName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MetricMetadata(""));

    }

    @Test
    void testNullName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MetricMetadata(null));
    }

    @Test
    void testIllegalName() {
        // let's see when we decide to allow slashes :)
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MetricMetadata("my_namespace/http_server_duration"));
    }

    @Test
    void testSanitizationIllegalCharacters() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_namespace/http.server.duration"), "help string", Unit.SECONDS);
        Assertions.assertEquals("my_namespace_http.server.duration", metadata.getName());
        Assertions.assertEquals("my_namespace_http_server_duration", metadata.getPrometheusName());
        Assertions.assertEquals("help string", metadata.getHelp());
        Assertions.assertEquals("seconds", metadata.getUnit().toString());
    }

    @Test
    void testSanitizationCounter() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("my_events_total"));
        Assertions.assertEquals("my_events", metadata.getName());
    }

    @Test
    void testSanitizationInfo() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("target_info"));
        Assertions.assertEquals("target", metadata.getName());
    }

    @Test
    void testSanitizationWeirdCornerCase() {
        MetricMetadata metadata = new MetricMetadata(sanitizeMetricName("_total_created"));
        Assertions.assertEquals("total", metadata.getName());
    }

    @Test
    void testSanitizeEmptyString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> sanitizeMetricName(""));
    }
}
