package io.prometheus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.prometheus.client.exemplars.Exemplar;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;

public class CounterTest {
  CollectorRegistry registry;
  Counter noLabels, labels;

  @Rule
  public final ExpectedException thrown = none();

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Counter.build().name("nolabels").help("help").register(registry);
    labels = Counter.build().name("labels").unit("seconds").help("help").labelNames("l").register(registry);
  }

  private double getValue() {
    return registry.getSampleValue("nolabels_total").doubleValue();
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
    
  @Test
  public void testNegativeIncrementFails() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Amount to increment must be non-negative.");
    noLabels.inc(-1);
  }
  
  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getValue(), .001);
  }
  
  private Double getLabelsValue(String labelValue) {
    return registry.getSampleValue("labels_seconds_total", new String[]{"l"}, new String[]{labelValue});
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
  public void testTotalStrippedFromName() {
    Counter c = Counter.build().name("foo_total").unit("seconds").help("h").create();
    assertEquals("foo_seconds", c.fullname);

    // This is not a good unit, but test it anyway.
    c = Counter.build().name("foo_total").unit("total").help("h").create();
    assertEquals("foo_total", c.fullname);
    c = Counter.build().name("foo").unit("total").help("h").create();
    assertEquals("foo_total", c.fullname);
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
    samples.add(new Collector.MetricFamilySamples.Sample("labels_seconds_total", labelNames, labelValues, 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_seconds_created", labelNames, labelValues, labels.labels("a").created() / 1000.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels_seconds", "seconds", Collector.Type.COUNTER, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

  @Test
  public void testExemplars() {
    Map<String, String> labels = new HashMap<String, String>();
    labels.put("mapKey1", "mapValue1");
    labels.put("mapKey2", "mapValue2");

    noLabels.incWithExemplar("key", "value");
    assertExemplar(noLabels, 1, "key", "value");

    noLabels.incWithExemplar();
    assertExemplar(noLabels, 1);

    noLabels.incWithExemplar(labels);
    assertExemplar(noLabels, 1, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

    noLabels.incWithExemplar(2, "key1", "value1", "key2", "value2");
    assertExemplar(noLabels, 2, "key1", "value1", "key2", "value2");

    noLabels.incWithExemplar(3);
    assertExemplar(noLabels, 3);

    noLabels.incWithExemplar(4, new HashMap<String, String>());
    assertExemplar(noLabels, 4);

    noLabels.incWithExemplar(5, labels);
    assertExemplar(noLabels, 5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");
    noLabels.inc(); // should not alter the exemplar
    assertExemplar(noLabels, 5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

    noLabels.incWithExemplar(5, (String[]) null); // should not alter the exemplar
    assertExemplar(noLabels, 5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

    noLabels.incWithExemplar(5, (Map<String, String>) null); // should not alter the exemplar
    assertExemplar(noLabels, 5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");
  }

  private void assertExemplar(Counter counter, double value, String... labels) {
    List<Collector.MetricFamilySamples> mfs = counter.collect();
    Exemplar exemplar = mfs.get(0).samples.get(0).exemplar;
    Assert.assertEquals(value, exemplar.getValue(), 0.001);
    Assert.assertEquals(labels.length/2, exemplar.getNumberOfLabels());
    for (int i=0; i<labels.length; i+=2) {
      Assert.assertEquals(labels[i], exemplar.getLabelName(i/2));
      Assert.assertEquals(labels[i+1], exemplar.getLabelValue(i/2));
    }
  }
}
