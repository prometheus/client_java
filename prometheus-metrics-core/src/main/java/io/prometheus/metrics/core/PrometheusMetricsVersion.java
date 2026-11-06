package io.prometheus.metrics.core;

import io.prometheus.metrics.annotations.StableApi;

/** Provides the version of the Prometheus Java Metrics Library. */
@StableApi
public final class PrometheusMetricsVersion {

  private PrometheusMetricsVersion() {}

  /**
   * Returns the Maven version of the {@code prometheus-metrics-core} artifact containing this
   * class.
   *
   * @return the artifact version, including any suffix such as {@code -SNAPSHOT}
   */
  public static String getVersion() {
    return VersionInfo.VERSION;
  }
}
