package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handler for the /-/healthy endpoint
 */
public class HealthyHandler implements HttpHandler {

    private final byte[] responseBytes;
    private final String contentType;

    public HealthyHandler() {
        String responseString = "Exporter is healthy.\n";
        this.responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        this.contentType = "text/plain; charset=utf-8";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Content-Length", Integer.toString(responseBytes.length));
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } finally {
            exchange.close();
        }
    }
}
