package io.prometheus.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.List;

/**
 * Common functionality for {@link Gauge}, {@link Counter} and {@link Summary}.
 * <p>
 * This class handles common initlization and label logic for the standard metrics.
 * You should never need to subclass this class.
 * <p>
 * <h2>Initilization</h2>
 * After calling build() on a subclass, {@link Builder#name(String) name},
 * {@link SimpleCollector.Builder#help(String) help},
 * {@link SimpleCollector.Builder#labelNames(String...) labelNames},
 * {@link SimpleCollector.Builder#namespace(String) namespace} and
 * {@link SimpleCollector.Builder#subsystem(String) subsystem} can be called to configure the Collector.
 * These return <code>this</code> to allow calls to be chained.
 * Once configured, call {@link SimpleCollector.Builder#create create}
 * (which is also called by {@link SimpleCollector.Builder#register register}).
 * <p>
 * The fullname of the metric is <code>namespace_subsystem_name</code>, but only <code>name</code> is required.
 *
 * <h2>Labels</h2>
 * {@link SimpleCollector.Builder#labelNames labelNames} specifies which (if any) labels the metrics will have, and 
 * {@link #labels} returns the Child of the metric that represents that particular set of labels.
 * {@link Gauge}, {@link Counter} and {@link Summary} all offer convienence methods to avoid needing to call
 * {@link #labels} for metrics with no labels.
 * <p>
 * {@link #remove} and {@link #clear} can be used to remove children.
 * <p>
 * <em>Warning #1:</em> Metrics that don't always something are difficult to monitor, if you know in advance
 * what labels will be in use you should initilise them to 0. This is done for you for metrics with no labels.
 * <p>
 * <em>Warning #2:</em> While labels are very powerful, avoid overly granular metric labels. 
 * The combinatorial explosion of breaking out a metric in many dimensions can produce huge numberts
 * of timeseries, which will then take longer and more resources to process.
 * <br/>
 * As a rule of thumb aim to keep the cardinality of metrics below ten, and limit where the
 * cardinality exceeds that value. For example rather than breaking out latency
 * by customer and endpoint in one metric, you might have two metrics with one breaking out
 * by each. If the cardinality is in the hundreds, you may wish to consider removing the breakout
 * by one of the dimensions altogether.
 */
abstract public class SimpleCollector<Child> extends Collector {
  final String fullname;
  final String help;
  final String[] labelNames;

  final ConcurrentHashMap<List<String>, Child> children = new ConcurrentHashMap<List<String>, Child>();;

  /**
   * Return the Child with the given labels, creating it if needed.
   * <p>
   * Must be passed the same number of labels are were passed to {@link #labelNames}.
   */
  public Child labels(String... labelValues) {
    if (labelValues.length != labelNames.length) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    List<String> key = Arrays.asList(labelValues);
    Child c = children.get(key);
    if (c != null) {
      return c;
    }
    children.putIfAbsent(key, newChild());
    return children.get(key);
  }

  /**
   * Remove the Child with the given labels.
   * <p>
   * Any references to the Child are invalidated.
   */
  public void remove(String... labelValues) {
    children.remove(Arrays.asList(labelValues));
  }
  
  /**
   * Remove all children.
   * <p>
   * Any references to any children are invalidated.
   */
  public void clear() {
    children.clear();
  }

  /**
   * Return a new child, workaround for Java generics limitations.
   */
  abstract protected Child newChild();

  protected SimpleCollector(Builder b) {
    if (b.name.isEmpty()) throw new IllegalStateException("Name hasn't been set.");
    String name = b.name;
    if (!b.subsystem.isEmpty()) {
      name = b.subsystem + '_' + name;
    }
    if (!b.namespace.isEmpty()) {
      name = b.namespace + '_' + name;
    }
    fullname = name;
    if (b.help.isEmpty()) throw new IllegalStateException("Help hasn't been set.");
    help = b.help;
    labelNames = b.labelNames;
  }

  /**
   * Builders let you configure and then create collectors.
   */
  abstract public static class Builder {
    String namespace = "";
    String subsystem = "";
    String name = "";
    String fullname = "";
    String help = "";
    String[] labelNames = new String[]{};

    /**
     * Set the name of the metric. Required.
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }
    /**
     * Set the subsystem of the metric. Optional.
     */
    public Builder subsystem(String subsystem) {
      this.subsystem = subsystem;
      return this;
    }
    /**
     * Set the namespace of the metric. Optional.
     */
    public Builder namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }
    /**
     * Set the help string of the metric. Required.
     */
    public Builder help(String help) {
      this.help = help;
      return this;
    }
    /**
     * Set the labelNames of the metric. Optional, defaults to no labels.
     */
    public Builder labelNames(String... labelNames) {
      this.labelNames = labelNames;
      return this;
    }

    /**
     * Return the constructed collector.
     * <p>
     * Abstract due to generics limitations.
     */
    abstract public SimpleCollector create();

    /**
     * Create and register the Collector with the default registry.
     */
    public SimpleCollector register() {
      return register(CollectorRegistry.defaultRegistry);
    }

    /**
     * Create and register the Collector with the given registry.
     */
    public SimpleCollector register(CollectorRegistry registry) {
      SimpleCollector sc = create();
      registry.register(sc);
      return sc;
    }
  }
}
