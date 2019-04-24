package io.prometheus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.Before;


public class SimpleCollectorTest {

  CollectorRegistry registry;
  Gauge metric;
  Gauge noLabels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    metric = Gauge.build().name("labels").help("help").labelNames("l").register(registry);
    noLabels = Gauge.build().name("nolabels").help("help").register(registry);
  }
  
  private Double getValue(String labelValue) {
    return registry.getSampleValue("labels", new String[]{"l"}, new String[]{labelValue});
  }

  private Double getValueNoLabels() {
    return registry.getSampleValue("nolabels");
  }

  @Test
  public void testTooFewLabelsSilent() {
    metric.labels().inc();
    assertEquals(getValue(SimpleCollector.DEFAULT_LABEL_VALUE), 1.0, .001);
  }

  @Test
  public void testNullLabelSilent() {
    metric.labels(new String[]{null}).inc();
    assertEquals(getValue(SimpleCollector.DEFAULT_LABEL_VALUE), 1.0, .001);
  }

  @Test
  public void testTooManyLabelsSilent() {
    metric.labels("a", "b").inc();
    assertEquals(getValue("a"), 1.0, .001);
  }
  
  @Test
  public void testRemove() {
    metric.labels("a");
    assertNotNull(getValue("a"));
    assertNull(getValue("b"));
    metric.labels("b").set(7);
    assertNotNull(getValue("a"));
    assertNotNull(getValue("b"));
    metric.remove("b");
    assertNotNull(getValue("a"));
    assertNull(getValue("b"));
   
    // Brand new Child.
    metric.labels("b").inc();
    assertEquals(1.0, getValue("b").doubleValue(), .001);
  }

  @Test
  public void testNoLabelsWorkAfterRemove() {
    noLabels.inc(1);
    assertEquals(getValueNoLabels(), 1.0, .001);
    noLabels.remove();
    noLabels.inc(2);
    assertEquals(getValueNoLabels(), 2.0, .001);
  }

  @Test
  public void testClear() {
    assertNull(getValue("a"));
    metric.labels("a").set(7);
    assertNotNull(getValue("a"));
    metric.clear();
    assertNull(getValue("a"));
   
    // Brand new Child.
    metric.labels("a").inc();
    assertEquals(1.0, getValue("a").doubleValue(), .001);
  }

  @Test
  public void testNoLabelsWorkAfterClear() {
    noLabels.inc(1);
    assertEquals(getValueNoLabels(), 1.0, .001);
    noLabels.clear();
    noLabels.inc(2);
    assertEquals(getValueNoLabels(), 2.0, .001);
  }

  @Test
  public void testNameIsConcatenated() {
    assertEquals("a_b_c", Gauge.build().name("c").subsystem("b").namespace("a").help("h").create().fullname);
  }

  @Test(expected=IllegalStateException.class)
  public void testNameIsRequired() {
    Gauge.build().help("h").create();
  }

  @Test(expected=IllegalStateException.class)
  public void testHelpIsRequired() {
    Gauge.build().name("c").create();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidNameThrows() {
    Gauge.build().name("c'a").create();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidLabelNameThrows() {
    Gauge.build().name("a").labelNames("c:d").help("h").create();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testReservedLabelNameThrows() {
    Gauge.build().name("a").labelNames("__name__").help("h").create();
  }

  @Test
  public void testSetChild() {
    metric.setChild(new Gauge.Child(){
      public double get() {
        return 42;
      }
    }, "a");
    assertEquals(42.0, getValue("a").doubleValue(), .001);
  }

  @Test
  public void testSetChildReturnsGauge() {
    Gauge g = metric.setChild(new Gauge.Child(){
      public double get() {
        return 42;
      }
    }, "a");
  }

  @Test
  public void testCreateReturnsGauge() {
    Gauge g = Gauge.build().name("labels").help("help").labelNames("l").create();
  }
}
