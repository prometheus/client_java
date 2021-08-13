package io.prometheus.client.servlet.common.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.servlet.common.adapter.HttpServletRequestAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletResponseAdapter;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The MetricsServlet class exists to provide a simple way of exposing the metrics values.
 *
 */
public class Exporter {

  private CollectorRegistry registry;

  /**
   * Construct a MetricsServlet for the default registry.
   */
  public Exporter() {
    this(CollectorRegistry.defaultRegistry);
  }

  /**
   * Construct a MetricsServlet for the given registry.
   * @param registry collector registry
   */
  public Exporter(CollectorRegistry registry) {
    this.registry = registry;
  }

  public void doGet(final HttpServletRequestAdapter req, final HttpServletResponseAdapter resp) throws IOException {
    resp.setStatus(200);
    String contentType = TextFormat.chooseContentType(req.getHeader("Accept"));
    resp.setContentType(contentType);

    Writer writer = new BufferedWriter(resp.getWriter());
    try {
      TextFormat.writeFormat(contentType, writer, registry.filteredMetricFamilySamples(parse(req)));
      writer.flush();
    } finally {
      writer.close();
    }
  }

  private Set<String> parse(HttpServletRequestAdapter req) {
    String[] includedParam = req.getParameterValues("name[]");
    if (includedParam == null) {
      return Collections.emptySet();
    } else {
      return new HashSet<String>(Arrays.asList(includedParam));
    }
  }

  public void doPost(final HttpServletRequestAdapter req, final HttpServletResponseAdapter resp)
          throws IOException {
    doGet(req, resp);
  }
}
