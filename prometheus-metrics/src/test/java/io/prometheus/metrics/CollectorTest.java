package io.prometheus.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollectorTest {

  @Test
  public void sanitizeMetricPrefix() throws Exception {
      assertEquals("afoo", Collector.sanitizeMetricName("afoo"));
      assertEquals("zfoo", Collector.sanitizeMetricName("zfoo"));
      assertEquals("Afoo", Collector.sanitizeMetricName("Afoo"));
      assertEquals("Zfoo", Collector.sanitizeMetricName("Zfoo"));
      assertEquals(":foo", Collector.sanitizeMetricName(":foo"));

      assertEquals("_foo", Collector.sanitizeMetricName("0foo"));
      assertEquals("_foo", Collector.sanitizeMetricName("5foo"));
      assertEquals("_foo", Collector.sanitizeMetricName("9foo"));
      assertEquals("_foo", Collector.sanitizeMetricName("/foo"));
      assertEquals("_foo", Collector.sanitizeMetricName("*foo"));
  }

  @Test
  public void sanitizeMetricBody() throws Exception {
      assertEquals("aamzAMZ059", Collector.sanitizeMetricName("aamzAMZ059"));
      assertEquals("aaMzAmZ009", Collector.sanitizeMetricName("aaMzAmZ009"));
      assertEquals("aZmA950aMz", Collector.sanitizeMetricName("aZmA950aMz"));
      assertEquals("aZ9mA0a5Mz", Collector.sanitizeMetricName("aZ9mA0a5Mz"));
      assertEquals("aZ9mA_0a5Mz", Collector.sanitizeMetricName("aZ9mA*0a5Mz"));
      assertEquals("aZ9mA_0a5Mz", Collector.sanitizeMetricName("aZ9mA&0a5Mz"));
  }

  @Test
  public void sanitizeMetricName() throws Exception {
      assertEquals("_hoge", Collector.sanitizeMetricName("0hoge"));
      assertEquals("foo_bar0", Collector.sanitizeMetricName("foo.bar0"));
      assertEquals(":baz::", Collector.sanitizeMetricName(":baz::"));
  }

  @Test
  public void testTotalHandling() throws Exception {
    class YourCustomCollector extends Collector {
      public List<MetricFamilySamples> collect() {
        List<String> emptyList = new ArrayList<String>();
        return Arrays.<MetricFamilySamples>asList(
            new MetricFamilySamples("a_total", Type.COUNTER, "help", Arrays.asList(
                new MetricFamilySamples.Sample("a_total", emptyList, emptyList, 1.0))),
            new MetricFamilySamples("b", Type.COUNTER, "help", Arrays.asList(
                new MetricFamilySamples.Sample("b", emptyList, emptyList, 2.0))),
            new MetricFamilySamples("c_total", Type.COUNTER, "help", Arrays.asList(
                new MetricFamilySamples.Sample("c", emptyList, emptyList, 3.0))),
            new MetricFamilySamples("d", Type.COUNTER, "help", Arrays.asList(
                new MetricFamilySamples.Sample("d_total", emptyList, emptyList, 4.0)))
        );
      }
    }
    CollectorRegistry registry = new CollectorRegistry();
    new YourCustomCollector().register(registry);

    assertEquals(1.0, registry.getSampleValue("a_total").doubleValue(), .001);
    assertEquals(2.0, registry.getSampleValue("b_total").doubleValue(), .001);
    assertEquals(3.0, registry.getSampleValue("c_total").doubleValue(), .001);
    assertEquals(4.0, registry.getSampleValue("d_total").doubleValue(), .001);
  }
}
