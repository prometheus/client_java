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

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.prometheus.client.Metrics;
import io.prometheus.client.utility.labels.Reserved;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * {@link Counter} is a {@link Metric} that tracks the addition or subtraction
 * of a value from itself.
 * </p>
 * <ul>
 * <li>
 * Tallies: Number of people who walked through that door.</li>
 * <li>
 * Running Sums: Amount of money that has been brought through the door.</li>
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
 * public class CashRegister {
 *   // Annotate this with "Register" if this class is not explicitly loaded
 *   // by your project.
 *   private static final Counter operations = Counter.newBuilder()
 *     .namespace("cash_register")
 *     .name("operations")
 *     .labelNames("operation", "result")
 *     .documentation("Cash register operations partitioned by type and outcome.")
 *     .build()
 *
 *   public float divide(float dividend, float divisor) {
 *     Counter.Partial result = operations.newPartial()
 *       .labelPair("operation", "division");
 *     try {
 *       float f = dividend / divisor;
 *       result.labelPair("result", "success");
 *       return f;
 *     } catch (ArithmeticException e) {
 *       result.labelPair("result", "failure");
 *       throw e;
 *     } finally {
 *       result.apply().increment();
 *     }
 *   }
 * }}
 * </pre>
 *
 * <p>
 * Assuming that each code path is executed once, {@code operations} yields the following
 * child metrics:
 * </p>
 *
 * <pre>
 *   cash_register_operations{operation="division", result="failure"} = 1
 *   cash_register_operations{operation="division", result="success"} = 1
 * </pre>
 *
 * <p>
 * <em>Note:</em> To represent blackbox values, use {@link Gauge}.
 * </p>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 */
@ThreadSafe
public class Counter extends Metric<Counter, Counter.Child, Counter.Partial> {
  private final double defaultValue;

  private Counter(final String n, final String d, final List<String> ds, final double defaultValue,
      final Metrics.MetricFamily p, final boolean rs) {
    super(n, d, ds, p, rs);
    this.defaultValue = defaultValue;
  }

