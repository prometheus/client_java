package io.prometheus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import org.junit.Test;
import org.junit.Before;


public class CollectorRegistryTest {

  CollectorRegistry registry;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
  }

  @Test
  public void testRegisteredCollectorsAreCollected() {
    Collector g = Gauge.build().name("g").help("h").register(registry);
    List<Collector.MetricFamilySamples> mfs = Collections.list(registry.metricFamilySamples());
    assertEquals(1, mfs.size());
    assertEquals("g", mfs.get(0).name);
  }

  @Test
  public void testUnregister() {
    Collector g = Gauge.build().name("g").help("h").register(registry);
    Collector c = Counter.build().name("c").help("h").register(registry);
    List<Collector.MetricFamilySamples> mfs = Collections.list(registry.metricFamilySamples());
    assertEquals(2, mfs.size());
    registry.unregister(g);
    mfs = Collections.list(registry.metricFamilySamples());
    assertEquals(1, mfs.size());
    assertEquals("c", mfs.get(0).name);
  }

  @Test
  public void testClear() {
    Collector g = Gauge.build().name("g").help("h").register(registry);
    Collector c = Counter.build().name("c").help("h").register(registry);
    List<Collector.MetricFamilySamples> mfs = Collections.list(registry.metricFamilySamples());
    assertEquals(2, mfs.size());
    registry.clear();
    mfs = Collections.list(registry.metricFamilySamples());
    assertEquals(0, mfs.size());
  }

  class EmptyCollector extends Collector {
    public MetricFamilySamples[] collect(){
      return new MetricFamilySamples[]{};
    }
  }

  @Test
  public void testMetricFamilySamples() {
    Collector g = Gauge.build().name("g").help("h").register(registry);
    Collector c = Counter.build().name("c").help("h").register(registry);
    Collector s = Summary.build().name("s").help("h").register(registry);
    Collector ec = new EmptyCollector().register(registry);
    HashSet<String> names = new HashSet<String>();
    for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(registry.metricFamilySamples())) {
      names.add(metricFamilySamples.name);
    }
    assertEquals(new HashSet<String>(Arrays.asList("g", "c", "s")), names);
  }

  @Test
  public void testEmptyRegistryHasNoMoreElements() {
    assertFalse(registry.metricFamilySamples().hasMoreElements());
  }

  @Test
  public void testRegistryWithEmptyCollectorHasNoMoreElements() {
    registry.register(new EmptyCollector());
    assertFalse(registry.metricFamilySamples().hasMoreElements());
  }
  
}
