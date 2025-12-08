package io.prometheus.metrics.model.registry;

/**
 * Represents the type of a Prometheus metric.
 *
 * <p>This enum is used for early validation when registering collectors with duplicate Prometheus
 * names. All collectors with the same Prometheus name must have the same metric type.
 */
public enum MetricType {
  /** Counter metric type - monotonically increasing value. */
  COUNTER,

  /** Gauge metric type - value that can go up or down. */
  GAUGE,

  /** Histogram metric type - samples observations and counts them in buckets. */
  HISTOGRAM,

  /** Summary metric type - samples observations and calculates quantiles. */
  SUMMARY,

  /** Info metric type - key-value pairs providing information about the entity. */
  INFO,

  /** StateSet metric type - represents a set of boolean states. */
  STATESET,

  /** Unknown metric type - for custom or legacy collectors. */
  UNKNOWN
}
