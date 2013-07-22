package io.prometheus.client.metrics;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.ThreadSafe;

import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.joda.time.ReadableDuration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.*;
import com.matttproud.quantile.Estimator;
import com.matttproud.quantile.Quantile;

import io.prometheus.client.Metrics;
import io.prometheus.client.utility.labels.Reserved;

/**
 * <p>{@link Summary} is a {@link Metric} that samples events over sliding windows of time.</p>
 * <ul>
 *     <li>
 *         Distributions: The spread and depth of observed samples, including quantile ranks.
 *     </li>
 * </ul>
 *
 *
 * <p>An example from {@link io.prometheus.client.Prometheus}:</p>
 * <pre>
 * {@code
 * package example;
 *
 * import io.prometheus.client.Prometheus;
 * import io.prometheus.client.Register;
 * import io.prometheus.client.metrics.Counter;
 *
 * public class BirdWatcher {
 *   // Annotate this with Register!
 *   private static final Summary observations = Summary.builder()
 *     .inNamespace("birds")
 *     .named("weights")
 *     .withDimension("genus")
 *     .documentedAs("Weights of birds partitioned by genus.")
 *     .withTarget(0.5, 0.05)  // Gimme median!
 *     .withTarget(0.99, 0.001)  // Gimme 99th!
 *     .build()
 *
 *   public float visitForest() {
 *     while (true) {
 *       // Busy loop.  :eat berries, watch birds, and try not to poison yourself:
 *       observations.newPartial()
 *         .withDimension("genus", "garrulus")  // Got a Eurasian Jay.
 *         .apply()
 *         .observe(175);
 *
 *       observations.newPartial()
 *         .withDimension("genus", "corvus")  // Got a Hooded Crow.
 *         .apply()
 *         .observe(500);
 *     }
 *   }
 * }}
 * </pre>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 */
@ThreadSafe
public class Summary extends Metric<Summary, Summary.Child, Summary.Partial> {
  private final int purgeInterval;
  private final ReadableDuration purgeTimeUnit;
  private final ImmutableMap<Double, Double> targets;

  private Instant lastPurge;

  private Summary(final String n, final String d, final ImmutableList<String> ds, final int pi,
      final ReadableDuration pu, final ImmutableMap<Double, Double> t, final Metrics.MetricFamily p) {
    super(n, d, ds, p);

    purgeInterval = pi;
    purgeTimeUnit = pu;
    targets = t;

    lastPurge = Instant.now();
  }

  void purge() {
    if (lastPurge.withDurationAdded(purgeTimeUnit, purgeInterval).isAfterNow()) {
      return;
    }

    for (final Child c : children.values()) {
      c.reset();
    }

    children.clear();

    lastPurge = Instant.now();
  }

  @Override
  Metrics.MetricFamily.Builder annotateBuilder(final Metrics.MetricFamily.Builder b) {
    try {
      for (final Map<String, String> labels : children.keySet()) {
        final Child child = children.get(labels);
        final Metrics.Metric.Builder m = b.addMetricBuilder();

        for (final String label : labels.keySet()) {
          final String value = labels.get(label);
          m.addLabelBuilder().setName(label).setValue(value);
        }

        final Metrics.Summary.Builder builder = Metrics.Summary.newBuilder();

        for (final Child c : children.values()) {
          builder.setSampleCount(c.count.get());
          builder.setSampleSum(c.sum.get());

          for (final double q : c.targets.keySet()) {
            final double v = c.query(q);
            final Metrics.Quantile.Builder qs = builder.addQuantileBuilder();

            qs.setQuantile(q);
            qs.setValue(v);
          }
        }

        m.setSummary(builder.build());
      }

      return b;
    } finally {
      purge();
    }
  }

