package io.prometheus.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SummaryTest {

  CollectorRegistry registry;
  Summary noLabels, labels, labelsAndQuantiles, noLabelsAndQuantiles;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Summary.build().name("nolabels").help("help").register(registry);
    labels = Summary.build().name("labels").help("help").labelNames("l").register(registry);
    noLabelsAndQuantiles = Summary.build()
            .quantile(0.5, 0.05)
            .quantile(0.9, 0.01)
            .quantile(0.99, 0.001)
            .name("no_labels_and_quantiles").help("help").register(registry);
    labelsAndQuantiles = Summary.build()
            .quantile(0.5, 0.05)
            .quantile(0.9, 0.01)
            .quantile(0.99, 0.001)
            .labelNames("l")
            .name("labels_and_quantiles").help("help").register(registry);
  }

  @After
  public void tearDown() {
    SimpleTimer.defaultTimeProvider = new SimpleTimer.TimeProvider();
  }

  private double getCount() {
    return registry.getSampleValue("nolabels_count").doubleValue();
  }
  private double getSum() {
    return registry.getSampleValue("nolabels_sum").doubleValue();
  }
  private double getNoLabelQuantile(double q) {
    return registry.getSampleValue("no_labels_and_quantiles", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(q)}).doubleValue();
  }
  private double getLabeledQuantile(String labelValue, double q) {
    return registry.getSampleValue("labels_and_quantiles", new String[]{"l", "quantile"}, new String[]{labelValue, Collector.doubleToGoString(q)}).doubleValue();
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
  public void testQuantiles() {
    int nSamples = 1000000; // simulate one million samples

    for (int i=1; i<=nSamples; i++) {
      // In this test, we observe the numbers from 1 to nSamples,
      // because that makes it easy to verify if the quantiles are correct.
      labelsAndQuantiles.labels("a").observe(i);
      noLabelsAndQuantiles.observe(i);
    }
    assertEquals(getNoLabelQuantile(0.5), 0.5 * nSamples, 0.05 * nSamples);
    assertEquals(getNoLabelQuantile(0.9), 0.9 * nSamples, 0.01 * nSamples);
    assertEquals(getNoLabelQuantile(0.99), 0.99 * nSamples, 0.001 * nSamples);

    assertEquals(getLabeledQuantile("a", 0.5), 0.5 * nSamples, 0.05 * nSamples);
    assertEquals(getLabeledQuantile("a", 0.9), 0.9 * nSamples, 0.01 * nSamples);
    assertEquals(getLabeledQuantile("a", 0.99), 0.99 * nSamples, 0.001 * nSamples);
  }

  @Test
  public void testMaxAge() throws InterruptedException {
    Summary summary = Summary.build()
            .quantile(0.99, 0.001)
            .maxAgeSeconds(1) // After 1s, all observations will be discarded.
            .ageBuckets(2)   // We got 2 buckets, so we discard one bucket every 500ms.
            .name("short_attention_span").help("help").register(registry);
    summary.observe(8.0);
    double val = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)}).doubleValue();
    assertEquals(8.0, val, 0.0); // From bucket 1.
    Thread.sleep(600);
    val = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)}).doubleValue();
    assertEquals(8.0, val, 0.0); // From bucket 2.
    Thread.sleep(600);
    val = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)}).doubleValue();
    assertEquals(Double.NaN, val, 0.0); // Bucket 1 again, now it is empty.
  }

  @Test
  public void testTimer() {
    SimpleTimer.defaultTimeProvider = new SimpleTimer.TimeProvider() {
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
  public void testCollectWithQuantiles() {
    labelsAndQuantiles.labels("a").observe(2);
    List<Collector.MetricFamilySamples> mfs = labelsAndQuantiles.collect();

    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles", asList("l", "quantile"), asList("a", "0.5"), 2.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles", asList("l", "quantile"), asList("a", "0.9"), 2.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles", asList("l", "quantile"), asList("a", "0.99"), 2.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles_count", asList("l"), asList("a"), 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles_sum", asList("l"), asList("a"), 2.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels_and_quantiles", Collector.Type.SUMMARY, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

  @Test
  public void testChildAndValuePublicApi() throws Exception {
    assertTrue(Modifier.isPublic(Summary.Child.class.getModifiers()));

    final Method getMethod = Summary.Child.class.getMethod("get");
    assertTrue(Modifier.isPublic(getMethod.getModifiers()));
    assertEquals(Summary.Child.Value.class, getMethod.getReturnType());

    assertTrue(Modifier.isPublic(Summary.Child.Value.class.getModifiers()));
    assertTrue(Modifier.isPublic(Summary.Child.Value.class.getField("count").getModifiers()));
    assertTrue(Modifier.isPublic(Summary.Child.Value.class.getField("sum").getModifiers()));
    assertTrue(Modifier.isPublic(Summary.Child.Value.class.getField("quantiles").getModifiers()));
  }
}
