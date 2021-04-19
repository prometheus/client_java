package io.prometheus.client.exemplars;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static io.prometheus.client.exemplars.Exemplar.SPAN_ID;
import static io.prometheus.client.exemplars.Exemplar.TRACE_ID;

public class ExemplarTest {

  @Test
  public void testCompleteExemplar() {
    double value = 42;
    long timestamp = System.currentTimeMillis();
    String traceId = "abc";
    String spanId = "def";
    Exemplar exemplar = new Exemplar(value, timestamp, TRACE_ID, traceId, SPAN_ID, spanId);
    Assert.assertEquals(42, exemplar.getValue(), 0.001);
    Assert.assertEquals(timestamp, exemplar.getTimestampMs().longValue());
    Assert.assertEquals(2, exemplar.getNumberOfLabels());
    Assert.assertEquals(TRACE_ID, exemplar.getLabelName(0));
    Assert.assertEquals(traceId, exemplar.getLabelValue(0));
    Assert.assertEquals(SPAN_ID, exemplar.getLabelName(1));
    Assert.assertEquals(spanId, exemplar.getLabelValue(1));
  }

  @Test
  public void testNoTimestamp() {
    double value = 42;
    String traceId = "abc";
    String spanId = "def";
    Exemplar exemplar = new Exemplar(value, TRACE_ID, traceId, SPAN_ID, spanId);
    Assert.assertEquals(42, exemplar.getValue(), 0.001);
    Assert.assertNull(exemplar.getTimestampMs());
    Assert.assertEquals(2, exemplar.getNumberOfLabels());
    Assert.assertEquals(TRACE_ID, exemplar.getLabelName(0));
    Assert.assertEquals(traceId, exemplar.getLabelValue(0));
    Assert.assertEquals(SPAN_ID, exemplar.getLabelName(1));
    Assert.assertEquals(spanId, exemplar.getLabelValue(1));
  }

  @Test
  public void testNoLabels() {
    double value = 42;
    long timestamp = System.currentTimeMillis();
    Exemplar exemplar = new Exemplar(value, timestamp);
    Assert.assertEquals(42, exemplar.getValue(), 0.001);
    Assert.assertEquals(timestamp, exemplar.getTimestampMs().longValue());
    Assert.assertEquals(0, exemplar.getNumberOfLabels());
  }

  @Test
  public void testMissingLabelValue() {
    try {
      new Exemplar(42, TRACE_ID);
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail("expected IllegalArgumentException");
  }

  @Test
  public void testNullLabelValue() {
    try {
      new Exemplar(42, TRACE_ID, null);
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail("expected IllegalArgumentException");
  }

  @Test
  public void testMaxLabelLength() {
    String eight = "12345678";
    String ten = "1234567890";
    String twenty = ten + ten;
    String thirty = twenty + ten;
    String fourty = thirty + ten;
    new Exemplar(42, ten, twenty, thirty, fourty, eight, twenty); // 128 chars total
    try {
      new Exemplar(42, ten, twenty, thirty, fourty, eight, twenty + "1"); // 129 chars total
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail("expected IllegalArgumentException");
  }

  @Test
  public void testNotUniqueLabelNames() {
    try {
      new Exemplar(42, "label1", "value1", "label2", "value2", "label1", "value1");
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail("expected an IllegalArgumentException");
  }

  @Test
  public void testNotUniqueLabelValues() {
    // label values don't need to be unique
    new Exemplar(42, "label1", "value1", "label2", "value1", "label3", "value1");
  }

  @Test
  public void testEqualsHashCode() {
    Exemplar e1 = new Exemplar(17, "label1", "value1", "label2", "value2");
    Exemplar e2 = new Exemplar(17, "label1", "value1", "label2", "value2");
    Assert.assertEquals(e1, e2);
    Assert.assertEquals(e1.hashCode(), e2.hashCode());

    long timestamp = System.currentTimeMillis();
    e1 = new Exemplar(17, timestamp, "label1", "value1", "label2", "value2");
    e2 = new Exemplar(17, timestamp, "label1", "value1", "label2", "value2");
    Assert.assertEquals(e1, e2);
    Assert.assertEquals(e1.hashCode(), e2.hashCode());

    e1 = new Exemplar(17);
    e2 = new Exemplar(17);
    Assert.assertEquals(e1, e2);
    Assert.assertEquals(e1.hashCode(), e2.hashCode());

    e1 = new Exemplar(17, "label1", "value1", "label2", "value2");
    e2 = new Exemplar(17, timestamp, "label1", "value1", "label2", "value2");
    Assert.assertNotEquals(e1, e2);
    Assert.assertNotEquals(e1.hashCode(), e2.hashCode());

    e1 = new Exemplar(17, timestamp, "label1", "value1");
    e2 = new Exemplar(17, timestamp, "label1", "value1", "label2", "value2");
    Assert.assertNotEquals(e1, e2);
    Assert.assertNotEquals(e1.hashCode(), e2.hashCode());

    e1 = new Exemplar(17, timestamp, "label1", "value1");
    e2 = new Exemplar(17, timestamp);
    Assert.assertNotEquals(e1, e2);
    Assert.assertNotEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  public void testLabelMap() {
    TreeMap<String, String> map = new TreeMap<String, String>();
    map.put("label1", "value1");
    map.put("label2", "value2");
    Exemplar e = new Exemplar(3, map);
    Assert.assertEquals(3, e.getValue(), 0.001);
    Assert.assertNull(e.getTimestampMs());
    Assert.assertEquals(2, e.getNumberOfLabels());
    Assert.assertEquals("label1", e.getLabelName(0));
    Assert.assertEquals("value1", e.getLabelValue(0));
    Assert.assertEquals("label2", e.getLabelName(1));
    Assert.assertEquals("value2", e.getLabelValue(1));
  }

  @Test
  public void testEmptyLabelMap() {
    Map<String, String> map = new HashMap<String, String>();
    long timestamp = System.currentTimeMillis();
    Exemplar e = new Exemplar(3, timestamp, map);
    Assert.assertEquals(3, e.getValue(), 0.001);
    Assert.assertEquals(timestamp, e.getTimestampMs().longValue());
    Assert.assertEquals(0, e.getNumberOfLabels());
  }
}
