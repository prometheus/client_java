package io.prometheus.metrics.model.registry;

import javax.annotation.Nullable;

/**
 * Identifies a registered metric by its Prometheus name and type.
 *
 * <p>Used internally by PrometheusRegistry to track registered metrics and validate that
 * collectors with the same Prometheus name have consistent types.
 */
class MetricIdentifier {
  private final String prometheusName;
  @Nullable private final MetricType type;

  MetricIdentifier(String prometheusName, @Nullable MetricType type) {
    this.prometheusName = prometheusName;
    this.type = type;
  }

  public String getPrometheusName() {
    return prometheusName;
  }

  @Nullable
  public MetricType getType() {
    return type;
  }

  /**
   * Checks if this identifier is compatible with another collector.
   *
   * <p>Two identifiers are compatible if:
   *
   * <ul>
   *   <li>They have different Prometheus names, OR
   *   <li>At least one has a null type (unknown), OR
   *   <li>They have the same type
   * </ul>
   *
   * @param other the other identifier to check compatibility with
   * @return true if compatible, false if there's a type conflict
   */
  public boolean isCompatibleWith(MetricIdentifier other) {
    // Different names are always compatible
    if (!prometheusName.equals(other.prometheusName)) {
      return true;
    }

    // If either type is null (unknown), skip validation
    if (type == null || other.type == null) {
      return true;
    }

    // Same name requires same type
    return type.equals(other.type);
  }
}