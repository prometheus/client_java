package io.prometheus.client;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.rules.ExpectedException.none;


public class HistogramTest {

  CollectorRegistry registry;
  Histogram noLabels, labels;

  @Rule
  public final ExpectedException thrown = none();

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
    return getCount("nolabels");
  }

  private double getCount(String name) {
    return registry.getSampleValue(name + "_count").doubleValue();
  }

  private double getSum() {
    return getSum("nolabels");
  }

  private double getSum(String name) {
    return registry.getSampleValue(name + "_sum").doubleValue();
  }

  private double getBucket(double b) {
    return getBucket(b, "nolabels");
  }

  private double getBucket(double b, String name) {
    return registry.getSampleValue(name + "_bucket",
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
  // See https://github.com/prometheus/client_java/issues/646
  public void testNegativeAmount() {
    Histogram histogram = Histogram.build()
        .name("histogram")
        .help("test histogram for negative values")
        .buckets(-10, -5, 0, 5, 10)
        .register(registry);
    double expectedCount = 0;
    double expectedSum = 0;
    for (int i=10; i>=-11; i--) {
      histogram.observe(i);
      expectedCount++;
      expectedSum += i;
      assertEquals(expectedSum, getSum("histogram"), .001);
      assertEquals(expectedCount, getCount("histogram"), .001);
    }
    double[] expectedBucketValues = new double[]{2.0, 7.0, 12.0, 17.0, 22.0, 22.0}; // buckets -10, -5, 0, 5, 10, +Inf
    for (int i=0; i<expectedBucketValues.length; i++) {
      double bucket = histogram.getBuckets()[i];
      assertEquals(expectedBucketValues[i], getBucket(bucket, "histogram"), .001);
    }
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

    int result = noLabels.time(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 123;
      }
    });
    assertEquals(123, result);

    Histogram.Timer timer = noLabels.startTimer();
    elapsed = timer.observeDuration();
    assertEquals(3, getCount(), .001);
    assertEquals(30, getSum(), .001);
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
  public void testLeLabelThrows() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Histogram cannot have a label named 'le'.");
    Histogram.build().name("labels").help("help").labelNames("le").create();
  }

  @Test
  public void testObserveWithExemplar() {
    Map<String, String> labels = new HashMap<String, String>();
    labels.put("mapKey1", "mapValue1");
    labels.put("mapKey2", "mapValue2");

    noLabels.observeWithExemplar(0.5, "key", "value");
    assertExemplar(noLabels, 0.5, "key", "value");

    noLabels.observeWithExemplar(0.5);
    assertExemplar(noLabels, 0.5);

    noLabels.observeWithExemplar(0.5, labels);
    assertExemplar(noLabels, 0.5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

    // default buckets are {.005, .01, .025, .05, .075, .1, .25, .5, .75, 1, 2.5, 5, 7.5, 10}
    noLabels.observeWithExemplar(2.0, "key1", "value1", "key2", "value2");
    assertExemplar(noLabels, 2.0, "key1", "value1", "key2", "value2");
    assertExemplar(noLabels, 0.5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

    noLabels.observeWithExemplar(0.4, new HashMap<String, String>()); // same bucket as 0.5
    assertNoExemplar(noLabels, 0.5);
    assertExemplar(noLabels, 0.4);
    assertExemplar(noLabels, 2.0, "key1", "value1", "key2", "value2");

    noLabels.observeWithExemplar(2.0, (String[]) null); // should not alter the exemplar
    assertExemplar(noLabels, 2.0, "key1", "value1", "key2", "value2");

    noLabels.observeWithExemplar(2.0, (Map<String, String>) null); // should not alter the exemplar
    assertExemplar(noLabels, 2.0, "key1", "value1", "key2", "value2");
  }

  @Test
  public void testTimeWithExemplar() {
    Map<String, String> labels = new HashMap<String, String>();
    labels.put("mapKey1", "mapValue1");
    labels.put("mapKey2", "mapValue2");

    noLabels.timeWithExemplar(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(15);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    assertExemplar(noLabels, 0.015);

    noLabels.timeWithExemplar(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        Thread.sleep(20);
        return null;
      }
    }, labels);
    assertNoExemplar(noLabels, 0.015);
    assertExemplar(noLabels, 0.02,"mapKey1", "mapValue1", "mapKey2", "mapValue2");
  }

  private void assertExemplar(Histogram histogram, double value, String... labels) {
    List<Collector.MetricFamilySamples> mfs = histogram.collect();
    double lowerBound;
    double upperBound = Double.NEGATIVE_INFINITY;
    for (Sample bucket : mfs.get(0).samples) {
      if (!bucket.name.endsWith("_bucket")) {
        continue;
      }
      lowerBound = upperBound;
      if ("+Inf".equals(findLabelValue(bucket, "le"))) {
        upperBound = Double.POSITIVE_INFINITY;
      } else {
        upperBound = Double.parseDouble(findLabelValue(bucket, "le"));
      }
      if (lowerBound < value && value <= upperBound) {
        Assert.assertNotNull("No exemplar found in bucket [" + lowerBound + ", " + upperBound + "]", bucket.exemplar);
        Assert.assertEquals(value, bucket.exemplar.getValue(), 0.01);
        Assert.assertEquals(labels.length/2, bucket.exemplar.getNumberOfLabels());
        for (int i=0; i<labels.length; i+=2) {
          Assert.assertEquals(labels[i], bucket.exemplar.getLabelName(i/2));
          Assert.assertEquals(labels[i+1], bucket.exemplar.getLabelValue(i/2));
        }
        return;
      }
    }
    throw new AssertionError("exemplar not found in histogram");
  }

  private void assertNoExemplar(Histogram histogram, double value) {
    List<Collector.MetricFamilySamples> mfs = histogram.collect();
    double lowerBound;
    double upperBound = Double.NEGATIVE_INFINITY;
    for (Sample bucket : mfs.get(0).samples) {
      if (!bucket.name.endsWith("_bucket")) {
        continue;
      }
      lowerBound = upperBound;
      if ("+Inf".equals(findLabelValue(bucket, "le"))) {
        upperBound = Double.POSITIVE_INFINITY;
      } else {
        upperBound = Double.parseDouble(findLabelValue(bucket, "le"));
      }
      if (lowerBound < value && value <= upperBound) {
        if (bucket.exemplar != null) {
          Assert.assertNotEquals("expecting no exemplar with value " + value, value, bucket.exemplar.getValue(), 0.0001);
        }
      }
    }
  }

  private String findLabelValue(Sample sample, String labelName) {
    for (int i = 0; i < sample.labelNames.size(); i++) {
      if (sample.labelNames.get(i).equals(labelName)) {
        return sample.labelValues.get(i);
      }
    }
    throw new AssertionError("label " + labelName + " not found in " + sample);
  }

  @Test
  public void testCollect() {
    labels.labels("a").observe(2);
    List<Collector.MetricFamilySamples> mfs = labels.collect();

    ArrayList<Sample> samples = new ArrayList<Sample>();
    ArrayList<String> labelNames = new ArrayList<String>();
    labelNames.add("l");
    ArrayList<String> labelValues = new ArrayList<String>();
    labelValues.add("a");
    ArrayList<String> labelNamesLe = new ArrayList<String>(labelNames);
    labelNamesLe.add("le");
    for (String bucket: new String[]{"0.005", "0.01", "0.025", "0.05", "0.075", "0.1", "0.25", "0.5", "0.75", "1.0"}) {
      ArrayList<String> labelValuesLe = new ArrayList<String>(labelValues);
      labelValuesLe.add(bucket);
      samples.add(new Sample("labels_bucket", labelNamesLe, labelValuesLe, 0.0));
    }
    for (String bucket: new String[]{"2.5", "5.0", "7.5", "10.0", "+Inf"}) {
      ArrayList<String> labelValuesLe = new ArrayList<String>(labelValues);
      labelValuesLe.add(bucket);
      samples.add(new Sample("labels_bucket", labelNamesLe, labelValuesLe, 1.0));
    }
    samples.add(new Sample("labels_count", labelNames, labelValues, 1.0));
    samples.add(new Sample("labels_sum", labelNames, labelValues, 2.0));
    samples.add(new Sample("labels_created", labelNames, labelValues, labels.labels("a").get().created / 1000.0));

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
