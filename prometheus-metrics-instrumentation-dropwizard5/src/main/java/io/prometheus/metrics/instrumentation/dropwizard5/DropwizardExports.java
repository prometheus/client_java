package io.prometheus.metrics.instrumentation.dropwizard5;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Snapshot;
import io.dropwizard.metrics5.Timer;
import io.prometheus.metrics.instrumentation.dropwizard5.labels.CustomLabelMapper;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Collect Dropwizard metrics from a MetricRegistry. */
public class DropwizardExports implements MultiCollector {
  private static final Logger logger = Logger.getLogger(DropwizardExports.class.getName());
  private final MetricRegistry registry;
  private final MetricFilter metricFilter;
  private final Optional<CustomLabelMapper> labelMapper;
  private final InvalidMetricHandler invalidMetricHandler;

  /**
   * Creates a new DropwizardExports and {@link MetricFilter#ALL}.
   *
   * @param registry a metric registry to export in prometheus.
   */
  public DropwizardExports(MetricRegistry registry) {
    super();
    this.registry = registry;
    this.metricFilter = MetricFilter.ALL;
    this.labelMapper = Optional.empty();
    this.invalidMetricHandler = InvalidMetricHandler.ALWAYS_THROW;
  }

  /**
   * Creates a new DropwizardExports with a custom {@link MetricFilter}.
   *
   * @param registry a metric registry to export in prometheus.
   * @param metricFilter a custom metric filter.
   */
  public DropwizardExports(MetricRegistry registry, MetricFilter metricFilter) {
    this.registry = registry;
    this.metricFilter = metricFilter;
    this.labelMapper = Optional.empty();
    this.invalidMetricHandler = InvalidMetricHandler.ALWAYS_THROW;
  }

  /**
   * @param registry a metric registry to export in prometheus.
   * @param metricFilter a custom metric filter.
   * @param labelMapper a labelMapper to use to map labels.
   */
  public DropwizardExports(
      MetricRegistry registry, MetricFilter metricFilter, CustomLabelMapper labelMapper) {
    this.registry = registry;
    this.metricFilter = metricFilter;
    this.labelMapper = Optional.ofNullable(labelMapper);
    this.invalidMetricHandler = InvalidMetricHandler.ALWAYS_THROW;
  }

  /**
   * @param registry a metric registry to export in prometheus.
   * @param metricFilter a custom metric filter.
   * @param labelMapper a labelMapper to use to map labels.
   */
  private DropwizardExports(
      MetricRegistry registry,
      MetricFilter metricFilter,
      CustomLabelMapper labelMapper,
      InvalidMetricHandler invalidMetricHandler) {
    this.registry = registry;
    this.metricFilter = metricFilter;
    this.labelMapper = Optional.ofNullable(labelMapper);
    this.invalidMetricHandler = invalidMetricHandler;
  }

  private static String getHelpMessage(String metricName, Metric metric) {
    return String.format(
        "Generated from Dropwizard metric import (metric=%s, type=%s)",
        metricName, metric.getClass().getName());
  }

  private MetricMetadata getMetricMetaData(String metricName, Metric metric) {
    String name = labelMapper.isPresent() ? labelMapper.get().getName(metricName) : metricName;
    return new MetricMetadata(
        PrometheusNaming.sanitizeMetricName(name), getHelpMessage(metricName, metric));
  }

