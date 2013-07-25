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
import com.google.gson.*;
import io.prometheus.client.Metrics;
import io.prometheus.client.utility.labels.Reserved;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * {@link Gauge} is a {@link Metric} that reports instantaneous values based on
 * external state.
 * </p>
 *
 * <ul>
 * <li>
 * Instantaneous value: The amount of money currently in the room. The value
 * comes from a blackbox system that can only return the state result; it does
 * not return how the state was derived.</li>
 * </ul>
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
 * public class Aquarium {
 *   // Annotate this with "Register" if this class is not explicitly loaded
 *   // by your project.
 *   private static final Gauge waterTemp = Gauge.newBuilder()
 *     .namespace("seaworld")
 *     .inSubsystem("aquatic_tanks")
 *     .name("water_temperature_c")
 *     .registerStatic("tank_name")
 *     .documentation("The current aquarium tank temperature partitioned by tank name.")
 *     .build()
 *
 *   public void run() {
 *     while (true) {
 *       // Busy loop.  :sad-trombone:
 *       waterTemp.newPartial()
 *         .registerStatic("tank_name", "shamu")
 *         .apply()
 *         .set(getShamuTemperature());
 *
 *       waterTemp.newPartial()
 *         .registerStatic("tank_name", "urchin")
 *         .apply()
 *         .set(getUrchinTemperature());
 *     }
 *   }
 *
 *   private double getShamuTemperature() {
 *       // That poor orca's boiling alive!
 *       return 42;
 *   }
 *
 *   private double getUrchinTemperature() {
 *       return 9;
 *   }
 * }}
 * </pre>
 *
 * <p>
 * Assuming each code path is hit once, {@code waterTemp} yields the following
 * child metrics:
 * </p>
 *
 * <pre>
 *   seaworld_aquatic_tanks_water_temperature_c{tank_name="shamu"}  = 42
 *   seaworld_aquatic_tanks_water_temperature_c{tank_name="urchin"} = 9
 * </pre>
 *
 * <p>
 * <em>For representing whitebox values inside of business logic control, use a {@link Counter}.</em>
 * </p>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 */
@ThreadSafe
public class Gauge extends Metric<Gauge, Gauge.Child, Gauge.Partial> {
  private final double defaultValue;

  private Gauge(final String n, final String d, final List<String> ds, final double defaultValue,
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

      m.setGauge(Metrics.Gauge.newBuilder().setValue(child.value.get()));
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
   * Create a {@link Builder} to configure the {@link Gauge}.
   * </p>
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * <p>
   * Define the characteristics for this {@link Gauge}.
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
  public static class Builder implements Metric.Builder<Builder, Gauge> {
    private static final Double DEFAULT_VALUE = Double.valueOf(0);

    private final BaseBuilder base;
    private final Optional<Double> defaultValue;

    Builder() {
      base = new BaseBuilder();
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
     * Provide a custom default value for this {@link Gauge} when it undergoes a
     * {@link io.prometheus.client.metrics.Gauge#resetAll()} or a specific
     * {@link Child} undergoes a {@link Gauge.Child#reset()}.
     *
     * @return A <em>new copy</em> of the original {@link Builder} with the new
     *         target value.
     *         </p>
     *
     * @return A <em>new copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public Builder defaultValue(final Double v) {
      return new Builder(base, Optional.of(v));
    }

    private double getDefaultValue() {
      return defaultValue.or(DEFAULT_VALUE);
    }

    @Override
    public Gauge build() {
      final String name = base.buildName();
      final String docstring = base.buildDocstring();

      final Metrics.MetricFamily.Builder builder =
          Metrics.MetricFamily.newBuilder().setName(name).setHelp(docstring)
              .setType(Metrics.MetricType.GAUGE);

      return new Gauge(base.buildName(), base.buildDocstring(), base.buildLabelNames(),
          getDefaultValue(), builder.build(), base.getRegisterStatic());
    }
  }

  /**
   * <p>
   * A derivative of {@link Gauge} that lets you accumulate labels to build a
   * concrete metric via {@link #apply()} for mutation with the methods of
   * {@link Gauge.Child}.
   * </p>
   *
   * @see Metric.Partial
   */
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
    protected Gauge.Child newChild() {
      return new Child();
    }

    /**
     * <p>
     * Finalize this child to perform mutations under this set of label-value
     * pairs.
     * </p>
     *
     * @see io.prometheus.client.metrics.Metric.Partial#apply()
     */
    @Override
    public Gauge.Child apply() {
      return (Child) baseApply();
    }
  }

  /**
   * <p>
   * A concrete instance of {@link Gauge} for a unique set of label dimensions.
   * </p>
   *
   * @see Metric.Child
   */
  public class Child implements Metric.Child {
    final AtomicDouble value = new AtomicDouble();

    /**
     * <p>
     * Set this {@link io.prometheus.client.metrics.Gauge.Child} to an arbitrary
     * value.
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
   * Used to serialize {@link Gauge} instances for {@link Gson}.
   * </p>
   */
  public static class Serializer implements JsonSerializer<Gauge> {
    @Override
    public JsonElement serialize(final Gauge src, final Type typeOfSrc,
        final JsonSerializationContext context) {
      final JsonObject container = new JsonObject();
      final JsonObject baseLabels = new JsonObject();
      baseLabels.addProperty(Reserved.NAME.label(), src.name);

      container.add(SERIALIZE_BASE_LABELS, baseLabels);
      container.addProperty(SERIALIZE_DOCSTRING, src.docstring);

      final JsonObject metric = new JsonObject();
      metric.addProperty("type", "gauge");
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
    if (!(o instanceof Gauge)) return false;
    if (!super.equals(o)) return false;

    final Gauge gauge = (Gauge) o;

    if (Double.compare(gauge.defaultValue, defaultValue) != 0) return false;

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
