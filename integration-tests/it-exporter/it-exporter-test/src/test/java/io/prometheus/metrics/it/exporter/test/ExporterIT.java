package io.prometheus.metrics.it.exporter.test;

import io.prometheus.client.it.common.LogConsumer;
import io.prometheus.client.it.common.Volume;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_25_3.Metrics;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@RunWith(Parameterized.class)
public class ExporterIT {

    private final GenericContainer<?> sampleAppContainer;
    private final Volume sampleAppVolume;
    private final String sampleApp;

    @Parameterized.Parameters(name = "{0}")
    public static String[] sampleApps() {
        return new String[]{
                "exporter-httpserver-sample",
                "exporter-servlet-tomcat-sample",
                "exporter-servlet-jetty-sample",
        };
    }

    public ExporterIT(String sampleApp) throws IOException, URISyntaxException {
        this.sampleApp = sampleApp;
        this.sampleAppVolume = Volume.create("it-exporter")
                .copy("../../it-" + sampleApp + "/target/" + sampleApp + ".jar");
        this.sampleAppContainer = new GenericContainer<>("openjdk:17")
                .withFileSystemBind(sampleAppVolume.getHostPath(), "/app", BindMode.READ_ONLY)
                .withWorkingDirectory("/app")
                .withLogConsumer(LogConsumer.withPrefix(sampleApp))
                .withExposedPorts(9400);
    }

    @After
    public void tearDown() throws IOException {
        sampleAppContainer.stop();
        sampleAppVolume.remove();
    }

    @Test
    public void testOpenMetricsTextFormat() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", "", "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
        Assert.assertEquals(200, response.status);
        assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
        Assert.assertNull(response.getHeader("Content-Encoding"));
        Assert.assertNull(response.getHeader("Transfer-Encoding"));
        Assert.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
        String bodyString = new String(response.body);
        Assert.assertTrue(bodyString.contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1"));
        Assert.assertTrue(bodyString.contains("temperature_celsius{location=\"inside\"} 23.0"));
        Assert.assertTrue(bodyString.contains("temperature_celsius{location=\"outside\"} 27.0"));
        Assert.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
        // OpenMetrics text format has a UNIT.
        Assert.assertTrue(bodyString.contains("# UNIT uptime_seconds seconds"));
    }

    @Test
    public void testPrometheusTextFormat() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", "");
        Assert.assertEquals(200, response.status);
        assertContentType("text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
        Assert.assertNull(response.getHeader("Content-Encoding"));
        Assert.assertNull(response.getHeader("Transfer-Encoding"));
        Assert.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
        String bodyString = new String(response.body);
        Assert.assertTrue(bodyString.contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1"));
        Assert.assertTrue(bodyString.contains("temperature_celsius{location=\"inside\"} 23.0"));
        Assert.assertTrue(bodyString.contains("temperature_celsius{location=\"outside\"} 27.0"));
        Assert.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
        // Prometheus text format does not have a UNIT.
        Assert.assertFalse(bodyString.contains("# UNIT uptime_seconds seconds"));
    }

    @Test
    public void testPrometheusProtobufFormat() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", "", "Accept", "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
        Assert.assertEquals(200, response.status);
        assertContentType("application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited", response.getHeader("Content-Type"));
        Assert.assertNull(response.getHeader("Content-Encoding"));
        Assert.assertNull(response.getHeader("Transfer-Encoding"));
        Assert.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
        List<Metrics.MetricFamily> metrics = new ArrayList<>();
        InputStream in = new ByteArrayInputStream(response.body);
        while (in.available() > 0) {
            metrics.add(Metrics.MetricFamily.parseDelimitedFrom(in));
        }
        Assert.assertEquals(3, metrics.size());
        // metrics are sorted by name
        Assert.assertEquals("integration_test_info", metrics.get(0).getName());
        Assert.assertEquals("temperature_celsius", metrics.get(1).getName());
        Assert.assertEquals("uptime_seconds_total", metrics.get(2).getName());
    }

