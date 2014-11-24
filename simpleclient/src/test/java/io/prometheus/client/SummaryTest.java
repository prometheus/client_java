package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.Vector;
import org.junit.Test;
import org.junit.Before;


public class SummaryTest {

  CollectorRegistry registry;
  Summary noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = (Summary) Summary.build().name("nolabels").help("help").register(registry);
    labels = (Summary) Summary.build().name("labels").help("help").labelNames("l").register(registry);
  }

  private double getCount() {
    return registry.getSampleValue("nolabels_count", new String[]{}, new String[]{}).doubleValue();
  }
  private double getSum() {
    return registry.getSampleValue("nolabels_sum", new String[]{}, new String[]{}).doubleValue();
  }
  
  @Test
  public void testObserve() {
    noLabels.observe(2);
    assertEquals(1.0, getCount(), .001);
    assertEquals(2.0, getSum(), .001);
    noLabels.labels().observe(4);
    assertEquals(2.0, getCount(), .001);
    assertEquals(6.0, getSum(), .001);
  }
  
  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getCount(), .001);
    assertEquals(0.0, getSum(), .001);
  }
  
  private Double getLabelsCount(String labelValue) {
    return registry.getSampleValue("labels_count", new String[]{"l"}, new String[]{labelValue});
  }
  private Double getLabelsSum(String labelValue) {
    return registry.getSampleValue("labels_sum", new String[]{"l"}, new String[]{labelValue});
  }

  @Test
  public void testLabels() {
    assertEquals(null, getLabelsCount("a"));
    assertEquals(null, getLabelsSum("a"));
    assertEquals(null, getLabelsCount("b"));
    assertEquals(null, getLabelsSum("b"));
    labels.labels("a").observe(2);
    assertEquals(1.0, getLabelsCount("a").doubleValue(), .001);
    assertEquals(2.0, getLabelsSum("a").doubleValue(), .001);
    assertEquals(null, getLabelsCount("b"));
    assertEquals(null, getLabelsSum("b"));
    labels.labels("b").observe(3);
    assertEquals(1.0, getLabelsCount("a").doubleValue(), .001);
    assertEquals(2.0, getLabelsSum("a").doubleValue(), .001);
    assertEquals(1.0, getLabelsCount("b").doubleValue(), .001);
    assertEquals(3.0, getLabelsSum("b").doubleValue(), .001);
  }

  @Test
  public void testCollect() {
    labels.labels("a").observe(2);
    Collector.MetricFamilySamples[] mfs = labels.collect();
    
    Vector<Collector.MetricFamilySamples.Sample> samples = new Vector<Collector.MetricFamilySamples.Sample>();
    Vector<String> labelValues = new Vector<String>();
    labelValues.add("a");
    samples.add(new Collector.MetricFamilySamples.Sample("labels_count", new String[]{"l"}, labelValues, 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_sum", new String[]{"l"}, labelValues, 2.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.SUMMARY, "help", samples);

    assertEquals(1, mfs.length);
    assertEquals(mfsFixture, mfs[0]);
  }

}
