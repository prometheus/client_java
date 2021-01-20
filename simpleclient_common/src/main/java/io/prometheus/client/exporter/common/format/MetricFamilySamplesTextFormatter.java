package io.prometheus.client.exporter.common.format;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Enumeration;

import io.prometheus.client.Collector;

/**
 * Writes {@code MetricFamilySamples} to text for wire.
 */
public interface MetricFamilySamplesTextFormatter {
  /**
   * Gets the charset.
   * @return The charset.
   */
  Charset getCharset();

  /**
   * Write out the text version of the given {@code MetricFamilySamples}.
   * @param writer The writer.
   * @param mfs The metric family samples.
   * @throws IOException If the metrics could not be written.
   */
  void write(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException;
}
