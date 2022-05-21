package io.prometheus.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MetricsFormatter {

  /**
   * Format given samples.
   *
   * @param samples
   */
  public abstract void format(MetricSnapshotSamples samples);

  /**
   * Format given samples.
   *
   * @param mfs
   */
  public abstract void format(List<Collector.MetricFamilySamples> mfs);

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
}
