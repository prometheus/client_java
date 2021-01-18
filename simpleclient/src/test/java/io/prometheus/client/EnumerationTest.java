package io.prometheus.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EnumerationTest {

  CollectorRegistry registry;
  Enumeration noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Enumeration.build().states("foo", "bar").name("nolabels").help("help").register(registry);
    labels = Enumeration.build().states("foo", "bar").name("labels").help("help").labelNames("l").register(registry);
  }

  private Double getNoLabelState(String s) {
    return registry.getSampleValue("nolabels", new String[]{"nolabels"}, new String[]{s});
  }
  private Double getLabeledState(String labelValue, String s) {
    return registry.getSampleValue("labels", new String[]{"l", "labels"}, new String[]{labelValue, s});
  }

  @Test
  public void testState() {
    noLabels.state("bar");
    assertEquals(0.0, getNoLabelState("foo"), .001);
    assertEquals(1.0, getNoLabelState("bar"), .001);
    noLabels.state("foo");
    assertEquals(1.0, getNoLabelState("foo"), .001);
    assertEquals(0.0, getNoLabelState("bar"), .001);
  }

  @Test
  public void testDefaultValue() {
    assertEquals(1.0, getNoLabelState("foo"), .001);
    assertEquals(0.0, getNoLabelState("bar"), .001);
  }

  @Test
  public void testLabels() {
    assertEquals(null, getLabeledState("a", "foo"));
    assertEquals(null, getLabeledState("a", "bar"));
    assertEquals(null, getLabeledState("b", "foo"));
    assertEquals(null, getLabeledState("b", "bar"));
    labels.labels("a").state("foo");
    assertEquals(1.0, getLabeledState("a", "foo"), .001);
    assertEquals(0.0, getLabeledState("a", "bar"), .001);
    assertEquals(null, getLabeledState("b", "foo"));
    assertEquals(null, getLabeledState("b", "bar"));
    labels.labels("b").state("bar");
    assertEquals(1.0, getLabeledState("a", "foo"), .001);
    assertEquals(0.0, getLabeledState("a", "bar"), .001);
    assertEquals(0.0, getLabeledState("b", "foo"), .001);
    assertEquals(1.0, getLabeledState("b", "bar"), .001);
  }

  public enum myEnum {
    FOO,
    BAR,
  }

  @Test
  public void testJavaEnum() {
    Enumeration metric = Enumeration.build().states(myEnum.class).name("enum").help("help").register(registry);
    metric.state(myEnum.BAR);
    assertEquals(0.0, registry.getSampleValue("enum", new String[]{"enum"}, new String[]{"FOO"}), .001);
    assertEquals(1.0, registry.getSampleValue("enum", new String[]{"enum"}, new String[]{"BAR"}), .001);
  }

  @Test(expected=IllegalStateException.class)
  public void testDuplicateNameLabelThrows() {
    Enumeration.build().states("foo", "bar").name("labels").help("help").labelNames("labels").create();
  }

  @Test(expected=IllegalStateException.class)
  public void testNoStatesThrows() {
    Enumeration.build().name("nolabels").help("help").create();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testEmptyStatesThrows() {
    Enumeration.build().states().name("nolabels").help("help").create();
  }

  @Test(expected=IllegalStateException.class)
  public void testUnitThrows() {
    Enumeration.build().states("foo", "bar").unit("seconds").name("nolabels").help("help").create();
  }

  @Test
  public void testCollect() {
    labels.labels("a").state("bar");
    List<Collector.MetricFamilySamples> mfs = labels.collect();

    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    samples.add(new Collector.MetricFamilySamples.Sample("labels", asList("l", "labels"), asList("a", "foo"), 0.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels", asList("l", "labels"), asList("a", "bar"), 1.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.STATE_SET, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }
}
