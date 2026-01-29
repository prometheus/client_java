package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * To be registered with the Prometheus collector registry. See <i>Overall Structure</i> on <a
 * href="https://prometheus.io/docs/instrumenting/writing_clientlibs/">https://prometheus.io/docs/instrumenting/writing_clientlibs/</a>.
 */
@FunctionalInterface
public interface Collector {

  /** Called when the Prometheus server scrapes metrics. */
  MetricSnapshot collect();

  /**
   * Provides Collector with the details of the request issued by Prometheus to allow multi-target
   * pattern implementation Override to implement request dependent logic to provide MetricSnapshot
   */
  default MetricSnapshot collect(PrometheusScrapeRequest scrapeRequest) {
    return collect();
  }

  /**
   * Like {@link #collect()}, but returns {@code null} if {@code includedNames.test(name)} is {@code
   * false}.
   *
   * <p>Override this if there is a more efficient way than first collecting the snapshot and then
   * discarding it.
   */
  @Nullable
  default MetricSnapshot collect(Predicate<String> includedNames) {
    MetricSnapshot result = collect();
    if (includedNames.test(result.getMetadata().getPrometheusName())) {
      return result;
    } else {
      return null;
    }
  }

  /**
   * Like {@link #collect(Predicate)}, but with support for multi-target pattern.
   *
   * <p>Override this if there is a more efficient way than first collecting the snapshot and then
   * discarding it.
   */
  @Nullable
  default MetricSnapshot collect(
      Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
    MetricSnapshot result = collect(scrapeRequest);
    if (includedNames.test(result.getMetadata().getPrometheusName())) {
      return result;
    } else {
      return null;
    }
  }

  /**
   * This is called in two places:
   *
   * <ol>
   *   <li>During registration to check if a metric with that name already exists.
   *   <li>During scrape to check if this collector can be skipped because a name filter is present
   *       and the metric name is excluded.
   * </ol>
   *
   * Returning {@code null} means checks are omitted (registration the metric always succeeds), and
   * the collector is always scraped (the result is dropped after scraping if a name filter is
   * present and the metric name is excluded).
   *
   * <p>If your metric has a name that does not change at runtime it is a good idea to overwrite
   * this and return the name.
   *
   * <p>All metrics in {@code prometheus-metrics-core} override this.
   */
  @Nullable
  default String getPrometheusName() {
    return null;
  }

  /**
   * Returns the metric type for registration-time validation.
   *
   * <p>This is used to prevent different metric types (e.g., Counter and Gauge) from sharing the
   * same name. Returning {@code null} means type validation is skipped for this collector.
   *
   * <p>Validation is performed only at registration time. If this method returns {@code null}, no
   * type validation is performed for this collector, and duplicate or conflicting metrics may
   * result in invalid exposition output.
   *
   * @return the metric type, or {@code null} to skip validation
   */
  @Nullable
  default MetricType getMetricType() {
    return null;
  }

  /**
   * Returns the complete set of label names for this metric.
   *
   * <p>This includes both dynamic label names (specified in {@code labelNames()}) and constant
   * label names (specified in {@code constLabels()}). Label names are normalized using Prometheus
   * naming conventions.
   *
   * <p>This is used for registration-time validation to prevent duplicate label schemas for the
   * same metric name. Two collectors with the same name and type can coexist if they have different
   * label name sets.
   *
   * <p>Returning {@code null} means label schema validation is skipped for this collector.
   *
   * <p>Validation is performed only at registration time. If this method returns {@code null}, no
   * label-schema validation is performed for this collector. If such a collector produces the same
   * metric name and label schema as another at scrape time, the exposition may contain duplicate
   * time series, which is invalid in Prometheus.
   *
   * @return the set of all label names, or {@code null} to skip validation
   */
  @Nullable
  default Set<String> getLabelNames() {
    return null;
  }
}
