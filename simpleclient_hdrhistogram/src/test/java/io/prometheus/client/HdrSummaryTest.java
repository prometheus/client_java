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
      .name("no_labels").help("help")
      .register(registry);
    labels = HdrSummary.build()
      .name("labels").help("help")
      .labelNames("l")
      .register(registry);
    noLabelsAndQuantiles = HdrSummary.build()
      .name("no_labels_and_quantiles").help("help")
      .quantile(0.5).quantile(0.9).quantile(0.99)
      .register(registry);
    labelsAndQuantiles = HdrSummary.build()
      .name("labels_and_quantiles").help("help")
      .labelNames("l")
      .quantile(0.5).quantile(0.9).quantile(0.99)
      .register(registry);
  }

  @After
  public void tearDown() {
    SimpleTimer.defaultTimeProvider = new SimpleTimer.TimeProvider();
  }

  private Double getCount() {
    return registry.getSampleValue("no_labels_count");
  }

  private Double getSum() {
    return registry.getSampleValue("no_labels_sum");
  }

  private Double getMin() {
    return registry.getSampleValue("no_labels_min");
  }

  private Double getMax() {
    return registry.getSampleValue("no_labels_max");
  }

  private Double getCount(String labelValue) {
    return registry.getSampleValue("labels_count", new String[]{"l"}, new String[]{labelValue});
  }

  private Double getSum(String labelValue) {
    return registry.getSampleValue("labels_sum", new String[]{"l"}, new String[]{labelValue});
  }

  private Double getMin(String labelValue) {
    return registry.getSampleValue("labels_min", new String[]{"l"}, new String[]{labelValue});
  }

  private Double getMax(String labelValue) {
    return registry.getSampleValue("labels_max", new String[]{"l"}, new String[]{labelValue});
  }

  private Double getNoLabelsQuantile(double q) {
    return registry.getSampleValue("no_labels_and_quantiles", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(q)});
  }

  private Double getLabelsQuantile(double q) {
    return registry.getSampleValue("labels_and_quantiles", new String[]{"l", "quantile"}, new String[]{"a", Collector.doubleToGoString(q)});
  }

  @Test
  public void testObserve() {
    noLabels.observe(2.0);
    assertEquals(1.0, getCount(), .001);
    assertEquals(2.0, getSum(), .001);
    assertEquals(null, getMin());
    assertEquals(null, getMax());

    noLabels.labels().observe(4.0);
    assertEquals(2.0, getCount(), .001);
    assertEquals(6.0, getSum(), .001);
    assertEquals(null, getMin());
    assertEquals(null, getMax());
  }

  @Test
  public void testQuantiles() {
    int nSamples = 1000000; // simulate one million samples
    double error = .01; // default `numberOfSignificantValueDigits` is `2`

    for (int i = 1; i <= nSamples; i++) {
      // In this test, we observe the numbers from 1 to nSamples,
      // because that makes it easy to verify if the quantiles are correct.
      noLabelsAndQuantiles.observe(i);
      labelsAndQuantiles.labels("a").observe(i);
    }

    assertEquals((double) nSamples, registry.getSampleValue("no_labels_and_quantiles_count"), .001);
    assertEquals((1.0 + nSamples) * nSamples / 2.0, registry.getSampleValue("no_labels_and_quantiles_sum"), .001);
    assertEquals(1.0, registry.getSampleValue("no_labels_and_quantiles_min"), error * 1.0);
    assertEquals((double) nSamples, registry.getSampleValue("no_labels_and_quantiles_max"), error * nSamples);
    assertEquals(0.5 * nSamples, getNoLabelsQuantile(0.5), error * nSamples);
    assertEquals(0.9 * nSamples, getNoLabelsQuantile(0.9), error * nSamples);
    assertEquals(0.99 * nSamples, getNoLabelsQuantile(0.99), error * nSamples);

    assertEquals((double) nSamples, registry.getSampleValue("labels_and_quantiles_count", new String[]{"l"}, new String[]{"a"}), .001);
    assertEquals((1.0 + nSamples) * nSamples / 2.0, registry.getSampleValue("labels_and_quantiles_sum", new String[]{"l"}, new String[]{"a"}), .001);
    assertEquals(1.0, registry.getSampleValue("labels_and_quantiles_min", new String[]{"l"}, new String[]{"a"}), error * 1.0);
    assertEquals((double) nSamples, registry.getSampleValue("labels_and_quantiles_max", new String[]{"l"}, new String[]{"a"}), error * nSamples);
    assertEquals(0.5 * nSamples, getLabelsQuantile(0.5), error * nSamples);
    assertEquals(0.9 * nSamples, getLabelsQuantile(0.9), error * nSamples);
    assertEquals(0.99 * nSamples, getLabelsQuantile(0.99), error * nSamples);
  }

  @Test
  public void testError() {
    for (int n = 1; n <= 5; ++n) {
      double error = Math.pow(10, -n);

      HdrSummary summary = HdrSummary.build()
        .name("test_precision_" + n).help("help")
        .quantile(0.99)
        .numberOfSignificantValueDigits(n)
        .register(registry);

      summary.observe(1.0);
      double val1 = registry.getSampleValue("test_precision_" + n, new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
      assertEquals(1.0, val1, error * 1.0);

      summary.observe(1000.0);
      double val2 = registry.getSampleValue("test_precision_" + n, new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
      assertEquals(1000.0, val2, error * 1000.0);

      summary.observe(1000000.0);
      double val3 = registry.getSampleValue("test_precision_" + n, new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
      assertEquals(1000000.0, val3, error * 1000000.0);

      summary.observe(1000000000.0);
      double val4 = registry.getSampleValue("test_precision_" + n, new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
      assertEquals(1000000000.0, val4, error * 1000000000.0);
    }
  }

  @Test
  public void testMaxAge() throws InterruptedException {
    HdrSummary summary = HdrSummary.build()
      .name("short_attention_span").help("help")
      .quantile(0.99)
      .maxAgeSeconds(1) // After 1s, all observations will be discarded.
      .ageBuckets(2)    // We got 2 buckets, so we discard one bucket every 500ms.
      .register(registry);

    summary.observe(8.0);
    double val1 = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
    assertEquals(8.0, val1, .001); // From bucket 1.

    Thread.sleep(600);
    double val2 = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
    assertEquals(8.0, val2, .001); // From bucket 2.

    Thread.sleep(600);
    double val3 = registry.getSampleValue("short_attention_span", new String[]{"quantile"}, new String[]{Collector.doubleToGoString(0.99)});
    assertEquals(Double.NaN, val3, .001); // From bucket 1 again, but now it is empty.
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

    double elapsed1 = noLabels.time(new Runnable() {
      @Override
      public void run() {
        // no op
      }
    });
    assertEquals(10, elapsed1, .001);

    int result = noLabels.time(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 123;
      }
    });
    assertEquals(123, result);

    HdrSummary.Timer timer = noLabels.startTimer();
    double elapsed2 = timer.observeDuration();
    assertEquals(10, elapsed2, .001);

    assertEquals(3, getCount(), .001);
    assertEquals(30, getSum(), .001);
  }

  @Test
  public void testNoLabels() {
    assertEquals(0.0, getCount(), .001);
    assertEquals(0.0, getSum(), .001);
    assertEquals(null, getMin());
    assertEquals(null, getMax());

    noLabels.observe(2.0);
    assertEquals(1.0, getCount(), .001);
    assertEquals(2.0, getSum(), .001);
    assertEquals(null, getMin());
    assertEquals(null, getMax());
  }

  @Test
  public void testLabels() {
    assertEquals(null, getCount("a"));
    assertEquals(null, getSum("a"));
    assertEquals(null, getMin("a"));
    assertEquals(null, getMax("a"));
    assertEquals(null, getCount("b"));
    assertEquals(null, getSum("b"));
    assertEquals(null, getMin("b"));
    assertEquals(null, getMax("b"));

    labels.labels("a").observe(2.0);
    assertEquals(1.0, getCount("a"), .001);
    assertEquals(2.0, getSum("a"), .001);
    assertEquals(null, getMin("a"));
    assertEquals(null, getMax("a"));
    assertEquals(null, getCount("b"));
    assertEquals(null, getSum("b"));
    assertEquals(null, getMin("b"));
    assertEquals(null, getMax("b"));


    labels.labels("b").observe(3.0);
    assertEquals(1.0, getCount("a"), .001);
    assertEquals(2.0, getSum("a"), .001);
    assertEquals(null, getMin("a"));
    assertEquals(null, getMax("a"));
    assertEquals(1.0, getCount("b"), .001);
    assertEquals(3.0, getSum("b"), .001);
    assertEquals(null, getMin("b"));
    assertEquals(null, getMax("b"));

  }

  @Test
  public void testCollect() {
    labels.labels("a").observe(2.0);
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
