package io.prometheus.client.it.exemplars_otel;

import io.prometheus.client.it.common.LogConsumer;
import io.prometheus.client.it.common.Scraper;
import io.prometheus.client.it.common.Volume;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Test if traces from the OpenTelemetry SDK are picked up as Exemplars.
 * <p>
 * In addition, we make sure that missing OpenTelemetry dependencies do not cause client_java to crash the application.
 **/
public class ExemplarsOpenTelemetrySdkIT {

    private Volume volume;
    private GenericContainer<?> javaContainer;

    private final String appJar = "example-open-telemetry-app.jar";
    private final String image = "openjdk:11-jre";
    private final String[] cmd = new String[] {"java", "-cp", appJar + ":dependency/*", ExampleApplication.class.getName()};

    @Before
    public void setUp() throws IOException, URISyntaxException {
        volume = Volume.create("exemplars-otel-sdk-test")
                .copyFromTargetDirectory(appJar)
                .copyFromTargetDirectory("dependency");
        javaContainer = new GenericContainer<>(image)
                .withFileSystemBind(volume.getHostPath(), "/app", BindMode.READ_ONLY)
                .withWorkingDirectory("/app")
                .withLogConsumer(LogConsumer.withPrefix(image))
                .withExposedPorts(9000)
                .withCommand(cmd);
        System.out.println("Java image: " + image);
        System.out.println("Temp dir: " + volume.getHostPath());
        System.out.println("cmd: " + String.join(" ", cmd));
    }

    @After
    public void tearDown() throws IOException {
        javaContainer.stop();
        volume.remove();
    }

    public void removeDependency(String prefix) throws IOException, URISyntaxException {
        volume.rm(path -> path.getFileName().toString().startsWith(prefix));
    }

    /**
     * All dependencies present -> Exemplars should be found.
     */
    @Test
    public void testGoodCase() throws IOException {
        startContainerAndValidateMetrics(true);
    }

    /**
     * The dependency simpleclient_tracer_otel_agent is for getting the trace context from the OpenTelemetry Java agent.
     * As we are getting the trace context from the OpenTelemetry SDK and not from the agent, Exemplars should work.
     * <p>
     * We test this because if a user excludes simpleclient_tracer_otel_agent from the transitive dependencies for some reason
     * we don't want client_java to break.
     */
    @Test
    public void testOtelAgentMissing() throws IOException, URISyntaxException {
        removeDependency("simpleclient_tracer_otel_agent-");
        startContainerAndValidateMetrics(true);
    }

    /**
     * The dependency simpleclient_tracer_otel is for getting the trace context from the OpenTelemetry SDK.
     * If this dependency is missing, Exemplars will be missing, but metrics should still work.
     * <p>
     * We test this because users may exclude simpleclient_tracer_otel as a way to disable OpenTelemetry Exemplars.
     */
    @Test
    public void testOtelSdkMissing() throws IOException, URISyntaxException {
        removeDependency("simpleclient_tracer_otel-");
        startContainerAndValidateMetrics(false);
    }

    /**
     * This will remove both, simpleclient_tracer_otel_agent and simpleclient_tracer_otel.
     * The expected result is that Exemplars are missing, but metrics still work.
     */
    @Test
    public void testOtelAllMissing() throws IOException, URISyntaxException {
        removeDependency("simpleclient_tracer_otel");
        startContainerAndValidateMetrics(false);
    }

    /**
     * Without simpleclient_tracer_common, Exemplars will be missing but metrics should still work.
     */
    @Test
    public void testTracerCommonMissing() throws IOException, URISyntaxException {
        removeDependency("simpleclient_tracer_common-");
        startContainerAndValidateMetrics(false);
    }

    /**
     * If a user excludes all simpleclient_tracer dependencies, Exemplars will be missing but metrics should still work.
     */
    @Test
    public void testAllMissing() throws IOException, URISyntaxException {
        removeDependency("simpleclient_tracer");
        startContainerAndValidateMetrics(false);
    }

    private void startContainerAndValidateMetrics(boolean exemplarsExpected) throws IOException {
        javaContainer.start();
        List<String> metrics = Scraper.scrape("http://localhost:" + javaContainer.getMappedPort(9000) + "/metrics", 10_000);
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
