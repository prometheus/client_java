package io.prometheus.metrics.exporter.opentelemetry;

import java.util.HashMap;
import java.util.Map;

public class ResourceAttributes {

    // TODO: The OTel Java instrumentation also has a SpringBootServiceNameDetector, we should port this over.
    public static Map<String, String> get(String instrumentationScopeName,
                                          String serviceName,
                                          String serviceNamespace,
                                          String serviceInstanceId,
                                          String serviceVersion,
                                          Map<String, String> configuredResourceAttributes) {
        Map<String, String> result = new HashMap<>();
        ResourceAttributesFromOtelAgent.addIfAbsent(result, instrumentationScopeName);
        putIfAbsent(result, "service.name", serviceName);
        putIfAbsent(result, "service.namespace", serviceNamespace);
        putIfAbsent(result, "service.instance.id", serviceInstanceId);
        putIfAbsent(result, "service.version", serviceVersion);
        for (Map.Entry<String, String> attribute : configuredResourceAttributes.entrySet()) {
            putIfAbsent(result, attribute.getKey(), attribute.getValue());
        }
        ResourceAttributesFromJarFileName.addIfAbsent(result);
        ResourceAttributesDefaults.addIfAbsent(result);
        return result;
    }

    private static void putIfAbsent(Map<String, String> result, String key, String value) {
        if (value != null) {
            result.putIfAbsent(key, value);
        }
    }
}
