package io.prometheus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    public List<MetricFamilySamples> collect(){
      return new ArrayList<MetricFamilySamples>();
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

  @Test
  public void testGettingSamples() {
    Gauge g = Gauge.build().name("g").help("h").labelNames("l1", "l2").register(registry);
    g.labels("l1Value", "l2Value").inc();

    Counter c = Counter.build().name("c").help("h").labelNames("l1", "l2").register(registry);
    c.labels("l1Value", "l2Value").inc();

    Summary s = Summary.build().name("s").help("h").labelNames("l1", "l2").register(registry);
    s.labels("l1Value", "l2Value").observe(5);

    assertEquals(1, registry.listSampleValuesOfPrefix("g").size());
    assertEquals(1, registry.listSampleValuesOfPrefix("c").size());
    assertEquals(2, registry.listSampleValuesOfPrefix("s").size());

    assertEquals((Double)1.0, registry.getSampleValue("g", new String[]{"l1", "l2"}, new String[]{"l1Value", "l2Value"}));
    assertEquals((Double)1.0, registry.getSampleValue("c", new String[]{"l1", "l2"}, new String[]{"l1Value", "l2Value"}));
    assertEquals((Double)1.0, registry.getSampleValue("s_count", new String[]{"l1", "l2"}, new String[]{"l1Value", "l2Value"}));
    assertEquals((Double)5.0, registry.getSampleValue("s_sum", new String[]{"l1", "l2"}, new String[]{"l1Value", "l2Value"}));
  }
  
}