    @Test
    public void testCompression() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", "",
                "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8",
                "Accept-Encoding", "gzip");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("gzip", response.getHeader("Content-Encoding"));
        if (response.getHeader("Content-Length") != null) {
            // The servlet container might set a content length as the body is very small.
            Assert.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
            Assert.assertNull(response.getHeader("Transfer-Encoding"));
        } else {
            // If no content length is set, transfer-encoding chunked must be used.
            Assert.assertEquals("chunked", response.getHeader("Transfer-Encoding"));
        }
        assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
        String body = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))), UTF_8);
        Assert.assertTrue(body.contains("uptime_seconds_total 17.0"));
    }

    @Test
    public void testErrorHandling() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "error")
                .start();
        Response response = scrape("GET", "");
        Assert.assertEquals(500, response.status);
        Assert.assertTrue(new String(response.body, UTF_8).contains("Simulating an error."));
    }

    @Test
    public void testHeadRequest() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response fullResponse = scrape("GET", "");
        int size = fullResponse.body.length;
        Assert.assertTrue(size > 0);
        Response headResponse = scrape("HEAD", "");
        Assert.assertEquals(200, headResponse.status);
        Assert.assertEquals(Integer.toString(size), headResponse.getHeader("Content-Length"));
        Assert.assertEquals(0, headResponse.body.length);
    }

    @Test
    public void testDebug() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", "debug=openmetrics");
        Assert.assertEquals(200, response.status);
        assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
        String bodyString = new String(response.body, UTF_8);
        Assert.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
        Assert.assertTrue(bodyString.contains("# UNIT uptime_seconds seconds"));
    }

    @Test
    public void testNameFilter() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", nameParam("integration_test_info") + "&" + nameParam("uptime_seconds_total"),
                "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
        Assert.assertEquals(200, response.status);
        assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
        String bodyString = new String(response.body, UTF_8);
        Assert.assertTrue(bodyString.contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1"));
        Assert.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
        Assert.assertFalse(bodyString.contains("temperature_celsius"));
    }

    @Test
    public void testEmptyResponseOpenMetrics() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", nameParam("none_existing"),
                "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
        Assert.assertEquals(200, response.status);
        assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
        Assert.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
        Assert.assertEquals("# EOF\n", new String(response.body, UTF_8));
    }

    @Test
    public void testEmptyResponseText() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", nameParam("none_existing"));
        Assert.assertEquals(200, response.status);
        assertContentType("text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
        if (response.getHeader("Content-Length") != null) { // HTTPServer does not send a zero content length, which is ok
            Assert.assertEquals("0", response.getHeader("Content-Length"));
        }
        Assert.assertEquals(0, response.body.length);
    }

    @Test
    public void testEmptyResponseProtobuf() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", nameParam("none_existing"),
                "Accept", "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
        Assert.assertEquals(200, response.status);
        assertContentType("application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited", response.getHeader("Content-Type"));
        Assert.assertEquals(0, response.body.length);
    }

    @Test
    public void testEmptyResponseGzipOpenMetrics() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", nameParam("none_existing"),
                "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8",
                "Accept-Encoding", "gzip");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("gzip", response.getHeader("Content-Encoding"));
        String body = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))), UTF_8);
        Assert.assertEquals("# EOF\n", body);
    }

    @Test
    public void testEmptyResponseGzipText() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", nameParam("none_existing"),
                "Accept-Encoding", "gzip");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("gzip", response.getHeader("Content-Encoding"));
        String body = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))), UTF_8);
        Assert.assertEquals(0, body.length());
    }

    private String nameParam(String name) throws UnsupportedEncodingException {
        return URLEncoder.encode("name[]", UTF_8.name()) + "=" + URLEncoder.encode(name, UTF_8.name());
    }

    @Test
    public void testDebugUnknown() throws IOException {
        sampleAppContainer
                .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
                .start();
        Response response = scrape("GET", "debug=unknown");
        Assert.assertEquals(500, response.status);
        assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
    }

    private void assertContentType(String expected, String actual) {
        if (!expected.replace(" ", "").equals(actual)) {
            Assert.assertEquals(expected, actual);
        }
    }

    private Response scrape(String method, String queryString, String... requestHeaders) throws IOException {
        long timeoutMillis = TimeUnit.SECONDS.toMillis(5);
        URL url = new URL("http://localhost:" + sampleAppContainer.getMappedPort(9400) + "/metrics?" + queryString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        for (int i = 0; i < requestHeaders.length; i += 2) {
            con.setRequestProperty(requestHeaders[i], requestHeaders[i + 1]);
        }
        long start = System.currentTimeMillis();
        Exception exception = null;
        while (System.currentTimeMillis() - start < timeoutMillis) {
            try {
                if (con.getResponseCode() == 200) {
                    return new Response(con.getResponseCode(), con.getHeaderFields(), IOUtils.toByteArray(con.getInputStream()));
                } else {
                    return new Response(con.getResponseCode(), con.getHeaderFields(), IOUtils.toByteArray(con.getErrorStream()));
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
        Assert.fail("timeout while getting metrics from " + url);
        return null; // will not happen
    }

    private static class Response {
        private final int status;
        private final Map<String, String> headers;
        private final byte[] body;

        private Response(int status, Map<String, List<String>> headers, byte[] body) {
            this.status = status;
            this.headers = new HashMap<>(headers.size());
            this.body = body;
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null) { // HttpUrlConnection uses pseudo key "null" for the status line
                    this.headers.put(entry.getKey().toLowerCase(), entry.getValue().get(0));
                }
            }
        }

        private String getHeader(String name) {
            // HTTP headers are case-insensitive
            return headers.get(name.toLowerCase());
        }
    }
}
