package io.prometheus.client.exporter.common;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.format.MetricFamilySamplesTextFormatter;
import io.prometheus.client.exporter.common.format.PrometheusV004MetricFamilySamplesTextFormatter;

public class TextFormat {
  /**
   * Default instance of {@code TextFormat}.
   */
  private static final MetricFamilySamplesTextFormatter V004 = new PrometheusV004MetricFamilySamplesTextFormatter();

  /**
   * Content-type for text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

  /**
   * Write out the text version 0.0.4 of the given MetricFamilySamples.
   */
  public static void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    V004.write(writer, mfs);
  }
}
