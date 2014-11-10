package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.Vector;
import org.junit.Test;
import org.junit.Before;


public class GaugeTest {

  CollectorRegistry registry;
  Gauge noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = (Gauge) Gauge.build().name("nolabels").help("help").register(registry);
    labels = (Gauge) Gauge.build().name("labels").help("help").labelNames("l").register(registry);
  }

  private double getValue() {
    return registry.getSampleValue("nolabels", new String[]{}, new String[]{}).doubleValue();
  }
  
  @Test
  public void testIncrement() {
    noLabels.inc();
    assertEquals(1.0, getValue(), .001);
    noLabels.inc(2);
    assertEquals(3.0, getValue(), .001);
    noLabels.labels().inc(4);
    assertEquals(7.0, getValue(), .001);
    noLabels.labels().inc();
    assertEquals(8.0, getValue(), .001);
  }
    
  @Test
  public void testDecrement() {
    noLabels.dec();
    assertEquals(-1.0, getValue(), .001);
    noLabels.dec(2);
    assertEquals(-3.0, getValue(), .001);
    noLabels.labels().dec(4);
    assertEquals(-7.0, getValue(), .001);
    noLabels.labels().dec();
    assertEquals(-8.0, getValue(), .001);
  }
  
  @Test
  public void testSet() {
    noLabels.set(42);
    assertEquals(42, getValue(), .001);
    noLabels.labels().set(7);
    assertEquals(7.0, getValue(), .001);
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
    Collector.MetricFamilySamples[] mfs = labels.collect();
    
    Vector<Collector.MetricFamilySamples.Sample> samples = new Vector<Collector.MetricFamilySamples.Sample>();
    Vector<String> labelValues = new Vector<String>();
    labelValues.add("a");
    samples.add(new Collector.MetricFamilySamples.Sample("labels", new String[]{"l"}, labelValues, 1.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.GAUGE, "help", samples);

    assertEquals(1, mfs.length);
    assertEquals(mfsFixture, mfs[0]);
  }

}
