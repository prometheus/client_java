package io.prometheus.metrics.exporter.servlet.jakarta;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.common.PrometheusScrapeHandler;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A javax exporter servlet for Prometheus.
 * @author kingster
 *
 */
public class PrometheusMetricsServlet extends HttpServlet {

    private final PrometheusScrapeHandler handler;

    public PrometheusMetricsServlet() {
        this(PrometheusProperties.get(), PrometheusRegistry.defaultRegistry);
    }

    public PrometheusMetricsServlet(PrometheusRegistry registry) {
        this(PrometheusProperties.get(), registry);
    }

    public PrometheusMetricsServlet(PrometheusProperties config) {
        this(config, PrometheusRegistry.defaultRegistry);
    }

    public PrometheusMetricsServlet(PrometheusProperties config, PrometheusRegistry registry) {
        this.handler = new PrometheusScrapeHandler(config, registry);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handler.handleRequest(new HttpExchangeAdapter(request, response));
    }
}


