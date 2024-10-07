package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class PrometheusPropertiesTest {

  @Test
  public void testPrometheusConfig() {
    PrometheusProperties result = PrometheusProperties.get();
    assertThat(result.getDefaultMetricProperties().getHistogramClassicUpperBounds()).hasSize(11);
    assertThat(result.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds())
        .hasSize(4);
  }

  @Test
  public void testEmptyUpperBounds() throws IOException {
    Properties properties = new Properties();
    try (InputStream stream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("emptyUpperBounds.properties")) {
      properties.load(stream);
    }
    assertThat(properties).hasSize(1);
    MetricsProperties.load("io.prometheus.metrics", properties);
    assertThat(properties).isEmpty();
  }
}
