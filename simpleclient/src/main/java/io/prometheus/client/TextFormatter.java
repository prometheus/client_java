package io.prometheus.client;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public abstract class TextFormatter {

  protected final MetricsWriter writer;

  public TextFormatter(MetricsWriter writer) {
    if (null == writer) {
      throw new IllegalArgumentException();
    }

    this.writer = writer;
  }

  public abstract void format(MetricSnapshotSamples samples) throws IOException;

  public abstract void format(Enumeration<Collector.MetricFamilySamples> mfs) throws IOException;

  protected void flush() throws IOException {
    // noop
  }

  public static class MetricSnapshotSamples {
    public final String name;
    public final String unit;
    public final Collector.Type type;
    public final String help;
    public final List<String> labelNames;
    public final Map<List<String>, ?> children;

    public MetricSnapshotSamples(
        String name,
        String unit,
        Collector.Type type,
        String help,
        List<String> labelNames,
        Map<List<String>, ?> children) {
      this.name = name;
      this.unit = unit;
      this.type = type;
      this.help = help;
      this.labelNames = labelNames;
      this.children = children;
    }
  }

  public static class HistogramMetricSnapshotSamples extends MetricSnapshotSamples {
    public final double[] buckets;

    public HistogramMetricSnapshotSamples(
        String name,
        String unit,
        Collector.Type type,
        String help,
        List<String> labelNames,
        Map<List<String>, ?> children,
        double[] buckets) {
      super(name, unit, type, help, labelNames, children);
      this.buckets = buckets;
    }
  }

  public abstract static class MetricsWriter extends Writer {
    public void write(byte[] bytes) throws IOException {
      this.write(bytes, 0, bytes.length);
    }

    public abstract void write(byte[] bytes, int offset, int length) throws IOException;

    public <T> T getBuffer() {
      throw new UnsupportedOperationException();
    }

    public MetricsWriter append(MetricsWriter other) throws IOException {
      throw new UnsupportedOperationException();
    }
  }
}
