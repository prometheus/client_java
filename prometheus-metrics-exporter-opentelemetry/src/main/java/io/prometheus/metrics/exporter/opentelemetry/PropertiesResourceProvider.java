package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;

final class PropertiesResourceProvider {

  static Resource mergeResource(
      Map<String, String> resourceAttributes,
      String serviceName,
      String serviceNamespace,
      String serviceInstanceId,
      String serviceVersion) {
    Map<String, String> resource = new HashMap<>(resourceAttributes);
    if (serviceName != null) {
      resource.put("service.name", serviceName);
    }
    if (serviceNamespace != null) {
      resource.put("service.namespace", serviceNamespace);
    }
    if (serviceInstanceId != null) {
      resource.put("service.instance.id", serviceInstanceId);
    }
    if (serviceVersion != null) {
      resource.put("service.version", serviceVersion);
    }

    AttributesBuilder builder = Attributes.builder();
    resource.forEach(builder::put);
    return Resource.create(builder.build());
  }
}
