package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Immutable list of metric snapshots. */
public class MetricSnapshots implements Iterable<MetricSnapshot> {

  private final List<MetricSnapshot> snapshots;

  /** See {@link #MetricSnapshots(Collection)} */
  public MetricSnapshots(MetricSnapshot... snapshots) {
    this(Arrays.asList(snapshots));
  }

  /**
   * To create MetricSnapshots, you can either call the constructor directly or use {@link
   * #builder()}.
   *
   * @param snapshots the constructor creates a sorted copy of snapshots.
   * @throws IllegalArgumentException if snapshots contain conflicting metric types (same name but
   *     different metric types like Counter vs Gauge), or if two HistogramSnapshots share a name
   *     but differ in gauge histogram vs classic histogram.
   */
  public MetricSnapshots(Collection<MetricSnapshot> snapshots) {
    List<MetricSnapshot> list = new ArrayList<>(snapshots);
    list.sort(comparing(s -> s.getMetadata().getPrometheusName()));

    // Validate no conflicting metric types
    for (int i = 0; i < list.size() - 1; i++) {
      String name1 = list.get(i).getMetadata().getPrometheusName();
      String name2 = list.get(i + 1).getMetadata().getPrometheusName();

      if (name1.equals(name2)) {
        MetricSnapshot s1 = list.get(i);
        MetricSnapshot s2 = list.get(i + 1);
        Class<?> type1 = s1.getClass();
        Class<?> type2 = s2.getClass();

        if (!type1.equals(type2)) {
          throw new IllegalArgumentException(
              name1
                  + ": conflicting metric types: "
                  + type1.getSimpleName()
                  + " and "
                  + type2.getSimpleName());
        }

        // HistogramSnapshot: gauge histogram vs classic histogram are semantically different
        if (s1 instanceof HistogramSnapshot) {
          HistogramSnapshot h1 = (HistogramSnapshot) s1;
          HistogramSnapshot h2 = (HistogramSnapshot) s2;
          if (h1.isGaugeHistogram() != h2.isGaugeHistogram()) {
            throw new IllegalArgumentException(
                name1 + ": conflicting histogram types: gauge histogram and classic histogram");
          }
        }
      }
    }

    this.snapshots = unmodifiableList(list);
  }

  public static MetricSnapshots of(MetricSnapshot... snapshots) {
    return new MetricSnapshots(snapshots);
  }

  @Override
  public Iterator<MetricSnapshot> iterator() {
    return snapshots.iterator();
  }

  public int size() {
    return snapshots.size();
  }

  public MetricSnapshot get(int i) {
    return snapshots.get(i);
  }

  public Stream<MetricSnapshot> stream() {
    return snapshots.stream();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final List<MetricSnapshot> snapshots = new ArrayList<>();
    private final Set<String> prometheusNames = new HashSet<>();

    private Builder() {}

    public boolean containsMetricName(String name) {
      if (name == null) {
        return false;
      }
      String prometheusName = prometheusName(name);
      return prometheusNames.contains(prometheusName);
    }

    /** Add a metric snapshot. Call multiple times to add multiple metric snapshots. */
    public Builder metricSnapshot(MetricSnapshot snapshot) {
      snapshots.add(snapshot);
      prometheusNames.add(snapshot.getMetadata().getPrometheusName());
      return this;
    }

    public MetricSnapshots build() {
      return new MetricSnapshots(snapshots);
    }
  }
}
