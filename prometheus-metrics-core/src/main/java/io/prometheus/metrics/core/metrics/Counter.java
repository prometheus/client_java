package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSampler;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Counter metric.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Counter requestCount = Counter.builder()
 *     .name("requests_total")
 *     .help("Total number of requests")
 *     .labelNames("path", "status")
 *     .register();
 * requestCount.labelValues("/hello-world", "200").inc();
 * requestCount.labelValues("/hello-world", "500").inc();
 * }</pre>
 */
public class Counter extends StatefulMetric<CounterDataPoint, Counter.DataPoint>
    implements CounterDataPoint {

  private final ExemplarSamplerConfig exemplarSamplerConfig;

  private Counter(Builder builder, PrometheusProperties prometheusProperties) {
    super(builder);
    MetricsProperties[] properties = getMetricProperties(builder, prometheusProperties);
    boolean exemplarsEnabled = getConfigProperty(properties, MetricsProperties::getExemplarsEnabled);
    // exemplars might be enabled specifically for a metric, however, if the code
    // says withoutExemplars they should stay disabled.
    boolean notTurnedOffWithinCode =
      builder == null || builder.exemplarsEnabled == null || builder.exemplarsEnabled;
    if (exemplarsEnabled && notTurnedOffWithinCode) {
      exemplarSamplerConfig =
          new ExemplarSamplerConfig(prometheusProperties.getExemplarProperties(), 1);
    } else {
      exemplarSamplerConfig = null;
    }
  }

  @Override
  public void inc(long amount) {
    getNoLabels().inc(amount);
  }

  @Override
  public void inc(double amount) {
    getNoLabels().inc(amount);
  }

  @Override
  public void incWithExemplar(long amount, Labels labels) {
    getNoLabels().incWithExemplar(amount, labels);
  }

  @Override
  public void incWithExemplar(double amount, Labels labels) {
    getNoLabels().incWithExemplar(amount, labels);
  }

  @Override
  public double get() {
    return getNoLabels().get();
  }

  @Override
  public long getLongValue() {
    return getNoLabels().getLongValue();
  }

  @Override
  public CounterSnapshot collect() {
    return (CounterSnapshot) super.collect();
  }

  @Override
  protected CounterSnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
    List<CounterSnapshot.CounterDataPointSnapshot> data = new ArrayList<>(labels.size());
    for (int i = 0; i < labels.size(); i++) {
      data.add(metricData.get(i).collect(labels.get(i)));
    }
    return new CounterSnapshot(getMetadata(), data);
  }

  @Override
  protected boolean isExemplarsEnabled() {
    return exemplarSamplerConfig != null;
  }

  @Override
  protected DataPoint newDataPoint() {
    if (isExemplarsEnabled()) {
      return new DataPoint(new ExemplarSampler(exemplarSamplerConfig));
    } else {
      return new DataPointIgnoringExemplars();
    }
  }

  static String stripTotalSuffix(String name) {
    if (name != null && (name.endsWith("_total") || name.endsWith(".total"))) {
      name = name.substring(0, name.length() - 6);
    }
    return name;
  }

  public static class DataPoint implements CounterDataPoint {

    private final DoubleAdder doubleValue = new DoubleAdder();
    // LongAdder is 20% faster than DoubleAdder. So let's use the LongAdder for long observations,
    // and DoubleAdder for double observations. If the user doesn't observe any double at all,
    // we will be using the LongAdder and get the best performance.
    protected final LongAdder longValue = new LongAdder();
    private final long createdTimeMillis = System.currentTimeMillis();
    private final ExemplarSampler exemplarSampler; // null if isExemplarsEnabled() is false

    private DataPoint(ExemplarSampler exemplarSampler) {
      this.exemplarSampler = exemplarSampler;
    }

    @Override
    public double get() {
      return longValue.sum() + doubleValue.sum();
    }

    @Override
    public long getLongValue() {
      return longValue.sum() + (long) doubleValue.sum();
    }

    @Override
    public void inc(long amount) {
      validateAndAdd(amount);
      if (isExemplarsEnabled()) {
        exemplarSampler.observe((double) amount);
      }
    }

    @Override
    public void inc(double amount) {
      validateAndAdd(amount);
      if (isExemplarsEnabled()) {
        exemplarSampler.observe(amount);
      }
    }

    @Override
    public void incWithExemplar(long amount, Labels labels) {
      validateAndAdd(amount);
      if (isExemplarsEnabled()) {
        exemplarSampler.observeWithExemplar((double) amount, labels);
      }
    }

    @Override
    public void incWithExemplar(double amount, Labels labels) {
      validateAndAdd(amount);
      if (isExemplarsEnabled()) {
        exemplarSampler.observeWithExemplar(amount, labels);
      }
    }

    protected boolean isExemplarsEnabled() {
      return exemplarSampler != null;
    }

    protected void validateAndAdd(long amount) {
      if (amount < 0) {
        throw new IllegalArgumentException(
            "Negative increment " + amount + " is illegal for Counter metrics.");
      }
      longValue.add(amount);
    }

    private void validateAndAdd(double amount) {
      if (amount < 0) {
        throw new IllegalArgumentException(
            "Negative increment " + amount + " is illegal for Counter metrics.");
      }
      doubleValue.add(amount);
    }

    protected CounterSnapshot.CounterDataPointSnapshot collect(Labels labels) {
      // Read the exemplar first. Otherwise, there is a race condition where you might
      // see an Exemplar for a value that's not counted yet.
      // If there are multiple Exemplars (by default it's just one), use the newest.
      Exemplar latestExemplar = null;
      if (exemplarSampler != null) {
        for (Exemplar exemplar : exemplarSampler.collect()) {
          if (latestExemplar == null
              || exemplar.getTimestampMillis() > latestExemplar.getTimestampMillis()) {
            latestExemplar = exemplar;
          }
        }
      }
      return new CounterSnapshot.CounterDataPointSnapshot(
          get(), labels, latestExemplar, createdTimeMillis);
    }
  }

  /**
   * Specialized data point, which is used in case no exemplar support is enabled.
   * Applications can cast to this data time to speed up counter increment operations.
   */
  public static final class DataPointIgnoringExemplars extends DataPoint {

    private DataPointIgnoringExemplars() {
      super(null);
    }

    /**
     * This is one of the most used metric. Override for speed. Direct shortcut to
     * the {@code LongAdder.add} method with just only validating the argument. This
     * override is actually not really needed because the override of {@link #isExemplarsEnabled()}
     * and inlining has the same effect in the end, however, for everyone inspecting
     * the code it makes it obvious that the increment a long counter code path is as short
     * as possible.
     */
    @Override
    public void inc(long amount) {
      validateAndAdd(amount);
    }

    /**
     * Counter increment, probably the most used metric update method. Specialise and skip
     * validation. Basically, the JVM JIT is doing this for us and inlining the code. However,
     * for people that are curious about performance and inspecting the code, it might be good
     * to be assured that this goes directly to the `LongAdder` method.
     */
    @Override
    public void inc() {
      longValue.increment();
    }

    /**
     * Override for speed. Since final, the JVM will inline code for this
     * method and all Exemplar code will be left out.
     */
    @Override
    protected boolean isExemplarsEnabled() {
      return false;
    }

  }

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder extends StatefulMetric.Builder<Builder, Counter> {

    private Builder(PrometheusProperties properties) {
      super(Collections.emptyList(), properties);
    }

    /**
     * The {@code _total} suffix will automatically be appended if it's missing.
     *
     * <pre>{@code
     * Counter c1 = Counter.builder()
     *     .name("events_total")
     *     .build();
     * Counter c2 = Counter.builder()
     *     .name("events")
     *     .build();
     * }</pre>
     *
     * In the example above both {@code c1} and {@code c2} would be named {@code "events_total"} in
     * Prometheus.
     *
     * <p>Throws an {@link IllegalArgumentException} if {@link
     * io.prometheus.metrics.model.snapshots.PrometheusNaming#isValidMetricName(String)
     * MetricMetadata.isValidMetricName(name)} is {@code false}.
     */
    @Override
    public Builder name(String name) {
      return super.name(stripTotalSuffix(name));
    }

    @Override
    public Counter build() {
      return new Counter(this, properties);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
