package io.prometheus.client.exporter;

import io.prometheus.client.internal.Adapter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Predicate;
import io.prometheus.client.servlet.common.exporter.Exporter;
import io.prometheus.client.servlet.common.exporter.ServletConfigurationException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.prometheus.client.internal.Adapter.wrap;

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
      exporter.init(Adapter.wrap(servletConfig));
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