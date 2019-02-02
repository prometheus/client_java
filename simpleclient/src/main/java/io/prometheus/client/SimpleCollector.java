package io.prometheus.client;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Arrays;
import java.util.List;

/**
 * Common functionality for {@link Gauge}, {@link Counter}, {@link Summary} and {@link Histogram}.
 * <p>
 * This class handles common initialization and label logic for the standard metrics.
 * You should never subclass this class.
 * <p>
 * <h2>Initialization</h2>
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
 * {@link Gauge}, {@link Counter} and {@link Summary} all offer convenience methods to avoid needing to call
 * {@link #labels} for metrics with no labels.
 * <p>
 * {@link #remove} and {@link #clear} can be used to remove children.
 * <p>
 * <em>Warning #1:</em> Metrics that don't always export something are difficult to monitor, if you know in advance
 * what labels will be in use you should initialise them be calling {@link #labels}.
 * This is done for you for metrics with no labels.
 * <p>
 * <em>Warning #2:</em> While labels are very powerful, avoid overly granular metric labels. 
 * The combinatorial explosion of breaking out a metric in many dimensions can produce huge numbers
 * of timeseries, which will then take longer and more resources to process.
 * <p>
 * As a rule of thumb aim to keep the cardinality of metrics below ten, and limit where the
 * cardinality exceeds that value. For example rather than breaking out latency
 * by customer and endpoint in one metric, you might have two metrics with one breaking out
 * by each. If the cardinality is in the hundreds, you may wish to consider removing the breakout
 * by one of the dimensions altogether.
 */
public abstract class SimpleCollector<Child> extends Collector {
  protected final String fullname;
  protected final String help;
  protected final List<String> labelNames;

  protected final ConcurrentMap<List<String>, Child> children = new ConcurrentHashMap<List<String>, Child>();
  protected Child noLabelsChild;

  /**
   * Return the Child with the given labels, creating it if needed.
   * <p>
   * Must be passed the same number of labels are were passed to {@link #labelNames}.
   */
  public Child labels(String... labelValues) {
    if (labelValues.length != labelNames.size()) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    for (String label: labelValues) {
      if (label == null) {
        throw new IllegalArgumentException("Label cannot be null.");
      }
    }
    List<String> key = Arrays.asList(labelValues);
    Child c = children.get(key);
    if (c != null) {
      return c;
    }
    Child c2 = newChild();
    Child tmp = children.putIfAbsent(key, c2);
    return tmp == null ? c2 : tmp;
  }

  /**
   * Remove the Child with the given labels.
   * <p>
   * Any references to the Child are invalidated.
   */
  public void remove(String... labelValues) {
    children.remove(Arrays.asList(labelValues));
    initializeNoLabelsChild();
  }
  
  /**
   * Remove all children.
   * <p>
   * Any references to any children are invalidated.
   */
  public void clear() {
    children.clear();
    initializeNoLabelsChild();
  }
  
  /**
   * Initialize the child with no labels.
   */
  protected void initializeNoLabelsChild() {
    // Initialize metric if it has no labels.
    if (labelNames.size() == 0) {
      noLabelsChild = labels();
    }
  }

  /**
   * Replace the Child with the given labels.
   * <p>
   * This is intended for advanced uses, in particular proxying metrics
   * from another monitoring system. This allows for callbacks for returning
   * values for {@link Counter} and {@link Gauge} without having to implement
   * a full {@link Collector}.
   * <p>
   * An example with {@link Gauge}:
   * <pre>
   * {@code
   *   Gauge.build().name("current_time").help("Current unixtime.").create()
   *       .setChild(new Gauge.Child() {
   *         public double get() {
   *           return System.currentTimeMillis() / MILLISECONDS_PER_SECOND;
   *         }
   *       }).register();
   * }
   * </pre>
   * <p>
   * Any references any previous Child with these labelValues are invalidated. 
   * A metric should be either all callbacks, or none.
   */
  public <T extends Collector> T setChild(Child child, String... labelValues) {
    if (labelValues.length != labelNames.size()) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    children.put(Arrays.asList(labelValues), child);
    return (T)this;
  }

  /**
   * Return a new child, workaround for Java generics limitations.
   */
  protected abstract Child newChild();

  protected List<MetricFamilySamples> familySamplesList(Collector.Type type, List<MetricFamilySamples.Sample> samples) {
    MetricFamilySamples mfs = new MetricFamilySamples(fullname, type, help, samples);
    List<MetricFamilySamples> mfsList = new ArrayList<MetricFamilySamples>(1);
    mfsList.add(mfs);
    return mfsList;
  }

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
    checkMetricName(fullname);
    if (b.help != null && b.help.isEmpty()) throw new IllegalStateException("Help hasn't been set.");
    help = b.help;
    labelNames = Arrays.asList(b.labelNames);

    for (String n: labelNames) {
      checkMetricLabelName(n);
    }

    if (!b.dontInitializeNoLabelsChild) {
      initializeNoLabelsChild();
    }
  }

  /**
   * Builders let you configure and then create collectors.
   */
  public abstract static class Builder<B extends Builder<B, C>, C extends SimpleCollector> {
    String namespace = "";
    String subsystem = "";
    String name = "";
    String fullname = "";
    String help = "";
    String[] labelNames = new String[]{};
    // Some metrics require additional setup before the initialization can be done.
    boolean dontInitializeNoLabelsChild;

    /**
     * Set the name of the metric. Required.
     */
    public B name(String name) {
      this.name = name;
      return (B)this;
    }
    /**
     * Set the subsystem of the metric. Optional.
     */
    public B subsystem(String subsystem) {
      this.subsystem = subsystem;
      return (B)this;
    }
    /**
     * Set the namespace of the metric. Optional.
     */
    public B namespace(String namespace) {
      this.namespace = namespace;
      return (B)this;
    }
    /**
     * Set the help string of the metric. Required.
     */
    public B help(String help) {
      this.help = help;
      return (B)this;
    }
    /**
     * Set the labelNames of the metric. Optional, defaults to no labels.
     */
    public B labelNames(String... labelNames) {
      this.labelNames = labelNames;
      return (B)this;
    }

    /**
     * Return the constructed collector.
     * <p>
     * Abstract due to generics limitations.
     */
    public abstract C create();

    /**
     * Create and register the Collector with the default registry.
     */
    public C register() {
      return register(CollectorRegistry.defaultRegistry);
    }

    /**
     * Create and register the Collector with the given registry.
     */
    public C register(CollectorRegistry registry) {
      C sc = create();
      registry.register(sc);
      return sc;
    }
  }
}
