package io.prometheus.metrics.model.registry;

/**
 * Represents the type of Prometheus metric.
 *
 * <p>This enum is used for early validation when registering collectors with duplicate Prometheus
 * names. All collectors with the same Prometheus name must have the same metric type.
 */
public enum MetricType {
  COUNTER,

  GAUGE,

  HISTOGRAM,

  SUMMARY,

  INFO,

  STATESET,

  /** Unknown metric type - for custom or legacy collectors. */
  UNKNOWN
}
