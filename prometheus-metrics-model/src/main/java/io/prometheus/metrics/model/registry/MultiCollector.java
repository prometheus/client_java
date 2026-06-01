package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricFamilyDescriptor;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** Like {@link Collector}, but collecting multiple Snapshots at once. */
@FunctionalInterface
public interface MultiCollector {

  /** Called when the Prometheus server scrapes metrics. */
  MetricSnapshots collect();

  /**
   * Provides Collector with the details of the request issued by Prometheus to allow multi-target
   * pattern implementation Override to implement request dependent logic to provide MetricSnapshot
   */
  default MetricSnapshots collect(PrometheusScrapeRequest scrapeRequest) {
    return collect();
  }

  /**
   * Like {@link #collect()}, but returns only the snapshots where {@code includedNames.test(name)}
   * is {@code true}.
   *
   * <p>Override this if there is a more efficient way than first collecting all snapshot and then
   * discarding the excluded ones.
   */
  default MetricSnapshots collect(Predicate<String> includedNames) {
    return collect(includedNames, null);
  }

  /**
   * Like {@link #collect(Predicate)}, but with support for multi-target pattern.
   *
   * <p>Override this if there is a more efficient way than first collecting the snapshot and then
   * discarding it.
   */
  default MetricSnapshots collect(
      Predicate<String> includedNames, @Nullable PrometheusScrapeRequest scrapeRequest) {
    MetricSnapshots allSnapshots = scrapeRequest == null ? collect() : collect(scrapeRequest);
    MetricSnapshots.Builder result = MetricSnapshots.builder();
    for (MetricSnapshot snapshot : allSnapshots) {
      if (includedNames.test(snapshot.getMetadata().getPrometheusName())) {
        result.metricSnapshot(snapshot);
      }
    }
    return result.build();
  }

  /**
   * Returns registration-time descriptors for the metric families collected by this collector.
   *
   * <p>The registry uses these descriptors for duplicate-name, type, label-schema, help, and unit
   * validation at registration time. Returning an empty list means registration-time validation is
   * skipped for this collector.
   *
   * <p>The default implementation adapts the deprecated fragmented metadata methods. New collectors
   * with fixed registration-time metadata should override this method directly.
   */
  @SuppressWarnings("deprecation")
  default List<MetricFamilyDescriptor> getMetricFamilyDescriptors() {
    List<String> prometheusNames = getPrometheusNames();
    List<MetricFamilyDescriptor> descriptors = new ArrayList<>(prometheusNames.size());
    for (String prometheusName : prometheusNames) {
      MetricType metricType = getMetricType(prometheusName);
      if (metricType != null) {
        MetricMetadata metadata = getMetadata(prometheusName);
        if (metadata == null) {
          metadata = new MetricMetadata(prometheusName);
        }
        Set<String> labelNames = getLabelNames(prometheusName);
        descriptors.add(
            MetricFamilyDescriptor.of(
                metricType, metadata, labelNames == null ? Collections.emptySet() : labelNames));
      }
    }
    return Collections.unmodifiableList(descriptors);
  }

  /**
   * This is called in two places:
   *
   * <ol>
   *   <li>During registration to check if a metric with that name already exists.
   *   <li>During scrape to check if the collector can be skipped because a name filter is present
   *       and all names are excluded.
   * </ol>
   *
   * <p>Returning an empty list means checks are omitted (registration metric always succeeds), and
   * the collector is always scraped (if a name filter is present and all names are excluded the
   * result is dropped).
   *
   * <p>If your collector returns a constant list of metrics that have names that do not change at
   * runtime it is a good idea to overwrite this and return the names.
   *
   * @deprecated Override {@link #getMetricFamilyDescriptors()} instead.
   */
  @Deprecated
  default List<String> getPrometheusNames() {
    return Collections.emptyList();
  }

  /**
   * Returns the metric type for the given Prometheus name.
   *
   * <p>This is used for per-name type validation during registration. Returning {@code null} means
   * type validation is skipped for that specific metric name.
   *
   * <p>Validation is performed only at registration time. If this method returns {@code null}, no
   * type validation is performed for that name, and duplicate or conflicting metrics may result in
   * invalid exposition output.
   *
   * @param prometheusName the Prometheus metric name
   * @return the metric type for the given name, or {@code null} to skip validation
   * @deprecated Override {@link #getMetricFamilyDescriptors()} instead.
   */
  @Deprecated
  @Nullable
  default MetricType getMetricType(String prometheusName) {
    return null;
  }

  /**
   * Returns the complete set of label names for the given Prometheus name.
   *
   * <p>This includes both dynamic label names and constant label names. Label names are normalized
   * using Prometheus naming conventions (dots converted to underscores).
   *
   * <p>This is used for per-name label schema validation during registration. Two collectors with
   * the same name and type can coexist if they have different label name sets.
   *
   * <p>Returning {@code null} is treated as an empty label set: the registry normalizes it to
   * {@code Collections.emptySet()} and performs full label-schema validation and duplicate
   * detection. Two collectors with the same name, type, and {@code null} (or empty) label names are
   * considered duplicate and registration of the second will fail.
   *
   * @param prometheusName the Prometheus metric name
   * @return the set of all label names for the given name, or {@code null} (treated as empty) for a
   *     metric with no labels
   * @deprecated Override {@link #getMetricFamilyDescriptors()} instead.
   */
  @Deprecated
  @Nullable
  default Set<String> getLabelNames(String prometheusName) {
    return null;
  }

  /**
   * Returns the metric metadata (name, help, unit) for the given Prometheus name.
   *
   * <p>When non-null, the registry uses this to validate that metrics with the same name have
   * consistent help and unit. Returning {@code null} means help/unit validation is skipped for that
   * name.
   *
   * @param prometheusName the Prometheus metric name
   * @return the metric metadata for that name, or {@code null} to skip help/unit validation
   * @deprecated Override {@link #getMetricFamilyDescriptors()} instead.
   */
  @Deprecated
  @Nullable
  default MetricMetadata getMetadata(String prometheusName) {
    return null;
  }
}
