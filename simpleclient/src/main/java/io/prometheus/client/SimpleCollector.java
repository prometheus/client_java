package io.prometheus.client;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
  protected final int labelCount;
  
  @SuppressWarnings("rawtypes")
  private static final ChildEntry[] EMPTY = new ChildEntry[1]; // one null slot

  @SuppressWarnings("rawtypes")
  private static final AtomicReferenceFieldUpdater<SimpleCollector, ChildEntry[]> UPDATER
    = AtomicReferenceFieldUpdater.newUpdater(
        SimpleCollector.class, ChildEntry[].class, "children");

  // linear-probe table, updated via copy-on-write
  @SuppressWarnings("unchecked")
  private volatile ChildEntry<Child>[] children = EMPTY;

  protected Child noLabelsChild;

  static final class ChildEntry<Child> extends AbstractList<String>
    implements Entry<List<String>, Child> {
      final String[] labels; // should be considered immutable
      final int hashCode;
      final Child child;

      public ChildEntry(String[] labels, int hash, Child child) {
          this.labels = labels;
          this.hashCode = hash;
          this.child = child;
      }
      @Override
      public String get(int index) {
        return labels[index];
      }
      @Override
      public int size() {
        return labels.length;
      }
      @Override
      public List<String> getKey() {
        return this;
      }
      @Override
      public Child getValue() {
        return child;
      }
      @Override
      public Child setValue(Child value) {
        throw new UnsupportedOperationException();
      }
  }
  
  Map.Entry<List<String>,Child>[] children() {
    return children;
  }

  /**
   * @throws IllegalArgumentException for null label
   */
  private static int labelsHashCode(String... labelValues) {
    int hashCode = 1;
    for (String label : labelValues) {
      hashCode = buildHashCode(hashCode, label);
    }
    return hashCode;
  }

  private static int hash(int hashCode, int length) {
    // Multiply by -127, and left-shift to use least bit as part of hash
    // Based on the equivalent function in java.util.IdentityHashMap
    return ((hashCode << 1) - (hashCode << 8)) & (length - 1);
  }

  /**
   * @throws IllegalArgumentException for null label
   */
  private static int buildHashCode(int hashCode, String label) {
    if (label == null) {
      throw new IllegalArgumentException("Label cannot be null.");
    }
    return 31 * hashCode + label.hashCode();
  }

  private static int nextIdx(int i, int len) {
    return (i == len - 1) ? 0 : (i + 1);
  }

  private void validateCount(int count) {
    if (count != labelCount) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
  }

  /**
   * Return the Child with the given labels, creating it if needed.
   * <p>
   * Must be passed the same number of labels as were passed to {@link #labelNames}.
   */
  public Child labels(String... labelValues) {
    validateCount(labelValues.length);

    int hashCode = labelsHashCode(labelValues); // also checks for null values
    final ChildEntry<Child>[] arr = children;
    int arrLen = arr.length, i = hash(hashCode, arrLen);
    for (ChildEntry<Child> ce; (ce = arr[i]) != null; i = nextIdx(i, arrLen)) {
      if (ce.hashCode == hashCode && Arrays.equals(ce.labels, labelValues)) {
        return ce.child;
      }
    }
    return labelsMiss(hashCode, arr, i, labelValues);
  }
  
  public Child labels() {
    validateCount(0);
    return noLabelsChild;
  }

  public Child labels(String v1) {
    validateCount(1);

    int hashCode = buildHashCode(1, v1);
    final ChildEntry<Child>[] arr = children;
    int arrLen = arr.length, i = hash(hashCode, arrLen);
    for (ChildEntry<Child> ce; (ce = arr[i]) != null; i = nextIdx(i, arrLen)) {
      if (v1.equals(ce.labels[0])) {
        return ce.child;
      }
    }
    return labelsMiss(hashCode, arr, i, v1);
  }

  public Child labels(String v1, String v2) {
    validateCount(2);

    int hashCode = buildHashCode(buildHashCode(1, v1), v2);
    final ChildEntry<Child>[] arr = children;
    int arrLen = arr.length, i = hash(hashCode, arrLen);
    for (ChildEntry<Child> ce; (ce = arr[i]) != null; i = nextIdx(i, arrLen)) {
      if (ce.hashCode == hashCode) {
        String[] ls = ce.labels;
        if (ls[0].equals(v1) && ls[1].equals(v2)) {
          return ce.child;
        }
      }
    }
    return labelsMiss(hashCode, arr, i, v1, v2);
  }

  public Child labels(String v1, String v2, String v3) {
    validateCount(3);

    int hashCode = buildHashCode(buildHashCode(buildHashCode(1, v1), v2), v3);
    final ChildEntry<Child>[] arr = children;
    int arrLen = arr.length, i = hash(hashCode, arrLen);
    for (ChildEntry<Child> ce; (ce = arr[i]) != null; i = nextIdx(i, arrLen)) {
      if (ce.hashCode == hashCode) {
        String[] ls = ce.labels;
        if (ls[0].equals(v1) && ls[1].equals(v2) && ls[2].equals(v3)) {
          return ce.child;
        }
      }
    }
    return labelsMiss(hashCode, arr, i, v1, v2, v3);
  }

  public Child labels(String v1, String v2, String v3, String v4) {
    validateCount(4);

    int hashCode = buildHashCode(buildHashCode(buildHashCode(buildHashCode(1, v1), v2), v3), v4);
    final ChildEntry<Child>[] arr = children;
    int arrLen = arr.length, i = hash(hashCode, arrLen);
    for (ChildEntry<Child> ce; (ce = arr[i]) != null; i = nextIdx(i, arrLen)) {
      if (ce.hashCode == hashCode) {
        String[] ls = ce.labels;
        if (ls[0].equals(v1) && ls[1].equals(v2) && ls[2].equals(v3) && ls[3].equals(v4)) {
          return ce.child;
        }
      }
    }
    return labelsMiss(hashCode, arr, i, v1, v2, v3, v4);
  }

  private Child labelsMiss(int hashCode, ChildEntry<Child>[] arr, int pos, String... values) {
    return updateChild(values, hashCode, arr, pos,
        new ChildEntry<Child>(values, hashCode, newChild()));
  }

  /**
   * Multi-purpose used for set, remove and get after not found (cache miss)
   *
   * @param pos -1 for set and remove, otherwise insert (null) position in arr
   * @param newEntry null for remove, otherwise new entry to add
   * @return new/existing Child in case of get, prior Child in case of set/remove
   */
  @SuppressWarnings("unchecked")
  private Child updateChild(String[] values, int hashCode,
      ChildEntry<Child>[] arr, int pos, ChildEntry<Child> newEntry) {
    final boolean set = pos == -1;
    int arrLen = arr.length;
    while (true) {
      final ChildEntry<Child>[] newArr;
      ChildEntry<Child> replacing = null;
      if (pos == -1) {
        pos = hash(hashCode, arrLen);
        while (true) {
          ChildEntry<Child> ce = arr[pos];
          if (ce == null) {
            if (newEntry == null) {
              return null;
            }
            break;
          }
          if (ce.hashCode == hashCode && Arrays.equals(ce.labels, values)) {
            if (set) {
              replacing = ce;
              break;
            }
            return ce.child;
          }
          pos = nextIdx(pos, arrLen);
        }
      }
      boolean resizeNeeded;
      if (newEntry == null | replacing != null) {
        resizeNeeded = false;
      } else {
        int count = 1;
        for (ChildEntry<Child> ce : arr) {
          if (ce != null) {
            count++;
          }
        }
        resizeNeeded = count > arrLen / 2;
      }
      if (resizeNeeded) {
        newArr = new ChildEntry[arrLen * 2];
        int newArrLen = newArr.length;
        newArr[hash(hashCode, newArrLen)] = newEntry;
        for (ChildEntry<Child> ce : arr) {
          if (ce != null & ce != replacing) {
            for (int j = hash(ce.hashCode, newArrLen);; j = nextIdx(j, newArrLen)) {
              if (newArr[j] == null) {
                newArr[j] = ce;
                break;
              }
            }
          }
        }
      } else {
        newArr = Arrays.copyOf(arr, arrLen, ChildEntry[].class);
        newArr[pos] = newEntry;
      }
      if (UPDATER.compareAndSet(this, arr, newArr)) {
        if (set) {
          return replacing != null ? replacing.child : null;
        }
        return newEntry.child;
      }
      arr = children;
      arrLen = arr.length;
      pos = -1;
    }
  }

  /**
   * Remove the Child with the given labels.
   * <p>
   * Any references to the Child are invalidated.
   */
  public void remove(String... labelValues) {
    updateChild(labelValues, labelsHashCode(labelValues), children, -1, null);
    initializeNoLabelsChild();
  }
  
  /**
   * Remove all children.
   * <p>
   * Any references to any children are invalidated.
   */
  public void clear() {
    UPDATER.set(this, EMPTY);
    initializeNoLabelsChild();
  }
  
  /**
   * Initialize the child with no labels.
   */
  protected void initializeNoLabelsChild() {
    // Initialize metric if it has no labels.
    if (labelCount == 0) {
      noLabelsChild = labels(new String[0]);
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
    validateCount(labelValues.length);
    int hashCode = labelsHashCode(labelValues);
    updateChild(labelValues, hashCode, children, -1,
        new ChildEntry<Child>(labelValues, hashCode, child));
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
    if (b.help.isEmpty()) throw new IllegalStateException("Help hasn't been set.");
    help = b.help;
    labelNames = Arrays.asList(b.labelNames);
    labelCount = b.labelNames.length;

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
