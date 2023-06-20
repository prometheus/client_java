package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handler for the /-/healthy endpoint
 */
public class HealthyHandler implements HttpHandler {

    private final static byte[] HEALTHY_RESPONSE = "Exporter is Healthy.".getBytes(StandardCharsets.UTF_8);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO: This is not complete (http status? compression? content length?)
        exchange.getResponseBody().write(HEALTHY_RESPONSE);
        exchange.close();
    }
}
