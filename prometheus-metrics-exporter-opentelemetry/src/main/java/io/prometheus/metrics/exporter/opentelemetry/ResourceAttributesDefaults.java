package io.prometheus.metrics.exporter.opentelemetry;

import java.util.Map;
import java.util.UUID;

public class ResourceAttributesDefaults {

    private static final String instanceId = UUID.randomUUID().toString();

    public static void addIfAbsent(Map<String, String> result) {
        result.putIfAbsent("service.instance.id", instanceId);
        result.putIfAbsent("service.name", "unknown_service:java");
    }
}
