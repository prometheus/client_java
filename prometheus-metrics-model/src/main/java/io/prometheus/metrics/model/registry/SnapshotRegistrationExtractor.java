package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extracts registration-relevant data (metric type, label names) from a {@link MetricSnapshot}.
 * Used when a collector does not implement {@link Collector#getMetricType()} / {@link
 * Collector#getLabelNames()} so the registry can validate from a one-time {@link
 * Collector#collect()} at registration.
 *
 * <p>Collectors whose {@code collect()} has side effects (e.g. callbacks) should implement the
 * getters so the registry does not call {@code collect()} at registration.
 */
final class SnapshotRegistrationExtractor {

  private SnapshotRegistrationExtractor() {}

  static MetricType metricTypeFromSnapshot(MetricSnapshot snapshot) {
    if (snapshot instanceof CounterSnapshot) {
      return MetricType.COUNTER;
    }
    if (snapshot instanceof GaugeSnapshot) {
      return MetricType.GAUGE;
    }
    if (snapshot instanceof HistogramSnapshot) {
      return MetricType.HISTOGRAM;
    }
    if (snapshot instanceof SummarySnapshot) {
      return MetricType.SUMMARY;
    }
    if (snapshot instanceof InfoSnapshot) {
      return MetricType.INFO;
    }
    if (snapshot instanceof StateSetSnapshot) {
      return MetricType.STATESET;
    }
    if (snapshot instanceof UnknownSnapshot) {
      return MetricType.UNKNOWN;
    }
    return MetricType.UNKNOWN;
  }

  /**
   * Returns the set of label names (Prometheus-normalized) from the snapshot's first data point, or
   * empty set if there are no data points.
   */
  static Set<String> labelNamesFromSnapshot(MetricSnapshot snapshot) {
    if (snapshot.getDataPoints().isEmpty()) {
      return Collections.emptySet();
    }
    Labels labels = snapshot.getDataPoints().get(0).getLabels();
    if (labels.isEmpty()) {
      return Collections.emptySet();
    }
    Set<String> names = new HashSet<>();
    for (int i = 0; i < labels.size(); i++) {
      names.add(labels.getPrometheusName(i));
    }
    return names;
  }

  /** Returns metadata from the snapshot for help/unit validation (snapshot always has metadata). */
  static MetricMetadata metadataFromSnapshot(MetricSnapshot snapshot) {
    return snapshot.getMetadata();
  }
}
