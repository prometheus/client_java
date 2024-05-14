package io.prometheus.metrics.exporter.pushgateway;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This can be used for creating {@link Scheme#HTTP} and {@link Scheme#HTTPS} connections.
 * <p>
 * However, if you want to use it with {@link Scheme#HTTPS} you must make sure that the keychain for verifying the server certificate is set up correctly.
 * For an example of how to skip certificate verification see {@code PushGatewayTestApp} in {@code integration-tests/it-pushgateway/}.
 */
public class DefaultHttpConnectionFactory implements HttpConnectionFactory {
    @Override
    public HttpURLConnection create(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}
