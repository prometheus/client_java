/*
 * Copyright 2013 Prometheus Team Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.prometheus.client.metrics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.*;
import com.matttproud.quantile.Estimator;
import com.matttproud.quantile.Quantile;

import io.prometheus.client.Metrics;
import io.prometheus.client.utility.labels.Reserved;

/**
 * <p>
 * {@link Summary} is a {@link Metric} that samples events over sliding windows
 * of time.
 * </p>
 * <ul>
 * <li>
 * Distributions: The spread and depth of observed samples, including quantile
 * ranks.</li>
 * </ul>
 *
 *
 * <p>
 * An example follows:
 * </p>
 *
 * <pre>
 * {@code
 * package example;
 *
 * import io.prometheus.client.Prometheus;
 * import io.prometheus.client.Register;
 * import io.prometheus.client.metrics.Counter;
 *
 * public class BirdWatcher {
 *   // Annotate this with "Register" if this class is not explicitly loaded
 *   // by your project.
 *   private static final Summary observations = Summary.newBuilder()
 *     .namespace("birds")
 *     .name("weights")
 *     .labelNames("genus")
 *     .documentation("Weights of birds partitioned by genus.")
 *     .targetQuantile(0.5, 0.05)  // Gimme median!
 *     .targetQuantile(0.99, 0.001)  // Gimme 99th!
 *     .build()
 *
 *   public float visitForest() {
 *     while (true) {
 *       // Busy loop.  :eat berries, watch birds, and try not to poison yourself:
 *       observations.newPartial()
 *         .labelPair("genus", "garrulus")  // Got a Eurasian Jay.
 *         .apply()
 *         .observe(175);
 *
 *       observations.newPartial()
 *         .labelPair("genus", "corvus")  // Got a Hooded Crow.
 *         .apply()
 *         .observe(500);
 *     }
 *   }
 * }}
 * </pre>
 *
 * <p>
 * Assuming that each code path is executed twice, {@code observations} yields the
 * following child metrics:
 * </p>
 *
 * <pre>
 *   birds_weights{genus="garrulus", quantile="0.5"}  = 175
 *   birds_weights{genus="garrulus", quantile="0.99"} = 175
 *   birds_weights{genus="corvus", quantile="0.5"}    = 500
 *   birds_weights{genus="corvus", quantile="0.99"}   = 500
 * </pre>
 *
 * <p>
 * {@code observations} also yield supplemental synthetic children:
 * </p>
 *
 * <pre>
 *   birds_weights_count{genus="garrulus"}  = 2
 *   birds_weights_count{genus="corvus"}    = 2
 *   birds_weights_sum{genus="garrulus"}    = 350
 *   birds_weights_sum{genus="corvus"}      = 1000
 * </pre>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 */
@ThreadSafe
public class Summary extends Metric<Summary, Summary.Child, Summary.Partial> {
  private final long purgeIntervalMs;
  private final Map<Double, Double> targets;

  Long lastPurgeInstantMs;

  private Summary(final String n, final String d, final List<String> ds, final long pi,
      final Map<Double, Double> t, final Metrics.MetricFamily p, final boolean rs) {
    super(n, d, ds, p, rs);

    purgeIntervalMs = pi;
    targets = t;

    lastPurgeInstantMs = System.currentTimeMillis();
  }

  void purge() {
    synchronized (lastPurgeInstantMs) {
      final long now = System.currentTimeMillis();
      if (now - lastPurgeInstantMs < purgeIntervalMs) {
        return;
      }

      for (final Child c : children.values()) {
        c.reset();
      }

      children.clear();

      lastPurgeInstantMs = now;
    }
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

        builder.setSampleCount(child.count.get());
        builder.setSampleSum(child.sum.get());

        for (final double q : child.targets.keySet()) {
          final double v = child.query(q);
          final Metrics.Quantile.Builder qs = builder.addQuantileBuilder();

          qs.setQuantile(q);
          qs.setValue(v);
        }

        m.setSummary(builder.build());
      }