  /**
   * Export counter as Prometheus <a
   * href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
   */
  MetricSnapshot fromCounter(String dropwizardName, Counter counter) {
    MetricMetadata metadata = getMetricMetaData(dropwizardName, counter);
    CounterSnapshot.CounterDataPointSnapshot.Builder dataPointBuilder =
        CounterSnapshot.CounterDataPointSnapshot.builder()
            .value(Long.valueOf(counter.getCount()).doubleValue());
    labelMapper.ifPresent(
        mapper ->
            dataPointBuilder.labels(
                mapper.getLabels(
                    dropwizardName, Collections.emptyList(), Collections.emptyList())));
    return new CounterSnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  /** Export gauge as a prometheus gauge. */
  MetricSnapshot fromGauge(String dropwizardName, Gauge<?> gauge) {
    Object obj = gauge.getValue();
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
    MetricMetadata metadata = getMetricMetaData(dropwizardName, gauge);
    GaugeSnapshot.GaugeDataPointSnapshot.Builder dataPointBuilder =
        GaugeSnapshot.GaugeDataPointSnapshot.builder().value(value);
    labelMapper.ifPresent(
        mapper ->
            dataPointBuilder.labels(
                mapper.getLabels(
                    dropwizardName, Collections.emptyList(), Collections.emptyList())));
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
  MetricSnapshot fromSnapshotAndCount(
      String dropwizardName, Snapshot snapshot, long count, double factor, String helpMessage) {
    Quantiles quantiles =
        Quantiles.builder()
            .quantile(0.5, snapshot.getMedian() * factor)
            .quantile(0.75, snapshot.get75thPercentile() * factor)
            .quantile(0.95, snapshot.get95thPercentile() * factor)
            .quantile(0.98, snapshot.get98thPercentile() * factor)
            .quantile(0.99, snapshot.get99thPercentile() * factor)
            .quantile(0.999, snapshot.get999thPercentile() * factor)
            .build();

    String name =
        labelMapper.isPresent() ? labelMapper.get().getName(dropwizardName) : dropwizardName;
    MetricMetadata metadata =
        new MetricMetadata(PrometheusNaming.sanitizeMetricName(name), helpMessage);
    SummarySnapshot.SummaryDataPointSnapshot.Builder dataPointBuilder =
        SummarySnapshot.SummaryDataPointSnapshot.builder().quantiles(quantiles).count(count);
    labelMapper.ifPresent(
        mapper ->
            dataPointBuilder.labels(
                mapper.getLabels(
                    dropwizardName, Collections.emptyList(), Collections.emptyList())));
    return new SummarySnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  /** Convert histogram snapshot. */
  MetricSnapshot fromHistogram(String dropwizardName, Histogram histogram) {
    return fromSnapshotAndCount(
        dropwizardName,
        histogram.getSnapshot(),
        histogram.getCount(),
        1.0,
        getHelpMessage(dropwizardName, histogram));
  }

  /** Export Dropwizard Timer as a histogram. Use TIME_UNIT as time unit. */
  MetricSnapshot fromTimer(String dropwizardName, Timer timer) {
    return fromSnapshotAndCount(
        dropwizardName,
        timer.getSnapshot(),
        timer.getCount(),
        1.0D / TimeUnit.SECONDS.toNanos(1L),
        getHelpMessage(dropwizardName, timer));
  }

  /** Export a Meter as a prometheus COUNTER. */
  MetricSnapshot fromMeter(String dropwizardName, Meter meter) {
    MetricMetadata metadata = getMetricMetaData(dropwizardName + "_total", meter);
    CounterSnapshot.CounterDataPointSnapshot.Builder dataPointBuilder =
        CounterSnapshot.CounterDataPointSnapshot.builder().value(meter.getCount());
    labelMapper.ifPresent(
        mapper ->
            dataPointBuilder.labels(
                mapper.getLabels(
                    dropwizardName, Collections.emptyList(), Collections.emptyList())));
    return new CounterSnapshot(metadata, Collections.singletonList(dataPointBuilder.build()));
  }

  @Override
  public MetricSnapshots collect() {
    MetricSnapshots.Builder metricSnapshots = MetricSnapshots.builder();
    collectMetricKind(metricSnapshots, registry.getGauges(metricFilter), this::fromGauge);
    collectMetricKind(metricSnapshots, registry.getCounters(metricFilter), this::fromCounter);
    collectMetricKind(metricSnapshots, registry.getHistograms(metricFilter), this::fromHistogram);
    collectMetricKind(metricSnapshots, registry.getTimers(metricFilter), this::fromTimer);
    collectMetricKind(metricSnapshots, registry.getMeters(metricFilter), this::fromMeter);
    return metricSnapshots.build();
  }

  private <T> void collectMetricKind(
      MetricSnapshots.Builder builder,
      Map<MetricName, T> metric,
      BiFunction<String, T, MetricSnapshot> toSnapshot) {
    for (Map.Entry<MetricName, T> entry : metric.entrySet()) {
      String metricName = entry.getKey().getKey();
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

  public static Builder builder() {
    return new Builder();
  }

  // Builder class for DropwizardExports
  public static class Builder {
    private MetricRegistry registry;
    private MetricFilter metricFilter;
    private CustomLabelMapper labelMapper;
    private InvalidMetricHandler invalidMetricHandler;

    private Builder() {
      this.metricFilter = MetricFilter.ALL;
      this.invalidMetricHandler = InvalidMetricHandler.ALWAYS_THROW;
    }

    public Builder dropwizardRegistry(MetricRegistry registry) {
      this.registry = registry;
      return this;
    }

    public Builder metricFilter(MetricFilter metricFilter) {
      this.metricFilter = metricFilter;
      return this;
    }

    public Builder customLabelMapper(CustomLabelMapper labelMapper) {
      this.labelMapper = labelMapper;
      return this;
    }

    public Builder invalidMetricHandler(InvalidMetricHandler invalidMetricHandler) {
      this.invalidMetricHandler = invalidMetricHandler;
      return this;
    }

    DropwizardExports build() {
      if (registry == null) {
        throw new IllegalArgumentException("MetricRegistry must be set");
      }
      return new DropwizardExports(registry, metricFilter, labelMapper, invalidMetricHandler);
    }

    public void register() {
      register(PrometheusRegistry.defaultRegistry);
    }

    public void register(PrometheusRegistry registry) {
      DropwizardExports dropwizardExports = build();
      registry.register(dropwizardExports);
    }
  }
}
