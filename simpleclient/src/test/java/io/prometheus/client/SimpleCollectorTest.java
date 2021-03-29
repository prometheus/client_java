package io.prometheus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.rules.ExpectedException.none;

import io.prometheus.client.exemplars.impl.NoopExemplarSampler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;


public class SimpleCollectorTest {

  CollectorRegistry registry;
  Gauge metric;
  Gauge noLabels;

  @Rule
  public final ExpectedException thrown = none();

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
  public void testTooFewLabelsThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect number of labels.");
    metric.labels();
  }

  @Test
  public void testNullLabelThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label cannot be null.");
    metric.labels(new String[]{null});
  }

  @Test
  public void testTooManyLabelsThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect number of labels.");
    metric.labels("a", "b");
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

  @Test
  public void testNameIsRequired() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Name hasn't been set.");
    Gauge.build().help("h").create();
  }

  @Test
  public void testHelpIsRequired() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Help hasn't been set.");
    Gauge.build().name("c").create();
  }

  @Test
  public void testInvalidNameThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid metric name: c'a");
    Gauge.build().name("c'a").create();
  }

  @Test
  public void testInvalidLabelNameThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid metric label name: c:d");
    Gauge.build().name("a").labelNames("c:d").help("h").create();
  }

  @Test
  public void testReservedLabelNameThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
            "Invalid metric label name, reserved for internal use: __name__");
    Gauge.build().name("a").labelNames("__name__").help("h").create();
  }

  @Test
  public void testUnitsAdded() {
    Gauge g = Gauge.build().name("a").unit("seconds").help("h").create();
    assertEquals("a_seconds", g.fullname);

    Gauge g2 = Gauge.build().name("a_seconds").unit("seconds").help("h").create();
    assertEquals("a_seconds", g2.fullname);
  }

  @Test
  public void testSetChild() {
    metric.setChild(new Gauge.Child(new NoopExemplarSampler()){
      public double get() {
        return 42;
      }
    }, "a");
    assertEquals(42.0, getValue("a").doubleValue(), .001);
  }

  @Test
  public void testSetChildReturnsGauge() {
    Gauge g = metric.setChild(new Gauge.Child(new NoopExemplarSampler()){
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
