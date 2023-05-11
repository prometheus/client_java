package io.prometheus.client.it.java_versions;

import com.github.dockerjava.api.model.Ulimit;
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
 * Smoke test with different Java versions.
 */
@RunWith(Parameterized.class)
public class JavaVersionsIT {

  private final Volume exampleApplicationDir;
  private final GenericContainer<?> javaContainer;

  @Parameterized.Parameters(name="{0}")
  public static String[] images() {
    return new String[] {

            // HotSpot
            "openjdk:8-jre",
            "openjdk:11-jre",
            "openjdk:17",
            "adoptopenjdk/openjdk16:ubi-minimal-jre",

            // OpenJ9
            "ibmjava:8-jre",
            "adoptopenjdk/openjdk11-openj9:alpine",
    };
  }

  public JavaVersionsIT(String image) throws IOException, URISyntaxException {
    String exampleApplicationJar = "it_java_versions-" + Version.loadProjectVersion() + ".jar";
    exampleApplicationDir = Volume.create("it-java-versions")
            .copyFromTargetDirectory(exampleApplicationJar);
    javaContainer = new GenericContainer<>(image)
            .withCreateContainerCmdModifier(c -> c.getHostConfig().withUlimits(new Ulimit[]{new Ulimit("nofile", 65536L, 65536L)}))
            .withFileSystemBind(exampleApplicationDir.getHostPath(), "/app", BindMode.READ_ONLY)
            .withWorkingDirectory("/app")
            .withLogConsumer(LogConsumer.withPrefix(image))
            .withExposedPorts(9000)
            .withCommand("/bin/sh", "-c", "java -version && java -jar " + exampleApplicationJar);
  }

  private final List<String> exampleMetrics = Arrays.asList(
      "test_total{path=\"/hello-world\"}",
      "jvm_memory_bytes_used{area=\"heap\"}"
  );

  @Before
  public void setUp() {
    javaContainer.start();
  }

  @After
  public void tearDown() throws IOException {
    javaContainer.stop();
    exampleApplicationDir.remove();
  }

  @Test
  public void testExampleMetrics() {
    List<String> metrics = Scraper.scrape("http://localhost:" + javaContainer.getMappedPort(9000) + "/metrics", 10_000);
    for (String metric : exampleMetrics) {
      Assert.assertTrue(metric + " not found", metrics.stream()
          .filter(m -> m.startsWith(metric + " "))
          .peek(System.out::println)
          .findAny()
          .isPresent());
    }
  }
}
