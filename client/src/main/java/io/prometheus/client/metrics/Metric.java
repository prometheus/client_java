package io.prometheus.client.metrics;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Metrics;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * A @{link Metric} is the base type of all Prometheus metrics.
 * </p>
 * 
 * <p>
 * <em>Users of Prometheus do not need to be familiar with this class.</em>
 * </p>
 * 
 * @param <M> The concrete implementation of {@link Metric}.
 * @param <C> The concrete implementation of {@link Metric.Child}.
 * @param <P> The concrete implementation of {@link Metric.Partial}.
 * 
 * @see Counter
 * @see Gauge
 * @see Summary
 */
public abstract class Metric<M extends Metric, C extends Metric.Child, P extends Metric.Partial> {
  static final String SERIALIZE_BASE_LABELS = "baseLabels";
  static final String SERIALIZE_DOCSTRING = "docstring";
  static final String SERIALIZE_METRIC = "metric";

  private final ImmutableList<String> dimensions;
  private final Metrics.MetricFamily partial;

  final String name;
  final String docstring;
  final ConcurrentHashMap<Map<String, String>, C> children = new ConcurrentHashMap<Map<String, String>, C>();

  protected Metric(final String n, final String d, final ImmutableList<String> ds,
      final Metrics.MetricFamily p) {
    name = n;
    docstring = d;
    dimensions = ds;
    partial = p;
  }

  Metrics.MetricFamily.Builder getPartialBuilder() {
    return Metrics.MetricFamily.newBuilder(partial);
  }

  abstract Metrics.MetricFamily.Builder annotateBuilder(final Metrics.MetricFamily.Builder b);

  public Metrics.MetricFamily dump() {
    return annotateBuilder(getPartialBuilder()).build();
  }

  public void resetAll() {
    for (final C child : children.values()) {
      // BUG(matt): This is nasty.
      final Child cast = (Child) child;
      cast.reset();
    }
  }

  public void disbandChildren() {
    children.clear();
  }

  public static class BaseBuilder {
    protected Optional<String> ns = Optional.absent();
    protected Optional<String> ss = Optional.absent();
    protected Optional<String> n = Optional.absent();
    protected Optional<String> d = Optional.absent();
    protected final ImmutableList.Builder<String> ds = ImmutableList.builder();

    BaseBuilder() {}

    /**
     * <p>
     * Associate this metric with a namespace.
     * </p>
     * 
     * <p>
     * A namespace is the ownership scope of a metric, such as an organization, company, or work group.
     * </p>
     * 
     * <p>
     * For example, you have provided {@link Metric}'s {@link Metric.Builder} with the following
     * parameters:
     *
     * <ul>
     * <li>
     * a {@code namespace = "seaworld"},</li>
     * <li>
     * a {@code subsystem = "water_heaters"}, and</li>
     * <li>
     * a {@code name = "efficiency_percentage"},</li>
     * </ul>
     *
     * The {@link Metric}'s naming system generates the following composite
     * metric name: {@code seaworld_water_heaters_efficiency_percentage}.
     * The namespace is <em>seaworld</em>.
     * </p>
     */
    public BaseBuilder inNamespace(final String ns) {
      this.ns = Optional.of(ns);
      return this;
    }

    /**
     * <p>
     * Associate this metric with a subsystem.
     * </p>
     * 
     * <p>
     * A subsystem is a collection of systems that perform
     * related units of work that might have multiple aspects against which they are
     * measured.
     * </p>
     * 
     * <p>
     * For example, you have provided {@link Metric}'s {@link Metric.Builder} with the following
     * parameters:

     * <ul>
     * <li>
     * a {@code namespace = "seaworld"},</li>
     * <li>
     * a {@code subsystem = "water_heaters"}, and</li>
     * <li>
     * a {@code name = "efficiency_percentage"},</li>
     * </ul>
     *
     * The {@link Metric}'s naming system generates the following composite
     * metric name: {@code seaworld_water_heaters_efficiency_percentage}
     * . The subsystem is <em>water_heaters</em>.
     * </p>
     */
    public BaseBuilder ofSubsystem(final String ss) {
      this.ss = Optional.of(ss);
      return this;
    }

