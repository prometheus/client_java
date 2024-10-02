package io.prometheus.metrics.core.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class SummaryTest {

  private final Label label = new Label("name", "value");
  private final Labels labels = Labels.builder().label(label.getName(), label.getValue()).build();

  private PrometheusRegistry registry;
  private Summary noLabels;
  private Summary withLabels;
  private Summary withLabelsAndQuantiles;
  private Summary noLabelsAndQuantiles;

  @Before
  public void setUp() {
    registry = new PrometheusRegistry();
    noLabels =
        Summary.builder().name("nolabels").unit(Unit.SECONDS).help("help").register(registry);
    withLabels =
        Summary.builder()
            .name("labels")
            .unit(Unit.SECONDS)
            .help("help")
            .labelNames(label.getName())
            .register(registry);
    noLabelsAndQuantiles =
        Summary.builder()
            .quantile(0.5, 0.05)
            .quantile(0.9, 0.01)
            .quantile(0.99, 0.001)
            .name("no_labels_and_quantiles")
            .unit(Unit.SECONDS)
            .help("help")
            .register(registry);
    withLabelsAndQuantiles =
        Summary.builder()
            .quantile(0.5)
            .quantile(0.9)
            .quantile(0.99)
            .name("labels_and_quantiles")
            .unit(Unit.SECONDS)
            .help("help")
            .labelNames(label.getName())
            .register(registry);
  }

  @Test
  public void testObserve() {
    noLabels.observe(2);
    assertEquals(1, getCount(noLabels, Labels.EMPTY));
    assertEquals(2.0, getSum(noLabels, Labels.EMPTY), .001);
    noLabels.observe(3);
    assertEquals(2, getCount(noLabels, Labels.EMPTY));
    assertEquals(5.0, getSum(noLabels, Labels.EMPTY), .001);

    withLabels.labelValues(label.getValue()).observe(4);
    assertEquals(1, getCount(withLabels, labels));
    assertEquals(4.0, getSum(withLabels, labels), .001);

    withLabels.labelValues(label.getValue()).observeWithExemplar(6, labels);
    assertEquals(2, getCount(withLabels, labels));
    assertEquals(10.0, getSum(withLabels, labels), .001);
  }

  @Test
  public void testNegativeAmount() {
    noLabels.observe(-1);
    noLabels.observe(-3);
    assertEquals(2, getCount(noLabels, Labels.EMPTY));
    assertEquals(-4.0, getSum(noLabels, Labels.EMPTY), .001);
  }

  @Test
  public void testQuantiles() {
    int nSamples = 1000000; // simulate one million samples

    for (int i = 1; i <= nSamples; i++) {
      // In this test, we observe the numbers from 1 to nSamples,
      // because that makes it easy to verify if the quantiles are correct.
      withLabelsAndQuantiles.labelValues(label.getValue()).observe(i);
      noLabelsAndQuantiles.observe(i);
    }
    assertEquals(
        getQuantile(noLabelsAndQuantiles, 0.5, Labels.EMPTY), 0.5 * nSamples, 0.05 * nSamples);
    assertEquals(
        getQuantile(noLabelsAndQuantiles, 0.9, Labels.EMPTY), 0.9 * nSamples, 0.01 * nSamples);
    assertEquals(
        getQuantile(noLabelsAndQuantiles, 0.99, Labels.EMPTY), 0.99 * nSamples, 0.001 * nSamples);

    assertEquals(getQuantile(withLabelsAndQuantiles, 0.5, labels), 0.5 * nSamples, 0.05 * nSamples);
    assertEquals(getQuantile(withLabelsAndQuantiles, 0.9, labels), 0.9 * nSamples, 0.01 * nSamples);
    assertEquals(
        getQuantile(withLabelsAndQuantiles, 0.99, labels), 0.99 * nSamples, 0.001 * nSamples);
  }

  @Test
  public void testMaxAge() throws InterruptedException {
    Summary summary =
        Summary.builder()
            .quantile(0.99, 0.001)
            .maxAgeSeconds(1) // After 1s, all observations will be discarded.
            .numberOfAgeBuckets(2) // We got 2 buckets, so we discard one bucket every 500ms.
            .name("short_attention_span")
            .help("help")
            .register(registry);
    summary.observe(8.0);
    assertEquals(8.0, getQuantile(summary, 0.99, Labels.EMPTY), 0.0); // From bucket 1.
    Thread.sleep(600);
    assertEquals(8.0, getQuantile(summary, 0.99, Labels.EMPTY), 0.0); // From bucket 2.
    Thread.sleep(600);
    assertEquals(
        Double.NaN,
        getQuantile(summary, 0.99, Labels.EMPTY),
        0.0); // Bucket 1 again, now it is empty.
  }

  @Test
  public void testTimer() {
    int result = noLabels.time(() -> 123);
    assertEquals(123, result);
    assertEquals(1, getCount(noLabels, Labels.EMPTY));

    try (Timer timer = noLabels.startTimer()) {
      timer.observeDuration();
      assertEquals(2, getCount(noLabels, Labels.EMPTY), .001);
    }
  }

  @Test
  public void noLabelsDefaultZeroValue() {
    assertEquals(0.0, getCount(noLabels, Labels.EMPTY), .001);
    assertEquals(0.0, getSum(noLabels, Labels.EMPTY), .001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderInvalidNumberOfAgeBuckets() {
    Summary.builder().name("name").numberOfAgeBuckets(-1).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderInvalidMaxAge() {
    Summary.builder().name("name").maxAgeSeconds(-1).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderInvalidQuantile() {
    Summary.builder().name("name").quantile(42).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderInvalidQuantileError() {
    Summary.builder().name("name").quantile(0.5, 20).build();
  }

  private double getQuantile(Summary summary, double quantile, Labels labels) {
    SummarySnapshot.SummaryDataPointSnapshot datapoint = getDatapoint(summary, labels);
    Quantiles quantiles = datapoint.getQuantiles();
    for (Quantile q : quantiles) {
      if (q.getQuantile() == quantile) {
        return q.getValue();
      }
    }
    fail("Unable to find quantile");
    return 0.0;
  }

  private SummarySnapshot.SummaryDataPointSnapshot getDatapoint(Summary summary, Labels labels) {
    SummarySnapshot snapshot = summary.collect();
    List<SummarySnapshot.SummaryDataPointSnapshot> datapoints = snapshot.getDataPoints();
    assertEquals(1, datapoints.size());
    SummarySnapshot.SummaryDataPointSnapshot datapoint = datapoints.get(0);
    assertEquals(labels, datapoint.getLabels());
    return datapoint;
  }

  private long getCount(Summary summary, Labels labels) {
    SummarySnapshot.SummaryDataPointSnapshot datapoint = getDatapoint(summary, labels);
    return datapoint.getCount();
  }

  private double getSum(Summary summary, Labels labels) {
    SummarySnapshot.SummaryDataPointSnapshot datapoint = getDatapoint(summary, labels);
    return datapoint.getSum();
  }
}
