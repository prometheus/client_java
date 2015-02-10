package io.prometheus.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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
  public static final CollectorRegistry defaultRegistry = new CollectorRegistry();

  private final Set<Collector> collectors = 
      Collections.newSetFromMap(new ConcurrentHashMap<Collector, Boolean>());

  /**
   * Register a Collector.
   * </p>
   * A collector can be registered to multiple CollectorRegistries.
   */
  public void register(Collector m) {
    collectors.add(m);
  }
  
  /**
   * Unregister a Collector.
   */
  public void unregister(Collector m) {
    collectors.remove(m);
  }
  /**
   * Unregister all Collectors.
   */
  public void clear() {
    collectors.clear();
  }
 
  /**
   * Enumeration of metrics of all registered collectors.
   */
  public Enumeration<Collector.MetricFamilySamples> metricFamilySamples() {
    return new MetricFamilySamplesEnumeration();
  }
  class MetricFamilySamplesEnumeration implements Enumeration<Collector.MetricFamilySamples> {

    private final Iterator<Collector> collectorIter = collectors.iterator();
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
   * </p>
   * This is inefficient, and intended only for use in unittests.
   */
  public Double getSampleValue(String name) {
    return getSampleValue(name, new String[]{}, new String[]{});
  }

  /**
   * Returns the given value, or null if it doesn't exist.
   * </p>
   * This is inefficient, and intended only for use in unittests.
   */
  public Double getSampleValue(String name, String[] labelNames, String[] labelValues) {
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(metricFamilySamples())) {
      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        if (sample.name.equals(name)
            && Arrays.equals(sample.labelNames.toArray(), labelNames)
            && Arrays.equals(sample.labelValues.toArray(), labelValues)) {
          return new Double(sample.value);
        }
      }
    }
    return null;
  }

}
