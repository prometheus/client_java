package io.prometheus.client.exporter.common.formatter;

import io.prometheus.client.Collector;
import io.prometheus.client.TextFormatter;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.util.Enumeration;

/** Implement TextFormat#writeOpenMetrics100. */
public class OpenMetricsTextFormatter extends TextFormatter {
  public OpenMetricsTextFormatter(MetricsWriter writer) {
    super(writer);
  }

  @Override
  public void format(MetricSnapshotSamples samples) throws IOException {
    // TODO
  }

  @Override
  public void format(Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
    TextFormat.writeOpenMetrics100(this.writer, mfs);
  }
}
