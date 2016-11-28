package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CounterMetricFamilyTest {

  CollectorRegistry registry;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
  }

  @Test
  public void testJavadocExample() {
    class YourCustomCollector extends Collector {
      public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        // With no labels.
        mfs.add(new CounterMetricFamily("my_counter", "help", 42));
        // With labels
        CounterMetricFamily labeledCounter = new CounterMetricFamily("my_other_counter", "help", Arrays.asList("labelname"));
        labeledCounter.addMetric(Arrays.asList("foo"), 4);
        labeledCounter.addMetric(Arrays.asList("bar"), 5);
        mfs.add(labeledCounter);
        return mfs;
      }
    }
    new YourCustomCollector().register(registry);

    assertEquals(42.0, registry.getSampleValue("my_counter").doubleValue(), .001);
    assertEquals(null, registry.getSampleValue("my_other_counter"));
    assertEquals(4.0, registry.getSampleValue("my_other_counter", new String[]{"labelname"}, new String[]{"foo"}).doubleValue(), .001);
    assertEquals(5.0, registry.getSampleValue("my_other_counter", new String[]{"labelname"}, new String[]{"bar"}).doubleValue(), .001);
  }

}
