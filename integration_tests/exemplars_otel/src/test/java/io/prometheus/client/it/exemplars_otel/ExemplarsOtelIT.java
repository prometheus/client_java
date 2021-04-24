package io.prometheus.client.it.exemplars_otel;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Rule;
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

  private final OkHttpClient client = new OkHttpClient();

  private static class DockerContainer extends GenericContainer<DockerContainer> {
    DockerContainer() {
      super(new ImageFromDockerfile("exemplars-otel-test")
          .withFileFromPath("exemplars_otel.jar", Paths.get("target/exemplars_otel.jar"))
          .withFileFromClasspath("Dockerfile", "Dockerfile"));
    }
  }

  @Rule
  public DockerContainer dockerContainer = new DockerContainer()
      .withExposedPorts(9000);

  @Test
  public void testExemplars() throws IOException {
    List<String> metrics = scrapeMetrics();
    boolean found = false;
    for (String metric : metrics) {
      System.out.println(metric);
      if (metric.matches("^test_total 1\\.0 # \\{span_id=\"[0-9a-f]+\",trace_id=\"[0-9a-f]+\"} 1.0 [0-9.]+$")) {
        found = true;
      }
    }
    Assert.assertTrue("test_total metric with exemplars not found", found);
  }

  private List<String> scrapeMetrics() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + dockerContainer.getMappedPort(9000) + "/metrics")
        .header("Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8")
        .build();
    try (Response response = client.newCall(request).execute()) {
      String body=response.body().string();
      System.out.println("body:---");
      System.out.println(body);
      System.out.println("---");
      //return Arrays.asList(response.body().string().split("\\n"));
      return Arrays.asList(body.split("\\n"));
    }
  }
}
