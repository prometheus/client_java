package io.prometheus.metrics.it.exporter.test;

import io.prometheus.client.it.common.LogConsumer;
import io.prometheus.client.it.common.Volume;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_21_7.Metrics;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

class ExporterIT {
    private static class SampleApp implements AutoCloseable{
        public SampleApp(String appName) throws IOException, URISyntaxException {
            this.sampleAppVolume = Volume.create("it-exporter")
                    .copy("../../it-" + appName + "/target/" + appName + ".jar");
            this.sampleAppContainer = new GenericContainer<>("openjdk:17")
                    .withFileSystemBind(sampleAppVolume.getHostPath(), "/app", BindMode.READ_ONLY)
                    .withWorkingDirectory("/app")
                    .withLogConsumer(LogConsumer.withPrefix(appName))
                    .withExposedPorts(9400);
        }
        private final GenericContainer<?> sampleAppContainer;
        private final Volume sampleAppVolume;

        @Override
        public void close() throws Exception {
            sampleAppContainer.stop();
            sampleAppVolume.remove();
        }
    }

    private static Stream<Arguments> provideSampleAppNames() {
        return Stream.of(
                Arguments.of("exporter-httpserver-sample"),
                Arguments.of("exporter-servlet-tomcat-sample"),
                Arguments.of("exporter-servlet-jetty-sample")
        );
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testOpenMetricsTextFormat(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET", "", sampleApp.sampleAppContainer.getMappedPort(9400), "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
            Assertions.assertEquals(200, response.status);
            assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
            Assertions.assertNull(response.getHeader("Content-Encoding"));
            Assertions.assertNull(response.getHeader("Transfer-Encoding"));
            Assertions.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
            String bodyString = new String(response.body);
            Assertions.assertTrue(bodyString.contains("integration_test_info{test_name=\"" + appName + "\"} 1"));
            Assertions.assertTrue(bodyString.contains("temperature_celsius{location=\"inside\"} 23.0"));
            Assertions.assertTrue(bodyString.contains("temperature_celsius{location=\"outside\"} 27.0"));
            Assertions.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
            // OpenMetrics text format has a UNIT.
            Assertions.assertTrue(bodyString.contains("# UNIT uptime_seconds seconds"));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testPrometheusTextFormat(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET", "", sampleApp.sampleAppContainer.getMappedPort(9400));
            Assertions.assertEquals(200, response.status);
            assertContentType("text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
            Assertions.assertNull(response.getHeader("Content-Encoding"));
            Assertions.assertNull(response.getHeader("Transfer-Encoding"));
            Assertions.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
            String bodyString = new String(response.body);
            Assertions.assertTrue(bodyString.contains("integration_test_info{test_name=\"" + appName + "\"} 1"));
            Assertions.assertTrue(bodyString.contains("temperature_celsius{location=\"inside\"} 23.0"));
            Assertions.assertTrue(bodyString.contains("temperature_celsius{location=\"outside\"} 27.0"));
            Assertions.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
            // Prometheus text format does not have a UNIT.
            Assertions.assertFalse(bodyString.contains("# UNIT uptime_seconds seconds"));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testPrometheusProtobufFormat(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET", "", sampleApp.sampleAppContainer.getMappedPort(9400), "Accept", "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
            Assertions.assertEquals(200, response.status);
            assertContentType("application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited", response.getHeader("Content-Type"));
            Assertions.assertNull(response.getHeader("Content-Encoding"));
            Assertions.assertNull(response.getHeader("Transfer-Encoding"));
            Assertions.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
            List<Metrics.MetricFamily> metrics = new ArrayList<>();
            InputStream in = new ByteArrayInputStream(response.body);
            while (in.available() > 0) {
                metrics.add(Metrics.MetricFamily.parseDelimitedFrom(in));
            }
            Assertions.assertEquals(3, metrics.size());
            // metrics are sorted by name
            Assertions.assertEquals("integration_test_info", metrics.get(0).getName());
            Assertions.assertEquals("temperature_celsius", metrics.get(1).getName());
            Assertions.assertEquals("uptime_seconds_total", metrics.get(2).getName());
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testCompression(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET",
                    "", sampleApp.sampleAppContainer.getMappedPort(9400), "Accept",
                    "application/openmetrics-text; version=1.0.0; charset=utf-8", "Accept-Encoding", "gzip");
            Assertions.assertEquals(200, response.status);
            Assertions.assertEquals("gzip", response.getHeader("Content-Encoding"));
            if (response.getHeader("Content-Length") != null) {
                // The servlet container might set a content length as the body is very small.
                Assertions.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
                Assertions.assertNull(response.getHeader("Transfer-Encoding"));
            } else {
                // If no content length is set, transfer-encoding chunked must be used.
                Assertions.assertEquals("chunked", response.getHeader("Transfer-Encoding"));
            }
            assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
            String body = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))), UTF_8);
            Assertions.assertTrue(body.contains("uptime_seconds_total 17.0"));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testErrorHandling(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "error")
                .start();
        Response response = scrape("GET", "", sampleApp.sampleAppContainer.getMappedPort(9400));
        Assertions.assertEquals(500, response.status);
        Assertions.assertTrue(new String(response.body, UTF_8).contains("Simulating an error."));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testHeadRequest(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response fullResponse = scrape("GET", "", sampleApp.sampleAppContainer.getMappedPort(9400));
            int size = fullResponse.body.length;
            Assertions.assertTrue(size > 0);
            Response headResponse = scrape("HEAD", "", sampleApp.sampleAppContainer.getMappedPort(9400));
            Assertions.assertEquals(200, headResponse.status);
            Assertions.assertEquals(Integer.toString(size), headResponse.getHeader("Content-Length"));
            Assertions.assertEquals(0, headResponse.body.length);
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testDebug(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET", "debug=openmetrics", sampleApp.sampleAppContainer.getMappedPort(9400));
            Assertions.assertEquals(200, response.status);
            assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
            String bodyString = new String(response.body, UTF_8);
            Assertions.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
            Assertions.assertTrue(bodyString.contains("# UNIT uptime_seconds seconds"));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testNameFilter(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET",
                    nameParam("integration_test_info") + "&" + nameParam("uptime_seconds_total"), sampleApp.sampleAppContainer.getMappedPort(9400), "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
            Assertions.assertEquals(200, response.status);
            assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
            String bodyString = new String(response.body, UTF_8);
            Assertions.assertTrue(bodyString.contains("integration_test_info{test_name=\"" + appName + "\"} 1"));
            Assertions.assertTrue(bodyString.contains("uptime_seconds_total 17.0"));
            Assertions.assertFalse(bodyString.contains("temperature_celsius"));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testEmptyResponseOpenMetrics(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET",
                    nameParam("none_existing"), sampleApp.sampleAppContainer.getMappedPort(9400), "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
            Assertions.assertEquals(200, response.status);
            assertContentType("application/openmetrics-text; version=1.0.0; charset=utf-8", response.getHeader("Content-Type"));
            Assertions.assertEquals(Integer.toString(response.body.length), response.getHeader("Content-Length"));
            Assertions.assertEquals("# EOF\n", new String(response.body, UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testEmptyResponseText(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET", nameParam("none_existing"), sampleApp.sampleAppContainer.getMappedPort(9400));
            Assertions.assertEquals(200, response.status);
            assertContentType("text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
            if (response.getHeader("Content-Length") != null) { // HTTPServer does not send a zero content length, which is ok
                Assertions.assertEquals("0", response.getHeader("Content-Length"));
            }
            Assertions.assertEquals(0, response.body.length);
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testEmptyResponseProtobuf(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET",
                    nameParam("none_existing"), sampleApp.sampleAppContainer.getMappedPort(9400), "Accept", "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
            Assertions.assertEquals(200, response.status);
            assertContentType("application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited", response.getHeader("Content-Type"));
            Assertions.assertEquals(0, response.body.length);
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testEmptyResponseGzipOpenMetrics(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET",
                    nameParam("none_existing"), sampleApp.sampleAppContainer.getMappedPort(9400), "Accept",
                    "application/openmetrics-text; version=1.0.0; charset=utf-8", "Accept-Encoding", "gzip");
            Assertions.assertEquals(200, response.status);
            Assertions.assertEquals("gzip", response.getHeader("Content-Encoding"));
            String body = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))), UTF_8);
            Assertions.assertEquals("# EOF\n", body);
        }
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testEmptyResponseGzipText(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET",
                    nameParam("none_existing"), sampleApp.sampleAppContainer.getMappedPort(9400), "Accept-Encoding", "gzip");
            Assertions.assertEquals(200, response.status);
            Assertions.assertEquals("gzip", response.getHeader("Content-Encoding"));
            String body = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))), UTF_8);
            Assertions.assertEquals(0, body.length());
        }
    }

    private String nameParam(String name) throws UnsupportedEncodingException {
        return URLEncoder.encode("name[]", UTF_8.name()) + "=" + URLEncoder.encode(name, UTF_8.name());
    }

    @ParameterizedTest
    @MethodSource("provideSampleAppNames")
    void testDebugUnknown(String appName) throws Exception {
        try(SampleApp sampleApp = new SampleApp(appName)) {
            sampleApp.sampleAppContainer
                    .withCommand("java", "-jar", "/app/" + appName + ".jar", "9400", "success")
                    .start();
            Response response = scrape("GET", "debug=unknown", sampleApp.sampleAppContainer.getMappedPort(9400));
            Assertions.assertEquals(500, response.status);
            assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
        }
    }

    private void assertContentType(String expected, String actual) {
        if (!expected.replace(" ", "").equals(actual)) {
            Assertions.assertEquals(expected, actual);
        }
    }

    private Response scrape(String method, String queryString, Integer mappedPort, String... requestHeaders) throws IOException {
        long timeoutMillis = TimeUnit.SECONDS.toMillis(5);
        URL url = new URL("http://localhost:" + mappedPort + "/metrics?" + queryString);
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
        Assertions.fail("timeout while getting metrics from " + url);
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
