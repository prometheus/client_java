package io.prometheus.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SummaryTest {

  CollectorRegistry registry;
  Summary noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Summary.build().name("nolabels").help("help").register(registry);
    labels = Summary.build().name("labels").help("help").labelNames("l").register(registry);
  }

  @After
  public void tearDown() {
    Summary.Child.timeProvider = new Summary.TimeProvider();
  }

  private double getCount() {
    return registry.getSampleValue("nolabels_count").doubleValue();
  }
  private double getSum() {
    return registry.getSampleValue("nolabels_sum").doubleValue();
  }
  
  @Test
  public void testObserve() {
    noLabels.observe(2);
    assertEquals(1.0, getCount(), .001);
    assertEquals(2.0, getSum(), .001);
    noLabels.labels().observe(4);
    assertEquals(2.0, getCount(), .001);
    assertEquals(6.0, getSum(), .001);
  }

  @Test
  public void testTimer() {
    Summary.Child.timeProvider = new Summary.TimeProvider() {
      long value = (long)(30 * 1e9);
      long nanoTime() {
        value += (long)(10 * 1e9);
        return value;
      }
    };
    Summary.Timer timer = noLabels.startTimer();
    double elapsed = timer.observeDuration();
    assertEquals(1, getCount(), .001);
    assertEquals(10, getSum(), .001);
    assertEquals(10, elapsed, .001);
  }
  
  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getCount(), .001);
    assertEquals(0.0, getSum(), .001);
  }
  
  private Double getLabelsCount(String labelValue) {
    return registry.getSampleValue("labels_count", new String[]{"l"}, new String[]{labelValue});
  }
  private Double getLabelsSum(String labelValue) {
    return registry.getSampleValue("labels_sum", new String[]{"l"}, new String[]{labelValue});
  }

  @Test
  public void testLabels() {
    assertEquals(null, getLabelsCount("a"));
    assertEquals(null, getLabelsSum("a"));
    assertEquals(null, getLabelsCount("b"));
    assertEquals(null, getLabelsSum("b"));
    labels.labels("a").observe(2);
    assertEquals(1.0, getLabelsCount("a").doubleValue(), .001);
    assertEquals(2.0, getLabelsSum("a").doubleValue(), .001);
    assertEquals(null, getLabelsCount("b"));
    assertEquals(null, getLabelsSum("b"));
    labels.labels("b").observe(3);
    assertEquals(1.0, getLabelsCount("a").doubleValue(), .001);
    assertEquals(2.0, getLabelsSum("a").doubleValue(), .001);
    assertEquals(1.0, getLabelsCount("b").doubleValue(), .001);
    assertEquals(3.0, getLabelsSum("b").doubleValue(), .001);
  }

  @Test
  public void testCollect() {
    labels.labels("a").observe(2);
    List<Collector.MetricFamilySamples> mfs = labels.collect();
    
    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    ArrayList<String> labelNames = new ArrayList<String>();
    labelNames.add("l");
    ArrayList<String> labelValues = new ArrayList<String>();
    labelValues.add("a");
    samples.add(new Collector.MetricFamilySamples.Sample("labels_count", labelNames, labelValues, 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_sum", labelNames, labelValues, 2.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.SUMMARY, "help", samples);
    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

  @Test
  public void testCollectQuantiles(){
    /*labels = Summary.build().name("labels").help("help").labelNames("l").quantiles(10, 0.25, 0.50, 0.75, 0.90).register(registry);

    labels.labels("a").observe(1);
    labels.labels("a").observe(3);
    labels.labels("a").observe(3);
    labels.labels("a").observe(2);
    labels.labels("a").observe(4);
    labels.labels("a").observe(3);
    labels.labels("a").observe(9);

    List<Collector.MetricFamilySamples> mfs = labels.collect();

    ArrayList<String> labelNames = new ArrayList<String>();
    labelNames.add("l");
    ArrayList<String> labelValues = new ArrayList<String>();
    labelValues.add("a");

    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();


    samples.add(new Collector.MetricFamilySamples.Sample("labels_count", labelNames, labelValues, 7.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_sum", labelNames, labelValues, 25.0));
    samples.add(new Collector.MetricFamilySamples.Sample(
            "labels",
            Arrays.asList("l", "quantile"),
            Arrays.asList("a", "0.25"), 2.0));

    samples.add(new Collector.MetricFamilySamples.Sample(
            "labels",
            Arrays.asList("l", "quantile"),
            Arrays.asList("a", "0.5"), 3.0));

    samples.add(new Collector.MetricFamilySamples.Sample(
            "labels",
            Arrays.asList("l", "quantile"),
            Arrays.asList("a", "0.75"), 4.0));

    samples.add(new Collector.MetricFamilySamples.Sample(
            "labels",
            Arrays.asList("l", "quantile"),
            Arrays.asList("a", "0.9"), 9));

    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.SUMMARY, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
    */
  }

}
