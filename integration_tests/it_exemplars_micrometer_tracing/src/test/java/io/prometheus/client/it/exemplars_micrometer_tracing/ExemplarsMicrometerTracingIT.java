package io.prometheus.client.it.exemplars_micrometer_tracing;

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
 * Test if traces from Micrometer Tracing are picked up as Exemplars.
 **/
public class ExemplarsMicrometerTracingIT {

    private Volume volume;
    private GenericContainer<?> javaContainer;

    private final String appJar = "example-micrometer-tracing-app.jar";
    private final String image = "openjdk:11-jre";
    private final String[] cmd = new String[] {"java", "-cp", appJar + ":dependency/*", ExampleApplication.class.getName()};

    @Before
    public void setUp() throws IOException, URISyntaxException {
        volume = Volume.create("exemplars-micrometer-tracing-test")
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

    @Test
    public void exemplarsShouldBeFound() {
        javaContainer.start();
        List<String> metrics = Scraper.scrape("http://localhost:" + javaContainer.getMappedPort(9000) + "/metrics", 10_000);
        boolean testTotalWithExemplarFound = false;
        boolean testTotalWithoutExemplarFound = false;
        for (String metric : metrics) {
            System.out.println(metric);
            if (metric.matches("^test_total 1\\.0 # \\{span_id=\"22f69a0e2c0ab635\",trace_id=\"45e09ee1c39e1f8f\"} 1.0 [0-9.]+$")) {
                testTotalWithExemplarFound = true;
            }
            if (metric.matches("^test_total 1\\.0$")) {
                testTotalWithoutExemplarFound = true;
            }
        }

        Assert.assertTrue("test_total metric with exemplars expected", testTotalWithExemplarFound);
        Assert.assertFalse("test_total without exemplar should not be there", testTotalWithoutExemplarFound);
    }
}
