package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.annotations.StableApi;

/**
 * Represents the type of Prometheus metric.
 *
 * <p>This enum is used for registration-time validation to ensure that metrics with the same name
 * have consistent types across all registered collectors.
 */
@StableApi
public enum MetricType {
  COUNTER,
  GAUGE,
  HISTOGRAM,
  SUMMARY,
  INFO,
  STATESET,
  /** Unknown metric type, used as a fallback. */
  UNKNOWN
}
