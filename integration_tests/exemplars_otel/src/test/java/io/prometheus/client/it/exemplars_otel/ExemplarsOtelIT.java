package io.prometheus.client.it.exemplars_otel;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * This is just a smoke test, as completeness is already tested with exemplars_otel_agent
 */
public class ExemplarsOtelIT {

  private final String cmd = "java -cp \"/app/exemplars_otel.jar:/app/lib/*\" io.prometheus.client.it.exemplars_otel.Server";
  private final OkHttpClient client = new OkHttpClient();

  private static class DockerContainer extends GenericContainer<DockerContainer> {
    DockerContainer() {
      super(new ImageFromDockerfile("exemplars-otel-test")
          .withFileFromPath("exemplars_otel.jar", Paths.get("target/exemplars_otel.jar"))
          .withFileFromPath("dependency", Paths.get("target/dependency"))
          .withFileFromClasspath("Dockerfile", "Dockerfile"));
    }
  }

  @Test
  public void testGoodCase() throws IOException {
    runTest(":", true);
  }

  @Test
  public void testOtelAgentMissing() throws IOException {
    runTest("rm /app/lib/simpleclient_tracer_otel_agent-*", true);
  }

  @Test
  public void testOtelMissing() throws IOException {
    runTest("rm /app/lib/simpleclient_tracer_otel-*", false);
  }

  @Test
  public void testOtelAllMissing() throws IOException {
    runTest("rm /app/lib/simpleclient_tracer_otel*", false);
  }

  @Test
  public void testTracerCommonMissing() throws IOException {
    runTest("rm /app/lib/simpleclient_tracer_common-*", false);
  }

  @Test
  public void testAllMissing() throws IOException {
    runTest("rm /app/lib/simpleclient_tracer*", false);
  }

  private void runTest(String rmCmd, boolean exemplarsExpected) throws IOException {
    try (DockerContainer container = new DockerContainer()
        .withExposedPorts(9000)
        .withCommand("/bin/bash", "-c", rmCmd + " ; " + cmd)) {
      container.start();
      List<String> metrics = scrapeMetrics(container);
      boolean testTotalWithExemplarFound = false;
      boolean testTotalWithoutExemplarFound = false;
      for (String metric : metrics) {
        System.out.println(metric);
        if (metric.matches("^test_total 1\\.0 # \\{span_id=\"[0-9a-f]+\",trace_id=\"[0-9a-f]+\"} 1.0 [0-9.]+$")) {
          testTotalWithExemplarFound = true;
        }
        if (metric.matches("^test_total 1\\.0$")) {
          testTotalWithoutExemplarFound = true;
        }
      }
      if (exemplarsExpected) {
        Assert.assertTrue("test_total metric with exemplars expected", testTotalWithExemplarFound);
        Assert.assertFalse("test_total without exemplar should not be there", testTotalWithoutExemplarFound);
      } else {
        Assert.assertFalse("test_total metric with exemplar should not be there", testTotalWithExemplarFound);
        Assert.assertTrue("test_total without exemplar expected", testTotalWithoutExemplarFound);
      }
    }
  }

  private List<String> scrapeMetrics(DockerContainer dockerContainer) throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + dockerContainer.getMappedPort(9000) + "/metrics")
        .header("Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8")
        .build();
    try (Response response = client.newCall(request).execute()) {
      String body = response.body().string();
      System.out.println("body:---");
      System.out.println(body);
      System.out.println("---");
      return Arrays.asList(body.split("\\n"));
    }
  }
}
