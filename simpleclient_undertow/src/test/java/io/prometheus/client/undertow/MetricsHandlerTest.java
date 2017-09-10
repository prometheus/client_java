package io.prometheus.client.undertow;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsHandlerTest {

    private static Undertow server;
    private static URI testURI;

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException {

        String host = InetAddress.getLocalHost().getHostAddress();

        ServerSocket socket = new ServerSocket(0);
        Integer port = socket.getLocalPort();
        socket.close();

        CollectorRegistry registry = new CollectorRegistry();
        HttpHandler handler = new MetricsHandler(registry);

        server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(handler)
                .build();

        testURI = new URI("http://" + host + ":" + port);

        server.start();

        Gauge.build("a", "a help").register(registry);
        Gauge.build("b", "b help").register(registry);
        Gauge.build("c", "c help").register(registry);
    }

    @AfterClass
    public static void tearDown() {
        server.stop();
    }

    @Test
    public void metricsRequest_shouldReturnMetrics() {
        String out = makeRequest();

        assertThat(out).contains("a 0.0");
        assertThat(out).contains("b 0.0");
        assertThat(out).contains("c 0.0");
    }

    @Test
    public void metricsRequest_shouldAllowFilteringMetrics() {
        String out = makeRequest("name[]=b", "name[]=c");

        assertThat(out).doesNotContain("a 0.0");
        assertThat(out).contains("b 0.0");
        assertThat(out).contains("c 0.0");
    }


    private String makeRequest(String... values) {
        URI uri = testURI;
        if (values.length > 0) {
            uri = createURI(testURI, values);
        }

        try (Scanner scanner = new Scanner(uri.toURL().openStream(), "UTF-8").useDelimiter("\\A")) {
            return scanner.next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URI createURI(URI uri, String[] values) {
        String query = StringUtils.join(values, "&");
        try {
            return new URI(uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    query,
                    uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
