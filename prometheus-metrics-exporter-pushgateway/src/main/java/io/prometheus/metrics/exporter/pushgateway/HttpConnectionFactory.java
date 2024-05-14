package io.prometheus.metrics.exporter.pushgateway;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * See {@link DefaultHttpConnectionFactory}.
 */
@FunctionalInterface
public interface HttpConnectionFactory {
    HttpURLConnection create(URL url) throws IOException;
}
