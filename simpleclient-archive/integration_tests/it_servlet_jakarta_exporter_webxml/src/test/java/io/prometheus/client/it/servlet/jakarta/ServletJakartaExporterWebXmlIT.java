package io.prometheus.client.it.servlet.jakarta;

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
import java.util.List;

public class ServletJakartaExporterWebXmlIT {

    private final OkHttpClient client = new OkHttpClient();

    private static class DockerContainer extends GenericContainer<DockerContainer> {
        DockerContainer() {
            super(new ImageFromDockerfile("servlet-jakarta-exporter-webxml")
                    .withFileFromPath("servlet_jakarta_exporter_webxml.war", Paths.get("target/it_servlet_jakarta_exporter_webxml.war"))
                    .withFileFromClasspath("Dockerfile", "Dockerfile"));
        }
    }

    @Rule
    public DockerContainer dockerContainer = new DockerContainer()
            .withExposedPorts(8080)
            .waitingFor(Wait.forLogMessage(".*oejs.Server:main: Started Server.*", 1));

    @Test
    public void testSampleNameFilter() throws IOException, InterruptedException {
        callExampleServlet();
        List<String> metrics = scrapeMetrics();
        assertContains(metrics, "requests_bucket");
        assertContains(metrics, "requests_count");
        assertContains(metrics, "requests_sum");
        assertContains(metrics, "requests_created");
        assertContains(metrics, "requests_status_total");
        assertContains(metrics, "requests_status_created");
        assertContains(metrics, "jvm_gc_collection_seconds_count");
        assertNotContains(metrics, "jvm_threads_deadlocked");
        assertNotContains(metrics, "jvm_memory_pool");

        List<String> filteredMetrics = scrapeMetricsWithNameFilter("requests_count", "requests_sum");
        assertNotContains(filteredMetrics, "requests_bucket");
        assertContains(filteredMetrics, "requests_count");
        assertContains(filteredMetrics, "requests_sum");
        assertNotContains(filteredMetrics, "requests_created");
        assertNotContains(filteredMetrics, "requests_status_total");
        assertNotContains(filteredMetrics, "requests_status_created");
        assertNotContains(filteredMetrics, "jvm_gc_collection_seconds_count");
        assertNotContains(filteredMetrics, "jvm_threads_deadlocked");
        assertNotContains(filteredMetrics, "jvm_memory_pool");
    }

    private void assertContains(List<String> metrics, String prefix) {
        for (String metric : metrics) {
            if (metric.startsWith(prefix)) {
                return;
            }
        }
        Assert.fail("metric not found: " + prefix);
    }
    private void assertNotContains(List<String> metrics, String prefix) {
        for (String metric : metrics) {
            if (metric.startsWith(prefix)) {
                Assert.fail("unexpected metric found: " + metric);
            }
        }
    }
    private void callExampleServlet() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:" + dockerContainer.getMappedPort(8080) + "/hello")
                .build();
        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals("Hello, world!\n", response.body().string());
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

    private List<String> scrapeMetricsWithNameFilter(String... names) throws IOException {
        StringBuilder param = new StringBuilder();
        boolean first = true;
        for (String name : names) {
            if (!first) {
                param.append("&");
            }
            param.append("name[]=").append(name);
            first = false;
        }
        Request request = new Request.Builder()
                .url("http://localhost:" + dockerContainer.getMappedPort(8080) + "/metrics?" + param)
                .header("Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Arrays.asList(response.body().string().split("\\n"));
        }
    }
}