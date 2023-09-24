package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
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

    assertEquals(42.0, registry.getSampleValue("my_counter_total").doubleValue(), .001);
    assertEquals(null, registry.getSampleValue("my_other_counter_total"));
    assertEquals(4.0, registry.getSampleValue("my_other_counter_total", new String[]{"labelname"}, new String[]{"foo"}).doubleValue(), .001);
    assertEquals(5.0, registry.getSampleValue("my_other_counter_total", new String[]{"labelname"}, new String[]{"bar"}).doubleValue(), .001);
  }

  @Test
  public void testBuilderStyleUsage() {
    class YourCustomCollector extends Collector {
      public List<MetricFamilySamples> collect() {
        return Arrays.<MetricFamilySamples>asList(
            new CounterMetricFamily("my_metric", "help", Arrays.asList("name"))
                .addMetric(Arrays.asList("value1"), 1.0)
                .addMetric(Arrays.asList("value2"), 2.0)
        );
      }
    }
    new YourCustomCollector().register(registry);

    assertEquals(1.0,
        registry.getSampleValue("my_metric_total", new String[]{"name"}, new String[]{"value1"})
            .doubleValue(), .001);
    assertEquals(2.0,
        registry.getSampleValue("my_metric_total", new String[]{"name"}, new String[]{"value2"})
            .doubleValue(), .001);
  }

  @Test
  public void testTotalHandling() {
    class YourCustomCollector extends Collector {
      public List<MetricFamilySamples> collect() {
        return Arrays.<MetricFamilySamples>asList(
            new CounterMetricFamily("a_total", "help", Arrays.asList("name"))
                .addMetric(Arrays.asList("value"), 1.0),
            new CounterMetricFamily("b", "help", Arrays.asList("name"))
                .addMetric(Arrays.asList("value"), 2.0),
            new CounterMetricFamily("c_total", "help", 3.0),
            new CounterMetricFamily("d_total", "help", 4.0)
        );
      }
    }
    new YourCustomCollector().register(registry);

    assertEquals(1.0,
        registry.getSampleValue("a_total", new String[]{"name"}, new String[]{"value"})
            .doubleValue(), .001);
    assertEquals(2.0,
        registry.getSampleValue("b_total", new String[]{"name"}, new String[]{"value"})
            .doubleValue(), .001);
    assertEquals(3.0, registry.getSampleValue("c_total").doubleValue(), .001);
    assertEquals(4.0, registry.getSampleValue("d_total").doubleValue(), .001);
  }

}
