package io.prometheus.client;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollectorTest {
  @Test
  public void sanitizeMetricName() throws Exception {
      assertEquals("_hoge", Collector.sanitizeMetricName("0hoge"));
      assertEquals("foo_bar0", Collector.sanitizeMetricName("foo.bar0"));
  }
}
