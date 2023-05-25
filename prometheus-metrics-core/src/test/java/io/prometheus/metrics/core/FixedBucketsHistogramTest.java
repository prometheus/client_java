package io.prometheus.metrics.core;


import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.ClassicHistogramBucket;
import io.prometheus.metrics.model.HistogramSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.DistributionObserver;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FixedBucketsHistogramTest {

  /*
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

   */

  private HistogramSnapshot.HistogramData getData(Histogram histogram, String... labels) {
    return (histogram.collect()).getData().stream()
            .filter(d -> d.getLabels().equals(Labels.of(labels)))
            .findAny()
            .orElseThrow(() -> new RuntimeException("histogram with labels " + labels + " not found"));
  }

  private ClassicHistogramBucket getBucket(Histogram histogram, double le, String... labels) {
    return getData(histogram, labels).getClassicBuckets().stream()
            .filter(b -> b.getUpperBound() == le)
            .findAny()
            .orElseThrow(() -> new RuntimeException("bucket with le=" + le + " not found."));
  }


  @Test
  public void testExemplarSampler() throws Exception {
    SpanContextSupplier spanContextSupplier = new SpanContextSupplier() {
      int callCount = 0;
      @Override
      public String getTraceId() {
        return "traceId-" + callCount;
      }

      @Override
      public String getSpanId() {
        return "spanId-" + callCount;
      }

      @Override
      public boolean isSampled() {
        callCount++;
        return true;
      }
    };
    long sampleIntervalMillis = 10;
    Histogram histogram = Histogram.newBuilder()
            .withName("test")
            // The default number of Exemplars for an exponential histogram is 4.
            // Use 5 buckets to verify that the exemplar sample is configured with the buckets.
            .withClassicBuckets(1.0, 2.0, 3.0, 4.0, Double.POSITIVE_INFINITY)
            .withExemplarConfig(ExemplarConfig.newBuilder()
                    .withSpanContextSupplier(spanContextSupplier)
                    .withSampleIntervalMillis(sampleIntervalMillis)
                    .build())
            .withLabelNames("path")
            .build();

    Exemplar ex1a = Exemplar.newBuilder()
                    .withValue(0.5)
                            .withSpanId("spanId-1")
            .withTraceId("traceId-1")
                    .build();
    Exemplar ex1b = Exemplar.newBuilder()
            .withValue(0.5)
            .withSpanId("spanId-2")
            .withTraceId("traceId-2")
            .build();
    Exemplar ex2a = Exemplar.newBuilder()
            .withValue(4.5)
            .withSpanId("spanId-3")
            .withTraceId("traceId-3")
            .build();
    Exemplar ex2b = Exemplar.newBuilder()
            .withValue(4.5)
            .withSpanId("spanId-4")
            .withTraceId("traceId-4")
            .build();
    Exemplar ex3a = Exemplar.newBuilder()
            .withValue(1.5)
            .withSpanId("spanId-5")
            .withTraceId("traceId-5")
            .build();
    Exemplar ex3b = Exemplar.newBuilder()
            .withValue(1.5)
            .withSpanId("spanId-6")
            .withTraceId("traceId-6")
            .build();
    Exemplar ex4a = Exemplar.newBuilder()
            .withValue(2.5)
            .withSpanId("spanId-7")
            .withTraceId("traceId-7")
            .build();
    Exemplar ex4b = Exemplar.newBuilder()
            .withValue(2.5)
            .withSpanId("spanId-8")
            .withTraceId("traceId-8")
            .build();
    Exemplar ex5a = Exemplar.newBuilder()
            .withValue(3.5)
            .withSpanId("spanId-9")
            .withTraceId("traceId-9")
            .build();
    Exemplar ex5b = Exemplar.newBuilder()
            .withValue(3.5)
            .withSpanId("spanId-10")
            .withTraceId("traceId-10")
            .build();
    histogram.withLabels("/hello").observe(0.5);
    histogram.withLabels("/world").observe(0.5); // different labels are tracked independently, i.e. we don't need to wait for sampleIntervalMillis
    /*
    assertExemplarEquals(ex1a, getBucket(histogram, 1.0, "path", "/hello").getExemplar());
    assertExemplarEquals(ex1b, getBucket(histogram, 1.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, 2.0, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, 2.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, 3.0, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, 3.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, 4.0, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, 4.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, Double.POSITIVE_INFINITY, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, Double.POSITIVE_INFINITY, "path", "/world").getExemplar());
     */
    Thread.sleep(sampleIntervalMillis + 1);
    histogram.withLabels("/hello").observe(4.5);
    histogram.withLabels("/world").observe(4.5);
    /*
    assertExemplarEquals(ex1a, getBucket(histogram, 1.0, "path", "/hello").getExemplar());
    assertExemplarEquals(ex1b, getBucket(histogram, 1.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, 2.0, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, 2.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, 3.0, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, 3.0, "path", "/world").getExemplar());
    assertNull(getBucket(histogram, 4.0, "path", "/hello").getExemplar());
    assertNull(getBucket(histogram, 4.0, "path", "/world").getExemplar());
    assertExemplarEquals(ex2a, getBucket(histogram, Double.POSITIVE_INFINITY, "path", "/hello").getExemplar());
    assertExemplarEquals(ex2b, getBucket(histogram, Double.POSITIVE_INFINITY, "path", "/world").getExemplar());
     */
    Thread.sleep(sampleIntervalMillis + 1);
    histogram.withLabels("/hello").observe(1.5);
    histogram.withLabels("/world").observe(1.5);
    Thread.sleep(sampleIntervalMillis + 1);
    histogram.withLabels("/hello").observe(2.5);
    histogram.withLabels("/world").observe(2.5);
    Thread.sleep(sampleIntervalMillis + 1);
    histogram.withLabels("/hello").observe(3.5);
    histogram.withLabels("/world").observe(3.5);
    /*
    assertExemplarEquals(ex1a, getBucket(histogram, 1.0, "path", "/hello").getExemplar());
    assertExemplarEquals(ex1b, getBucket(histogram, 1.0, "path", "/world").getExemplar());
    assertExemplarEquals(ex3a, getBucket(histogram, 2.0, "path", "/hello").getExemplar());
    assertExemplarEquals(ex3b, getBucket(histogram, 2.0, "path", "/world").getExemplar());
    assertExemplarEquals(ex4a, getBucket(histogram, 3.0, "path", "/hello").getExemplar());
    assertExemplarEquals(ex4b, getBucket(histogram, 3.0, "path", "/world").getExemplar());
    assertExemplarEquals(ex5a, getBucket(histogram, 4.0, "path", "/hello").getExemplar());
    assertExemplarEquals(ex5b, getBucket(histogram, 4.0, "path", "/world").getExemplar());
    assertExemplarEquals(ex2a, getBucket(histogram, Double.POSITIVE_INFINITY, "path", "/hello").getExemplar());
    assertExemplarEquals(ex2b, getBucket(histogram, Double.POSITIVE_INFINITY, "path", "/world").getExemplar());
     */

    Exemplar custom = Exemplar.newBuilder()
            .withValue(3.4)
            .withLabels(Labels.of("key2", "value2", "key1", "value1", "trace_id", "traceId-11", "span_id", "spanId-11"))
            .build();
    Thread.sleep(sampleIntervalMillis + 1);
    histogram.withLabels("/hello").observeWithExemplar(3.4, Labels.of("key1", "value1", "key2", "value2"));
    // custom exemplars have preference, so the automatic exemplar is replaced
    //assertExemplarEquals(custom, getBucket(histogram, 4.0, "path", "/hello").getExemplar());
  }

  /*
  @Test
  public void testObserveWithExemplar() {
    Histogram histogram = Histogram.newBuilder()
            .withName("test")
            .withExemplars()
            .withDefaultBuckets()
            .build();
    Map<String, String> labels = new HashMap<String, String>();
    labels.put("mapKey1", "mapValue1");
    labels.put("mapKey2", "mapValue2");

    histogram.observeWithExemplar(0.5, Labels.of("key", "value"));
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
   */

  /*


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

   */
}
