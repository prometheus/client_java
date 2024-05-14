package io.prometheus.metrics.exporter.pushgateway;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface HttpConnectionFactory {
    HttpURLConnection create(String url) throws IOException;
}
