package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.exporter.common.PrometheusScrapeHandler;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.IOException;

/** Handler for the /metrics endpoint */
@StableApi
public class MetricsHandler implements HttpHandler {

  private final PrometheusScrapeHandler prometheusScrapeHandler;
  private final HttpErrorHandlingPolicy errorHandlingPolicy;

  public MetricsHandler() {
    this(new PrometheusScrapeHandler(), HttpErrorHandlingPolicy.builder().build());
  }

  public MetricsHandler(PrometheusRegistry registry) {
    this(new PrometheusScrapeHandler(registry), HttpErrorHandlingPolicy.builder().build());
  }

  public MetricsHandler(PrometheusProperties config) {
    this(new PrometheusScrapeHandler(config), HttpErrorHandlingPolicy.builder().build());
  }

  public MetricsHandler(PrometheusProperties config, PrometheusRegistry registry) {
    this(new PrometheusScrapeHandler(config, registry), HttpErrorHandlingPolicy.builder().build());
  }

  public MetricsHandler(
      PrometheusProperties config,
      PrometheusRegistry registry,
      HttpErrorHandlingPolicy errorHandlingPolicy) {
    this(new PrometheusScrapeHandler(config, registry), errorHandlingPolicy);
  }

  private MetricsHandler(
      PrometheusScrapeHandler prometheusScrapeHandler,
      HttpErrorHandlingPolicy errorHandlingPolicy) {
    this.prometheusScrapeHandler = prometheusScrapeHandler;
    this.errorHandlingPolicy = errorHandlingPolicy;
  }

  @Override
  public void handle(HttpExchange t) throws IOException {
    prometheusScrapeHandler.handleRequest(new HttpExchangeAdapter(t, errorHandlingPolicy));
  }
}
