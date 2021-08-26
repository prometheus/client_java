package io.prometheus.client.servlet.common.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.SampleNameFilter;
import io.prometheus.client.Predicate;
import io.prometheus.client.servlet.common.adapter.HttpServletRequestAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletResponseAdapter;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.servlet.common.adapter.ServletConfigAdapter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The MetricsServlet class exists to provide a simple way of exposing the metrics values.
 */
public class Exporter {

  public static final String NAME_MUST_BE_EQUAL_TO = "name-must-be-equal-to";
  public static final String NAME_MUST_NOT_BE_EQUAL_TO = "name-must-not-be-equal-to";
  public static final String NAME_MUST_START_WITH = "name-must-start-with";
  public static final String NAME_MUST_NOT_START_WITH = "name-must-not-start-with";

  private CollectorRegistry registry;
  private Predicate<String> sampleNameFilter;

  /**
   * Construct a MetricsServlet for the given registry.
   * @param registry collector registry
   * @param sampleNameFilter programmatically set a {@link SampleNameFilter}.
   *                         If there are any filter options configured in {@code ServletConfig}, they will be merged
   *                         so that samples need to pass both filters to be exported.
   *                         sampleNameFilter may be {@code null} indicating that nothing should be filtered.
   */
  public Exporter(CollectorRegistry registry, Predicate<String> sampleNameFilter) {
    this.registry = registry;
    this.sampleNameFilter = sampleNameFilter;
  }

  public void init(ServletConfigAdapter servletConfig) throws ServletConfigurationException {
    List<String> allowedNames = SampleNameFilter.stringToList(servletConfig.getInitParameter(NAME_MUST_BE_EQUAL_TO));
    List<String> excludedNames = SampleNameFilter.stringToList(servletConfig.getInitParameter(NAME_MUST_NOT_BE_EQUAL_TO));
    List<String> allowedPrefixes = SampleNameFilter.stringToList(servletConfig.getInitParameter(NAME_MUST_START_WITH));
    List<String> excludedPrefixes = SampleNameFilter.stringToList(servletConfig.getInitParameter(NAME_MUST_NOT_START_WITH));
    if (!allowedPrefixes.isEmpty() || !excludedPrefixes.isEmpty() || !allowedNames.isEmpty() || !excludedNames.isEmpty()) {
      SampleNameFilter filter = new SampleNameFilter.Builder()
              .nameMustBeEqualTo(allowedNames)
              .nameMustNotBeEqualTo(excludedNames)
              .nameMustStartWith(allowedPrefixes)
              .nameMustNotStartWith(excludedPrefixes)
              .build();
      if (this.sampleNameFilter != null) {
        this.sampleNameFilter = filter.and(this.sampleNameFilter);
      } else {
        this.sampleNameFilter = filter;
      }
    }
  }

  public void doGet(final HttpServletRequestAdapter req, final HttpServletResponseAdapter resp) throws IOException {
    resp.setStatus(200);
    String contentType = TextFormat.chooseContentType(req.getHeader("Accept"));
    resp.setContentType(contentType);

    Writer writer = new BufferedWriter(resp.getWriter());
    try {
      Predicate<String> filter = SampleNameFilter.restrictToNamesEqualTo(sampleNameFilter, parse(req));
      if (filter == null) {
        TextFormat.writeFormat(contentType, writer, registry.metricFamilySamples());
      } else {
        TextFormat.writeFormat(contentType, writer, registry.filteredMetricFamilySamples(filter));
      }
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
