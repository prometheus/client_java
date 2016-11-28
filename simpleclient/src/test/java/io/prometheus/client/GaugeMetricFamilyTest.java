package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GaugeMetricFamilyTest {

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
        mfs.add(new GaugeMetricFamily("my_gauge", "help", 42));
        // With labels
        GaugeMetricFamily labeledGauge = new GaugeMetricFamily("my_other_gauge", "help", Arrays.asList("labelname"));
        labeledGauge.addMetric(Arrays.asList("foo"), 4);
        labeledGauge.addMetric(Arrays.asList("bar"), 5);
        mfs.add(labeledGauge);
        return mfs;
      }
    }
    new YourCustomCollector().register(registry);

    assertEquals(42.0, registry.getSampleValue("my_gauge").doubleValue(), .001);
    assertEquals(null, registry.getSampleValue("my_other_gauge"));
    assertEquals(4.0, registry.getSampleValue("my_other_gauge", new String[]{"labelname"}, new String[]{"foo"}).doubleValue(), .001);
    assertEquals(5.0, registry.getSampleValue("my_other_gauge", new String[]{"labelname"}, new String[]{"bar"}).doubleValue(), .001);
  }

}
