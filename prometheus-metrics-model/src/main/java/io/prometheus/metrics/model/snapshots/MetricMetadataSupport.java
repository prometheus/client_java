package io.prometheus.metrics.model.snapshots;

import javax.annotation.Nullable;

final class MetricMetadataSupport {

  private MetricMetadataSupport() {}

  static MetricMetadata metricMetadata(String name, @Nullable String help, @Nullable Unit unit) {
    return new MetricMetadata(name, help, unit);
  }

  static MetricMetadata counterMetadata(String name, @Nullable String help, @Nullable Unit unit) {
    return typedMetadata(name, help, unit, "_total", ".total");
  }

  static MetricMetadata infoMetadata(String name, @Nullable String help) {
    return typedMetadata(name, help, null, "_info", ".info");
  }

  @SuppressWarnings("deprecation")
  private static MetricMetadata typedMetadata(
      String originalName,
      @Nullable String help,
      @Nullable Unit unit,
      String suffix,
      String dotSuffix) {
    String baseName = stripSuffix(originalName, suffix, dotSuffix);
    return new MetricMetadata(
        appendUnitIfMissing(baseName, unit),
        appendUnitIfMissing(originalName, unit),
        originalName,
        help,
        unit);
  }

  private static String appendUnitIfMissing(String name, @Nullable Unit unit) {
    if (unit != null && !name.endsWith("_" + unit) && !name.endsWith("." + unit)) {
      return name + "_" + unit;
    }
    return name;
  }

  private static String stripSuffix(String name, String suffix, String dotSuffix) {
    if (name.endsWith(suffix)) {
      return name.substring(0, name.length() - suffix.length());
    }
    if (name.endsWith(dotSuffix)) {
      return name.substring(0, name.length() - dotSuffix.length());
    }
    return name;
  }
}