  @Override
  Metrics.MetricFamily.Builder annotateBuilder(final Metrics.MetricFamily.Builder b) {
    for (final Map<String, String> labels : children.keySet()) {
      final Child child = children.get(labels);
      final Metrics.Metric.Builder m = b.addMetricBuilder();

      for (final String label : labels.keySet()) {
        final String value = labels.get(label);
        m.addLabelBuilder().setName(label).setValue(value);
      }

      m.setCounter(Metrics.Counter.newBuilder().setValue(child.value.get()));
    }

    return b;
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

  /**
   * <p>
   * Create a {@link Builder} to configure the {@link Counter}.
   * </p>
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * <p>
   * Define the characteristics for this {@link Counter}.
   * </p>
   * <p>
   * Implementation-Specific Behaviors:
   * <ul>
   * <li>
   * If the metric and its children are reset, a default value of {@code 0} is
   * used.</li>
   * </ul>
   * </p>
   * <p>
   * For all other behaviors, see {@link Metric.BaseBuilder}.
   * </p>
   */
  @ThreadSafe
  public static class Builder implements Metric.Builder<Builder, Counter> {
    private static final Double DEFAULT_VALUE = Double.valueOf(0);

    private final Metric.BaseBuilder base;
    private final Optional<Double> defaultValue;

    Builder() {
      base = new Metric.BaseBuilder();
      defaultValue = Optional.absent();
    }

    private Builder(final BaseBuilder base, final Optional<Double> defaultValue) {
      this.base = base;
      this.defaultValue = defaultValue;
    }

    @Override
    public Builder labelNames(String... ds) {
      return new Builder(base.labelNames(ds), defaultValue);
    }

    @Override
    public Builder documentation(String d) {
      return new Builder(base.documentation(d), defaultValue);
    }

    @Override
    public Builder name(String n) {
      return new Builder(base.name(n), defaultValue);
    }

    @Override
    public Builder subsystem(String ss) {
      return new Builder(base.subsystem(ss), defaultValue);
    }

    @Override
    public Builder namespace(String ns) {
      return new Builder(base.namespace(ns), defaultValue);
    }

    @Override
    public Builder registerStatic(final boolean rs) {
      return new Builder(base.registerStatic(rs), defaultValue);
    }

    /**
     * <p>
     * Provide a custom default value for this {@link Counter} when it undergoes
     * a {@link io.prometheus.client.metrics.Counter#resetAll()} or a specific
     * {@link Child} undergoes a {@link Counter.Child#reset()}.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public Builder defaultValue(final Double v) {
      return new Builder(base, Optional.of(v));
    }

    private double getDefaultValue() {
      return defaultValue.or(DEFAULT_VALUE);
    }

    /**
     * <p>
     * Generate a concrete {@link Counter} from this {@link Builder}.
     * </p>
     */
    public Counter build() {
      final String name = base.buildName();
      final String docstring = base.buildDocstring();

      final Metrics.MetricFamily.Builder builder =
          Metrics.MetricFamily.newBuilder().setName(name).setHelp(docstring)
              .setType(Metrics.MetricType.COUNTER);

      return new Counter(name, docstring, base.buildLabelNames(), getDefaultValue(),
          builder.build(), base.getRegisterStatic());
    }
  }

  /**
   * <p>
   * A derivative of {@link Counter} that lets you accumulate labels to build a
   * concrete metric via {@link #apply()} for mutation with the methods of
   * {@link Counter.Child}.
   * </p>
   *
   * <p>
   * <em>Warning:</em> All mutations to {@link Partial} are retained.  You should <em>not</em>
   * share {@link Partial} between distinct label sets unless you have a parent
   * {@link Partial} that you {@link io.prometheus.client.metrics.Counter.Partial#clone()}.
   * </p>
   *
   * <p>
   * In this example below, we have both a race condition with a nasty outcome that
   * unformedMetric is mutated in both threads and that it is an undefined behavior, which
   * {@code data-type} label pair setting wins.
   * </p>
   *
   * <pre>
   * {@code
   *   Counter.Partial unformedMetric = …;
   *
   *   new Thread() {
   *     public void run() {
   *       unformedMetric.labelPair("system", "cache");
   *           .labelPair("data-type", "user-profile");  // Difference
   *           .apply()
   *           .increment();
   *     }
   *   }.start();
   *
   *   new Thread() {
   *     public void run() {
   *       unformedMetric.labelPair("system", "cache");
   *           .labelPair("data-type", "avatar");  // Difference
   *           .apply()
   *           .increment();
   *     }
   *   }.start();
   * }
   * </pre>
   *
   * <p>
   * The following is preferable and {@link ThreadSafe}:
   * </p>
   * <pre>
   * {@code
   *   Counter.Partial unformedMetric = …;
   *
   *   new Thread() {
   *     public void run() {
   *       Counter.Partial local = unformedMetric.clone();  // Safe step!
   *
   *       local.labelPair("system", "cache");
   *           .labelPair("data-type", "user-profile");  // Difference
   *           .apply()
   *           .increment();
   *     }
   *   }.start();
   *
   *   new Thread() {
   *     public void run() {
   *       Counter.Partial local = unformedMetric.clone();  // Safe step!
   *
   *       local.labelPair("system", "cache");
   *           .labelPair("data-type", "avatar");  // Difference
   *           .apply()
   *           .increment();
   *     }
   *   }.start();
   * }
   * </pre>
   *
   * @see Metric.Partial
   */
  @NotThreadSafe
  public class Partial extends Metric.Partial {
    @Override
    public Partial labelPair(String labelName, String labelValue) {
      return (Partial) baseLabelPair(labelName, labelValue);
    }

    @Override
    public Partial clone() {
      return (Partial) super.clone();
    }

    @Override
    protected Counter.Child newChild() {
      return new Child();
    }

    /**
     * <p>
     * Finalize this child under this set of label-value pairs and create a {@link Counter.Child}
     * to mutate.
     * </p>
     *
     * @see io.prometheus.client.metrics.Metric.Partial#apply()
     */
    @Override
    public Counter.Child apply() {
      return (Counter.Child) baseApply();
    }
  }

  /**
   * <p>
   * A concrete instance of {@link Counter} for a unique set of label
   * dimensions.
   * </p>
   *
   * <p>
   * <em>Warning:</em> Do not hold onto a reference of a {@link Child} if you
   * ever use the {@link #resetAll()}.  If you want to hold onto a concrete
   * instance, please hold onto a {@link io.prometheus.client.metrics.Counter.Partial} and use
   * {@link io.prometheus.client.metrics.Counter.Partial#apply()}.
   * </p>
   *
   * @see Metric.Child
   */
  @ThreadSafe
  public class Child implements Metric.Child {
    final AtomicDouble value = new AtomicDouble();

    /**
     * <p>
     * Increment this {@link Counter.Child} by one.
     * </p>
     */
    public void increment() {
      increment(1);
    }

    /**
     * <p>
     * Increment this {@link Counter.Child} by {@code v}.
     * </p>
     */
    public void increment(final double v) {
      value.getAndAdd(v);
    }

    /**
     * <p>
     * Set this {@link io.prometheus.client.metrics.Counter.Child} to {@code v}.
     * </p>
     */
    public void set(final double v) {
      value.getAndSet(v);
    }

    /**
     * <p>
     * Reset this {@link Counter} to its default value per
     * {@link Counter.Builder#defaultValue(Double)}.
     * </p>
     */
    @Override
    public void reset() {
      value.getAndSet(defaultValue);
    }
  }

  /**
   * <p>
   * Used to serialize {@link Counter} instances for {@link Gson}.
   * </p>
   */
  @Deprecated
  public static class Serializer implements JsonSerializer<Counter> {
    @Override
    public JsonElement serialize(final Counter src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      final JsonObject container = new JsonObject();
      final JsonObject baseLabels = new JsonObject();
      baseLabels.addProperty(Reserved.NAME.label(), src.name);

      container.add(SERIALIZE_BASE_LABELS, baseLabels);
      container.addProperty(SERIALIZE_DOCSTRING, src.docstring);

      final JsonObject metric = new JsonObject();
      metric.addProperty("type", "counter");
      final JsonArray values = new JsonArray();
      for (final Map<String, String> labelSet : src.children.keySet()) {
        final JsonObject element = new JsonObject();
        element.add("labels", context.serialize(labelSet));
        final Child vector = src.children.get(labelSet);
        element.add("value", context.serialize(vector.value.get()));
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
    if (!(o instanceof Counter)) return false;
    if (!super.equals(o)) return false;

    final Counter counter = (Counter) o;

    if (Double.compare(counter.defaultValue, defaultValue) != 0) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(defaultValue);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
