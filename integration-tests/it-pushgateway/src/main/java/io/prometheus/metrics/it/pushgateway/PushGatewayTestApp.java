package io.prometheus.metrics.it.pushgateway;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.pushgateway.Format;
import io.prometheus.metrics.exporter.pushgateway.HttpConnectionFactory;
import io.prometheus.metrics.exporter.pushgateway.PushGateway;
import io.prometheus.metrics.model.snapshots.Unit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static io.prometheus.metrics.exporter.pushgateway.Scheme.HTTPS;

/**
 * Example application using the {@link PushGateway}.
 */
public class PushGatewayTestApp {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java -jar pushgateway-test-app.jar <test>");
            System.exit(-1);
        }
        switch (args[0]) {
            case "simple":
                runSimpleTest();
                break;
            case "textFormat":
                runTextFormatTest();
                break;
            case "basicauth":
                runBasicAuthTest();
                break;
            case "ssl":
                runSslTest();
                break;
            default:
                System.err.println(args[0] + ": Not implemented.");
                System.exit(-1);
        }
    }

    private static void runSimpleTest() throws IOException {
        makeMetrics();
        PushGateway pg = PushGateway.builder().build();
        System.out.println("Pushing metrics...");
        pg.push();
        System.out.println("Push successful.");
    }

    private static void runTextFormatTest() throws IOException {
        makeMetrics();
        PushGateway pg = PushGateway.builder().format(Format.PROMETHEUS_TEXT).build();
        System.out.println("Pushing metrics...");
        pg.push();
        System.out.println("Push successful.");
    }

    private static void runBasicAuthTest() throws IOException {
        makeMetrics();
        PushGateway pg = PushGateway.builder()
                .basicAuth("my_user", "secret_password")
                .build();
        System.out.println("Pushing metrics...");
        pg.push();
        System.out.println("Push successful.");
    }

    private static void runSslTest() throws IOException {
        makeMetrics();
        PushGateway pg = PushGateway.builder()
                .scheme(HTTPS)
                .connectionFactory(insecureConnectionFactory)
                .build();
        System.out.println("Pushing metrics...");
        pg.push();
        System.out.println("Push successful.");
    }

    static TrustManager insecureTrustManager = new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
    };

    static HttpConnectionFactory insecureConnectionFactory = url -> {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{insecureTrustManager}, null);
            SSLContext.setDefault(sslContext);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier((hostname, session) -> true);
            return connection;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    };

    private static void makeMetrics() {
        Histogram sizes = Histogram.builder()
                .name("file_sizes_bytes")
                .classicUpperBounds(256, 512, 1024, 2048)
                .unit(Unit.BYTES)
                .register();
        sizes.observe(513);
        sizes.observe(814);
        sizes.observe(1553);
        Gauge duration = Gauge.builder()
                .name("my_batch_job_duration_seconds")
                .help("Duration of my batch job in seconds.")
                .unit(Unit.SECONDS)
                .register();
        duration.set(0.5);
    }
}
