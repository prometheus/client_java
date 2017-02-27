package io.prometheus.client.spring.boot;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author Marco Aust
 * @author Eliezio Oliveira
 */
class PrometheusEndpoint extends AbstractEndpoint<ResponseEntity<String>> {

  private final CollectorRegistry collectorRegistry;

  PrometheusEndpoint(CollectorRegistry collectorRegistry, boolean sensitive, boolean enabled) {
    super("prometheus", sensitive, enabled);
    this.collectorRegistry = collectorRegistry;
  }

  @Override
  public ResponseEntity<String> invoke() {
    try {
      Writer writer = new StringWriter();
      TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
      return ResponseEntity.ok()
          .header(CONTENT_TYPE, TextFormat.CONTENT_TYPE_004)
          .body(writer.toString());
    } catch (IOException e) {
      // This actually never happens since StringWriter::write() doesn't throw any IOException
      throw new RuntimeException("Writing metrics failed", e);
    }
  }
}
