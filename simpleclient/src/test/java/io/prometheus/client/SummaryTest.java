package io.prometheus.client;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SummaryTest {

  static final double[] QUANTILE_VALUES = {0, .25, .5, .95, .98, .99, .999, 1.0};
  static final double[] EXPECTED_QUANTILES = {1, 1, 1.5, 2, 2, 2, 2, 2};

  CollectorRegistry registry;
  Summary noLabels, labels, quantiles;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Summary.build().name("nolabels").help("help").register(registry);
    labels = Summary.build().name("labels").help("help").labelNames("l").register(registry);
    quantiles = Summary.build().name("quant").help("help").quantiles(QUANTILE_VALUES).register(registry);
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

  private Double getQuantileCount() {
    return registry.getSampleValue("quant_count");
  }
  private Double getQuantileSum() {
    return registry.getSampleValue("quant_sum");
  }
  private Double getQuantileValue(double q){
    return registry.getSampleValue("quant", new String[]{Summary.QUANTILE_LABEL}, new String[]{String.valueOf(q)});
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
  public void testQuantiles() {
    for (int i = 0; i < QUANTILE_VALUES.length; i++) {
      assertEquals(Double.NaN, getQuantileValue(QUANTILE_VALUES[i]), .001);
    }

    for (double d : new double[] { 1.0, 2.0 }) {
      quantiles.observe(d);
    }

    assertEquals(2.0, getQuantileCount(), .0);
    assertEquals(3.0, getQuantileSum(), .001);

    for (int i = 0; i < QUANTILE_VALUES.length; i++) {
      assertEquals(EXPECTED_QUANTILES[i], getQuantileValue(QUANTILE_VALUES[i]), .001);
    }
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
  public void testCollectWithQuantiles() {
    quantiles.observe(2);
    List<Collector.MetricFamilySamples> mfs = quantiles.collect();

    List<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    List<String> quantileLabelName = singletonList(Summary.QUANTILE_LABEL);
    for (double q : QUANTILE_VALUES) {
      List<String> quantileLabelValue = singletonList(String.valueOf(q));
      samples.add(new Collector.MetricFamilySamples.Sample("quant", quantileLabelName, quantileLabelValue, 2.0));
    }
    samples.add(new Collector.MetricFamilySamples.Sample("quant_count", EMPTY_LIST, EMPTY_LIST, 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("quant_sum", EMPTY_LIST, EMPTY_LIST, 2.0));

    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("quant", Collector.Type.SUMMARY, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

}
