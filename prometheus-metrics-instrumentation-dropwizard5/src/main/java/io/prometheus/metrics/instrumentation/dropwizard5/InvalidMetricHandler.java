package io.prometheus.metrics.instrumentation.dropwizard5;

import io.prometheus.metrics.annotations.StableApi;

@FunctionalInterface
@StableApi
public interface InvalidMetricHandler {
  InvalidMetricHandler ALWAYS_THROW = (metricName, exc) -> false;

  /**
   * @param metricName the name of the metric that was collected.
   * @param exc The exception that was thrown when producing the metric snapshot.
   * @return true if the exception should be suppressed.
   */
  boolean suppressException(String metricName, Exception exc);
}
