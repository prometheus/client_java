package io.prometheus.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class HistogramTest {

  CollectorRegistry registry;
  Histogram noLabels, labels;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    noLabels = Histogram.build().name("nolabels").help("help").register(registry);
    labels = Histogram.build().name("labels").help("help").labelNames("l").register(registry);
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
  private double getBucket(double b) {
    return registry.getSampleValue("nolabels_bucket", 
        new String[]{"le"},
        new String[]{Collector.doubleToGoString(b)}).doubleValue();
  }
  
  @Test
  public void testObserve() {
    noLabels.observe(2);
    assertEquals(1.0, getCount(), .001);
    assertEquals(2.0, getSum(), .001);
    assertEquals(0.0, getBucket(1), .001);
    assertEquals(1.0, getBucket(2.5), .001);
    noLabels.labels().observe(4);
    assertEquals(2.0, getCount(), .001);
    assertEquals(6.0, getSum(), .001);
    assertEquals(0.0, getBucket(1), .001);
    assertEquals(1.0, getBucket(2.5), .001);
    assertEquals(2.0, getBucket(5), .001);
    assertEquals(2.0, getBucket(7.5), .001);
    assertEquals(2.0, getBucket(10), .001);
    assertEquals(2.0, getBucket(Double.POSITIVE_INFINITY), .001);
  }

  @Test
  public void testBoundaryConditions() {
    // Equal to a bucket.
    noLabels.observe(2.5);
    assertEquals(0.0, getBucket(1), .001);
    assertEquals(1.0, getBucket(2.5), .001);
    noLabels.labels().observe(Double.POSITIVE_INFINITY);

    // Infinity.
    assertEquals(0.0, getBucket(1), .001);
    assertEquals(1.0, getBucket(2.5), .001);
    assertEquals(1.0, getBucket(5), .001);
    assertEquals(1.0, getBucket(7.5), .001);
    assertEquals(1.0, getBucket(10), .001);
    assertEquals(2.0, getBucket(Double.POSITIVE_INFINITY), .001);
  }

  @Test
  public void testManualBuckets() {
    Histogram h = Histogram.build().name("h").help("help").buckets(1, 2).create();
    assertArrayEquals(new double[]{1, 2, Double.POSITIVE_INFINITY}, h.getBuckets(), .001);
  }

  @Test
  public void testManualBucketsInfinityAlreadyIncluded() {
    Histogram h = Histogram.build().buckets(1, 2, Double.POSITIVE_INFINITY).name("h").help("help").create();
    assertArrayEquals(new double[]{1, 2, Double.POSITIVE_INFINITY}, h.getBuckets(), .001);
  }

  @Test
  public void testLinearBuckets() {
    Histogram h = Histogram.build().name("h").help("help").linearBuckets(1, 2, 3).create();
    assertArrayEquals(new double[]{1, 3, 5, Double.POSITIVE_INFINITY}, h.getBuckets(), .001);
  }

  @Test
  public void testExponentialBuckets() {
    Histogram h = Histogram.build().name("h").help("help").exponentialBuckets(2, 2.5, 3).create();
    assertArrayEquals(new double[]{2, 5, 12.5, Double.POSITIVE_INFINITY}, h.getBuckets(), .001);
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

    double elapsed = noLabels.time(new Runnable() {
      @Override
      public void run() {
        //no op
      }
    });
    assertEquals(10, elapsed, .001);

    Histogram.Timer timer = noLabels.startTimer();
    elapsed = timer.observeDuration();
    assertEquals(2, getCount(), .001);
    assertEquals(20, getSum(), .001);
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

  @Test(expected=IllegalStateException.class)
  public void testLeLabelThrows() {
    Histogram.build().name("labels").help("help").labelNames("le").create();
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
    ArrayList<String> labelNamesLe = new ArrayList<String>(labelNames);
    labelNamesLe.add("le");
    for (String bucket: new String[]{"0.005", "0.01", "0.025", "0.05", "0.075", "0.1", "0.25", "0.5", "0.75", "1.0"}) {
      ArrayList<String> labelValuesLe = new ArrayList<String>(labelValues);
      labelValuesLe.add(bucket);
      samples.add(new Collector.MetricFamilySamples.Sample("labels_bucket", labelNamesLe, labelValuesLe, 0.0));
    }
    for (String bucket: new String[]{"2.5", "5.0", "7.5", "10.0", "+Inf"}) {
      ArrayList<String> labelValuesLe = new ArrayList<String>(labelValues);
      labelValuesLe.add(bucket);
      samples.add(new Collector.MetricFamilySamples.Sample("labels_bucket", labelNamesLe, labelValuesLe, 1.0));
    }
    samples.add(new Collector.MetricFamilySamples.Sample("labels_count", labelNames, labelValues, 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_sum", labelNames, labelValues, 2.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.HISTOGRAM, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

  @Test
  public void testChildAndValuePublicApi() throws Exception {
    assertTrue(Modifier.isPublic(Histogram.Child.class.getModifiers()));

    final Method getMethod = Histogram.Child.class.getMethod("get");
    assertTrue(Modifier.isPublic(getMethod.getModifiers()));
    assertEquals(Histogram.Child.Value.class, getMethod.getReturnType());

    assertTrue(Modifier.isPublic(Histogram.Child.Value.class.getModifiers()));
    assertTrue(Modifier.isPublic(Histogram.Child.Value.class.getField("sum").getModifiers()));
    assertTrue(Modifier.isPublic(Histogram.Child.Value.class.getField("buckets").getModifiers()));
  }

}
