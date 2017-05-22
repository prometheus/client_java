package io.prometheus.client;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


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
    public List<MetricFamilySamples> collect() {
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
    for (Collector.MetricFamilySamples metricFamilySamples : Collections.list(registry.metricFamilySamples())) {
      names.add(metricFamilySamples.name);
    }
    assertEquals(new HashSet<String>(Arrays.asList("g", "c", "s")), names);
  }

  @Test
  public void testMetricFamilySamples_filterNames() {
    Collector g = Gauge.build().name("g").help("h").register(registry);
    Collector c = Counter.build().name("c").help("h").register(registry);
    Collector s = Summary.build().name("s").help("h").register(registry);
    Collector ec = new EmptyCollector().register(registry);
    SkippedCollector sr = new SkippedCollector().register(registry);
    PartiallyFilterCollector pfr = new PartiallyFilterCollector().register(registry);
    HashSet<String> metrics = new HashSet<String>();
    HashSet<String> series = new HashSet<String>();
    for (Collector.MetricFamilySamples metricFamilySamples : Collections.list(registry.filteredMetricFamilySamples(
            new HashSet<String>(Arrays.asList("", "s_sum", "c", "part_filter_a", "part_filter_c"))))) {
      metrics.add(metricFamilySamples.name);
      for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
        series.add(sample.name);
      }
    }

    assertEquals(1, sr.collectCallCount);
    assertEquals(2, pfr.collectCallCount);
    assertEquals(new HashSet<String>(Arrays.asList("s", "c", "part_filter_a", "part_filter_c")), metrics);
    assertEquals(new HashSet<String>(Arrays.asList("s_sum", "c", "part_filter_a", "part_filter_c")), series);
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

  @Test(expected = IllegalArgumentException.class)
  public void testCounterAndGaugeWithSameNameThrows() {
    Gauge.build().name("g").help("h").register(registry);
    Counter.build().name("g").help("h").register(registry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCounterAndSummaryWithSameNameThrows() {
    Counter.build().name("s").help("h").register(registry);
    Summary.build().name("s").help("h").register(registry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCounterSumAndSummaryWithSameNameThrows() {
    Counter.build().name("s_sum").help("h").register(registry);
    Summary.build().name("s").help("h").register(registry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHistogramAndSummaryWithSameNameThrows() {
    Histogram.build().name("s").help("h").register(registry);
    Summary.build().name("s").help("h").register(registry);
  }

  @Test
  public void testCanUnAndReregister() {
    Histogram h = Histogram.build().name("s").help("h").create();
    registry.register(h);
    registry.unregister(h);
    registry.register(h);
  }

  class MyCollector extends Collector {
    public List<MetricFamilySamples> collect() {
      List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
      mfs.add(new GaugeMetricFamily("g", "help", 42));
      return mfs;
    }
  }

  @Test
  public void testAutoDescribeDisabledByDefault() {
    CollectorRegistry r = new CollectorRegistry();
    new MyCollector().register(r);
    // This doesn't throw.
    new MyCollector().register(r);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAutoDescribeThrowsOnReregisteringCustomCollector() {
    CollectorRegistry r = new CollectorRegistry(true);
    new MyCollector().register(r);
    new MyCollector().register(r);
  }

  private static class SkippedCollector extends Collector implements Collector.Describable {
    public int collectCallCount = 0;

    @Override
    public List<MetricFamilySamples> collect() {
      collectCallCount++;
      List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
      mfs.add(new GaugeMetricFamily("slow_gauge", "help", 123));
      return mfs;
    }

    @Override
    public List<MetricFamilySamples> describe() {
      return collect();
    }
  }

  private static class PartiallyFilterCollector extends Collector implements Collector.Describable {
    public int collectCallCount = 0;

    @Override
    public List<MetricFamilySamples> collect() {
      collectCallCount++;
      List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
      mfs.add(new GaugeMetricFamily("part_filter_a", "help", 123));
      mfs.add(new GaugeMetricFamily("part_filter_b", "help", 123));
      mfs.add(new GaugeMetricFamily("part_filter_c", "help", 123));
      return mfs;
    }

    @Override
    public List<MetricFamilySamples> describe() {
      return collect();
    }
  }

}
