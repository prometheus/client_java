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
import com.google.common.base.Preconditions;
import io.prometheus.client.Metrics;
import io.prometheus.client.Prometheus;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * A {@link Metric} is the base type of all Prometheus metrics.
 * </p>
 *
 * <p>
 * <em>Note:</em> If you are using Prometheus, you do not need to be familiar with the internals of
 * this class.
 * </p>
 *
 * @param <M> The concrete implementation of {@link Metric}
 * @param <C> The concrete implementation of {@link Metric.Child}
 * @param <P> The concrete implementation of {@link Metric.Partial}
 *
 * @see Counter
 * @see Gauge
 * @see Summary
 */
public abstract class Metric<M extends Metric, C extends Metric.Child, P extends Metric.Partial> {
  static final String SERIALIZE_BASE_LABELS = "baseLabels";
  static final String SERIALIZE_DOCSTRING = "docstring";
  static final String SERIALIZE_METRIC = "metric";

  private final List<String> labelNames;
  private final Metrics.MetricFamily partial;

  final String name;
  final String docstring;
  final ConcurrentHashMap<Map<String, String>, C> children =
      new ConcurrentHashMap<Map<String, String>, C>();

  protected Metric(final String n, final String d, final List<String> ds,
      final Metrics.MetricFamily p, final boolean rs) {
    name = n;
    docstring = d;
    labelNames = ds;
    partial = p;

    if (rs) {
      Prometheus.defaultRegister(this);
    }
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

  /**
   * <p>
   * Instruct this {@link Metric} to purge all {@link Child}, meaning all
   * accreted label pairs are forgotten unless re-instantiated.
   * </p>
   */
  private void disbandChildren() {
    // BUG(matt): Document this entirely and harden orphaned child cases
    // more thoroughly.
    children.clear();
  }

  @ThreadSafe
  public static interface Builder<B, M> {
    /**
     * <p>
     * Associate this metric with a namespace.
     * </p>
     *
     * <p>
     * A namespace is the ownership scope of a metric, such as an organization,
     * company, or work group.
     * </p>
     *
     * <p>
     * For example, you have provided {@link Metric}'s
     * {@link Metric.BaseBuilder} with the following parameters:
     *
     * <ul>
     * <li>
     * {@code namespace = "seaworld"}
     * </li>
     * <li>
     * {@code subsystem = "water_heaters"}</li>
     * <li>
     * {@code name = "efficiency_percentage"}</li>
     * </ul>
     *
     * The {@link Metric}'s naming system generates the following composite
     * metric name: {@code seaworld_water_heaters_efficiency_percentage}. The
     * namespace is <em>seaworld</em>.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public B namespace(String namespace);

    /**
     * <p>
     * Associate this metric with a subsystem.
     * </p>
     *
     * <p>
     * A subsystem is a collection of systems that perform related units of work
     * that might have multiple aspects against which they are measured.
     * </p>
     *
     * <p>
     * For example, you have provided {@link Metric}'s
     * {@link Metric.BaseBuilder} with the following parameters:
     *
     * <ul>
     * <li>
     * {@code namespace = "seaworld"}</li>
     * <li>
     * {@code subsystem = "water_heaters"}</li>
     * <li>
     * {@code name = "efficiency_percentage"}</li>
     * </ul>
     *
     * The {@link Metric}'s naming system generates the following composite
     * metric name: {@code seaworld_water_heaters_efficiency_percentage} . The
     * subsystem is <em>water_heaters</em>.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public B subsystem(String subsystem);

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
     * For example, you have provided {@link Metric}'s
     * {@link Metric.BaseBuilder} with the following parameters:
     *
     * <ul>
     * <li>
     * {@code namespace = "seaworld"}</li>
     * <li>
     * {@code subsystem = "water_heaters"}</li>
     * <li>
     * {@code name = "efficiency_percentage"}</li>
     * </ul>
     *
     * The {@link Metric}'s naming system generates the following composite
     * metric name: {@code seaworld_water_heaters_efficiency_percentage} . The
     * name is <em>efficiency_percentage</em>.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public B name(String name);

    /**
     * <p>
     * <em>Required:</em> Assign a human-readable documentation string to this
     * metric.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public B documentation(String documentation);

    /**
     * <p>
     * Declare this metric's label <em>names</em>.
     * </p>
     *
     * <p>
     * A label is used as a facet for aggregation or pivoting in data. If you
     * were a census technician building a system to show the current number of
     * people in a given postal code at a given time, you could create a label
     * called {@code "postal_code"}.
     * </p>
     *
     * <p>
     * <em>Important:</em> Children of this metric must be instantiated with all
     * of the declared labels. Failure to do so will result in a runtime error
     * due to a programming error.
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public B labelNames(String... labelNames);

    /**
     * <p>
     * Instructs Prometheus to register this metric upon its creation.
     * </p>
     *
     * <p>
     * <em>Important:</em> This defaults to true. If more than one metric of the
     * same (full name, docstring) is registered, the first one wins.
     * </p>
     *
     * <p>
     * The following are use cases for setting this to {@code true}:
     * <ul>
     * <li>
     * Runtimes where <em>runtime-retained annotations neither respected
     * nor supported</em>, meaning {@link io.prometheus.client.Register} cannot
     * be used and you wish to not explicitly register the metrics yourself.</li>
     * </ul>
     * </p>
     * <p>
     * The following are use cases for setting this to {@code false}:
     * <ul>
     * <li>
     * Preventing static side-effects from tests.</li>
     * <li>
     * Respecting dependency injection paradigms. Be sure to explicitly use
     * {@link Prometheus#defaultRegister(Metric)}.</li>
     * </ul>
     * </p>
     *
     * @return A <em>copy</em> of the original {@link Builder} with the new
     *         target value.
     */
    public B registerStatic(boolean registerStatic);

    /**
     * <p>
     * Generate a concrete {@link M} from this {@link Builder}.
     * </p>
     */
    M build();
  }

  static class BaseBuilder {
    private final boolean DEFAULT_REGISTER_STATIC = true;

    protected final Optional<String> namespace;
    protected final Optional<String> subsystem;
    protected final Optional<String> name;
    protected final Optional<String> d;
    protected final Optional<Boolean> registerStatic;
    protected final List<String> labelNames;

    BaseBuilder() {
      namespace = Optional.absent();
      subsystem = Optional.absent();
      name = Optional.absent();
      d = Optional.absent();
      registerStatic = Optional.absent();
      labelNames = new ArrayList<String>();
    }

    private BaseBuilder(final Optional<String> namespace, final Optional<String> subsystem,
        final Optional<String> name, final Optional<String> d,
        final Optional<Boolean> registerStatic, final List<String> labelNames) {
      this.namespace = namespace;
      this.subsystem = subsystem;
      this.name = name;
      this.d = d;
      this.registerStatic = registerStatic;
      this.labelNames = labelNames;
    }

    BaseBuilder namespace(final String ns) {
      return new BaseBuilder(Optional.of(ns), subsystem, name, d, registerStatic, labelNames);
    }

    BaseBuilder subsystem(final String ss) {
      return new BaseBuilder(namespace, Optional.of(ss), name, d, registerStatic, labelNames);
    }

    BaseBuilder name(final String n) {
      return new BaseBuilder(namespace, subsystem, Optional.of(n), d, registerStatic, labelNames);
    }

    BaseBuilder documentation(final String d) {
      return new BaseBuilder(namespace, subsystem, name, Optional.of(d), registerStatic, labelNames);
    }

    BaseBuilder labelNames(final String... ds) {
      final List<String> labels = new ArrayList(labelNames);
      labels.addAll(Arrays.asList(ds));

      return new BaseBuilder(namespace, subsystem, name, d, registerStatic, labels);
    }

    BaseBuilder registerStatic(final boolean rs) {
      return new BaseBuilder(namespace, subsystem, name, d, Optional.of(rs), labelNames);
    }

    boolean getRegisterStatic() {
      return registerStatic.or(DEFAULT_REGISTER_STATIC);
    }

    String buildName() {
      Preconditions.checkArgument(name.isPresent(), "name may not be empty");

      if (!(namespace.isPresent() || subsystem.isPresent())) {
        return name.get();
      } else if (namespace.isPresent() && subsystem.isPresent()) {
        return String.format("%s_%s_%s", namespace.get(), subsystem.get(), name.get());
      }

      if (namespace.isPresent()) {
        return String.format("%s_%s", namespace.get(), name.get());
      }

      return String.format("%s_%s", subsystem.get(), name.get());
    }

    String buildDocstring() {
      Preconditions.checkArgument(d.isPresent(), "docstring may not be empty");

      return d.get();
    }

    List<String> buildLabelNames() {
      return labelNames;
    }
  }

  /**
   * <p>
   * Create a new {@link P}.
   * </p>
   *
   * @see Metric.Partial
   */
  public abstract P newPartial();

  /**
   * <p>
   * {@link Partial} is an <em>incomplete incarnation</em> of a
   * {@link Metric.Child} that you add label value pair registerStatic to with
   * {@link #labelPair(String, String)}. They are used to provide measurements
   * in a trace-like fashion where the outcomes are not known a priori, and the
   * outcomes affect what label pairs the metric shall have.
   * </p>
   *
   * <p>
   * An example follows:
   * </p>
   *
   * <code>
   * <pre>
   * public class InvitationHandler {
   *   public static Summary latencies =
   *       Summary
   *           .newBuilder()
   *           .name(&quot;request_latency_ms&quot;)
   *           // There are three distinct registerStatic we care about:
   *           // - operation: What type of operation are we handling?
   *           // - result: What was its outcome?
   *           // - shard: What remote storage shard was used in answering this
   *           // request?
   *           .registerStatic(&quot;operation&quot;, &quot;result&quot;, &quot;shard&quot;)
   *           .documentation(
   *               &quot;Latency quantiles for requests partitioned by 'operation' type, 'result' disposition, and storage 'shard' name.&quot;)
   *           .build();
   *
   *   public void handleCreate(CreateReq r) {
   *     Summary.Partial op = latencies.newPartial().labelPair(&quot;operation&quot;, &quot;create&quot;);
   *     long start = System.currentTimeMillis();
   *
   *     try {
   *       doCreate(shard, r);
   *       op.labelPair(&quot;result&quot;, &quot;success&quot;);
   *     } catch (StorageException e) {
   *       op.labelPair(&quot;result&quot;, &quot;storage_failure&quot;);
   *     } catch (RuntimeException e) {
   *       op.labelPair(&quot;result&quot;, &quot;unknown_error&quot;);
   *     } finally {
   *       op.apply().observe(System.currentTimeMillis() - start);
   *     }
   *   }
   *
   *   private void doCreate(CreateReq r, Summary.Partial t) throws StorageException {
   *     String shard = shardMap.getForReq(r);
   *     op.registerStatic(&quot;shard&quot;, shard);
   *     // Do our work: Create the entity in the remote shard.
   *   }
   *
   *   public void handleDelete(DeleteReq r) {
   *     Summary.Partial op = latencies.newPartial().registerStatic(&quot;operation&quot;, &quot;delete&quot;);
   *     long start = System.currentTimeMillis();
   *
   *     try {
   *       doDelete(shard, r);
   *       op.registerStatic(&quot;result&quot;, &quot;success&quot;);
   *     } catch (StorageException e) {
   *       op.registerStatic(&quot;result&quot;, &quot;storage_failure&quot;);
   *     } catch (RuntimeException e) {
   *       op.registerStatic(&quot;result&quot;, &quot;unknown_error&quot;);
   *     } finally {
   *       op.apply().observe(System.currentTimeMillis() - start);
   *     }
   *   }
   *
   *   private void doDelete(deleteReq r, Summary.Partial t) throws StorageException {
   *     String shard = shardMap.getForReq(r);
   *     op.registerStatic(&quot;shard&quot;, shard);
   *     // Do our work: delete the entity in the remote shard.
   *   }
   *
   *   public static class CreateReq {}
   *
   *   public static class DeleteReq {}
   * }
   * </pre>
   * </code>
   *
   * <p>
   * Assuming each code path is hit twice, {@code latencies} could yield the
   * following child metrics:
   * </p>
   * <code>
   * <pre>
   *   request_latency_ms{operation="create", result="success", shard="a", quantile="0.5"}            = ?
   *   request_latency_ms{operation="create", result="success", shard="a", quantile="0.99"}           = ?
   *   request_latency_ms{operation="create", result="success", shard="b", quantile="0.5"}            = ?
   *   request_latency_ms{operation="create", result="success", shard="b", quantile="0.99"}           = ?
   *   request_latency_ms{operation="create", result="storage_failure", shard="a", quantile="0.5"}    = ?
   *   request_latency_ms{operation="create", result="storage_failure", shard="a", quantile="0.99"}   = ?
   *   request_latency_ms{operation="create", result="storage_failure", shard="b", quantile="0.5"}    = ?
   *   request_latency_ms{operation="create", result="storage_failure", shard="b", quantile="0.99"}   = ?
   *   request_latency_ms{operation="create", result="unknown_error", shard="a", quantile="0.5"}      = ?
   *   request_latency_ms{operation="create", result="unknown_error", shard="a", quantile="0.99"}     = ?
   *   request_latency_ms{operation="create", result="unknown_error", shard="b", quantile="0.5"}      = ?
   *   request_latency_ms{operation="create", result="unknown_error", shard="b", quantile="0.99"}     = ?
   *   request_latency_ms{operation="delete", result="success", shard="a", quantile="0.5"}            = ?
   *   request_latency_ms{operation="delete", result="success", shard="a", quantile="0.99"}           = ?
   *   request_latency_ms{operation="delete", result="success", shard="b", quantile="0.5"}            = ?
   *   request_latency_ms{operation="delete", result="success", shard="b", quantile="0.99"}           = ?
   *   request_latency_ms{operation="delete", result="storage_failure", shard="a", quantile="0.5"}    = ?
   *   request_latency_ms{operation="delete", result="storage_failure", shard="a", quantile="0.99"}   = ?
   *   request_latency_ms{operation="delete", result="storage_failure", shard="b", quantile="0.5"}    = ?
   *   request_latency_ms{operation="delete", result="storage_failure", shard="b", quantile="0.99"}   = ?
   *   request_latency_ms{operation="delete", result="unknown_error", shard="a", quantile="0.5"}      = ?
   *   request_latency_ms{operation="delete", result="unknown_error", shard="a", quantile="0.99"}     = ?
   *   request_latency_ms{operation="delete", result="unknown_error", shard="b", quantile="0.5"}      = ?
   *   request_latency_ms{operation="delete", result="unknown_error", shard="b", quantile="0.99"}     = ?
   * </pre>
   * </code>
   *
   * <p>
   * {@code latencies} also yields supplemental synthetic children:
   * </p>
   * <code>
   * <pre>
   *   request_latency_ms_count{operation="create", result="success", shard="a"}         = 1
   *   request_latency_ms_count{operation="create", result="success", shard="b"}         = 1
   *   request_latency_ms_count{operation="create", result="storage_failure", shard="a"} = 1
   *   request_latency_ms_count{operation="create", result="storage_failure", shard="b"} = 1
   *   request_latency_ms_count{operation="create", result="unknown_error", shard="a"}   = 1
   *   request_latency_ms_count{operation="create", result="unknown_error", shard="b"}   = 1
   *   request_latency_ms_count{operation="delete", result="success", shard="a"}         = 1
   *   request_latency_ms_count{operation="delete", result="success", shard="b"}         = 1
   *   request_latency_ms_count{operation="delete", result="storage_failure", shard="a"} = 1
   *   request_latency_ms_count{operation="delete", result="storage_failure", shard="b"} = 1
   *   request_latency_ms_count{operation="delete", result="unknown_error", shard="a"}   = 1
   *   request_latency_ms_count{operation="delete", result="unknown_error", shard="b"}   = 1
   *
   *   request_latency_ms_sum{operation="create", result="success", shard="a"}         = ?
   *   request_latency_ms_sum{operation="create", result="success", shard="b"}         = ?
   *   request_latency_ms_sum{operation="create", result="storage_failure", shard="a"} = ?
   *   request_latency_ms_sum{operation="create", result="storage_failure", shard="b"} = ?
   *   request_latency_ms_sum{operation="create", result="unknown_error", shard="a"}   = ?
   *   request_latency_ms_sum{operation="create", result="unknown_error", shard="b"}   = ?
   *   request_latency_ms_sum{operation="delete", result="success", shard="a"}         = ?
   *   request_latency_ms_sum{operation="delete", result="success", shard="b"}         = ?
   *   request_latency_ms_sum{operation="delete", result="storage_failure", shard="a"} = ?
   *   request_latency_ms_sum{operation="delete", result="storage_failure", shard="b"} = ?
   *   request_latency_ms_sum{operation="delete", result="unknown_error", shard="a"}   = ?
   *   request_latency_ms_sum{operation="delete", result="unknown_error", shard="b"}   = ?
   * </pre>
   * </code>
   * <p>
   * <em>Important</em>:
   * <ul>
   * <li>
   * If there is a mismatch between the number of labels that have been
   * accumulated with {@link #labelPair(String, String)} and those defined in
   * the underlying {@code Builder#registerStatic}, a runtime exception will
   * occur, signifying illegal use.</li>
   * </ul>
   * </p>
   */
  @NotThreadSafe
  public abstract class Partial {
    private final Map<String, String> dimensions = new HashMap<String, String>();

    protected Partial() {}

    /**
     * <p>
     * Attach label-value pairs to this {@link Partial}.
     * </p>
     */
    public abstract P labelPair(final String labelName, final String labelValue);

    P baseLabelPair(final String labelName, final String labelValue) {
      dimensions.put(labelName, labelValue);

      return (P) this;
    }

    /**
     * <p>
     * Duplicate an existing {@link Partial} to create another metric
     * altogether.
     * </p>
     */
    public P clone() {
      final P clone = newPartial();
      for (final String name : dimensions.keySet()) {
        final String value = dimensions.get(name);
        clone.labelPair(name, value);
      }

      return clone;
    }

    private Map<String, String> validate() {
      final Map<String, String> ds = Collections.unmodifiableMap(dimensions);
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

    /**
     * <p>
     * Instantiates a concrete metric of {@link C} with the attached label-value
     * pairs.
     * </p>
     */
    public abstract C apply();

    C baseApply() {
      final TreeMap<String, String> t = new TreeMap<String, String>(validate());
      final C v = children.putIfAbsent(t, newChild());
      if (v != null) {
        return v;
      }
      return children.get(t);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      try {
        final Partial partial = (Partial) o;

        if (!dimensions.equals(partial.dimensions)) return false;

        return true;

      } catch (final ClassCastException unused) {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return dimensions.hashCode();
    }
  }
  /**
   * <p>
   * {@link Child} is a concrete metric, the thing you mutate.
   * </p>
   *
   * <p>
   * <em>Warning:</em> Do not hold onto a reference of a {@link Child} if you
   * ever use the {@link #resetAll()} or
   * {@link io.prometheus.client.metrics.Metric.Child#reset()} tools. This will
   * be fixed in a follow-up release.
   * </p>
   */
  public static interface Child {
    void reset();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Metric)) return false;

    final Metric metric = (Metric) o;

    if (!labelNames.equals(metric.labelNames)) return false;
    if (!docstring.equals(metric.docstring)) return false;
    if (!name.equals(metric.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = labelNames.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + docstring.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("Metric{name='%s', registerStatic=%s}", name, labelNames);
  }
}
