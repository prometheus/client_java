package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SummaryMetricFamilyTest {

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
				mfs.add(new SummaryMetricFamily("my_summary", "help", 1, 42));
				// With labels. Record 95th percentile as 3, and 99th percentile as 5.
				SummaryMetricFamily labeledSummary = new SummaryMetricFamily("my_other_summary", "help", 
						Arrays.asList("labelname"), Arrays.asList(.95, .99));
				labeledSummary.addMetric(Arrays.asList("foo"), 2, 10, Arrays.asList(3.0, 5.0));
				mfs.add(labeledSummary);
				return mfs;
			}
		}
		new YourCustomCollector().register(registry);

		assertEquals(1.0, registry.getSampleValue("my_summary_count").doubleValue(), .001);
		assertEquals(42.0, registry.getSampleValue("my_summary_sum").doubleValue(), .001);

		assertEquals(2.0, registry.getSampleValue("my_other_summary_count", new String[]{"labelname"}, new String[]{"foo"}).doubleValue(), .001);
		assertEquals(10.0, registry.getSampleValue("my_other_summary_sum", new String[]{"labelname"}, new String[]{"foo"}).doubleValue(), .001);
		assertEquals(3.0, registry.getSampleValue("my_other_summary", new String[]{"labelname", "quantile"}, new String[]{"foo", "0.95"}).doubleValue(), .001);
		assertEquals(5.0, registry.getSampleValue("my_other_summary", new String[]{"labelname", "quantile"}, new String[]{"foo", "0.99"}).doubleValue(), .001);
	}

	@Test
	public void testBuilderStyleUsage() {
		class YourCustomCollector extends Collector {
			public List<MetricFamilySamples> collect() {
				return Arrays.<MetricFamilySamples>asList(
						new SummaryMetricFamily("my_metric", "help", Arrays.asList("name"))
								.addMetric(Arrays.asList("value1"), 1, 1.0)
								.addMetric(Arrays.asList("value2"), 2, 2.0)
				);
			}
		}
		new YourCustomCollector().register(registry);

		assertEquals(1.0,
				registry.getSampleValue("my_metric_count", new String[]{"name"}, new String[]{"value1"})
						.doubleValue(), .001);
		assertEquals(2.0,
				registry.getSampleValue("my_metric_count", new String[]{"name"}, new String[]{"value2"})
						.doubleValue(), .001);
	}

}
