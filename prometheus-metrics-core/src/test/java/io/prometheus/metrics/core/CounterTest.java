package io.prometheus.metrics.core;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Labels;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

public class CounterTest {

  Counter noLabels;
  Counter labels;

  @Rule
  public final ExpectedException thrown = none();

  @Before
  public void setUp() {
    noLabels = Counter.newBuilder().withName("nolabels").build();
    labels = Counter.newBuilder().withName("labels")
            .withHelp("help")
            .withUnit("seconds")
            .withLabelNames("l")
            .build();
  }

  private double getValue(Counter counter, String... labels) {
    return ((CounterSnapshot) counter.collect()).getData().stream()
            .filter(d -> d.getLabels().equals(Labels.of(labels)))
            .findAny()
            .map(CounterSnapshot.CounterData::getValue)
            .orElseThrow(() -> new RuntimeException("counter without labels not found"));
  }

  private int getNumberOfLabels(Counter counter) {
    return ((CounterSnapshot) counter.collect()).getData().size();
  }

  @Test
  public void testIncrement() {
    noLabels.inc();
    assertEquals(1.0, getValue(noLabels), .001);
    noLabels.inc(2);
    assertEquals(3.0, getValue(noLabels), .001);
    noLabels.withLabels().inc(4);
    assertEquals(7.0, getValue(noLabels), .001);
    noLabels.withLabels().inc();
    assertEquals(8.0, getValue(noLabels), .001);
  }
    
  @Test
  public void testNegativeIncrementFails() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Negative increment -1.0 is illegal for Counter metrics.");
    noLabels.inc(-1);
  }
  
  @Test
  public void testEmptyCountersHaveNoLabels() {
    assertEquals(0, getNumberOfLabels(noLabels));
    assertEquals(0, getNumberOfLabels(labels));
  }

  @Test
  public void testLabels() {
    assertEquals(0, getNumberOfLabels(labels));
    labels.withLabels("a").inc();
    assertEquals(1, getNumberOfLabels(labels));
    assertEquals(1.0, getValue(labels, "l", "a"), .001);
    labels.withLabels("b").inc(3);
    assertEquals(2, getNumberOfLabels(labels));
    assertEquals(1.0, getValue(labels, "l", "a"), .001);
    assertEquals(3.0, getValue(labels, "l", "b"), .001);
  }

  /*
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
   */

  /*
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
   */

  /*
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
   */

  /*
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

   */
}
