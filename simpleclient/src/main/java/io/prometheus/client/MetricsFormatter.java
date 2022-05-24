package io.prometheus.client;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MetricsFormatter {

  private final MetricsWriter metricsWriter;

  public MetricsFormatter(MetricsWriter metricsWriter) {
    if (metricsWriter == null) {
      throw new IllegalArgumentException();
    }

    this.metricsWriter = metricsWriter;
  }

  /**
   * Format given samples.
   *
   * @param samples
   * @throws IOException thrown by MetricsWriter
   */
  public abstract void format(MetricSnapshotSamples samples) throws IOException;

  /**
   * Format given samples.
   *
   * @param mfs
   * @throws IOException thrown by MetricsWriter
   */
  public abstract void format(List<Collector.MetricFamilySamples> mfs) throws IOException;

  /**
   * Whether target type collect supported by MetricsFormatter#format(MetricSnapshotSamples samples)
   * or not.
   *
   * @param type Collect type
   * @return
   */
  public abstract boolean supported(Collector.Type type);

  public static class MetricSnapshotSamples {
    public final String name;
    public final String unit;
    public final String help;
    public final List<String> labelNames;
    public final Collector.Type type;
    public final Set<? extends Map.Entry<List<String>, ?>> children;

    public MetricSnapshotSamples(
        String name,
        String unit,
        String help,
        List<String> labelNames,
        Collector.Type type,
        Set<? extends Map.Entry<List<String>, ?>> children) {
      this.name = name;
      this.unit = unit;
      this.type = type;
      this.help = help;
      this.labelNames = labelNames;
      this.children = children;
    }
  }

  public static class HistogramSnapshotSamples extends MetricSnapshotSamples {
    public final double[] buckets;

    public HistogramSnapshotSamples(
        String name,
        String unit,
        String help,
        List<String> labelNames,
        Collector.Type type,
        Set<? extends Map.Entry<List<String>, ?>> children,
        double[] buckets) {
      super(name, unit, help, labelNames, type, children);
      this.buckets = buckets;
    }
  }

  /**
   * MetricsWriter for MetricsFormatter.
   */
  public static abstract class MetricsWriter extends Writer {
    public void write(byte[] bytes) throws IOException {
      this.write(bytes, 0, bytes.length);
    }

    public abstract void write(byte[] bytes, int offset, int length) throws IOException;

    /**
     * Get buffer.
     *
     * @param <T>
     * @return
     */
    public <T> T getBuffer() {
      throw new UnsupportedOperationException();
    }

    /**
     * Append other writer to current.
     *
     * @param other
     * @return
     * @throws IOException
     */
    public MetricsWriter append(MetricsWriter other) throws IOException {
      throw new UnsupportedOperationException();
    }
  }
}
