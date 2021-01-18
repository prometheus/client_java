package io.prometheus.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class InfoTest {

  CollectorRegistry registry;
  Info noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Info.build().name("nolabels").help("help").register(registry);
    labels = Info.build().name("labels").help("help").labelNames("l").register(registry);
  }

  private Double getInfo(String metric, String... labels) {
    String[] names = new String[labels.length / 2];
    String[] values = new String[labels.length / 2];
    for (int i = 0; i < labels.length; i+=2) {
      names[i/2] = labels[i];
      values[i/2] = labels[i+1];
    }
    return registry.getSampleValue(metric + "_info", names, values);
  }

  @Test
  public void testInfo() {
    assertEquals(null, getInfo("nolabels", "foo", "bar"));
    noLabels.info("foo", "bar");
    assertEquals(1.0, getInfo("nolabels", "foo", "bar"), .001);
    noLabels.info("foo", "bar", "baz", "meh");
    assertEquals(null, getInfo("nolabels", "foo", "bar"));
    assertEquals(1.0, getInfo("nolabels", "baz", "meh", "foo", "bar"), .001);
  }

  @Test
  public void testDefaultValue() {
    assertEquals(1.0, getInfo("nolabels"), .001);
  }

  @Test
  public void testLabels() {
    assertEquals(null, getInfo("labels", "l", "a", "foo", "bar"));
    assertEquals(null, getInfo("labels", "l", "b", "baz", "meh"));
    labels.labels("a").info("foo", "bar");
    assertEquals(1.0, getInfo("labels", "l", "a", "foo", "bar"), .001);
    assertEquals(null, getInfo("labels", "l", "b", "baz", "meh"));
    labels.labels("b").info("baz", "meh");
    assertEquals(1.0, getInfo("labels", "l", "a", "foo", "bar"), .001);
    assertEquals(1.0, getInfo("labels", "l", "b", "baz", "meh"), .001);
    
    assertEquals(null, getInfo("nolabels", "l", "a"));
    assertEquals(null, getInfo("nolabels", "l", "b"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testDuplicateNameLabelThrows() {
    Info i = Info.build().name("labels").help("help").labelNames("l").create();
    i.labels("a").info("l", "bar");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testOddInfoThrows() {
    Info i = Info.build().name("labels").help("help").create();
    i.info("odd");
  }

  @Test(expected=IllegalStateException.class)
  public void testUnitThrows() {
    Info.build().unit("seconds").name("nolabels").help("help").create();
  }

  @Test
  public void testCollect() {
    labels.labels("a").info("foo", "bar", "baz", "meh");
    List<Collector.MetricFamilySamples> mfs = labels.collect();

    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    samples.add(new Collector.MetricFamilySamples.Sample("labels_info", asList("l", "baz", "foo"), asList("a", "meh", "bar"), 1.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.INFO, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }
}
