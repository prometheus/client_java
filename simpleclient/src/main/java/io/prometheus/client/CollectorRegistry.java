package io.prometheus.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A registry of Collectors.
 * <p>
 * The majority of users should use the {@link #defaultRegistry}, rather than instantiating their own.
 * <p>
 * Creating a registry other than the default is primarily useful for unittests, or
 * pushing a subset of metrics to the <a href="https://github.com/prometheus/pushgateway">Pushgateway</a>
 * from batch jobs.
 */
public class CollectorRegistry {
  /**
   * The default registry.
   */
  public static final CollectorRegistry defaultRegistry = new CollectorRegistry(true);


  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<Collector, List<String>> collectorsToNames = new HashMap<Collector, List<String>>();
  private final Map<String, Collector> namesToCollectors = new HashMap<String, Collector>();

  private final boolean autoDescribe;

  public CollectorRegistry() {
    this(false);
  }

  public CollectorRegistry(boolean autoDescribe) {
    this.autoDescribe = autoDescribe;
  }

  /**
   * Register a Collector.
   * <p>
   * A collector can be registered to multiple CollectorRegistries.
   */
  public void register(Collector m) {
    List<String> names = collectorNames(m);
    lock.writeLock().lock();
    try {
      for (String name : names) {
        if (namesToCollectors.containsKey(name)) {
          throw new IllegalArgumentException("Collector already registered that provides name: " + name);
        }
      }
      for (String name : names) {
        namesToCollectors.put(name, m);
      }
      collectorsToNames.put(m, names);
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Unregister a Collector.
   */
  public void unregister(Collector m) {
    lock.writeLock().lock();
    try {
      List<String> names = collectorsToNames.remove(m);
      for (String name : names) {
        namesToCollectors.remove(name);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Unregister all Collectors.
   */
  public void clear() {
    lock.writeLock().lock();
    try {
      collectorsToNames.clear();
      namesToCollectors.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * A snapshot of the current collectors.
   */
  private Set<Collector> collectors() {
    lock.readLock().lock();
    try {
      return new HashSet<Collector>(collectorsToNames.keySet());
    } finally {
      lock.readLock().unlock();
    }
  }

  private List<String> collectorNames(Collector m) {
    List<Collector.MetricFamilySamples> mfs;
    if (m instanceof Collector.Describable) {
      mfs = ((Collector.Describable) m).describe();
    } else if (autoDescribe) {
      mfs = m.collect();
    } else {
      mfs = Collections.emptyList();
    }

    List<String> names = new ArrayList<String>();
    for (Collector.MetricFamilySamples family : mfs) {
      switch (family.type) {
        case SUMMARY:
          names.add(family.name + "_count");
          names.add(family.name + "_sum");
          names.add(family.name);
          break;
        case HISTOGRAM:
          names.add(family.name + "_count");
          names.add(family.name + "_sum");
          names.add(family.name + "_bucket");
          names.add(family.name);
          break;
        default:
          names.add(family.name);
      }
    }
    return names;
  }

  /**
   * Enumeration of metrics of all registered collectors.
   */
  public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
    return new MetricFamilySamplesEnumeration();
  }

  public Enumeration<Collector.MetricFamilySamples> filteredMetricFamilySamples(Set<String> includedNames) {
    return new MetricFamilySamplesEnumeration(includedNames);
  }

  class MetricFamilySamplesEnumeration implements Enumeration<Collector.MetricFamilySamples> {

    private final Iterator<Collector> collectorIter;
    private Iterator<Collector.MetricFamilySamples> metricFamilySamples;
    private Collector.MetricFamilySamples next;
    private Set<String> includedNames;

    MetricFamilySamplesEnumeration(Set<String> includedNames) {
      this.includedNames = includedNames;
      collectorIter = includedCollectorIterator(includedNames);
      findNextElement();
    }

    private Iterator<Collector> includedCollectorIterator(Set<String> includedNames) {
      if (includedNames.isEmpty()) {
        return collectors().iterator();
      } else {
        HashSet<Collector> collectors = new HashSet<Collector>();
        lock.readLock().lock();
        try {
          for (Map.Entry<String, Collector> entry : namesToCollectors.entrySet()) {
            if (includedNames.contains(entry.getKey())) {
              collectors.add(entry.getValue());
            }
          }
        } finally {
          lock.readLock().unlock();
        }

        return collectors.iterator();
      }
    }

    MetricFamilySamplesEnumeration() {
      this(Collections.<String>emptySet());
    }

    private void findNextElement() {
      next = null;

      while (metricFamilySamples != null && metricFamilySamples.hasNext()) {
        next = filter(metricFamilySamples.next());
        if (next != null) {
          return;
        }
      }

      if (next == null) {
        while (collectorIter.hasNext()) {
          metricFamilySamples = collectorIter.next().collect().iterator();
          while (metricFamilySamples.hasNext()) {
            next = filter(metricFamilySamples.next());
            if (next != null) {
              return;
            }
          }
        }
      }
    }

    private Collector.MetricFamilySamples filter(Collector.MetricFamilySamples next) {
      if (includedNames.isEmpty()) {
        return next;
      } else {
        Iterator<Collector.MetricFamilySamples.Sample> it = next.samples.iterator();
        while (it.hasNext()) {
            if (!includedNames.contains(it.next().name)) {
                it.remove();
            }
        }
        if (next.samples.size() == 0) {
          return null;
        }
        return next;
      }
    }

    public Collector.MetricFamilySamples nextElement() {
      Collector.MetricFamilySamples current = next;
      if (current == null) {
        throw new NoSuchElementException();
      }
      findNextElement();
      return current;
    }

    public boolean hasMoreElements() {
      return next != null;
    }
  }

  /**
   * Returns the given value, or null if it doesn't exist.
   * <p>
   * This is inefficient, and intended only for use in unittests.
   */
  public Double getSampleValue(String name) {
    return getSampleValue(name, new String[]{}, new String[]{});
  }

  /**
   * Returns the given value, or null if it doesn't exist.
   * <p>
   * This is inefficient, and intended only for use in unittests.
   */
  public Double getSampleValue(String name, String[] labelNames, String[] labelValues) {
    for (Collector.MetricFamilySamples metricFamilySamples : Collections.list(metricFamilySamples())) {
      for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
        if (sample.name.equals(name)
                && Arrays.equals(sample.labelNames.toArray(), labelNames)
                && Arrays.equals(sample.labelValues.toArray(), labelValues)) {
          return sample.value;
        }
      }
    }
    return null;
  }

}