      return b;
    } finally {
      purge();
    }
  }

  /**
   * <p>
   * Start generating a concrete {@link Child} instance by building a partial
   * and accumulating labels with it.
   * </p>
   *
   * @see io.prometheus.client.metrics.Metric#newPartial()
   */
  @Override
  public Partial newPartial() {
    return new Partial();
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @ThreadSafe
  public static class Builder implements Metric.Builder<Builder, Summary> {
    private static final Long DEFAULT_PURGE_INTERVAL = TimeUnit.MINUTES.toMillis(15);
    private static final ImmutableMap<Double, Double> DEFAULT_TARGETS = ImmutableMap.of(0.5, 0.05,
        0.90, 0.01, 0.99, 0.001);

    private final BaseBuilder base;
    private final Map<Double, Double> targets;
    private final Optional<Long> purgeIntervalMs;

    Builder() {
      base = new BaseBuilder();
      targets = new HashMap<Double, Double>();
      purgeIntervalMs = Optional.absent();
    }

    private Builder(BaseBuilder base, Map<Double, Double> targets, Optional<Long> purgeIntervalMs) {
      this.base = base;
      this.targets = targets;
      this.purgeIntervalMs = purgeIntervalMs;
    }

    @Override
    public Builder labelNames(String... ds) {
      return new Builder(base.labelNames(ds), targets, purgeIntervalMs);
    }

    @Override
    public Builder documentation(String d) {
      return new Builder(base.documentation(d), targets, purgeIntervalMs);
    }

    @Override
    public Builder name(String n) {
      return new Builder(base.name(n), targets, purgeIntervalMs);
    }

    @Override
    public Builder subsystem(String ss) {
      return new Builder(base.subsystem(ss), targets, purgeIntervalMs);
    }

    @Override
    public Builder namespace(String ns) {
      return new Builder(base.namespace(ns), targets, purgeIntervalMs);
    }

    @Override
    public Builder registerStatic(final boolean rs) {
      return new Builder(base.registerStatic(rs), targets, purgeIntervalMs);
    }

    /**
     * <p>
     * Set the frequency at which the {@link Summary}'s reported quantiles,
     * observation count, and observation sum are reset. This is useful to
     * prevent staleness.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public Builder purgeInterval(final int n, final TimeUnit u) {
      return new Builder(base, targets, Optional.of(u.toMillis(n)));
    }

    /**
     * <p>
     * <em>Important:</em> You may repeat calls to
     * {@link #targetQuantile(Double, Double)} to request additional values in
     * exposition!
     * </p>
     *
     * @param quantile The target quantile expressed over the
     *        <code>[0, 1]</code> interval.
     * @param inaccuracy The inaccuracy allowance expressed over the
     *        <code>[0, 1]</code> interval.
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public Builder targetQuantile(final Double quantile, final Double inaccuracy) {
      final Map<Double, Double> quantiles = new HashMap<Double, Double>(targets);
      quantiles.put(quantile, inaccuracy);
      return new Builder(base, quantiles, purgeIntervalMs);
    }


    private long getPurgeIntervalMs() {
      return purgeIntervalMs.or(DEFAULT_PURGE_INTERVAL);
    }

    private Map<Double, Double> getTargets() {
      if (targets.size() == 0) {
        return DEFAULT_TARGETS;
      }

      return targets;
    }

    public Summary build() {
      final String name = base.buildName();
      final String docstring = base.buildDocstring();

      final Metrics.MetricFamily.Builder builder =
          Metrics.MetricFamily.newBuilder().setName(name).setHelp(docstring)
              .setType(Metrics.MetricType.SUMMARY);

      return new Summary(base.buildName(), base.buildDocstring(), base.buildLabelNames(),
          getPurgeIntervalMs(), getTargets(), builder.build(), base.getRegisterStatic());
    }
  }

  @NotThreadSafe
  public class Partial extends Metric.Partial {
    /**
     * <p>
     * <em>Warning:</em> Do not hold onto a reference of a {@link Partial} if
     * you ever use the {@link #resetAll()} or
     * {@link io.prometheus.client.metrics.Metric.Child#reset()} tools. This
     * will be fixed in a follow-up release.
     * </p>
     *
     * @see Metric.Partial#labelPair(String, String)
     */
    @Override
    public Partial labelPair(String labelName, String labelValue) {
      return (Partial) baseLabelPair(labelName, labelValue);
    }

    @Override
    public Partial clone() {
      return (Partial) super.clone();
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

  @ThreadSafe
  public class Child implements Metric.Child {
    // How large of a buffer to use for internally queued sample observations before passing them
    // to the Estimator.  This value is found by performing microbenchmarks against the cross of
    // the following cases
    //
    // - thread count [1, 16]
    // - iteration count [1024, 131072]
    //
    // with a worker threads that #observe a constant value repeatedly in iteration to test
    // overhead of concurrency control.  To further minimize noise in the data, the VM was allowed
    // to warm up with three prior runs of the same case in the same process to enable the VM to
    // settle on whatever optimizations it so chooses (running in -server mode for most accurate
    // readout).
    //
    // The value below was reached when performing a binary search of the crosses above against an
    // interval of [128, 8192].  After 2048, diminishing returns were observed.  Further costs of
    // memory allocation seem unwarranted.  That said, the buffer allocations are single-time and
    // never resize throughout the life of the program once they reach capacity.
    //
    // At a standard metric request interval, which invokes #query, buffers should be compacted
    // rather frequently and probably will rarely reach saturation on their own.
    private static final int BUFFER_SIZE = 2048;

    private final AtomicDouble sum = new AtomicDouble();
    private final AtomicLong count = new AtomicLong();
    private final Map<Double, Double> targets;
    // Use a low latency buffer to receive incoming sample values.  This is done because
    // Estimator is not thread safe and requires coarse locking around it.
    private final ArrayBlockingQueue<Double> obsQueue = new ArrayBlockingQueue<>(BUFFER_SIZE);
    // Upon obsQueue saturation, values are immediately emptied into this pre-allocated buffer to
    // be passed onto the Estimator, which may at its convenience either accept the values as-is
    // for later computation or accept them and precompute the requested quantile values.  This
    // latter operation, while fast, should not force sample value producers to block until this
    // operation has been performed.
    private final ArrayList<Double> dequeued = new ArrayList<Double>(BUFFER_SIZE);

    private Estimator<Double> estimator;

    Child(final Map<Double, Double> targets) {
      this.targets = targets;

      final Quantile quantiles[] = new Quantile[targets.size()];
      int i = 0;
      for (final Double t : targets.keySet()) {
        final Double a = targets.get(t);

        quantiles[i] = new Quantile(t, a);
        i++;
      }

      // Default upstream buffer value pinned for predictability.
      estimator = new Estimator<Double>(4096, quantiles);
    }

    public void observe(final Double v) {
      try {
        if (obsQueue.offer(v)) {
          return;
        }

        synchronized (this) {
          if (obsQueue.offer(v)) {
            // Offer the ability to accept the value after potentially waiting without having to
            // force a premature compaction since this current thread may have been blocked with
            // others.
            return;
          }

          // Otherwise, this unlucky thread will force a compaction and then accept the value,
          // thereby liberating any waiting parties.
          compact();
          estimator.insert(v);
        }
      } finally {
        sum.getAndAdd(v);
        count.getAndIncrement();
      }
    }

    private void compact() {
      obsQueue.drainTo(dequeued);
      estimator.insert(dequeued);
      dequeued.clear();
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
      obsQueue.clear();
      dequeued.clear();
    }

    synchronized Double query(final Double q) {
      compact();  // Ensure any remaining observations are rendered available.
      return estimator.query(q);
    }
  }

  /**
   * <p>
   * Used to serialize {@link Summary} instances for {@link Gson}.
   * </p>
   */
  @Deprecated
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
}
