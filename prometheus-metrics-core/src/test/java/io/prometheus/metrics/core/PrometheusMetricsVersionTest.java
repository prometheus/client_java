package io.prometheus.metrics.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PrometheusMetricsVersionTest {

  @Test
  void versionMatchesMavenProjectVersion() {
    String version = PrometheusMetricsVersion.getVersion();

    assertThat(version).isEqualTo(System.getProperty("prometheus.metrics.core.version"));
    assertThat(version).isNotBlank().doesNotContain("${", "}");
  }
}
