package io.prometheus.client.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Enumeration;
import java.io.IOException;
import java.io.Writer;

public class MetricsServlet extends HttpServlet {

  private CollectorRegistry registry;

  /**
   * Construct a MetricsServlet for the default registry.
   */
  public MetricsServlet() {
    this(CollectorRegistry.defaultRegistry);
  }

  /**
   * Construct a MetricsServlet for the given registry.
   */
  public MetricsServlet(CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_OK);
    /* See https://docs.google.com/a/boxever.com/document/d/1ZjyKiKxZV83VI9ZKAXRGKaUKK2BIWCT7oiGBKDBpjEY/edit# 
     * for the output format specification. */
    resp.setContentType("text/plain; version=0.0.4; charset=utf-8");

    Writer writer = resp.getWriter();
    writeTextOutput(writer, registry.metricFamilySamples());
    writer.flush();
    writer.close();
  }

  static void writeTextOutput(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(mfs)) {
      writer.write("# HELP " + metricFamilySamples.name + " " + escapeHelp(metricFamilySamples.help) + "\n");
      writer.write("# TYPE " + metricFamilySamples.name + " " + typeString(metricFamilySamples.type) + "\n");
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        writer.write(sample.name);
        if (sample.labelNames.length > 0) {
          writer.write("{");
          for (int i = 0; i < sample.labelNames.length; ++i) {
            writer.write(sample.labelNames[i] + "=\"" + escapeLabelValue(sample.labelValues.get(i)) + "\",");
          }
          writer.write("}");
        }
        writer.write(" " + sample.value + "\n");
      }
    }
  }

  static String escapeHelp(String s) {
    return s.replace("\\", "\\\\").replace("\n", "\\n");
  }
  static String escapeLabelValue(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
  
  static String typeString(Collector.Type t) {
    switch (t) {
      case GAUGE:
        return "gauge";
      case COUNTER:
        return "counter";
      case SUMMARY:
        return "summary";
      default:
        return "uptyped";
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {
    doGet(req, resp);
  }

}
