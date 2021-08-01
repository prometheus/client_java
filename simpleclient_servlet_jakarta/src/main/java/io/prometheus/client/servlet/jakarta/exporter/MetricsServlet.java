package io.prometheus.client.servlet.jakarta.exporter;

import io.prometheus.client.servlet.common.exporter.Exporter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.prometheus.client.servlet.jakarta.Adapter.wrap;

/**
 * The MetricsServlet class provides a simple way of exposing the metrics values.
 */
public class MetricsServlet extends HttpServlet {

  private final Exporter exporter = new Exporter();

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    exporter.doGet(wrap(req), wrap(resp));
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    exporter.doPost(wrap(req), wrap(resp));
  }
}
