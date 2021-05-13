package io.prometheus.client.exporter.common;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.exemplars.CounterExemplarSampler;
import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exemplars.ExemplarConfig;
import io.prometheus.client.exemplars.HistogramExemplarSampler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class ExemplarTest {

  private final String defaultTraceId = "default-trace-id";
  private final String defaultSpanId = "default-span-id";
  private final String customTraceId = "custom-trace-id";
  private final String customSpanId = "custom-span-id";
  private final String defaultExemplarLabels = "{span_id=\"" + defaultSpanId + "\",trace_id=\"" + defaultTraceId + "\"}";
  private final String customExemplarLabels = "{span_id=\"" + customSpanId + "\",trace_id=\"" + customTraceId + "\"}";
  private final long timestamp = System.currentTimeMillis();

  private CollectorRegistry registry;

  private final CounterExemplarSampler origCounterExemplarSampler = ExemplarConfig.getCounterExemplarSampler();
  private final HistogramExemplarSampler origHistogramExemplarSampler = ExemplarConfig.getHistogramExemplarSampler();

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    TestExemplarSampler defaultExemplarSampler = new TestExemplarSampler(defaultTraceId, defaultSpanId, timestamp);
    ExemplarConfig.setCounterExemplarSampler(defaultExemplarSampler);
    ExemplarConfig.setHistogramExemplarSampler(defaultExemplarSampler);
  }

  @After
  public void tearDown() {
    ExemplarConfig.setCounterExemplarSampler(origCounterExemplarSampler);
    ExemplarConfig.setHistogramExemplarSampler(origHistogramExemplarSampler);
  }

  @Test
  public void testCounterNoLabelsDefaultExemplar() throws IOException {
    Counter noLabelsDefaultExemplar = Counter.build()
        .name("no_labels_default_exemplar")
        .help("help")
        .register(registry);
    noLabelsDefaultExemplar.inc(3);
    assertOpenMetrics100Format("no_labels_default_exemplar_total 3.0 # " + defaultExemplarLabels + " 3.0 " + timestampString() + "\n");
    assert004Format("no_labels_default_exemplar_total 3.0\n");
    noLabelsDefaultExemplar.inc(2);
    // The TestExemplarSampler always produces a new Exemplar.
    // The Exemplar with value 3.0 should be replaced with an Exemplar with value 5.0.
    assertOpenMetrics100Format("no_labels_default_exemplar_total 5.0 # " + defaultExemplarLabels + " 2.0 " + timestampString() + "\n");
    assert004Format("no_labels_default_exemplar_total 5.0\n");
  }

  @Test
  public void testCounterLabelsDefaultExemplar() throws IOException {
    Counter labelsDefaultExemplar = Counter.build()
        .name("labels_default_exemplar")
        .help("help")
        .labelNames("label")
        .register(registry);
    labelsDefaultExemplar.labels("test").inc();
    assertOpenMetrics100Format("labels_default_exemplar_total{label=\"test\"} 1.0 # " + defaultExemplarLabels + " 1.0 " + timestampString() + "\n");
    assert004Format("labels_default_exemplar_total{label=\"test\",} 1.0\n");
  }

  @Test
  public void testCounterNoLabelsNoExemplar() throws IOException {
    Counter noLabelsNoExemplar = Counter.build()
        .name("no_labels_no_exemplar")
        .help("help")
        .withoutExemplars()
        .register(registry);
    noLabelsNoExemplar.inc();
    assertOpenMetrics100Format("no_labels_no_exemplar_total 1.0\n");
    assert004Format("no_labels_no_exemplar_total 1.0\n");
  }

  @Test
  public void testCounterLabelsNoExemplar() throws IOException {
    Counter labelsNoExemplar = Counter.build()
        .name("labels_no_exemplar")
        .help("help")
        .labelNames("label")
        .withoutExemplars()
        .register(registry);
    labelsNoExemplar.labels("test").inc();
    assertOpenMetrics100Format("labels_no_exemplar_total{label=\"test\"} 1.0\n");
    assert004Format("labels_no_exemplar_total{label=\"test\",} 1.0\n");
  }

  @Test
  public void testCounterNoLabelsCustomExemplar() throws IOException {
    Counter noLabelsCustomExemplar = Counter.build()
        .name("no_labels_custom_exemplar")
        .help("help")
        .withExemplarSampler(new TestExemplarSampler(customTraceId, customSpanId, timestamp))
        .register(registry);
    noLabelsCustomExemplar.inc();
    assertOpenMetrics100Format("no_labels_custom_exemplar_total 1.0 # " + customExemplarLabels + " 1.0 " + timestampString() + "\n");
    assert004Format("no_labels_custom_exemplar_total 1.0\n");
  }

  @Test
  public void testCounterLabelsCustomExemplar() throws IOException {
    Counter labelsCustomExemplar = Counter.build()
        .name("labels_custom_exemplar")
        .help("help")
        .withExemplarSampler(new TestExemplarSampler(customTraceId, customSpanId, timestamp))
        .labelNames("label")
        .register(registry);
    labelsCustomExemplar.labels("test").inc();
    assertOpenMetrics100Format("labels_custom_exemplar_total{label=\"test\"} 1.0 # " + customExemplarLabels + " 1.0 " + timestampString() + "\n");
    assert004Format("labels_custom_exemplar_total{label=\"test\",} 1.0\n");
  }

  @Test
  public void testGaugeNoLabels() throws IOException {
    Gauge noLabelsDefaultExemplar = Gauge.build()
        .name("no_labels_default_exemplar")
        .help("help")
        .register(registry);
    noLabelsDefaultExemplar.set(37);
    // Gauges do not have Exemplars according to the OpenMetrics spec.
    // Make sure there are no Exemplars.
    assertOpenMetrics100Format("no_labels_default_exemplar 37.0\n");
    assert004Format("no_labels_default_exemplar 37.0\n");
  }

  @Test
  public void testGaugeLabels() throws IOException {
    Gauge labelsDefaultExemplar = Gauge.build()
        .name("labels_default_exemplar")
        .help("help")
        .labelNames("label")
        .register(registry);
    labelsDefaultExemplar.labels("test").inc();
    // Gauges do not have Exemplars according to the OpenMetrics spec.
    // Make sure there are no Exemplars.
    assertOpenMetrics100Format("labels_default_exemplar{label=\"test\"} 1.0\n");
    assert004Format("labels_default_exemplar{label=\"test\",} 1.0\n");
  }

  @Test
  public void testHistogramNoLabelsDefaultExemplar() throws IOException {
    Histogram noLabelsDefaultExemplar = Histogram.build()
        .name("no_labels_default_exemplar")
        .help("help")
        .buckets(5.0, 8.0)
        .register(registry);
    noLabelsDefaultExemplar.observe(3.0);
    noLabelsDefaultExemplar.observe(6.0);
    noLabelsDefaultExemplar.observe(9.0);

    assertOpenMetrics100Format("no_labels_default_exemplar_bucket{le=\"5.0\"} 1.0 # " + defaultExemplarLabels + " 3.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_bucket{le=\"8.0\"} 2.0 # " + defaultExemplarLabels + " 6.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_bucket{le=\"+Inf\"} 3.0 # " + defaultExemplarLabels + " 9.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_count 3.0\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_sum 18.0\n");

    assert004Format("no_labels_default_exemplar_bucket{le=\"5.0\",} 1.0\n");
    assert004Format("no_labels_default_exemplar_bucket{le=\"8.0\",} 2.0\n");
    assert004Format("no_labels_default_exemplar_bucket{le=\"+Inf\",} 3.0\n");
    assert004Format("no_labels_default_exemplar_count 3.0\n");
    assert004Format("no_labels_default_exemplar_sum 18.0\n");

    noLabelsDefaultExemplar.observe(4.0);
    // The TestExemplarSampler always produces a new Exemplar.
    // The Exemplar with value 3.0 should be replaced with an Exemplar with value 4.0.

    assertOpenMetrics100Format("no_labels_default_exemplar_bucket{le=\"5.0\"} 2.0 # " + defaultExemplarLabels + " 4.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_bucket{le=\"8.0\"} 3.0 # " + defaultExemplarLabels + " 6.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_bucket{le=\"+Inf\"} 4.0 # " + defaultExemplarLabels + " 9.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_count 4.0\n");
    assertOpenMetrics100Format("no_labels_default_exemplar_sum 22.0\n");

    assert004Format("no_labels_default_exemplar_bucket{le=\"5.0\",} 2.0\n");
    assert004Format("no_labels_default_exemplar_bucket{le=\"8.0\",} 3.0\n");
    assert004Format("no_labels_default_exemplar_bucket{le=\"+Inf\",} 4.0\n");
    assert004Format("no_labels_default_exemplar_count 4.0\n");
    assert004Format("no_labels_default_exemplar_sum 22.0\n");
  }

  @Test
  public void testHistogramLabelsDefaultExemplar() throws IOException {
    Histogram noLabelsDefaultExemplar = Histogram.build()
        .name("labels_default_exemplar")
        .help("help")
        .buckets(5.0)
        .labelNames("label")
        .register(registry);
    noLabelsDefaultExemplar.labels("test").observe(3.0);
    noLabelsDefaultExemplar.labels("test").observe(6.0);

    assertOpenMetrics100Format("labels_default_exemplar_bucket{label=\"test\",le=\"5.0\"} 1.0 # " + defaultExemplarLabels + " 3.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("labels_default_exemplar_bucket{label=\"test\",le=\"+Inf\"} 2.0 # " + defaultExemplarLabels + " 6.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("labels_default_exemplar_count{label=\"test\"} 2.0\n");
    assertOpenMetrics100Format("labels_default_exemplar_sum{label=\"test\"} 9.0\n");

    assert004Format("labels_default_exemplar_bucket{label=\"test\",le=\"5.0\",} 1.0\n");
    assert004Format("labels_default_exemplar_bucket{label=\"test\",le=\"+Inf\",} 2.0\n");
    assert004Format("labels_default_exemplar_count{label=\"test\",} 2.0\n");
    assert004Format("labels_default_exemplar_sum{label=\"test\",} 9.0\n");
  }

  @Test
  public void testHistogramNoLabelsNoExemplar() throws IOException {
    Histogram noLabelsNoExemplar = Histogram.build()
        .name("no_labels_no_exemplar")
        .help("help")
        .buckets(5.0)
        .withoutExemplars()
        .register(registry);
    noLabelsNoExemplar.observe(3.0);
    noLabelsNoExemplar.observe(6.0);
    assertOpenMetrics100Format("no_labels_no_exemplar_bucket{le=\"5.0\"} 1.0\n");
    assertOpenMetrics100Format("no_labels_no_exemplar_bucket{le=\"+Inf\"} 2.0\n");
    assertOpenMetrics100Format("no_labels_no_exemplar_count 2.0\n");
    assertOpenMetrics100Format("no_labels_no_exemplar_sum 9.0\n");

    assert004Format("no_labels_no_exemplar_bucket{le=\"5.0\",} 1.0\n");
    assert004Format("no_labels_no_exemplar_bucket{le=\"+Inf\",} 2.0\n");
    assert004Format("no_labels_no_exemplar_count 2.0\n");
    assert004Format("no_labels_no_exemplar_sum 9.0\n");
  }

  @Test
  public void testHistogramLabelsNoExemplar() throws IOException {
    Histogram labelsNoExemplar = Histogram.build()
        .name("labels_no_exemplar")
        .help("help")
        .buckets(5.0)
        .labelNames("label")
        .withoutExemplars()
        .register(registry);
    labelsNoExemplar.labels("test").observe(3.0);
    labelsNoExemplar.labels("test").observe(6.0);

    assertOpenMetrics100Format("labels_no_exemplar_bucket{label=\"test\",le=\"5.0\"} 1.0\n");
    assertOpenMetrics100Format("labels_no_exemplar_bucket{label=\"test\",le=\"+Inf\"} 2.0\n");
    assertOpenMetrics100Format("labels_no_exemplar_count{label=\"test\"} 2.0\n");
    assertOpenMetrics100Format("labels_no_exemplar_sum{label=\"test\"} 9.0\n");

    assert004Format("labels_no_exemplar_bucket{label=\"test\",le=\"5.0\",} 1.0\n");
    assert004Format("labels_no_exemplar_bucket{label=\"test\",le=\"+Inf\",} 2.0\n");
    assert004Format("labels_no_exemplar_count{label=\"test\",} 2.0\n");
    assert004Format("labels_no_exemplar_sum{label=\"test\",} 9.0\n");
  }

  @Test
  public void testHistogramNoLabelsCustomExemplar() throws IOException {
    Histogram noLabelsCustomExemplar = Histogram.build()
        .name("no_labels_custom_exemplar")
        .help("help")
        .buckets(5.0)
        .withExemplarSampler(new TestExemplarSampler(customTraceId, customSpanId, timestamp))
        .register(registry);
    noLabelsCustomExemplar.observe(3.0);
    noLabelsCustomExemplar.observe(6.0);

    assertOpenMetrics100Format("no_labels_custom_exemplar_bucket{le=\"5.0\"} 1.0 # " + customExemplarLabels + " 3.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_custom_exemplar_bucket{le=\"+Inf\"} 2.0 # " + customExemplarLabels + " 6.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("no_labels_custom_exemplar_count 2.0\n");
    assertOpenMetrics100Format("no_labels_custom_exemplar_sum 9.0\n");

    assert004Format("no_labels_custom_exemplar_bucket{le=\"5.0\",} 1.0\n");
    assert004Format("no_labels_custom_exemplar_bucket{le=\"+Inf\",} 2.0\n");
    assert004Format("no_labels_custom_exemplar_count 2.0\n");
    assert004Format("no_labels_custom_exemplar_sum 9.0\n");
  }

  @Test
  public void testHistogramLabelsCustomExemplar() throws IOException {
    Histogram labelsCustomExemplar = Histogram.build()
        .name("labels_custom_exemplar")
        .help("help")
        .buckets(5.0)
        .withExemplarSampler(new TestExemplarSampler(customTraceId, customSpanId, timestamp))
        .labelNames("label")
        .register(registry);
    labelsCustomExemplar.labels("test").observe(3.0);
    labelsCustomExemplar.labels("test").observe(6.0);

    assertOpenMetrics100Format("labels_custom_exemplar_bucket{label=\"test\",le=\"5.0\"} 1.0 # " + customExemplarLabels + " 3.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("labels_custom_exemplar_bucket{label=\"test\",le=\"+Inf\"} 2.0 # " + customExemplarLabels + " 6.0 " + timestampString() + "\n");
    assertOpenMetrics100Format("labels_custom_exemplar_count{label=\"test\"} 2.0\n");
    assertOpenMetrics100Format("labels_custom_exemplar_sum{label=\"test\"} 9.0\n");

    assert004Format("labels_custom_exemplar_bucket{label=\"test\",le=\"5.0\",} 1.0\n");
    assert004Format("labels_custom_exemplar_bucket{label=\"test\",le=\"+Inf\",} 2.0\n");
    assert004Format("labels_custom_exemplar_count{label=\"test\",} 2.0\n");
    assert004Format("labels_custom_exemplar_sum{label=\"test\",} 9.0\n");
  }


  @Test
  public void testSummaryNoLabels() throws IOException {
    Summary noLabelsDefaultExemplar = Summary.build()
        .name("no_labels")
        .help("help")
        .quantile(0.5, 0.01)
        .register(registry);
    for (int i=1; i<=11; i++) { // median is 5
      noLabelsDefaultExemplar.observe(i);
    }
    // Summaries don't have Exemplars according to the OpenMetrics spec.
    assertOpenMetrics100Format("no_labels{quantile=\"0.5\"} 5.0\n");
    assertOpenMetrics100Format("no_labels_count 11.0\n");
    assertOpenMetrics100Format("no_labels_sum 66.0\n");

    assert004Format("no_labels{quantile=\"0.5\",} 5.0\n");
    assert004Format("no_labels_count 11.0\n");
    assert004Format("no_labels_sum 66.0\n");
  }

  @Test
  public void testSummaryLabels() throws IOException {
    Summary labelsNoExemplar = Summary.build()
        .name("labels")
        .help("help")
        .labelNames("label")
        .quantile(0.5, 0.01)
        .register(registry);
    for (int i=1; i<=11; i++) { // median is 5
      labelsNoExemplar.labels("test").observe(i);
    }
    // Summaries don't have Exemplars according to the OpenMetrics spec.
    assertOpenMetrics100Format("labels{label=\"test\",quantile=\"0.5\"} 5.0\n");
    assertOpenMetrics100Format("labels_count{label=\"test\"} 11.0\n");
    assertOpenMetrics100Format("labels_sum{label=\"test\"} 66.0\n");

    assert004Format("labels{label=\"test\",quantile=\"0.5\",} 5.0\n");
    assert004Format("labels_count{label=\"test\",} 11.0\n");
    assert004Format("labels_sum{label=\"test\",} 66.0\n");
  }

  private static class TestExemplarSampler implements CounterExemplarSampler, HistogramExemplarSampler {

    private final String traceId;
    private final String spanId;
    private final long timestamp;

    private TestExemplarSampler(String traceId, String spanId, long timestamp) {
      this.traceId = traceId;
      this.spanId = spanId;
      this.timestamp = timestamp;
    }

    @Override
    public Exemplar sample(double increment, Exemplar previous) {
      return new Exemplar(increment, timestamp, "trace_id", traceId, "span_id", spanId);
    }

    @Override
    public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
      return new Exemplar(value, timestamp, "trace_id", traceId, "span_id", spanId);
    }
  }

  private void assert004Format(String line) throws IOException {
    StringWriter writer = new StringWriter();
    TextFormat.write004(writer, registry.metricFamilySamples());
    String metrics = writer.toString();
    Assert.assertTrue("Line not found. Expected metric:\n" + line + "Actual metrics:\n" + metrics,
        metrics.contains("\n" + line));
  }

  private void assertOpenMetrics100Format(String line) throws IOException {
    StringWriter writer = new StringWriter();
    TextFormat.writeOpenMetrics100(writer, registry.metricFamilySamples());
    String metrics = writer.toString();
    Assert.assertTrue("Line not found. Expected metric:\n" + line + "Actual metrics:\n" + metrics,
        metrics.contains("\n" + line));
  }

  private String timestampString() throws IOException {
    StringWriter stringWriter = new StringWriter();
    TextFormat.omWriteTimestamp(stringWriter, timestamp);
    return stringWriter.toString();
  }
}
