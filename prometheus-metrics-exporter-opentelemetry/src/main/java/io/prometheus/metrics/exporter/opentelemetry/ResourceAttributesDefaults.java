package io.prometheus.metrics.exporter.opentelemetry;

import java.util.Map;

public class ResourceAttributesDefaults {

    public static void addIfAbsent(Map<String, String> result) {
        result.putIfAbsent("service.name", "unknown_service:java");
    }
}
