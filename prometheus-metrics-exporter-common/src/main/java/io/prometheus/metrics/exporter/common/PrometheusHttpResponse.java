package io.prometheus.metrics.exporter.common;

import java.io.IOException;
import java.io.OutputStream;

public interface PrometheusHttpResponse {

    /**
     * See {@code jakarta.servlet.http.HttpServletResponse.setHeader(String, String)}
     */
    void setHeader(String name, String value);

    /**
     * This is equivalent to calling {@link com.sun.net.httpserver.HttpExchange#sendResponseHeaders(int, long)}
     * followed by {@link com.sun.net.httpserver.HttpExchange#getResponseBody()}.
     */
    OutputStream sendHeadersAndGetBody(int statusCode, int contentLength) throws IOException;
}
