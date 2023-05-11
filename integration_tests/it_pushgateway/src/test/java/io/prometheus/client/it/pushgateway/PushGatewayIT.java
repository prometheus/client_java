package io.prometheus.client.it.pushgateway;

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
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RunWith(Parameterized.class)
public class PushGatewayIT {

    private final String batchJobJar;
    private final Volume batchJobDir;
    private final GenericContainer<?> javaContainer;
    private final GenericContainer<?> pushGatewayContainer;

    @Parameterized.Parameters(name = "{0}")
    public static String[] images() {
        return new String[] {
                "ibmjava:8-jre",
                "openjdk:11-slim",
                "openjdk:17"
        };
    }

    public PushGatewayIT(String image) throws IOException, URISyntaxException {
        batchJobJar = "it_pushgateway-" + Version.loadProjectVersion() + ".jar";
        batchJobDir = Volume.create("it-pushgateway-batch-job")
                .copyFromTargetDirectory(batchJobJar);
        Network network = Network.newNetwork();
        pushGatewayContainer = new GenericContainer<>("prom/pushgateway")
                .withCreateContainerCmdModifier(c -> c.getHostConfig().withUlimits(new Ulimit[]{new Ulimit("nofile", 65536L, 65536L)}))
                .withCopyFileToContainer(MountableFile.forClasspathResource("web-config.yml", 0644), "/")
                .withNetwork(network)
                .withNetworkAliases("pushgateway")
                .withLogConsumer(LogConsumer.withPrefix("prom/pushgateway"))
                .withCommand("--web.config.file=/web-config.yml")
                .withExposedPorts(9091);
        javaContainer = new GenericContainer<>(image)
                .withCreateContainerCmdModifier(c -> c.getHostConfig().withUlimits(new Ulimit[]{new Ulimit("nofile", 65536L, 65536L)}))
                .withFileSystemBind(batchJobDir.getHostPath(), "/app", BindMode.READ_ONLY)
                .withNetwork(network)
                .withWorkingDirectory("/app")
                .withLogConsumer(LogConsumer.withPrefix(image))
                .withCommand("sleep", "30");
    }

    @Before
    public void setUp() {
        pushGatewayContainer.start();
        javaContainer.start();
    }

    @After
    public void tearDown() throws IOException {
        javaContainer.stop();
        pushGatewayContainer.stop();
        batchJobDir.remove();
    }

    @Test
    public void testPushGateway() throws IOException, InterruptedException {
        String user = "testUser";
        String password = "testPwd";
        Container.ExecResult r = javaContainer.execInContainer("java", "-jar", batchJobJar, "pushgateway:9091", user, password);
        System.err.println(r.getStderr());
        System.out.println(r.getStdout());
        List<String> metrics = Scraper.scrape("http://localhost:" + pushGatewayContainer.getMappedPort(9091) + "/metrics", user, password, 10_000);
        assertContains(metrics, "my_batch_job_duration_seconds");
        assertContains(metrics, "my_batch_job_last_success");
    }

    private void assertContains(List<String> metrics, String metric) {
        for (String line : metrics) {
            if (line.startsWith(metric + " ") || line.startsWith(metric + "{")) {
                return;
            }
        }
        Assert.fail(metric + " not found");
    }
}
