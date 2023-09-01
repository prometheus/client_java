package io.prometheus.client;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.junit.rules.ExpectedException.none;


public class SimpleCollectorWithLabelSanitizerTest {

  CollectorRegistry registry;
  Gauge metric;

  @Rule
  public final ExpectedException thrown = none();

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    metric = Gauge.build().name("nonulllabels").help("help").labelNames("l").labelValueSanitizer(Gauge.TransformNullLabelsToEmptyString()).register(registry);
  }

  private Double getValue(String labelValue) {
    return registry.getSampleValue("nonulllabels", new String[]{"l"}, new String[]{labelValue});
  }

  @Test
  public void testNullLabelDoesntThrowWithLabelSanitizer() {
    metric.labels(new String[]{null}).inc();
    assertEquals(1.0, getValue("").doubleValue(), .001);
  }
}
