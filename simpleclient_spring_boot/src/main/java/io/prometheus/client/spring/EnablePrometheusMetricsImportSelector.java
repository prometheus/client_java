package io.prometheus.client.spring;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Stuart Williams (pidster)
 */
class EnablePrometheusMetricsImportSelector implements ImportSelector {

  private static final String ENABLE_CLASS = EnablePrometheusMetrics.class.getName();

  @Override
  public String[] selectImports(AnnotationMetadata annotationMetadata) {

    Set<String> selectedImports = new HashSet<String>();

    if (annotationMetadata.hasAnnotation(ENABLE_CLASS)) {

      Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(ENABLE_CLASS);

      if (checkBoolean("hotspot", attributes)) {
        selectedImports.add(HotspotMetricCollectorsConfiguration.class.getName());
      }
      if (checkBoolean("spring", attributes)) {
        selectedImports.add(SpringPublicMetricsConfiguration.class.getName());
      }
    }

    String[] array = new String[selectedImports.size()];
    return selectedImports.toArray(array);
  }

  private static boolean checkBoolean(String field, Map<String, Object> attributes) {

    Object possibleBoolean = attributes.get(field);

    if (possibleBoolean instanceof Boolean) {
      return (Boolean) possibleBoolean;
    }

    return false;
  }

}
