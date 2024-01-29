package io.prometheus.metrics.exporter.servlet.javax;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.common.PrometheusScrapeHandler;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class extends HttpServlet to create a servlet for exporting Prometheus metrics.
 * It uses a PrometheusScrapeHandler to handle HTTP GET requests and export metrics.
 * The servlet can be configured with custom PrometheusProperties and a PrometheusRegistry.
 */
public class PrometheusMetricsServlet extends HttpServlet {

    private final PrometheusScrapeHandler handler;

    /**
     * Default constructor. Uses the default PrometheusProperties and PrometheusRegistry.
     */
    public PrometheusMetricsServlet() {
        this(PrometheusProperties.get(), PrometheusRegistry.defaultRegistry);
    }

    /**
     * Constructor with a custom PrometheusRegistry. Uses the default PrometheusProperties.
     *
     * @param registry the PrometheusRegistry to use
     */
    public PrometheusMetricsServlet(PrometheusRegistry registry) {
        this(PrometheusProperties.get(), registry);
    }

    /**
     * Constructor with custom PrometheusProperties. Uses the default PrometheusRegistry.
     *
     * @param config the PrometheusProperties to use
     */
    public PrometheusMetricsServlet(PrometheusProperties config) {
        this(config, PrometheusRegistry.defaultRegistry);
    }

    /**
     * Constructor with custom PrometheusProperties and PrometheusRegistry.
     *
     * @param config   the PrometheusProperties to use
     * @param registry the PrometheusRegistry to use
     */
    public PrometheusMetricsServlet(PrometheusProperties config, PrometheusRegistry registry) {
        this.handler = new PrometheusScrapeHandler(config, registry);
    }

    /**
     * Handles HTTP GET requests. Exports Prometheus metrics by delegating to the PrometheusScrapeHandler.
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handler.handleRequest(new HttpExchangeAdapter(request, response));
    }
}