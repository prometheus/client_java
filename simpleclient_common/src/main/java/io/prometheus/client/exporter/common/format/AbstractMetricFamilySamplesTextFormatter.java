package io.prometheus.client.exporter.common.format;

import java.nio.charset.Charset;

import io.prometheus.client.Collector;

/**
 * Formats {@code MetricFamilySamples} for wire.
 */
public abstract class AbstractMetricFamilySamplesTextFormatter implements MetricFamilySamplesTextFormatter {
  /**
   * The default charset; {@code UTF-8} charset.
   */
  private static final Charset DEFAULT_CHARSET_UTF8 = Charset.forName("UTF-8");

  /**
   * The charset.
   */
  private final Charset charset;

  /**
   * Constructs an instance of {@code AbstractMetricFamilySamplesTextFormatter}.
   * @param charset The charset.
   */
  protected AbstractMetricFamilySamplesTextFormatter(final Charset charset) {
    this.charset = charset;
  }

  /**
   * Constructs an instance of {@code AbstractMetricFamilySamplesTextFormatter}.
   */
  protected AbstractMetricFamilySamplesTextFormatter() {
    this(DEFAULT_CHARSET_UTF8);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Charset getCharset() {
    return this.charset;
  }

  /**
   * Converts the metric type to a string.
   * @param t The metric type.
   * @return The string.
   */
  protected String formatTypeToString(final Collector.Type t) {
    switch (t) {
      case GAUGE:
        return "gauge";
      case COUNTER:
        return "counter";
      case SUMMARY:
        return "summary";
      case HISTOGRAM:
        return "histogram";
      default:
        return "untyped";
    }
  }
}
