package io.prometheus.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HdrSummaryTest {

  private CollectorRegistry registry;
  private HdrSummary noLabels, labels, noLabelsAndQuantiles, labelsAndQuantiles;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = HdrSummary.build()
        .name("nolabels").help("help").register(registry);
    labels = HdrSummary.build()
        .labelNames("l")
        .name("labels").help("help").register(registry);
    noLabelsAndQuantiles = HdrSummary.build()
        .quantile(0.5).quantile(0.9).quantile(0.99)
        .name("no_labels_and_quantiles").help("help").register(registry);
    labelsAndQuantiles = HdrSummary.build()
        .labelNames("l").quantile(0.5).quantile(0.9).quantile(0.99)
        .name("labels_and_quantiles").help("help").register(registry);
  }

  @After
  public void tearDown() {
    SimpleTimer.defaultTimeProvider = new SimpleTimer.TimeProvider();
  }

  private double getCount() {
    return registry.getSampleValue("nolabels_count");
  }

  private double getSum() {
    return registry.getSampleValue("nolabels_sum");
  }

  private double getNoLabelQuantile(double q) {
    return registry.getSampleValue("no_labels_and_quantiles", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(q)});
  }

  private double getLabeledQuantile(double q) {
    return registry.getSampleValue("labels_and_quantiles", new String[]{"l", "quantile"}, new String[]{"a", Collector.doubleToGoString(q)});
  }

  private Double getLabelsCount(String labelValue) {
    return registry.getSampleValue("labels_count", new String[]{"l"}, new String[]{labelValue});
  }

  private Double getLabelsSum(String labelValue) {
    return registry.getSampleValue("labels_sum", new String[]{"l"}, new String[]{labelValue});
  }

  @Test
  public void testObserve() {
    noLabels.observe(2);
    assertEquals(1.0, getCount(), .001);
    assertEquals(2.0, getSum(), .001);
    assertEquals(1.0, noLabels.get().count, .001);
    assertEquals(2.0, noLabels.get().sum, .001);

    noLabels.labels().observe(4);
    assertEquals(2.0, getCount(), .001);
    assertEquals(6.0, getSum(), .001);
    assertEquals(2.0, noLabels.get().count, .001);
    assertEquals(6.0, noLabels.get().sum, .001);
  }

  @Test
  public void testQuantiles() {
    int nSamples = 1000000; // simulate one million samples

    for (int i = 1; i <= nSamples; i++) {
      // In this test, we observe the numbers from 1 to nSamples,
      // because that makes it easy to verify if the quantiles are correct.
      labelsAndQuantiles.labels("a").observe(i);
      noLabelsAndQuantiles.observe(i);
    }

    assertEquals(0.5 * nSamples, getNoLabelQuantile(0.5), 0.05 * nSamples);
    assertEquals(0.9 * nSamples, getNoLabelQuantile(0.9), 0.01 * nSamples);
    assertEquals(0.99 * nSamples, getNoLabelQuantile(0.99), 0.001 * nSamples);
    assertEquals(1.0, noLabelsAndQuantiles.get().min, 0.001 * 1.0);
    assertEquals((double) nSamples, noLabelsAndQuantiles.get().max, 0.001 * nSamples);

    assertEquals(0.5 * nSamples, getLabeledQuantile(0.5), 0.05 * nSamples);
    assertEquals(0.9 * nSamples, getLabeledQuantile(0.9), 0.01 * nSamples);
    assertEquals(0.99 * nSamples, getLabeledQuantile(0.99), 0.001 * nSamples);
    assertEquals(1.0, labelsAndQuantiles.labels("a").get().min, 0.001 * 1.0);
    assertEquals((double) nSamples, labelsAndQuantiles.labels("a").get().max, 0.001 * nSamples);
  }

  @Test
  public void testMaxAge() throws InterruptedException {
    HdrSummary summary = HdrSummary.build()
        .quantile(0.99)
        .maxAgeSeconds(1) // After 1s, all observations will be discarded.
        .ageBuckets(2)  // We got 2 buckets, so we discard one bucket every 500ms.
        .name("short_attention_span").help("help").register(registry);

    summary.observe(8.0);
    double val = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
    assertEquals(8.0, val, 0.0); // From bucket 1.

    Thread.sleep(600);
    val = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
    assertEquals(8.0, val, 0.0); // From bucket 2.

    Thread.sleep(600);
    val = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
    assertEquals(Double.NaN, val, 0.0); // Bucket 1 again, now it is empty.
  }

  @Test
  public void testTimer() {
    SimpleTimer.defaultTimeProvider = new SimpleTimer.TimeProvider() {
      long value = (long) (30 * 1e9);
      long nanoTime() {
        value += (long) (10 * 1e9);
        return value;
      }
    };

    double elapsed = noLabels.time(new Runnable() {
      @Override
      public void run() {
        // no op
      }
    });
    assertEquals(10, elapsed, .001);

    int result = noLabels.time(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 123;
      }
    });
    assertEquals(123, result);

    HdrSummary.Timer timer = noLabels.startTimer();
    elapsed = timer.observeDuration();
    assertEquals(10, elapsed, .001);

    assertEquals(3, getCount(), .001);
    assertEquals(30, getSum(), .001);
  }

  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getCount(), .001);
    assertEquals(0.0, getSum(), .001);
  }

  @Test
  public void testLabels() {
    assertEquals(null, getLabelsCount("a"));
    assertEquals(null, getLabelsSum("a"));
    assertEquals(null, getLabelsCount("b"));
    assertEquals(null, getLabelsSum("b"));

    labels.labels("a").observe(2);
    assertEquals(1.0, getLabelsCount("a"), .001);
    assertEquals(2.0, getLabelsSum("a"), .001);
    assertEquals(null, getLabelsCount("b"));
    assertEquals(null, getLabelsSum("b"));

    labels.labels("b").observe(3);
    assertEquals(1.0, getLabelsCount("a"), .001);
    assertEquals(2.0, getLabelsSum("a"), .001);
    assertEquals(1.0, getLabelsCount("b"), .001);
    assertEquals(3.0, getLabelsSum("b"), .001);
  }

  @Test
  public void testCollect() {
    labels.labels("a").observe(2);
    List<Collector.MetricFamilySamples> mfs = labels.collect();

    ArrayList<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    samples.add(new Collector.MetricFamilySamples.Sample("labels_count", asList("l"), asList("a"), 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_sum", asList("l"), asList("a"), 2.0));
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
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles_min", asList("l"), asList("a"), 2.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles_max", asList("l"), asList("a"), 2.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles_count", asList("l"), asList("a"), 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_and_quantiles_sum", asList("l"), asList("a"), 2.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels_and_quantiles", Collector.Type.SUMMARY, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

  @Test
  public void testChildAndValuePublicApi() throws Exception {
    assertTrue(Modifier.isPublic(HdrSummary.Child.class.getModifiers()));

    final Method getMethod = HdrSummary.Child.class.getMethod("get");
    assertTrue(Modifier.isPublic(getMethod.getModifiers()));
    assertEquals(HdrSummary.Child.Value.class, getMethod.getReturnType());

    assertTrue(Modifier.isPublic(HdrSummary.Child.Value.class.getModifiers()));
    assertTrue(Modifier.isPublic(HdrSummary.Child.Value.class.getField("min").getModifiers()));
    assertTrue(Modifier.isPublic(HdrSummary.Child.Value.class.getField("max").getModifiers()));
    assertTrue(Modifier.isPublic(HdrSummary.Child.Value.class.getField("count").getModifiers()));
    assertTrue(Modifier.isPublic(HdrSummary.Child.Value.class.getField("sum").getModifiers()));
    assertTrue(Modifier.isPublic(HdrSummary.Child.Value.class.getField("quantiles").getModifiers()));
  }

}
