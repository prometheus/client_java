package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

@ConfigurationProperties("endpoints.prometheus")
class PrometheusEndpoint extends AbstractEndpoint<String> {

  private final CollectorRegistry collectorRegistry;

  PrometheusEndpoint(CollectorRegistry collectorRegistry) {
    super("prometheus");
    this.collectorRegistry = collectorRegistry;
  }

  @Override
  public String invoke() {
    return writeRegistry(Collections.<String>emptySet());
  }

  public String writeRegistry(Set<String> metricsToInclude) {
    try {
      Writer writer = new StringWriter();
      TextFormat.write004(writer, collectorRegistry.filteredMetricFamilySamples(metricsToInclude));
      return writer.toString();
    } catch (IOException e) {
      // This actually never happens since StringWriter::write() doesn't throw any IOException
      throw new RuntimeException("Writing metrics failed", e);
    }
  }
}
