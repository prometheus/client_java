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
   * @throws IllegalArgumentException if there are duplicate metric names with different types, or
   *     duplicate label sets within the same metric name
   */
  public MetricSnapshots(Collection<MetricSnapshot> snapshots) {
    validateSnapshots(snapshots);
    List<MetricSnapshot> list = new ArrayList<>(snapshots);
    list.sort(comparing(s -> s.getMetadata().getPrometheusName()));
    this.snapshots = unmodifiableList(list);
  }

  /**
   * Validates that there are no duplicate time series (same metric name + same label set), and that
   * all metrics with the same name have the same type.
   */
  private static void validateSnapshots(Collection<MetricSnapshot> snapshots) {
    // Group snapshots by Prometheus name
    Map<String, List<MetricSnapshot>> groupedByName = new HashMap<>();
    for (MetricSnapshot snapshot : snapshots) {
      String prometheusName = snapshot.getMetadata().getPrometheusName();
      groupedByName.computeIfAbsent(prometheusName, k -> new ArrayList<>()).add(snapshot);
    }

    // For each group with multiple snapshots, validate type consistency and check for duplicate
    // labels
    for (Map.Entry<String, List<MetricSnapshot>> entry : groupedByName.entrySet()) {
      if (entry.getValue().size() > 1) {
        String prometheusName = entry.getKey();
        List<MetricSnapshot> snapshotsWithSameName = entry.getValue();

        // Check that all snapshots with the same name have the same type
        Class<?> firstType = snapshotsWithSameName.get(0).getClass();
        for (int i = 1; i < snapshotsWithSameName.size(); i++) {
          MetricSnapshot snapshot = snapshotsWithSameName.get(i);
          if (!firstType.equals(snapshot.getClass())) {
            throw new IllegalArgumentException(
                "Conflicting metric types for Prometheus name '"
                    + prometheusName
                    + "': "
                    + firstType.getSimpleName()
                    + " vs "
                    + snapshot.getClass().getSimpleName()
                    + ". All metrics with the same Prometheus name must have the same type.");
          }
        }

        // Check for duplicate label sets
        Set<Labels> seenLabels = new HashSet<>();
        for (MetricSnapshot snapshot : snapshotsWithSameName) {
          for (DataPointSnapshot dataPoint : snapshot.getDataPoints()) {
            Labels labels = dataPoint.getLabels();
            if (!seenLabels.add(labels)) {
              throw new IllegalArgumentException(
                  "Duplicate labels detected for metric '"
                      + prometheusName
                      + "': "
                      + labels
                      + ". Each time series (metric name + label set) must be unique.");
            }
          }
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
