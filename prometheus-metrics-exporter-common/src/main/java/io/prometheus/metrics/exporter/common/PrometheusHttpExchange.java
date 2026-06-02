package io.prometheus.metrics.exporter.common;

import io.prometheus.metrics.annotations.StableApi;
import java.io.IOException;

@StableApi
public interface PrometheusHttpExchange extends AutoCloseable {
  PrometheusHttpRequest getRequest();

  PrometheusHttpResponse getResponse();

  void handleException(IOException e) throws IOException;

  void handleException(RuntimeException e);

  @Override
  void close();
}
