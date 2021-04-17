package io.prometheus.client.exemplars.api;

import org.junit.Assert;
import org.junit.Test;

import static io.prometheus.client.exemplars.api.Exemplar.SPAN_ID;
import static io.prometheus.client.exemplars.api.Exemplar.TRACE_ID;

public class ExemplarTest {

  @Test
  public void testCompleteExemplar() {
    double value = 42;
    long timestamp = System.currentTimeMillis();
    String traceId = "abc";
    String spanId = "def";
    Exemplar exemplar = new Exemplar(value, timestamp, TRACE_ID, traceId, SPAN_ID, spanId);
    Assert.assertEquals(42, exemplar.getValue(), 0.001);
    Assert.assertEquals(timestamp, exemplar.getTimestampMs());
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
    Assert.assertEquals(0, exemplar.getTimestampMs());
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
    Assert.assertEquals(timestamp, exemplar.getTimestampMs());
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
    String ten = "1234567890";
    String twenty = ten + ten;
    // 128 = 4 * 30 + 8, should be allowed
    new Exemplar(42, ten, twenty, ten, twenty, ten, twenty, ten, twenty, "1234", "5678");
    try {
      // 129 = 4 * 30 + 9, should throw an Exception
      new Exemplar(42, ten, twenty, ten, twenty, ten, twenty, ten, twenty, "1234", "56789");
    } catch (IllegalArgumentException e) {
      return;
    }
    Assert.fail("expected IllegalArgumentException");
  }
}
