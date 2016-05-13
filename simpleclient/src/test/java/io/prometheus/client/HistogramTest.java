package io.prometheus.client;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    Histogram.Child.timeProvider = new Histogram.TimeProvider();
  }

  private double getCount() {
    return registry.getSampleValue("nolabels_count").doubleValue();
  }
  private double getSum() {
    return registry.getSampleValue("nolabels_sum").doubleValue();
  }
  private double getBucket(double b) {
    return registry.getSampleValue("nolabels_bucket",
            "le", Collector.doubleToGoString(b)).doubleValue();
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
    Histogram.Child.timeProvider = new Histogram.TimeProvider() {
      long value = (long)(30 * 1e9);
      long nanoTime() {
        value += (long)(10 * 1e9);
        return value;
      }
    };
    Histogram.Timer timer = noLabels.startTimer();
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
    return registry.getSampleValue("labels_count", "l", labelValue);
  }
  private Double getLabelsSum(String labelValue) {
    return registry.getSampleValue("labels_sum", "l", labelValue);
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
    
    List<Collector.MetricFamilySamples.Sample> samples = new ArrayList<Collector.MetricFamilySamples.Sample>();
    Map<String, String> labels = singletonMap("l", "a");
    for (String bucket: new String[]{"0.005", "0.01", "0.025", "0.05", "0.075", "0.1", "0.25", "0.5", "0.75", "1.0"}) {
      Map<String, String> labelsWithLe = new HashMap<String, String>(labels);
      labelsWithLe.put("le", bucket);
      samples.add(new Collector.MetricFamilySamples.Sample("labels_bucket", labelsWithLe, 0.0));
    }
    for (String bucket: new String[]{"2.5", "5.0", "7.5", "10.0", "+Inf"}) {
      Map<String, String> labelsWithLe = new HashMap<String, String>(labels);
      labelsWithLe.put("le", bucket);
      samples.add(new Collector.MetricFamilySamples.Sample("labels_bucket", labelsWithLe, 1.0));
    }
    samples.add(new Collector.MetricFamilySamples.Sample("labels_count", labels, 1.0));
    samples.add(new Collector.MetricFamilySamples.Sample("labels_sum", labels, 2.0));
    Collector.MetricFamilySamples mfsFixture = new Collector.MetricFamilySamples("labels", Collector.Type.HISTOGRAM, "help", samples);

    assertEquals(1, mfs.size());
    assertEquals(mfsFixture, mfs.get(0));
  }

}
