package io.prometheus.client.smoketest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Smoke test with different Java versions.
 */
@RunWith(Parameterized.class)
public class JavaVersionsIT {

  private final int port = 9000;
  private final OkHttpClient client = new OkHttpClient();

  @Rule
  public JavaContainer javaContainer;

  public JavaVersionsIT(String baseImage) {
    javaContainer = new JavaContainer(baseImage).withExposedPorts(port);
  }

  @Parameterized.Parameters(name="{0}")
  public static String[] images() {
    return new String[] {
        "openjdk:11-jre",
        "openjdk:8-jre",
        "ticketfly/java:6",
        "ibmjava:8-jre",
    };
  }

  @Test
  public void testExampleMetric() {
    List<String> metrics = scrapeMetrics(TimeUnit.SECONDS.toMillis(10));
    System.out.println(javaContainer.getLogs());
    Assert.assertTrue("jvm_memory_bytes_used{area=\"heap\"} not found", metrics.stream()
        .filter(m -> m.startsWith("jvm_memory_bytes_used{area=\"heap\"} "))
        .peek(System.out::println)
        .findAny()
        .isPresent());
  }

  private static class JavaContainer extends GenericContainer<JavaContainer> {
    JavaContainer(String baseImage) {
      super(new ImageFromDockerfile("prometheus-client-java-example-application")
          .withDockerfileFromBuilder(builder ->
              builder
                  .from(baseImage)
                  .run("mkdir /app")
                  .workDir("/app")
                  .copy("example_application.jar", "/app/")
                  .cmd("java -version && java -jar example_application.jar")
                  .build())
          .withFileFromPath("example_application.jar",
              Paths.get("../example_application/target/example_application.jar")));
    }
  }

  private List<String> scrapeMetrics(long timeoutMillis) {
    long start = System.currentTimeMillis();
    Exception exception = null;
    String host = javaContainer.getHost();
    Integer mappedPort = javaContainer.getMappedPort(port);
    String metricsUrl = "http://" + host + ":" + mappedPort + "/metrics";
    while (System.currentTimeMillis() - start < timeoutMillis) {
      try {
        Request request = new Request.Builder()
            .header("Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8")
            .url(metricsUrl)
            .build();
        try (Response response = client.newCall(request).execute()) {
          return Arrays.asList(response.body().string().split("\\n"));
        }
      } catch (Exception e) {
        exception = e;
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
      }
    }
    if (exception != null) {
      exception.printStackTrace();
    }
    Assert.fail("Timeout while getting metrics from " + metricsUrl + " (orig port: " + port + ")");
    return null; // will not happen
  }
}
