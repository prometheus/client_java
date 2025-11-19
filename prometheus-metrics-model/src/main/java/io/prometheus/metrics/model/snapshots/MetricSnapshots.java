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
   * @throws IllegalArgumentException if snapshots contains duplicate metric names. To avoid
   *     duplicate metric names use {@link #builder()} and check {@link
   *     Builder#containsMetricName(String)} before calling {@link
   *     Builder#metricSnapshot(MetricSnapshot)}.
   */
  public MetricSnapshots(Collection<MetricSnapshot> snapshots) {
    this(snapshots, true);
  }

  /**
   * Private constructor with option to skip duplicate validation.
   *
   * @param snapshots the snapshots to include
   * @param validateDuplicates if false, allows duplicate metric names
   */
  private MetricSnapshots(Collection<MetricSnapshot> snapshots, boolean validateDuplicates) {
    List<MetricSnapshot> list = new ArrayList<>(snapshots);
    list.sort(comparing(s -> s.getMetadata().getPrometheusName()));
    if (validateDuplicates) {
      for (int i = 0; i < snapshots.size() - 1; i++) {
        if (list.get(i)
            .getMetadata()
            .getPrometheusName()
            .equals(list.get(i + 1).getMetadata().getPrometheusName())) {
          throw new IllegalArgumentException(
              list.get(i).getMetadata().getPrometheusName() + ": duplicate metric name");
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
    private boolean allowDuplicates = false;

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

    /**
     * Allow duplicate metric names in the snapshots collection.
     *
     * <p>By default, duplicate metric names are not allowed. Call this method to allow multiple
     * snapshots with the same metric name, which is useful when different collectors produce
     * metrics with the same name but different label sets.
     */
    public Builder allowDuplicates(boolean allowDuplicates) {
      this.allowDuplicates = allowDuplicates;
      return this;
    }

    public MetricSnapshots build() {
      return new MetricSnapshots(snapshots, !allowDuplicates);
    }
  }
}
