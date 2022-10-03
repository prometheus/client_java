package io.prometheus.client.servlet.jakarta.exporter;

import io.prometheus.metrics.CollectorRegistry;
import io.prometheus.metrics.Predicate;
import io.prometheus.client.servlet.common.exporter.Exporter;

import io.prometheus.client.servlet.common.exporter.ServletConfigurationException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.prometheus.client.servlet.jakarta.Adapter.wrap;

/**
 * The MetricsServlet class provides a simple way of exposing the metrics values.
 */
public class MetricsServlet extends HttpServlet {

  private final Exporter exporter;

  public MetricsServlet() {
    this(CollectorRegistry.defaultRegistry, null);
  }

  public MetricsServlet(Predicate<String> sampleNameFilter) {
    this(CollectorRegistry.defaultRegistry, sampleNameFilter);
  }

  public MetricsServlet(CollectorRegistry registry) {
    this(registry, null);
  }

  public MetricsServlet(CollectorRegistry registry, Predicate<String> sampleNameFilter) {
    exporter = new Exporter(registry, sampleNameFilter);
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    try {
      super.init(servletConfig);
      exporter.init(wrap(servletConfig));
    } catch (ServletConfigurationException e) {
      throw new ServletException(e);
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    exporter.doGet(wrap(req), wrap(resp));
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    exporter.doPost(wrap(req), wrap(resp));
  }
}
