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
import io.prometheus.metrics.instrumentation.dropwizard5.internal.AbstractDropwizardExports;
import io.prometheus.metrics.instrumentation.dropwizard5.labels.CustomLabelMapper;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import javax.annotation.Nullable;

/**
 * Collect Dropwizard 5.x metrics from a MetricRegistry.
 *
 * <p>This is a thin wrapper around {@link AbstractDropwizardExports} that handles the Dropwizard
 * 5.x specific API where metric names are {@link MetricName} objects.
 */
public class DropwizardExports
    extends AbstractDropwizardExports<
        MetricRegistry,
        MetricFilter,
        Counter,
        Gauge<?>,
        Histogram,
        Timer,
        Meter,
        Metric,
        Snapshot> {

  /**
   * Creates a new DropwizardExports with {@link MetricFilter#ALL}.
   *
   * @param registry a metric registry to export in prometheus.
   */
  public DropwizardExports(MetricRegistry registry) {
    this(registry, MetricFilter.ALL, null, InvalidMetricHandler.ALWAYS_THROW);
  }

  /**
   * Creates a new DropwizardExports with a custom {@link MetricFilter}.
   *
   * @param registry a metric registry to export in prometheus.
   * @param metricFilter a custom metric filter.
   */
  public DropwizardExports(MetricRegistry registry, MetricFilter metricFilter) {
    this(registry, metricFilter, null, InvalidMetricHandler.ALWAYS_THROW);
  }

  /**
   * @param registry a metric registry to export in prometheus.
   * @param metricFilter a custom metric filter.
   * @param labelMapper a labelMapper to use to map labels.
   */
  public DropwizardExports(
      MetricRegistry registry, MetricFilter metricFilter, @Nullable CustomLabelMapper labelMapper) {
    this(registry, metricFilter, labelMapper, InvalidMetricHandler.ALWAYS_THROW);
  }

  /**
   * @param registry a metric registry to export in prometheus.
   * @param metricFilter a custom metric filter.
   * @param labelMapper a labelMapper to use to map labels.
   * @param invalidMetricHandler handler for invalid metrics.
   */
  private DropwizardExports(
      MetricRegistry registry,
      MetricFilter metricFilter,
      @Nullable CustomLabelMapper labelMapper,
      InvalidMetricHandler invalidMetricHandler) {
    super(registry, metricFilter, labelMapper, invalidMetricHandler);
  }

  @Override
  protected MetricSnapshots collectMetricSnapshots() {
    MetricSnapshots.Builder metricSnapshots = MetricSnapshots.builder();
    collectMetricKind(
        metricSnapshots,
        registry.getGauges(metricFilter),
        this::fromGauge,
        this::extractMetricName);
    collectMetricKind(
        metricSnapshots,
        registry.getCounters(metricFilter),
        this::fromCounter,
        this::extractMetricName);
    collectMetricKind(
        metricSnapshots,
        registry.getHistograms(metricFilter),
        this::fromHistogram,
        this::extractMetricName);
    collectMetricKind(
        metricSnapshots,
        registry.getTimers(metricFilter),
        this::fromTimer,
        this::extractMetricName);
    collectMetricKind(
        metricSnapshots,
        registry.getMeters(metricFilter),
        this::fromMeter,
        this::extractMetricName);
    return metricSnapshots.build();
  }

  private String extractMetricName(MetricName metricName) {
    return metricName.getKey();
  }

  @Override
  protected long getCounterCount(Counter counter) {
    return counter.getCount();
  }

  @Override
  protected Object getGaugeValue(Gauge<?> gauge) {
    return gauge.getValue();
  }

  @Override
  protected Snapshot getHistogramSnapshot(Histogram histogram) {
    return histogram.getSnapshot();
  }

  @Override
  protected long getHistogramCount(Histogram histogram) {
    return histogram.getCount();
  }

  @Override
  protected Snapshot getTimerSnapshot(Timer timer) {
    return timer.getSnapshot();
  }

  @Override
  protected long getTimerCount(Timer timer) {
    return timer.getCount();
  }

  @Override
  protected long getMeterCount(Meter meter) {
    return meter.getCount();
  }

  @Override
  protected double getMedian(Snapshot snapshot) {
    return snapshot.getMedian();
  }

  @Override
  protected double get75thPercentile(Snapshot snapshot) {
    return snapshot.get75thPercentile();
  }

  @Override
  protected double get95thPercentile(Snapshot snapshot) {
    return snapshot.get95thPercentile();
  }

  @Override
  protected double get98thPercentile(Snapshot snapshot) {
    return snapshot.get98thPercentile();
  }

  @Override
  protected double get99thPercentile(Snapshot snapshot) {
    return snapshot.get99thPercentile();
  }

  @Override
  protected double get999thPercentile(Snapshot snapshot) {
    return snapshot.get999thPercentile();
  }

  public static Builder builder() {
    return new Builder();
  }

  // Builder class for DropwizardExports
  public static class Builder {
    @Nullable private MetricRegistry registry;
    private MetricFilter metricFilter;
    @Nullable private CustomLabelMapper labelMapper;
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
