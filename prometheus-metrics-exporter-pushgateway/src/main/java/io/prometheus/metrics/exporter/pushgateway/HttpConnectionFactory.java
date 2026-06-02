package io.prometheus.metrics.exporter.pushgateway;

import io.prometheus.metrics.annotations.StableApi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/** See {@link DefaultHttpConnectionFactory}. */
@FunctionalInterface
@StableApi
public interface HttpConnectionFactory {
  HttpURLConnection create(URL url) throws IOException;
}
