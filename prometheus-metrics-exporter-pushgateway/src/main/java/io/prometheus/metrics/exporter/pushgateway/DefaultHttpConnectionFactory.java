package io.prometheus.metrics.exporter.pushgateway;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultHttpConnectionFactory implements HttpConnectionFactory {
    @Override
    public HttpURLConnection create(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }
}
