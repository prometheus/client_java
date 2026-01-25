package io.prometheus.metrics.it.pushgateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.squareup.okhttp.*;
import io.prometheus.client.it.common.LogConsumer;
import io.prometheus.client.it.common.Volume;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class PushGatewayIT {

  private GenericContainer<?> sampleAppContainer;
  private GenericContainer<?> pushGatewayContainer;
  private GenericContainer<?> prometheusContainer;
  private Volume sampleAppVolume;

  @BeforeEach
  public void setUp() throws IOException, URISyntaxException {
    Network network = Network.newNetwork();
    sampleAppVolume = Volume.create("it-pushgateway").copy("pushgateway-test-app.jar");
    String javaVersion = System.getenv("TEST_JAVA_VERSION");
    if (javaVersion == null || javaVersion.isEmpty()) {
      javaVersion = "25";
    }
    pushGatewayContainer =
        new GenericContainer<>("prom/pushgateway:v1.8.0")
            .withExposedPorts(9091)
            .withNetwork(network)
            .withNetworkAliases("pushgateway")
            .withLogConsumer(LogConsumer.withPrefix("pushgateway"))
            .waitingFor(Wait.forListeningPort());
    sampleAppContainer =
        new GenericContainer<>("eclipse-temurin:" + javaVersion)
            .withFileSystemBind(sampleAppVolume.getHostPath(), "/app", BindMode.READ_ONLY)
            .withNetwork(network)
            .withWorkingDirectory("/app")
            .dependsOn(pushGatewayContainer)
            .withLogConsumer(LogConsumer.withPrefix("test-app"));
    prometheusContainer =
        new GenericContainer<>("prom/prometheus:v2.51.2")
            .withNetwork(network)
            .dependsOn(pushGatewayContainer)
            .withExposedPorts(9090)
            .withLogConsumer(LogConsumer.withPrefix("prometheus"));
  }

  @AfterEach
  public void tearDown() throws IOException {
    prometheusContainer.stop();
    pushGatewayContainer.stop();
    sampleAppContainer.stop();
    sampleAppVolume.remove();
  }

  final OkHttpClient client = new OkHttpClient();

  @Test
  public void testSimple() throws IOException, InterruptedException {
    pushGatewayContainer.start();
    sampleAppContainer
        .withCommand(
            "java",
            "-Dio.prometheus.exporter.pushgateway.address=pushgateway:9091",
            "-jar",
            "/app/pushgateway-test-app.jar",
            "simple")
        .start();
    prometheusContainer
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/prometheus.yaml"),
            "/etc/prometheus/prometheus.yml")
        .start();
    awaitTermination(sampleAppContainer, 10, TimeUnit.SECONDS);
    assertMetrics();
  }

  @Test
  public void testTextFormat() throws IOException, InterruptedException {
    pushGatewayContainer.start();
    sampleAppContainer
        .withCommand(
            "java",
            "-Dio.prometheus.exporter.pushgateway.address=pushgateway:9091",
            "-jar",
            "/app/pushgateway-test-app.jar",
            "textFormat")
        .start();
    prometheusContainer
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/prometheus.yaml"),
            "/etc/prometheus/prometheus.yml")
        .start();
    awaitTermination(sampleAppContainer, 10, TimeUnit.SECONDS);
    assertMetrics();
  }

  @Test
  public void testBasicAuth() throws IOException, InterruptedException {
    pushGatewayContainer
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/pushgateway-basicauth.yaml"),
            "/pushgateway/pushgateway-basicauth.yaml")
        .withCommand("--web.config.file", "pushgateway-basicauth.yaml")
        .start();
    sampleAppContainer
        .withCommand(
            "java",
            "-Dio.prometheus.exporter.pushgateway.address=pushgateway:9091",
            "-jar",
            "/app/pushgateway-test-app.jar",
            "basicauth")
        .start();
    prometheusContainer
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/prometheus-basicauth.yaml"),
            "/etc/prometheus/prometheus.yml")
        .start();
    awaitTermination(sampleAppContainer, 10, TimeUnit.SECONDS);
    assertMetrics();
  }

  @Test
  public void testSsl() throws InterruptedException, IOException {
    pushGatewayContainer
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/pushgateway-ssl.yaml"),
            "/pushgateway/pushgateway-ssl.yaml")
        .withCommand("--web.config.file", "pushgateway-ssl.yaml")
        .start();
    sampleAppContainer
        .withCommand(
            "java",
            "-Dio.prometheus.exporter.pushgateway.address=pushgateway:9091",
            "-jar",
            "/app/pushgateway-test-app.jar",
            "ssl")
        .start();
    prometheusContainer
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/prometheus-ssl.yaml"),
            "/etc/prometheus/prometheus.yml")
        .start();
    awaitTermination(sampleAppContainer, 10, TimeUnit.SECONDS);
    assertMetrics();
  }

  @Test
  public void testProtobuf() throws IOException, InterruptedException {
    pushGatewayContainer.start();
    sampleAppContainer
        .withCommand(
            "java",
            "-Dio.prometheus.exporter.pushgateway.address=pushgateway:9091",
            "-jar",
            "/app/pushgateway-test-app.jar",
            "simple")
        .start();
    prometheusContainer
        .withCommand(
            "--enable-feature=native-histograms", "--config.file", "/etc/prometheus/prometheus.yml")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/prometheus.yaml"),
            "/etc/prometheus/prometheus.yml")
        .start();
    awaitTermination(sampleAppContainer, 10, TimeUnit.SECONDS);
    assertNativeHistogram();
  }

  private void assertMetrics() throws IOException, InterruptedException {
    double value = getValue("my_batch_job_duration_seconds", "job", "pushgateway-test-app");
    assertThat(value).isEqualTo(0.5);
    value = getValue("file_sizes_bytes_bucket", "job", "pushgateway-test-app", "le", "512");
    assertThat(value).isEqualTo(0.0);
    value = getValue("file_sizes_bytes_bucket", "job", "pushgateway-test-app", "le", "1024");
    assertThat(value).isEqualTo(2.0);
    value = getValue("file_sizes_bytes_bucket", "job", "pushgateway-test-app", "le", "+Inf");
    assertThat(value).isEqualTo(3.0);
  }

  private double getValue(String name, String... labels) throws IOException, InterruptedException {
    String scrapeResponseJson = scrape(name);
    Criteria criteria = Criteria.where("metric.__name__").eq(name);
    for (int i = 0; i < labels.length; i += 2) {
      criteria = criteria.and("metric." + labels[i]).eq(labels[i + 1]);
    }
    JSONArray result =
        JsonPath.parse(scrapeResponseJson)
            .read("$.data.result" + Filter.filter(criteria) + ".value[1]");
    assertThat(result.size()).isOne();
    return Double.parseDouble(result.get(0).toString());
  }

  private void assertNativeHistogram() throws IOException, InterruptedException {
    double count = getNativeHistogramCount("file_sizes_bytes", "pushgateway-test-app");
    assertThat(count).isEqualTo(3);
  }

  private double getNativeHistogramCount(String name, String job)
      throws IOException, InterruptedException {
    String scrapeResponseJson = scrape("histogram_count(" + name + ")");
    Criteria criteria = Criteria.where("metric.job").eq(job);
    JSONArray result =
        JsonPath.parse(scrapeResponseJson)
            .read("$.data.result" + Filter.filter(criteria) + ".value[1]");
    return Double.parseDouble(result.get(0).toString());
  }

  private String scrape(String query) throws IOException, InterruptedException {
    System.out.println(
        "Querying http://"
            + prometheusContainer.getHost()
            + ":"
            + prometheusContainer.getMappedPort(9090));
    HttpUrl baseUrl =
        HttpUrl.parse(
            "http://"
                + prometheusContainer.getHost()
                + ":"
                + prometheusContainer.getMappedPort(9090)
                + "/api/v1/query");
    HttpUrl url = baseUrl.newBuilder().addQueryParameter("query", query).build();
    long timeRemaining = TimeUnit.SECONDS.toMillis(15);
    while (timeRemaining > 0) {
      Request request = new Request.Builder().url(url).build();
      Call call = client.newCall(request);
      Response response = call.execute();
      String body = response.body().string();
      if (!body.contains("\"result\":[]")) {
        // Result when data is not available yet:
        // {"status":"success","data":{"resultType":"vector","result":[]}}
        return body;
      }
      Thread.sleep(250);
      timeRemaining -= 250;
    }
    fail("timeout while scraping " + url);
    return null;
  }

  private void awaitTermination(GenericContainer<?> container, long timeout, TimeUnit unit)
      throws InterruptedException {
    long waitTimeMillis = 0;
    while (container.isRunning()) {
      if (waitTimeMillis > unit.toMillis(timeout)) {
        fail(
            container.getContainerName()
                + " did not terminate after "
                + timeout
                + " "
                + unit
                + ".");
      }
      Thread.sleep(20);
      waitTimeMillis += 20;
    }
  }
}
