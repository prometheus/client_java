package io.prometheus.metrics.exporter.common;

import java.io.IOException;

public interface PrometheusHttpExchange extends AutoCloseable {
    PrometheusHttpRequest getRequest();
    PrometheusHttpResponse getResponse();
    void handleException(IOException e) throws IOException;
    void handleException(RuntimeException e);

    @Override
    void close();
}
