package io.prometheus.client.metrics;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.*;
import io.prometheus.client.Metrics;
import io.prometheus.client.utility.labels.Reserved;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * <p>{@link Gauge} is a {@link Metric} that reports instantaneous values based on external state.</p>
 *
 * <ul>
 *     <li>
 *         Instantaneous value: The amount of money currently in the room.  The value comes
 *         from a blackbox system that can only return the state result; it does not return
 *         how the state was derived.
 *     </li>
 * </ul>
 *
 * <p>{@link Gauge} is a {@link Metric} that tracks instantaneous values that are set outside of the context of control.</p>
 *
 *
 *
 * <p>An example:</p>
 * <pre>
 * {@code
 * package example;
 *
 * import io.prometheus.client.Prometheus;
 * import io.prometheus.client.Register;
 * import io.prometheus.client.metrics.Counter;
 *
 * public class Aquarium {
 *   // Annotate this with Register!
 *   private static final Gauge waterTemp = Gauge.builder()
 *     .inNamespace("seaworld")
 *     .named("water_temperature_c")
 *     .withDimension("tank_name")
 *     .documentedAs("The current aquarium tank temperature partitioned by tank name.")
 *     .build()
 *
 *   public void run() {
 *     while (true) {
 *       // Busy loop.  :sad-trombone:
 *       waterTemp.newPartial()
 *         .withDimension("tank_name", "shamu")
 *         .apply()
 *         .set(getShamuTemperature());
 *
 *       waterTemp.newPartial()
 *         .withDimension("tank_name", "urchin")
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
 * <p><em>For representing values inside of business logic direct control, use a {@link Counter}.</em></p>

 * @author Matt T. Proud (matt.proud@gmail.com)
 */
@ThreadSafe
public class Gauge extends Metric<Gauge, Gauge.Child, Gauge.Partial> {
  private final double defaultValue;

  private Gauge(final String n, final String d, final ImmutableList<String> ds,
      final double defaultValue, final Metrics.MetricFamily p) {
    super(n, d, ds, p);
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
   * Start generating a concrete {@link Counter.Guage} instance by building a
   * partial and accumulating labels with it.
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
  public static Builder builder() {
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
  public static class Builder {
    private static final Double DEFAULT_VALUE = Double.valueOf(0);

    private final BaseBuilder base = new BaseBuilder();

    private Optional<Double> defaultValue = Optional.absent();

    /**
     * @see Metric.BaseBuilder#withDimension(String...)
     */
    public Builder withDimension(String... ds) {
      base.withDimension(ds);
      return this;
    }

    /**
     * @see Metric.BaseBuilder#documentedAs(String)
     */
    public Builder documentedAs(String d) {
      base.documentedAs(d);
      return this;
    }

    /**
     * @see Metric.BaseBuilder#named(String) (String)
     */
    public Builder named(String n) {
      base.named(n);
      return this;
    }

    /**
     * @see Metric.BaseBuilder#ofSubsystem(String)
     */
    public Builder ofSubsystem(String ss) {
      base.ofSubsystem(ss);
      return this;
    }

    /**
     * @see Metric.BaseBuilder#inNamespace(String)
     */
    public Builder inNamespace(String ns) {
      base.inNamespace(ns);
      return this;
    }

    /**
     * <p>
     * Provide a custom default value for this {@link Gauge} when it undergoes a
     * {@link io.prometheus.client.metrics.Gauge#resetAll()} or a specific
     * {@link Child} undergoes a {@link Gauge.Child#reset()}.
     * </p>
     */
    public Builder withDefaultValue(final Double v) {
      defaultValue = Optional.of(v);
      return this;
    }

    private double getDefaultValue() {
      return defaultValue.or(DEFAULT_VALUE);
    }

    /**
     * <p>
     * Generate a concrete {@link Gauge} from this {@link Builder}.
     * </p>
     */
    public Gauge build() {
      final String name = base.buildName();
      final String docstring = base.buildDocstring();

      final Metrics.MetricFamily.Builder builder =
          Metrics.MetricFamily.newBuilder().setName(name).setHelp(docstring)
              .setType(Metrics.MetricType.GAUGE);

      return new Gauge(base.buildName(), base.buildDocstring(), base.buildDimensions(),
          getDefaultValue(), builder.build());
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
    private final AtomicDouble value = new AtomicDouble();

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
     * {@link Counter.Builder#withDefaultValue(Double)}.
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
