package io.prometheus.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.List;

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


  private final Map<Collector, List<String>> collectorsToNames = new HashMap<Collector, List<String>>();
  private final Map<String, Collector> namesToCollectors = new HashMap<String, Collector>();

  private final boolean autoDescribe;

  public CollectorRegistry(){
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
    synchronized (collectorsToNames) {
      for (String name : names) {
        if(namesToCollectors.containsKey(name)) {
          throw new IllegalArgumentException("Collector already registered that provides name: " + name);
        }
      }
      for (String name : names) {
        namesToCollectors.put(name, m);
      }
      collectorsToNames.put(m, names);
    }
  }

  /**
   * Unregister a Collector.
   */
  public void unregister(Collector m) {
    synchronized (collectorsToNames) {
      for (String name : collectorsToNames.get(m)) {
        namesToCollectors.remove(name);
      }
      collectorsToNames.remove(m);
    }
  }
  /**
   * Unregister all Collectors.
   */
  public void clear() {
    synchronized (collectorsToNames) {
      collectorsToNames.clear();
      namesToCollectors.clear();
    }
  }

  /**
   * A snapshot of the current collectors.
   */
  private Set<Collector> collectors() {
    synchronized (collectorsToNames) {
      return new HashSet(collectorsToNames.keySet());
    }
  }

  private List<String> collectorNames(Collector m) {
    List<Collector.MetricFamilySamples> mfs;
    if (m instanceof Collector.Describable) {
      mfs = ((Collector.Describable)m).describe();
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
        case HISTOGRAM:
          names.add(family.name + "_count");
          names.add(family.name + "_sum");
          names.add(family.name + "_bucket");
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
  class MetricFamilySamplesEnumeration implements Enumeration<Collector.MetricFamilySamples> {

    private final Iterator<Collector> collectorIter = collectors().iterator();
    private Iterator<Collector.MetricFamilySamples> metricFamilySamples;
    private Collector.MetricFamilySamples next;

    MetricFamilySamplesEnumeration() {
      findNextElement();
    }

    private void findNextElement() {
      if (metricFamilySamples != null && metricFamilySamples.hasNext()) {
        next = metricFamilySamples.next();
      } else {
        while (collectorIter.hasNext()) {
          metricFamilySamples = collectorIter.next().collect().iterator();
          if (metricFamilySamples.hasNext()) {
            next = metricFamilySamples.next();
            return;
          }
        }
        next = null;
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
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(metricFamilySamples())) {
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
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
