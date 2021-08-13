package io.prometheus.client.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.servlet.common.exporter.Exporter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.prometheus.client.Adapter.wrap;

/**
 * The MetricsServlet class provides a simple way of exposing the metrics values.
 */
public class MetricsServlet extends HttpServlet {

  private final Exporter exporter;

  public MetricsServlet() {
    exporter = new Exporter();
  }

  public MetricsServlet(CollectorRegistry registry) {
    exporter = new Exporter(registry);
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