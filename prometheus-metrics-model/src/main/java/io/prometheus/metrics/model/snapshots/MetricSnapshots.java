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

/**
 * Immutable list of metric snapshots.
 *
 * <p>Snapshots are automatically sorted by Prometheus name. The constructor validates:
 *
 * <ul>
 *   <li>Metrics with the same Prometheus name must have the same type
 *   <li>Each time series (metric name + label set) must be unique
 * </ul>
 *
 * throws IllegalArgumentException if validation fails
 */
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
    List<MetricSnapshot> list = new ArrayList<>(snapshots);
    if (snapshots.size() <= 1) {
      this.snapshots = unmodifiableList(list);
      return;
    }

    validateSnapshots(snapshots);
    list.sort(comparing(s -> s.getMetadata().getPrometheusName()));
    this.snapshots = unmodifiableList(list);
  }

  /** Validates type consistency and duplicate labels. */
  private static void validateSnapshots(Collection<MetricSnapshot> snapshots) {
    Map<String, ValidationGroup> groupsByName = new HashMap<>();

    for (MetricSnapshot snapshot : snapshots) {
      String prometheusName = snapshot.getMetadata().getPrometheusName();
      ValidationGroup group =
          groupsByName.computeIfAbsent(
              prometheusName, k -> new ValidationGroup(snapshot.getClass()));

      if (!group.type.equals(snapshot.getClass())) {
        throw new IllegalArgumentException(
            "Conflicting metric types for Prometheus name '"
                + prometheusName
                + "': "
                + group.type.getSimpleName()
                + " vs "
                + snapshot.getClass().getSimpleName()
                + ". All metrics with the same Prometheus name must have the same type.");
      }

      group.snapshots.add(snapshot);
    }

    for (Map.Entry<String, ValidationGroup> entry : groupsByName.entrySet()) {
      ValidationGroup group = entry.getValue();
      if (group.snapshots.size() > 1) {
        validateNoDuplicateLabelsInGroup(entry.getKey(), group.snapshots);
      }
    }
  }

  /** Helper class to track snapshots and their type during validation. */
  private static class ValidationGroup {
    final Class<? extends MetricSnapshot> type;
    final List<MetricSnapshot> snapshots = new ArrayList<>();

    ValidationGroup(Class<? extends MetricSnapshot> type) {
      this.type = type;
    }
  }

  /**
   * Validates that a group of snapshots with the same Prometheus name don't have duplicate label
   * sets.
   */
  private static void validateNoDuplicateLabelsInGroup(
      String prometheusName, List<MetricSnapshot> snapshots) {
    Set<Labels> seenLabels = new HashSet<>();

    for (MetricSnapshot snapshot : snapshots) {
      for (DataPointSnapshot dataPoint : snapshot.getDataPoints()) {
        Labels labels = dataPoint.getLabels();
        if (!seenLabels.add(labels)) {
          throw new IllegalArgumentException(
              "Duplicate labels detected for metric '"
                  + prometheusName
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
