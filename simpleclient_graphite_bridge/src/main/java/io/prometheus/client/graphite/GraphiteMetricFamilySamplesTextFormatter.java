package io.prometheus.client.graphite;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.format.AbstractMetricFamilySamplesTextFormatter;

/**
 * Implementation of {@code MetricFamilySamplesTextFormatter} for Graphite.
 */
public class GraphiteMetricFamilySamplesTextFormatter extends AbstractMetricFamilySamplesTextFormatter {
  /**
   * Graphite invalid characters.
   */
  private static final Pattern INVALID_GRAPHITE_CHARS = Pattern.compile("[^a-zA-Z0-9_-]");

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(final Writer writer, final Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    Matcher m = INVALID_GRAPHITE_CHARS.matcher("");
    long now = System.currentTimeMillis() / 1000;
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(mfs)) {
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        m.reset(sample.name);
        writer.write(m.replaceAll("_"));
        for (int i = 0; i < sample.labelNames.size(); ++i) {
          m.reset(sample.labelValues.get(i));
          writer.write("." + sample.labelNames.get(i) + "." + m.replaceAll("_"));
        }
        writer.write(" " + sample.value + " " + now + "\n");
      }
    }
  }
}
