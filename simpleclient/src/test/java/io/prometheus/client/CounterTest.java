package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Before;


public class CounterTest {
  CollectorRegistry registry;
  Counter noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Counter.build().name("nolabels").help("help").register(registry);
    labels = Counter.build().name("labels").help("help").labelNames("l").register(registry);
  }

  private double getValue() {
    return registry.getSampleValue("nolabels").doubleValue();
  }
  
  @Test
  public void testIncrement() {
    noLabels.inc();
    assertEquals(1.0, getValue(), .001);
    assertEquals(1.0, noLabels.get(), .001);
    noLabels.inc(2);
    assertEquals(3.0, getValue(), .001);
    assertEquals(3.0, noLabels.get(), .001);
    noLabels.labels().inc(4);
    assertEquals(7.0, getValue(), .001);
    assertEquals(7.0, noLabels.get(), .001);
    noLabels.labels().inc();
    assertEquals(8.0, getValue(), .001);
    assertEquals(8.0, noLabels.get(), .001);
  }
    
  @Test(expected=IllegalArgumentException.class)
  public void testNegativeIncrementFails() {
    noLabels.inc(-1);
  }
  
  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getValue(), .001);
  }
  
  private Double getLabelsValue(String labelValue) {
    return registry.getSampleValue("labels", new String[]{"l"}, new String[]{labelValue});
  }

  @Test
  public void testLabels() {
    assertEquals(null, getLabelsValue("a"));
    assertEquals(null, getLabelsValue("b"));
    labels.labels("a").inc();
    assertEquals(1.0, getLabelsValue("a").doubleValue(), .001);
    assertEquals(null, getLabelsValue("b"));
    labels.labels("b").inc(3);
    assertEquals(1.0, getLabelsValue("a").doubleValue(), .001);
    assertEquals(3.0, getLabelsValue("b").doubleValue(), .001);
  }

  @Test
  public void testCollect() {
    labels.labels("a").inc();
    List<Collector.MetricFamilySamples> mfs = labels.collect();
    
    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    ArrayList<String> labelNames = new ArrayList<String>();
    labelNames.add("l");
    ArrayList<String> labelValues = new ArrayList<String>();
    labelValues.add("a");
    samples.add(new Collector.MetricFamilySamples.Sample("labels", labelNames, labelValues, 1.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.COUNTER, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

}