    /**
     * <p>
     * <em>Required:</em> Assign this metric a name.
     * </p>
     * 
     * <p>
     * A name is a distinct component that is measured, such as temperature.
     * </p>
     * 
     * <p>
     * * For example, you have provided {@link Metric}'s {@link Metric.Builder} with the following
     * parameters:
     *
     * <ul>
     * <li>
     * a {@code namespace = "seaworld"},</li>
     * <li>
     * a {@code subsystem = "water_heaters"}, and</li>
     * <li>
     * a {@code name = "efficiency_percentage"},</li>
     * </ul>
     *
     * The {@link Metric}'s naming system generates the following composite
     * metric name: {@code seaworld_water_heaters_efficiency_percentage}
     * . The name is <em>efficiency_percentage</em>.
     * </p>
     */
    public BaseBuilder named(final String n) {
      this.n = Optional.of(n);
      return this;
    }

    /**
     * <p>
     * <em>Required:</em> Assign a human-readable documentation string to this metric.
     * </p>
     */
    public BaseBuilder documentedAs(final String d) {
      this.d = Optional.of(d);
      return this;
    }

    /**
     * <p>
     * Declare this metric's label <em>names</em>.
     * </p>
     * 
     * <p>
     * A label is used as a facet for aggregation or pivoting in
     * data. If you were a census technician building a system to show the
     * current number of people in a given postal code at a given time, you
     * could create a label called {@code "postal_code"}.
     * </p>
     *
     * <p><em>Important:</em> Children of this metric must be instantiated
     * with all of the declared labels.  Failure to do so will result in a runtime
     * error due to a programming error.</p>
     */
    public BaseBuilder withDimension(final String... ds) {
      this.ds.add(ds);

      return this;
    }

    String buildName() {
      Preconditions.checkArgument(n.isPresent(), "name may not be empty");

      if (!(ns.isPresent() || ss.isPresent())) {
        return n.get();
      } else if (ns.isPresent() && ss.isPresent()) {
        return String.format("%s_%s_%s", ns.get(), ss.get(), n.get());
      }

      if (ns.isPresent()) {
        return String.format("%s_%s", ns.get(), n.get());
      }

      return String.format("%s_%s", ss.get(), n.get());
    }

    String buildDocstring() {
      Preconditions.checkArgument(d.isPresent(), "docstring may not be empty");

      return d.get();
    }

    ImmutableList<String> buildDimensions() {
      return ds.build();
    }
  }

  /**
   * <p>
   * Create a new {@link Metric.Partial} derivative.
   * </p>
   * 
   * @see Metric.Partial
   */
  public abstract P newPartial();

  /**
   * <p>
   * An incomplete incarnation of a {@link Metric.Child} of type {@code D} that
   * one may accumulate label dimensions against before instantiating a concrete
   * {@link Metric.Child} with {@link #apply()}.
   * </p>
   * 
   * <p>
   * Important Usage Notes:
   * <ul>
   * <li>
   * If there is a mismatch between the number of labels that have been
   * accumulated with {@link #withDimension(String, String)} and those defined
   * in the underlying {@code Builder#withDimension}, a runtime exception will
   * occur, signifying illegal use.</li>
   * </ul>
   * </p>
   * 
   * @param <D> The concrete {@link Child} type.
   */
  public abstract class Partial<D extends Child> {
    private final ImmutableMap.Builder<String, String> dimensions = ImmutableMap.builder();

    protected Partial() {}

    public abstract P withDimension(final String labelName, final String labelValue);

    P baseWithDimension(final String labelName, final String labelValue) {
      dimensions.put(labelName, labelValue);

      return (P) this;
    }

    private ImmutableMap<String, String> validate() {
      final ImmutableMap<String, String> ds = dimensions.build();
      final HashSet<String> claimed = new HashSet<String>();

      for (final String k : ds.keySet()) {
        Preconditions.checkState(ds.containsKey(k),
            String.format("%s label dimension does not exist", k));

        Preconditions.checkState(!claimed.contains(k),
            String.format("%s label dimension is already used", k));
        claimed.add(k);
      }

      return ds;
    }

    protected abstract C newChild();

    public abstract C apply();

    C baseApply() {
      final TreeMap<String, String> t = new TreeMap<String, String>(validate());
      final C v = children.putIfAbsent(t, newChild());
      if (v != null) {
        return v;
      }
      return children.get(t);
    }
  }

  public static interface Child {
    void reset();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Metric)) return false;

    final Metric metric = (Metric) o;

    if (!dimensions.equals(metric.dimensions)) return false;
    if (!docstring.equals(metric.docstring)) return false;
    if (!name.equals(metric.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = dimensions.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + docstring.hashCode();
    return result;
  }
}
