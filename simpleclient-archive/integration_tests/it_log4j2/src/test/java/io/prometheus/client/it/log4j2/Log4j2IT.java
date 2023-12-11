package io.prometheus.client.it.log4j2;

import io.prometheus.client.it.common.LogConsumer;
import io.prometheus.client.it.common.Scraper;
import io.prometheus.client.it.common.Version;
import io.prometheus.client.it.common.Volume;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Test simpleclient_log4j2.
 */
public class Log4j2IT {

    private final String image = "openjdk:8-jre";
    private final Volume volume;
    private final GenericContainer<?> javaContainer;

    public Log4j2IT() throws IOException, URISyntaxException {
        String exampleApplicationJar = "it_log4j2-" + Version.loadProjectVersion() + ".jar";
        volume = Volume.create("it-log4j2")
                .copyFromTargetDirectory(exampleApplicationJar);
        String[] cmd = new String[]{"java", "-jar", exampleApplicationJar};
        javaContainer = new GenericContainer<>(image)
                .withFileSystemBind(volume.getHostPath(), "/app", BindMode.READ_ONLY)
                .withWorkingDirectory("/app")
                .withLogConsumer(LogConsumer.withPrefix(image))
                .withExposedPorts(9000)
                .withCommand(cmd);
        System.out.println("Volume directory: " + volume.getHostPath());
        System.out.println("Command: " + String.join(" ", cmd));
        System.out.println("Docker image: " + image);
    }

    @Before
    public void setUp() {
        javaContainer.start();
    }

    @After
    public void tearDown() throws IOException {
        javaContainer.stop();
        volume.remove();
    }

    @Test
    public void testLog4j2Metrics() {
        List<String> metrics = Scraper.scrape("http://localhost:" + javaContainer.getMappedPort(9000) + "/metrics", 10_000);
        for (String expectedMetric : new String[]{
                "log4j2_appender_total{level=\"trace\"} 0.0",
                "log4j2_appender_total{level=\"debug\"} 2.0",
                "log4j2_appender_total{level=\"info\"} 0.0",
                "log4j2_appender_total{level=\"warn\"} 1.0",
                "log4j2_appender_total{level=\"error\"} 0.0",
                "log4j2_appender_total{level=\"fatal\"} 0.0"
        }) {
            Assert.assertTrue(expectedMetric + " not found", metrics.contains(expectedMetric));
        }
    }
}