  @Override
  public Partial newPartial() {
    return new Partial();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private static final ReadableDuration DEFAULT_PURGE_TIME_UNIT = Minutes.ONE
        .toStandardDuration();
    private static final Integer DEFAULT_PURGE_INTERVAL = 15;
    private static final ImmutableMap<Double, Double> DEFAULT_TARGETS = ImmutableMap.of(0.5, 0.05,
        0.99, 0.001);

    private final BaseBuilder base = new BaseBuilder();
    private final ImmutableMap.Builder<Double, Double> targets = ImmutableMap.builder();

    private Optional<ReadableDuration> purgeTimeUnit = Optional.absent();
    private Optional<Integer> purgeInterval = Optional.absent();

    public Builder withDimension(String... ds) {
      base.withDimension(ds);
      return this;
    }

    public Builder documentedAs(String d) {
      base.documentedAs(d);
      return this;
    }

    public Builder named(String n) {
      base.named(n);
      return this;
    }

    public Builder ofSubsystem(String ss) {
      base.ofSubsystem(ss);
      return this;
    }

    public Builder inNamespace(String ns) {
      base.inNamespace(ns);
      return this;
    }

    public Builder purgesEvery(final int n, final ReadableDuration d) {
      purgeTimeUnit = Optional.of(d);
      purgeInterval = Optional.of(n);

      return this;
    }

    /**
     * 
     * @param quantile The target quantile expressed over the
     *        <code>[0, 1]</code> interval.
     * @param inaccuracy The inaccuracy allowance expressed over the
     *        <code>[0, 1]</code> interval.
     * @return
     */
    public Builder withTarget(final Double quantile, final Double inaccuracy) {
      targets.put(quantile, inaccuracy);

      return this;
    }

    private ReadableDuration getPurgeTimeUnit() {
      return purgeTimeUnit.or(DEFAULT_PURGE_TIME_UNIT);
    }

    private int getPurgeInterval() {
      return purgeInterval.or(DEFAULT_PURGE_INTERVAL);
    }

    private ImmutableMap<Double, Double> getTargets() {
      final ImmutableMap<Double, Double> setTargets = targets.build();
      if (setTargets.size() == 0) {
        return DEFAULT_TARGETS;
      }

      return setTargets;
    }

    public Summary build() {
      final String name = base.buildName();
      final String docstring = base.buildDocstring();

      final Metrics.MetricFamily.Builder builder =
          Metrics.MetricFamily.newBuilder().setName(name).setHelp(docstring)
              .setType(Metrics.MetricType.SUMMARY);

      return new Summary(base.buildName(), base.buildDocstring(), base.buildDimensions(),
          getPurgeInterval(), getPurgeTimeUnit(), getTargets(), builder.build());
    }
  }

  public class Partial extends Metric.Partial {
    /**
     * <p><em>Warning:</em> Do not hold onto a reference of a {@link Partial}
     * if you ever use the {@link #resetAll()} or
     * {@link io.prometheus.client.metrics.Metric.Child#reset()} tools.
     * This will be fixed in a follow-up release.</p>
     *
     * @see Metric.Partial#withDimension(String, String)
     */
    @Override
    public Partial withDimension(String labelName, String labelValue) {
      return (Partial) baseWithDimension(labelName, labelValue);
    }

    @Override
    protected Summary.Child newChild() {
      return new Child(targets);
    }

    @Override
    public Summary.Child apply() {
      return (Summary.Child) baseApply();
    }
  }

  public class Child implements Metric.Child {
    private final AtomicDouble sum = new AtomicDouble();
    private final AtomicLong count = new AtomicLong();
    private final ImmutableMap<Double, Double> targets;

    private Estimator<Double> estimator;

    Child(final ImmutableMap<Double, Double> targets) {
      this.targets = targets;

      final Quantile quantiles[] = new Quantile[targets.size()];
      int i = 0;
      for (final Double t : targets.keySet()) {
        final Double a = targets.get(t);

        quantiles[i] = new Quantile(t, a);
        i++;
      }

      estimator = new Estimator<Double>(quantiles);

    }

    synchronized public void observe(final Double v) {
      estimator.insert(v);
      sum.getAndAdd(v);
      count.getAndIncrement();
    }

    @Override
    synchronized public void reset() {
      final Quantile quantiles[] = new Quantile[targets.size()];
      int i = 0;
      for (final Double t : targets.keySet()) {
        final Double a = targets.get(t);

        quantiles[i] = new Quantile(t, a);
        i++;
      }

      estimator = new Estimator<Double>(quantiles);

      sum.set(0);
      count.set(0);
    }

    synchronized Double query(final Double q) {
      return estimator.query(q);
    }
  }

    /**
     * <p>
     * Used to serialize {@link Summary} instances for {@link Gson}.
     * </p>
     */
  public static class Serializer implements JsonSerializer<Summary> {
    @Override
    public JsonElement serialize(final Summary src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      final JsonObject container = new JsonObject();
      final JsonObject baseLabels = new JsonObject();
      baseLabels.addProperty(Reserved.NAME.label(), src.name);

      container.add(SERIALIZE_BASE_LABELS, baseLabels);
      container.addProperty(SERIALIZE_DOCSTRING, src.docstring);

      final JsonObject metric = new JsonObject();
      metric.addProperty("type", "histogram");
      final JsonArray values = new JsonArray();
      for (final Map<String, String> labelSet : src.children.keySet()) {
        final JsonObject element = new JsonObject();
        element.add("labels", context.serialize(labelSet));
        final Child vector = src.children.get(labelSet);

        final JsonObject quantiles = new JsonObject();
        for (final Double q : vector.targets.keySet()) {
          final double v = vector.query(q);
          quantiles.addProperty(q.toString(), v);
        }

        element.add("value", quantiles);
        values.add(element);
      }
      metric.add("value", values);

      container.add(SERIALIZE_METRIC, context.serialize(metric));

      return container;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Summary)) return false;
    if (!super.equals(o)) return false;

    final Summary summary = (Summary) o;

    if (purgeInterval != summary.purgeInterval) return false;
    if (!purgeTimeUnit.equals(summary.purgeTimeUnit)) return false;
    if (!targets.equals(summary.targets)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + purgeInterval;
    result = 31 * result + purgeTimeUnit.hashCode();
    result = 31 * result + targets.hashCode();
    return result;
  }
}
