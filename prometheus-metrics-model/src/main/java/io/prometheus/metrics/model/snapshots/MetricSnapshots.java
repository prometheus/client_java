package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
   * @throws IllegalArgumentException if snapshots with the same Prometheus name have conflicting
   *     types or have duplicate label sets
   */
  public MetricSnapshots(Collection<MetricSnapshot> snapshots) {
    validateTypeConsistency(snapshots);
    validateNoDuplicateLabelsAcrossSnapshots(snapshots);
    List<MetricSnapshot> list = new ArrayList<>(snapshots);
    list.sort(comparing(s -> s.getMetadata().getPrometheusName()));
    this.snapshots = unmodifiableList(list);
  }

  /** Validates that all snapshots with the same Prometheus name have the same type. */
  private static void validateTypeConsistency(Collection<MetricSnapshot> snapshots) {
    Map<String, Class<? extends MetricSnapshot>> typesByName = new HashMap<>();
    for (MetricSnapshot snapshot : snapshots) {
      String prometheusName = snapshot.getMetadata().getPrometheusName();
      Class<? extends MetricSnapshot> existingType = typesByName.get(prometheusName);
      if (existingType != null && !existingType.equals(snapshot.getClass())) {
        throw new IllegalArgumentException(
            "Conflicting metric types for Prometheus name '"
                + prometheusName
                + "': "
                + existingType.getSimpleName()
                + " vs "
                + snapshot.getClass().getSimpleName()
                + ". All metrics with the same Prometheus name must have the same type.");
      }
      typesByName.put(prometheusName, snapshot.getClass());
    }
  }

  /**
   * Validates that snapshots with the same Prometheus name don't have overlapping label sets.
   *
   * <p>This validation ensures that when multiple collectors with the same metric name are
   * registered, each time series (metric name + label set) is unique. Validation happens at scrape
   * time rather than registration time for efficiency.
   */
  private static void validateNoDuplicateLabelsAcrossSnapshots(
      Collection<MetricSnapshot> snapshots) {
    // Group snapshots by Prometheus name
    Map<String, List<MetricSnapshot>> snapshotsByName = new HashMap<>();
    for (MetricSnapshot snapshot : snapshots) {
      String prometheusName = snapshot.getMetadata().getPrometheusName();
      snapshotsByName.computeIfAbsent(prometheusName, k -> new ArrayList<>()).add(snapshot);
    }

    // For each group with multiple snapshots, check for duplicate labels
    for (Map.Entry<String, List<MetricSnapshot>> entry : snapshotsByName.entrySet()) {
      List<MetricSnapshot> group = entry.getValue();
      if (group.size() > 1) {
        validateNoDuplicateLabelsInGroup(group);
      }
    }
  }

  /**
   * Validates that a group of snapshots with the same Prometheus name don't have duplicate label
   * sets.
   */
  private static void validateNoDuplicateLabelsInGroup(List<MetricSnapshot> snapshots) {
    String metricName = snapshots.get(0).getMetadata().getName();
    Set<Labels> seenLabels = new HashSet<>();

    for (MetricSnapshot snapshot : snapshots) {
      for (DataPointSnapshot dataPoint : snapshot.getDataPoints()) {
        Labels labels = dataPoint.getLabels();
        if (!seenLabels.add(labels)) {
          throw new IllegalArgumentException(
              "Duplicate labels detected for metric '"
                  + metricName
                  + "' with labels "
                  + labels
                  + ". Each time series (metric name + label set) must be unique.");
        }
      }
    }
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
