package io.prometheus.it.exemplars_otel_agent;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExemplarsClientJavaIT {

  private final OkHttpClient client = new OkHttpClient();

  private static class DockerContainer extends GenericContainer<DockerContainer> {
    DockerContainer() {
      super(new ImageFromDockerfile("exemplars-otel-agent-test")
          .withFileFromPath("sample-rest-application.jar", Paths.get("target/sample-rest-application.jar"))
          .withFileFromClasspath("Dockerfile", "Dockerfile"));
    }
  }

  @Rule
  public DockerContainer dockerContainer = new DockerContainer()
      .withExposedPorts(8080)
      .waitingFor(Wait.forLogMessage(".* Started .*", 1));

  @Test
  public void testExemplars() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + dockerContainer.getMappedPort(8080) + "/hello")
        .build();

    // ---------------------------------------------------
    // first request
    // ---------------------------------------------------

    execute(request);
    List<String> metrics = scrapeMetrics();
    SpanContext outer = getSpanContext(metrics, "request_duration_histogram_bucket", "path", "/hello", "le", "0.001");
    SpanContext inner = getSpanContext(metrics, "request_duration_histogram_bucket", "path", "/god-of-fire", "le", "0.001");
    Assert.assertEquals(outer.traceId, inner.traceId);
    Assert.assertNotEquals(outer.spanId, inner.spanId);

    // counter
    assertExemplar(outer, metrics, "requests_total", "path", "/hello");
    assertNoExemplar(metrics, "requests_created", "path", "/hello");
    assertExemplar(inner, metrics, "requests_total", "path", "/god-of-fire");
    assertNoExemplar(metrics, "requests_created", "path", "/god-of-fire");

    // gauge
    assertExemplar(outer, metrics, "last_request_timestamp", "path", "/hello");
    assertExemplar(inner, metrics, "last_request_timestamp", "path", "/god-of-fire");

    // histogram: simulated duration is 0.5ms for the outer and 0.3ms for the inner request
    assertExemplar(outer, metrics, "request_duration_histogram_bucket", "path", "/hello", "le", "0.001");
    assertExemplar(inner, metrics, "request_duration_histogram_bucket", "path", "/god-of-fire", "le", "0.001");
    for (int i=2; i<=9; i++) {
      assertNoExemplar(metrics, "request_duration_histogram_bucket", "path", "/hello", "le", "0.00" + i);
      assertNoExemplar(metrics, "request_duration_histogram_bucket", "path", "/god-of-fire", "le", "0.00" + i);
    }
    assertNoExemplar(metrics, "request_duration_histogram_bucket", "path", "/hello", "le", "+Inf");
    assertNoExemplar(metrics, "request_duration_histogram_bucket", "path", "/god-of-fire", "le", "+Inf");

    // summary: all values are identical because there is only one observation
    assertExemplar(outer, metrics, "request_duration_summary", "path", "/hello", "quantile", "0.75");
    assertExemplar(outer, metrics, "request_duration_summary", "path", "/hello", "quantile", "0.85");
    assertNoExemplar(metrics, "request_duration_summary_count", "path", "/hello");
    assertNoExemplar(metrics, "request_duration_summary_sum", "path", "/hello");
    assertExemplar(inner, metrics, "request_duration_summary", "path", "/god-of-fire", "quantile", "0.75");
    assertExemplar(inner, metrics, "request_duration_summary", "path", "/god-of-fire", "quantile", "0.85");
    assertNoExemplar(metrics, "request_duration_summary_count", "path", "/god-of-fire");
    assertNoExemplar(metrics, "request_duration_summary_sum", "path", "/god-of-fire");

    // random unrelated metric
    assertNoExemplar(metrics, "jvm_threads_current");

    // ---------------------------------------------------
    // 10 more requests
    // ---------------------------------------------------

    for (int i=0; i<10; i++) {
      execute(request);
    }
    metrics = scrapeMetrics();
    Map<String, SpanContext> outers = new HashMap<>();
    Map<String, SpanContext> inners = new HashMap<>();
    for (String bucket : new String[]{"0.001", "0.002", "0.003", "0.004", "0.005", "0.006", "0.007", "0.008", "0.009", "+Inf"}) {
      outers.put(bucket, getSpanContext(metrics, "request_duration_histogram_bucket", "path", "/hello", "le", bucket));
      inners.put(bucket, getSpanContext(metrics, "request_duration_histogram_bucket", "path", "/god-of-fire", "le", bucket));
    }

    // counter: not updated, because the minimum retention interval is not over yet
    assertExemplar(outer, metrics, "requests_total", "path", "/hello");
    assertNoExemplar(metrics, "requests_created", "path", "/hello");
    assertExemplar(inner, metrics, "requests_total", "path", "/god-of-fire");
    assertNoExemplar(metrics, "requests_created", "path", "/god-of-fire");

    // gauge: not updated, because the minimum retention interval is not over yet
    assertExemplar(outer, metrics, "last_request_timestamp", "path", "/hello");
    assertExemplar(inner, metrics, "last_request_timestamp", "path", "/god-of-fire");

    // histogram
    assertHistogramAfterMoreThenTenCalls(outers, inners);

    // summary: each call is 1ms slower than the previous one, up to 10ms, then we start again.
    assertExemplar(outers.get("0.007"), metrics, "request_duration_summary", "path", "/hello", "quantile", "0.75");
    assertExemplar(outers.get("0.008"), metrics, "request_duration_summary", "path", "/hello", "quantile", "0.85");
    assertNoExemplar(metrics, "request_duration_summary_count", "path", "/hello");
    assertNoExemplar(metrics, "request_duration_summary_sum", "path", "/hello");
    assertExemplar(inners.get("0.007"), metrics, "request_duration_summary", "path", "/god-of-fire", "quantile", "0.75");
    assertExemplar(inners.get("0.008"), metrics, "request_duration_summary", "path", "/god-of-fire", "quantile", "0.85");
    assertNoExemplar(metrics, "request_duration_summary_count", "path", "/god-of-fire");
    assertNoExemplar(metrics, "request_duration_summary_sum", "path", "/god-of-fire");

    // random unrelated metric
    assertNoExemplar(metrics, "jvm_threads_current");
  }

  private void assertHistogramAfterMoreThenTenCalls(Map<String, SpanContext> outers, Map<String, SpanContext> inners) {
    for (String outerKey : outers.keySet()) {
      for (String innerKey : inners.keySet()) {
        if (outerKey.equals(innerKey)) {
          // same bucket == same trace, because the sample application simulates the same duration for both calls
          Assert.assertEquals(outers.get(outerKey).traceId, inners.get(innerKey).traceId);
        } else {
          // different bucket -> different trace
          Assert.assertNotEquals(outers.get(outerKey).traceId, inners.get(innerKey).traceId);
        }
        // span ids are unique
        Assert.assertNotEquals(outers.get(outerKey).spanId, inners.get(innerKey).spanId);
      }
    }
  }

  private void assertExemplar(SpanContext spanContext, List<String> responseBody, String metricName, String... labels) {
    String prefix = makeFullMetricName(metricName, labels);
    for (String line : responseBody) {
      if (line.startsWith(prefix)) {
        String exemplarLabels = "# {trace_id=\"" + spanContext.traceId + "\",span_id=\"" + spanContext.spanId + "\"}";
        String message = prefix + " did not have the expected exemplar labels " + exemplarLabels + ":\n" + line;
        Assert.assertTrue(message, line.contains(exemplarLabels));
        return;
      }
    }
    Assert.fail(prefix + " metric not found");
  }

  private void assertNoExemplar(List<String> responseBody, String metricName, String... labels) {
    String prefix = makeFullMetricName(metricName, labels);
    for (String line : responseBody) {
      if (line.startsWith(prefix)) {
        if (line.contains("trace_id")) {
          Assert.fail("unexpected exemplars in metric:\n" + line);
        } else {
          return;
        }
      }
    }
    Assert.fail(prefix + " metric not found");
  }

  private void execute(Request request) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      Assert.assertEquals("Hello, Prometheus!\n", response.body().string());
    }
  }

  private static class SpanContext {
    final String traceId;
    final String spanId;

    private SpanContext(String traceId, String spanId) {
      this.traceId = traceId;
      this.spanId = spanId;
    }
  }

  private List<String> scrapeMetrics() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + dockerContainer.getMappedPort(8080) + "/metrics")
        .header("Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8")
        .build();
    try (Response response = client.newCall(request).execute()) {
      return Arrays.asList(response.body().string().split("\\n"));
    }
  }

  private SpanContext getSpanContext(List<String> responseBody, String metricName, String... labels) {
    String prefix = makeFullMetricName(metricName, labels);
    Pattern pattern = Pattern.compile(".*trace_id=\"([0-9a-f]+)\",span_id=\"([0-9a-f]+).*");
    for (String line : responseBody) {
      if (line.startsWith(prefix)) {
        Matcher matcher = pattern.matcher(line);
        Assert.assertTrue(prefix + " should have an exemplar", matcher.matches());
        return new SpanContext(matcher.group(1), matcher.group(2));
      }
    }
    Assert.fail(prefix + " not found");
    return null;
  }

  // does not need to be perfect, it's ok if this works for the examples used in this test
  private String makeFullMetricName(String metricName, String... labels) {
    if (labels.length % 2 != 0) {
      throw new IllegalArgumentException("labels must be a list of key/value pairs");
    }
    if (labels.length == 0) {
      return metricName;
    } else {
      StringBuilder result = new StringBuilder()
          .append(metricName)
          .append("{");
      for (int i = 0; i < labels.length; i += 2) {
        if (i > 0) {
          result.append(",");
        }
        result.append(labels[i]).append("=\"").append(labels[i + 1]).append("\"");
      }
      result.append("}");
      return result.toString();
    }
  }
}
