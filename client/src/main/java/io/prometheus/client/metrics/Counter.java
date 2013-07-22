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
 * <p>{@link Counter} is a {@link Metric} that tracks the addition or subtraction of a value from itself.</p>
 * <ul>
 *     <li>
 *         Tallies: Number of people who walked through that door.
 *     </li>
 *     <li>
 *         Running Sums: Amount of money that has been brought through the door.
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
 * public class CashRegister {
 *   // Annotate this with Register!
 *   private static final Counter operations = Counter.builder()
 *     .inNamespace("cash_register")
 *     .named("operation")
 *     .withDimension("operation", "result")
 *     .documentedAs("Cash register operations partitioned by type and outcome.")
 *     .build()
 *
 *   public float divide(float dividend, float divisor) {
 *     Counter.Partial result = operations.newPartial()
 *       .withDimension("operation", "division");
 *     try {
 *       float f = dividend / divisor;
 *       result.withDimension("result", "success");
 *       return f;
 *     } catch (ArithmeticException e) {
 *       result.withDimension("result", "failure");
 *       throw e;
 *     } finally {
 *       result.apply().increment();
 *     }
 *   }
 * }}
 * </pre>
 *
 * <p><em>For representing values outside of business logic direct control, use a {@link Gauge}.</em></p>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 */
@ThreadSafe
public class Counter extends Metric<Counter, Counter.Child, Counter.Partial> {
  private final double defaultValue;

  private Counter(final String n, final String d, final ImmutableList<String> ds,
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

      m.setCounter(Metrics.Counter.newBuilder().setValue(child.value.get()));
    }

    return b;
  }

    /**
     * <p>
     *  Start generating a concrete {@link Counter.Child} instance by building a partial and accumulating
     *  labels with it.
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
  public static Builder builder() {
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
  public static class Builder {
    private static final Double DEFAULT_VALUE = Double.valueOf(0);

    private final Metric.BaseBuilder base = new Metric.BaseBuilder();

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
     * @see Metric.BaseBuilder#named(String)
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
     * Provide a custom default value for this {@link Counter} when it undergoes
     * a {@link io.prometheus.client.metrics.Counter#resetAll()} or a specific
     * {@link Child} undergoes a {@link Counter.Child#reset()}.
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
     * Generate a concrete {@link Counter} from this {@link Builder}.
     * </p>
     */
    public Counter build() {
      final String name = base.buildName();
      final String docstring = base.buildDocstring();

      final Metrics.MetricFamily.Builder builder =
          Metrics.MetricFamily.newBuilder().setName(name).setHelp(docstring)
              .setType(Metrics.MetricType.COUNTER);

      return new Counter(name, docstring, base.buildDimensions(), getDefaultValue(),
          builder.build());
    }
  }

  /**
   * <p>
   *     A derivative of {@link Counter} that lets you accumulate labels to build a concrete metric via
   *     {@link #apply()} for mutation with the methods of {@link Counter.Child}.
   * </p>
   * @see Metric.Partial
   */
  public class Partial extends Metric.Partial {
    /**
     * <p>Add a label-value pair to this metric.</p>
     *
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
    protected Counter.Child newChild() {
      return new Child();
    }

    /**
     * <p>Finalize this child to perform mutations under this set of label-value pairs.</p>
     * @see io.prometheus.client.metrics.Metric.Partial#apply()
     */
    @Override
    public Counter.Child apply() {
      return (Counter.Child) baseApply();
    }
  }

  /**
   * <p>
   *     A concrete instance of {@link Counter} for a unique set of label dimensions.
   * </p>
   * @see Metric.Child
   */
  public class Child implements Metric.Child {
    private final AtomicDouble value = new AtomicDouble();

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
     * Decrement this {@link Counter.Child} by one.
     * </p>
     */
    public void decrement() {
      decrement(1);
    }

    /**
     * <p>
     * Decrement this {@link Counter.Child} by {@code v}.
     * </p>
     */
    public void decrement(final double v) {
      value.getAndAdd(-1 * v);
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
   * Used to serialize {@link Counter} instances for {@link Gson}.
   * </p>
   */
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
