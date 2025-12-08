package io.prometheus.metrics.it.exporter.test;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.client.it.common.ExporterTest;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_33_2.Metrics;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Integration test for duplicate metric names with different label sets.
 *
 * <p>This test validates that:
 *
 * <ul>
 *   <li>Multiple metrics with the same Prometheus name but different labels can be registered
 *   <li>All exposition formats (text, OpenMetrics, protobuf) correctly merge and expose them
 *   <li>The merged output is valid and scrapeable by Prometheus
 * </ul>
 */
class DuplicateMetricsIT extends ExporterTest {

  public DuplicateMetricsIT() throws IOException, URISyntaxException {
    super("exporter-duplicate-metrics-sample");
  }

  @Test
  void testDuplicateMetricsInPrometheusTextFormat() throws IOException {
    start();
    Response response = scrape("GET", "");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));

    String body = response.stringBody();

    assertThat(body).contains("# TYPE http_requests_total counter");
    assertThat(body).contains("# HELP http_requests_total Total HTTP requests by status");

    // Verify all data points from both collectors are present
    assertThat(body)
        .contains("http_requests_total{method=\"GET\",status=\"success\"} 150.0")
        .contains("http_requests_total{method=\"POST\",status=\"success\"} 45.0")
        .contains("http_requests_total{endpoint=\"/api\",status=\"error\"} 5.0")
        .contains("http_requests_total{endpoint=\"/health\",status=\"error\"} 2.0");

    assertThat(body).contains("# TYPE active_connections gauge");
    assertThat(body).contains("# HELP active_connections Active connections");

    assertThat(body)
        .contains("active_connections{protocol=\"http\",region=\"us-east\"} 42.0")
        .contains("active_connections{protocol=\"http\",region=\"us-west\"} 38.0")
        .contains("active_connections{protocol=\"https\",region=\"eu-west\"} 55.0")
        .contains("active_connections{pool=\"primary\",type=\"read\"} 30.0")
        .contains("active_connections{pool=\"replica\",type=\"write\"} 10.0");

    assertThat(body).contains("unique_metric_bytes_total 1024.0");
  }

  @Test
  void testDuplicateMetricsInOpenMetricsTextFormat() throws IOException {
    start();
    Response response =
        scrape("GET", "", "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/openmetrics-text; version=1.0.0; charset=utf-8",
        response.getHeader("Content-Type"));

    String body = response.stringBody();

    // Verify http_requests_total is properly merged
    assertThat(body).contains("# TYPE http_requests counter");
    assertThat(body)
        .contains("http_requests_total{method=\"GET\",status=\"success\"} 150.0")
        .contains("http_requests_total{method=\"POST\",status=\"success\"} 45.0")
        .contains("http_requests_total{endpoint=\"/api\",status=\"error\"} 5.0")
        .contains("http_requests_total{endpoint=\"/health\",status=\"error\"} 2.0");

    // Verify active_connections is properly merged
    assertThat(body).contains("# TYPE active_connections gauge");
    assertThat(body)
        .contains("active_connections{protocol=\"http\",region=\"us-east\"} 42.0")
        .contains("active_connections{protocol=\"http\",region=\"us-west\"} 38.0")
        .contains("active_connections{protocol=\"https\",region=\"eu-west\"} 55.0")
        .contains("active_connections{pool=\"primary\",type=\"read\"} 30.0")
        .contains("active_connections{pool=\"replica\",type=\"write\"} 10.0");

    // OpenMetrics format should have UNIT for unique_metric_bytes (base name without _total)
    assertThat(body)
        .contains("unique_metric_bytes_total 1024.0")
        .contains("# UNIT unique_metric_bytes bytes");

    assertThat(body).endsWith("# EOF\n");
  }

  @Test
  void testDuplicateMetricsInPrometheusProtobufFormat() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            "",
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
                + " encoding=delimited");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
            + " encoding=delimited",
        response.getHeader("Content-Type"));

    List<Metrics.MetricFamily> metrics = response.protoBody();

    // Should have exactly 3 metric families (active_connections, http_requests_total,
    // unique_metric_bytes_total)
    assertThat(metrics).hasSize(3);

    // Metrics are sorted by name
    assertThat(metrics.get(0).getName()).isEqualTo("active_connections");
    assertThat(metrics.get(1).getName()).isEqualTo("http_requests_total");
    assertThat(metrics.get(2).getName()).isEqualTo("unique_metric_bytes_total");

    // Verify active_connections has all 5 data points merged
    Metrics.MetricFamily activeConnections = metrics.get(0);
    assertThat(activeConnections.getType()).isEqualTo(Metrics.MetricType.GAUGE);
    assertThat(activeConnections.getHelp()).isEqualTo("Active connections");
    assertThat(activeConnections.getMetricList()).hasSize(5);

    // Verify http_requests_total has all 4 data points merged
    Metrics.MetricFamily httpRequests = metrics.get(1);
    assertThat(httpRequests.getType()).isEqualTo(Metrics.MetricType.COUNTER);
    assertThat(httpRequests.getHelp()).isEqualTo("Total HTTP requests by status");
    assertThat(httpRequests.getMetricList()).hasSize(4);

    // Verify each data point has the expected labels
    boolean foundSuccessGet = false;
    boolean foundSuccessPost = false;
    boolean foundErrorApi = false;
    boolean foundErrorHealth = false;

    for (Metrics.Metric metric : httpRequests.getMetricList()) {
      List<Metrics.LabelPair> labels = metric.getLabelList();
      if (hasLabel(labels, "status", "success") && hasLabel(labels, "method", "GET")) {
        assertThat(metric.getCounter().getValue()).isEqualTo(150.0);
        foundSuccessGet = true;
      } else if (hasLabel(labels, "status", "success") && hasLabel(labels, "method", "POST")) {
        assertThat(metric.getCounter().getValue()).isEqualTo(45.0);
        foundSuccessPost = true;
      } else if (hasLabel(labels, "status", "error") && hasLabel(labels, "endpoint", "/api")) {
        assertThat(metric.getCounter().getValue()).isEqualTo(5.0);
        foundErrorApi = true;
      } else if (hasLabel(labels, "status", "error") && hasLabel(labels, "endpoint", "/health")) {
        assertThat(metric.getCounter().getValue()).isEqualTo(2.0);
        foundErrorHealth = true;
      }
    }

    assertThat(foundSuccessGet).isTrue();
    assertThat(foundSuccessPost).isTrue();
    assertThat(foundErrorApi).isTrue();
    assertThat(foundErrorHealth).isTrue();

    // Verify unique metric
    Metrics.MetricFamily uniqueMetric = metrics.get(2);
    assertThat(uniqueMetric.getType()).isEqualTo(Metrics.MetricType.COUNTER);
    assertThat(uniqueMetric.getMetricList()).hasSize(1);
    assertThat(uniqueMetric.getMetric(0).getCounter().getValue()).isEqualTo(1024.0);
  }

  @Test
  void testDuplicateMetricsWithNameFilter() throws IOException {
    start();
    // Only scrape http_requests_total
    Response response = scrape("GET", nameParam());
    assertThat(response.status).isEqualTo(200);

    String body = response.stringBody();

    assertThat(body)
        .contains("http_requests_total{method=\"GET\",status=\"success\"} 150.0")
        .contains("http_requests_total{endpoint=\"/api\",status=\"error\"} 5.0");

    // Should NOT contain active_connections or unique_metric_total
    assertThat(body).doesNotContain("active_connections").doesNotContain("unique_metric_total");
  }

  private boolean hasLabel(List<Metrics.LabelPair> labels, String name, String value) {
    return labels.stream()
        .anyMatch(label -> label.getName().equals(name) && label.getValue().equals(value));
  }

  private String nameParam() {
    return "name[]=" + "http_requests_total";
  }
}
