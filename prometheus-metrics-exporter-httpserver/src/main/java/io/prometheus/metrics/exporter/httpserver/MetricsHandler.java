package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.common.PrometheusScrapeHandler;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for the /metrics endpoint
 */
public class MetricsHandler implements HttpHandler {

    private final PrometheusScrapeHandler prometheusScrapeHandler;

    public MetricsHandler() {
        prometheusScrapeHandler = new PrometheusScrapeHandler();
    }

    public MetricsHandler(PrometheusRegistry registry) {
        prometheusScrapeHandler = new PrometheusScrapeHandler(registry);
    }

    public MetricsHandler(PrometheusProperties config) {
        prometheusScrapeHandler = new PrometheusScrapeHandler(config);
    }

    public MetricsHandler(PrometheusProperties config, PrometheusRegistry registry) {
        prometheusScrapeHandler = new PrometheusScrapeHandler(config, registry);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        prometheusScrapeHandler.handleRequest(new HttpExchangeAdapter(t));
    }
}
