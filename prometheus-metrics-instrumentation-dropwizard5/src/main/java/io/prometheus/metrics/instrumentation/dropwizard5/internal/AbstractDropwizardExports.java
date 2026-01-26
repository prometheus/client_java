package io.prometheus.metrics.instrumentation.dropwizard5.internal;

import io.prometheus.metrics.instrumentation.dropwizard5.InvalidMetricHandler;
import io.prometheus.metrics.instrumentation.dropwizard5.labels.CustomLabelMapper;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Abstract base class for Dropwizard metrics exporters. Contains all the common logic for
 * converting Dropwizard metrics to Prometheus metrics. Subclasses only need to implement {@link
 * #collectMetricSnapshots()} to handle version-specific registry APIs.
 *
 * @param <R> The Dropwizard MetricRegistry type
 * @param <F> The Dropwizard MetricFilter type
 * @param <C> The Dropwizard Counter type
 * @param <G> The Dropwizard Gauge type
 * @param <H> The Dropwizard Histogram type
 * @param <T> The Dropwizard Timer type
 * @param <M> The Dropwizard Meter type
 * @param <B> The Dropwizard Metric base type
 * @param <S> The Dropwizard Snapshot type
 */
public abstract class AbstractDropwizardExports<R, F, C, G, H, T, M, B, S>
    implements MultiCollector {

  private static final Logger logger = Logger.getLogger(AbstractDropwizardExports.class.getName());

  protected final R registry;
  protected final F metricFilter;
  @Nullable protected final CustomLabelMapper labelMapper;
  protected final InvalidMetricHandler invalidMetricHandler;

  protected AbstractDropwizardExports(
      R registry,
      F metricFilter,
      @Nullable CustomLabelMapper labelMapper,
      InvalidMetricHandler invalidMetricHandler) {
    this.registry = registry;
    this.metricFilter = metricFilter;
    this.labelMapper = labelMapper;
    this.invalidMetricHandler = invalidMetricHandler;
  }

  protected static String getHelpMessage(String metricName, Object metric) {
    return String.format(
        "Generated from Dropwizard metric import (metric=%s, type=%s)",
        metricName, metric.getClass().getName());
  }

  protected MetricMetadata getMetricMetaData(String metricName, B metric) {
    String name = labelMapper != null ? labelMapper.getName(metricName) : metricName;
    return new MetricMetadata(
        PrometheusNaming.sanitizeMetricName(name), getHelpMessage(metricName, metric));
  }

  /**
   * Export counter as Prometheus <a
   * href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
   */
  @SuppressWarnings("unchecked")
  protected MetricSnapshot fromCounter(String dropwizardName, C counter) {
    long count = getCounterCount(counter);
    MetricMetadata metadata = getMetricMetaData(dropwizardName, (B) counter);
    CounterSnapshot.CounterDataPointSnapshot.Builder dataPointBuilder =
        CounterSnapshot.CounterDataPointSnapshot.builder().value(Long.valueOf(count).doubleValue());
    if (labelMapper != null) {
      dataPointBuilder.labels(
          labelMapper.getLabels(dropwizardName, Collections.emptyList(), Collections.emptyList()));
    }
    return new CounterSnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  /** Export gauge as a prometheus gauge. */
  @SuppressWarnings("unchecked")
  @Nullable
  protected MetricSnapshot fromGauge(String dropwizardName, G gauge) {
    Object obj = getGaugeValue(gauge);
    double value;
    if (obj instanceof Number) {
      value = ((Number) obj).doubleValue();
    } else if (obj instanceof Boolean) {
      value = ((Boolean) obj) ? 1 : 0;
    } else {
      logger.log(
          Level.FINE,
          String.format(
              "Invalid type for Gauge %s: %s",
              PrometheusNaming.sanitizeMetricName(dropwizardName),
              obj == null ? "null" : obj.getClass().getName()));
      return null;
    }
    MetricMetadata metadata = getMetricMetaData(dropwizardName, (B) gauge);
    GaugeSnapshot.GaugeDataPointSnapshot.Builder dataPointBuilder =
        GaugeSnapshot.GaugeDataPointSnapshot.builder().value(value);
    if (labelMapper != null) {
      dataPointBuilder.labels(
          labelMapper.getLabels(dropwizardName, Collections.emptyList(), Collections.emptyList()));
    }
    return new GaugeSnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  /**
   * Export a histogram snapshot as a prometheus SUMMARY.
   *
   * @param dropwizardName metric name.
   * @param snapshot the histogram snapshot.
   * @param count the total sample count for this snapshot.
   * @param factor a factor to apply to histogram values.
   */
  protected MetricSnapshot fromSnapshotAndCount(
      String dropwizardName, S snapshot, long count, double factor, String helpMessage) {
    Quantiles quantiles =
        Quantiles.builder()
            .quantile(0.5, getMedian(snapshot) * factor)
            .quantile(0.75, get75thPercentile(snapshot) * factor)
            .quantile(0.95, get95thPercentile(snapshot) * factor)
            .quantile(0.98, get98thPercentile(snapshot) * factor)
            .quantile(0.99, get99thPercentile(snapshot) * factor)
            .quantile(0.999, get999thPercentile(snapshot) * factor)
            .build();

    String name = labelMapper != null ? labelMapper.getName(dropwizardName) : dropwizardName;
    MetricMetadata metadata =
        new MetricMetadata(PrometheusNaming.sanitizeMetricName(name), helpMessage);
    SummarySnapshot.SummaryDataPointSnapshot.Builder dataPointBuilder =
        SummarySnapshot.SummaryDataPointSnapshot.builder().quantiles(quantiles).count(count);
    if (labelMapper != null) {
      dataPointBuilder.labels(
          labelMapper.getLabels(dropwizardName, Collections.emptyList(), Collections.emptyList()));
    }
    return new SummarySnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  /** Convert histogram snapshot. */
  protected MetricSnapshot fromHistogram(String dropwizardName, H histogram) {
    S snapshot = getHistogramSnapshot(histogram);
    long count = getHistogramCount(histogram);
    return fromSnapshotAndCount(
        dropwizardName, snapshot, count, 1.0, getHelpMessage(dropwizardName, histogram));
  }

  /** Export Dropwizard Timer as a histogram. Use TIME_UNIT as time unit. */
  protected MetricSnapshot fromTimer(String dropwizardName, T timer) {
    S snapshot = getTimerSnapshot(timer);
    long count = getTimerCount(timer);
    return fromSnapshotAndCount(
        dropwizardName,
        snapshot,
        count,
        1.0D / TimeUnit.SECONDS.toNanos(1L),
        getHelpMessage(dropwizardName, timer));
  }

  /** Export a Meter as a prometheus COUNTER. */
  @SuppressWarnings("unchecked")
  protected MetricSnapshot fromMeter(String dropwizardName, M meter) {
    MetricMetadata metadata = getMetricMetaData(dropwizardName + "_total", (B) meter);
    CounterSnapshot.CounterDataPointSnapshot.Builder dataPointBuilder =
        CounterSnapshot.CounterDataPointSnapshot.builder().value(getMeterCount(meter));
    if (labelMapper != null) {
      dataPointBuilder.labels(
          labelMapper.getLabels(dropwizardName, Collections.emptyList(), Collections.emptyList()));
    }
    return new CounterSnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  @Override
  public MetricSnapshots collect() {
    return collectMetricSnapshots();
  }

  protected <K, V> void collectMetricKind(
      MetricSnapshots.Builder builder,
      Map<K, V> metrics,
      BiFunction<String, V, MetricSnapshot> toSnapshot,
      java.util.function.Function<K, String> keyExtractor) {
    for (Map.Entry<K, V> entry : metrics.entrySet()) {
      String metricName = keyExtractor.apply(entry.getKey());
      try {
        MetricSnapshot snapshot = toSnapshot.apply(metricName, entry.getValue());
        if (snapshot != null) {
          builder.metricSnapshot(snapshot);
        }
      } catch (Exception e) {
        if (!invalidMetricHandler.suppressException(metricName, e)) {
          throw e;
        }
      }
    }
  }

  // Abstract methods to be implemented by version-specific subclasses

  /** Collect all metric snapshots from the registry. */
  protected abstract MetricSnapshots collectMetricSnapshots();

  protected abstract long getCounterCount(C counter);

  protected abstract Object getGaugeValue(G gauge);

  protected abstract S getHistogramSnapshot(H histogram);

  protected abstract long getHistogramCount(H histogram);

  protected abstract S getTimerSnapshot(T timer);

  protected abstract long getTimerCount(T timer);

  protected abstract long getMeterCount(M meter);

  protected abstract double getMedian(S snapshot);

  protected abstract double get75thPercentile(S snapshot);

  protected abstract double get95thPercentile(S snapshot);

  protected abstract double get98thPercentile(S snapshot);

  protected abstract double get99thPercentile(S snapshot);

  protected abstract double get999thPercentile(S snapshot);
}
