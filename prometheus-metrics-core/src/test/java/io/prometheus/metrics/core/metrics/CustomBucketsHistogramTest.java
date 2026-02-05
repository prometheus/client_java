package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_33_5.Metrics;
import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBucket;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests to verify that client_java supports native histograms with custom buckets
 * (NHCB).
 *
 * <p>According to the Prometheus specification
 * (https://prometheus.io/docs/specs/native_histograms/), native histograms with custom buckets
 * (schema -53) are exposed as classic histograms with custom bucket boundaries. Prometheus servers
 * can then convert these to NHCB upon ingestion when configured with
 * convert_classic_histograms_to_nhcb.
 *
 * <p>These tests verify that:
 *
 * <ul>
 *   <li>Histograms with custom bucket boundaries can be created
 *   <li>Custom buckets are properly exposed in both text and protobuf formats
 *   <li>Both classic-only and dual (classic+native) histograms work with custom buckets
 *   <li>Various custom bucket configurations (linear, exponential, arbitrary) work correctly
 * </ul>
 *
 * <p>See issue #1838 for more context.
 */
class CustomBucketsHistogramTest {

  @Test
  void testCustomBucketsWithArbitraryBoundaries() {
    // Create a histogram with arbitrary custom bucket boundaries
    Histogram histogram =
        Histogram.builder()
            .name("http_request_duration_seconds")
            .help("HTTP request duration with custom buckets")
            .classicUpperBounds(0.01, 0.05, 0.1, 0.5, 1.0, 5.0, 10.0)
            .build();

    // Observe some values
    histogram.observe(0.008);
    histogram.observe(0.045);
    histogram.observe(0.3);
    histogram.observe(2.5);
    histogram.observe(7.8);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify custom bucket boundaries are set correctly
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds)
        .containsExactly(0.01, 0.05, 0.1, 0.5, 1.0, 5.0, 10.0, Double.POSITIVE_INFINITY);

    // Verify observations are distributed correctly across buckets
    // Note: counts are non-cumulative (count for that specific bucket only)
    ClassicHistogramBuckets buckets = data.getClassicBuckets();
    assertThat(buckets.getCount(0)).isEqualTo(1); // <= 0.01: (0.008)
    assertThat(buckets.getCount(1)).isEqualTo(1); // (0.01, 0.05]: (0.045)
    assertThat(buckets.getCount(2)).isEqualTo(0); // (0.05, 0.1]: none
    assertThat(buckets.getCount(3)).isEqualTo(1); // (0.1, 0.5]: (0.3)
    assertThat(buckets.getCount(4)).isEqualTo(0); // (0.5, 1.0]: none
    assertThat(buckets.getCount(5)).isEqualTo(1); // (1.0, 5.0]: (2.5)
    assertThat(buckets.getCount(6)).isEqualTo(1); // (5.0, 10.0]: (7.8)
    assertThat(buckets.getCount(7)).isEqualTo(0); // (10.0, +Inf]: none

    // Verify count and sum
    assertThat(data.getCount()).isEqualTo(5);
    assertThat(data.getSum()).isCloseTo(10.653, offset(0.01));
  }

  @Test
  void testCustomBucketsWithLinearBoundaries() {
    // Create a histogram with linear custom bucket boundaries
    // This represents a use case where equal-width buckets are needed
    Histogram histogram =
        Histogram.builder()
            .name("queue_size")
            .help("Queue size with linear buckets")
            .classicLinearUpperBounds(10.0, 10.0, 10) // start=10, width=10, count=10
            .build();

    // Observe some values
    for (int i = 5; i <= 95; i += 10) {
      histogram.observe(i);
    }

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify linear bucket boundaries
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds)
        .containsExactly(
            10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0, Double.POSITIVE_INFINITY);

    // Verify observations
    assertThat(data.getCount()).isEqualTo(10);
  }

  @Test
  void testCustomBucketsWithExponentialBoundaries() {
    // Create a histogram with exponential custom bucket boundaries
    // This is useful for metrics that span multiple orders of magnitude
    Histogram histogram =
        Histogram.builder()
            .name("response_size_bytes")
            .help("Response size with exponential buckets")
            .classicExponentialUpperBounds(100.0, 10.0, 5) // start=100, factor=10, count=5
            .build();

    // Observe some values across different magnitudes
    histogram.observe(50);
    histogram.observe(500);
    histogram.observe(5000);
    histogram.observe(50000);
    histogram.observe(500000);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify exponential bucket boundaries
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds)
        .containsExactly(100.0, 1000.0, 10000.0, 100000.0, 1000000.0, Double.POSITIVE_INFINITY);

    // Verify observations
    assertThat(data.getCount()).isEqualTo(5);
  }

  @Test
  void testCustomBucketsClassicOnlyHistogram() {
    // Verify that custom buckets work with classic-only histograms
    Histogram histogram =
        Histogram.builder()
            .name("test_classic_only")
            .help("Classic-only histogram with custom buckets")
            .classicOnly()
            .classicUpperBounds(1.0, 5.0, 10.0)
            .build();

    histogram.observe(2.0);
    histogram.observe(7.0);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify it's a classic-only histogram
    assertThat(data.getNativeSchema()).isEqualTo(HistogramSnapshot.CLASSIC_HISTOGRAM);

    // Verify custom buckets
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds).containsExactly(1.0, 5.0, 10.0, Double.POSITIVE_INFINITY);
  }

  @Test
  void testCustomBucketsDualModeHistogram() {
    // Verify that custom buckets work with dual-mode (classic+native) histograms
    // This is the default mode and most relevant for NHCB support
    Histogram histogram =
        Histogram.builder()
            .name("test_dual_mode")
            .help("Dual-mode histogram with custom buckets")
            .classicUpperBounds(0.1, 1.0, 10.0)
            .build();

    histogram.observe(0.5);
    histogram.observe(5.0);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify it has both classic and native representations
    assertThat(data.getClassicBuckets().size()).isGreaterThan(0);
    assertThat(data.getNativeSchema()).isNotEqualTo(HistogramSnapshot.CLASSIC_HISTOGRAM);

    // Verify custom classic buckets
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds).containsExactly(0.1, 1.0, 10.0, Double.POSITIVE_INFINITY);

    // Verify native histogram is also populated
    long nativeTotalCount =
        data.getNativeBucketsForPositiveValues().stream()
            .mapToLong(bucket -> bucket.getCount())
            .sum();
    assertThat(nativeTotalCount).isEqualTo(2);
  }

  @Test
  void testCustomBucketsTextFormatOutput() throws IOException {
    // Verify that custom buckets are correctly serialized in text format
    Histogram histogram =
        Histogram.builder()
            .name("test_custom_buckets")
            .help("Test histogram with custom buckets")
            .classicUpperBounds(0.5, 1.0, 2.0)
            .build();

    histogram.observe(0.3);
    histogram.observe(0.7);
    histogram.observe(1.5);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(false, true);
    writer.write(out, MetricSnapshots.of(histogram.collect()), EscapingScheme.ALLOW_UTF8);

    String output = out.toString(StandardCharsets.UTF_8);

    // Verify the output contains the custom bucket boundaries
    assertThat(output).contains("le=\"0.5\"");
    assertThat(output).contains("le=\"1.0\"");
    assertThat(output).contains("le=\"2.0\"");
    assertThat(output).contains("le=\"+Inf\"");

    // Verify bucket counts
    assertThat(output).containsPattern("le=\"0.5\".*1"); // 1 observation <= 0.5
    assertThat(output).containsPattern("le=\"1.0\".*2"); // 2 observations <= 1.0
    assertThat(output).containsPattern("le=\"2.0\".*3"); // 3 observations <= 2.0
    assertThat(output).containsPattern("le=\"\\+Inf\".*3"); // 3 observations total
  }

  @Test
  void testCustomBucketsProtobufFormatOutput() {
    // Verify that custom buckets are correctly serialized in Prometheus protobuf format
    Histogram histogram =
        Histogram.builder()
            .name("test_custom_buckets_protobuf")
            .help("Test histogram with custom buckets for protobuf")
            .classicUpperBounds(1.0, 5.0, 10.0)
            .build();

    histogram.observe(0.5);
    histogram.observe(3.0);
    histogram.observe(7.0);

    HistogramSnapshot snapshot = histogram.collect();
    Metrics.MetricFamily metricFamily =
        new PrometheusProtobufWriterImpl().convert(snapshot, EscapingScheme.ALLOW_UTF8);

    assertThat(metricFamily).isNotNull();
    assertThat(metricFamily.getName()).isEqualTo("test_custom_buckets_protobuf");
    assertThat(metricFamily.getType()).isEqualTo(Metrics.MetricType.HISTOGRAM);

    Metrics.Histogram protoHistogram = metricFamily.getMetric(0).getHistogram();

    // Verify classic buckets in protobuf
    assertThat(protoHistogram.getBucketCount()).isEqualTo(4); // 3 custom + +Inf

    // Verify bucket upper bounds
    assertThat(protoHistogram.getBucket(0).getUpperBound()).isEqualTo(1.0);
    assertThat(protoHistogram.getBucket(1).getUpperBound()).isEqualTo(5.0);
    assertThat(protoHistogram.getBucket(2).getUpperBound()).isEqualTo(10.0);
    assertThat(protoHistogram.getBucket(3).getUpperBound()).isEqualTo(Double.POSITIVE_INFINITY);

    // Verify bucket counts (cumulative)
    assertThat(protoHistogram.getBucket(0).getCumulativeCount()).isEqualTo(1); // <= 1.0
    assertThat(protoHistogram.getBucket(1).getCumulativeCount()).isEqualTo(2); // <= 5.0
    assertThat(protoHistogram.getBucket(2).getCumulativeCount()).isEqualTo(3); // <= 10.0
    assertThat(protoHistogram.getBucket(3).getCumulativeCount()).isEqualTo(3); // +Inf

    // Verify native histogram fields are also present (for dual-mode)
    assertThat(protoHistogram.hasSchema()).isTrue();
    assertThat(protoHistogram.getSchema()).isNotEqualTo(HistogramSnapshot.CLASSIC_HISTOGRAM);
  }

  @Test
  void testCustomBucketsWithNegativeValues() {
    // Verify that custom buckets work correctly with negative values
    Histogram histogram =
        Histogram.builder()
            .name("temperature_celsius")
            .help("Temperature readings with custom buckets")
            .classicUpperBounds(-20.0, -10.0, 0.0, 10.0, 20.0, 30.0)
            .build();

    histogram.observe(-15.0);
    histogram.observe(-5.0);
    histogram.observe(5.0);
    histogram.observe(15.0);
    histogram.observe(25.0);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify bucket boundaries
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds)
        .containsExactly(-20.0, -10.0, 0.0, 10.0, 20.0, 30.0, Double.POSITIVE_INFINITY);

    // Verify observations are distributed correctly
    // Note: counts are non-cumulative
    ClassicHistogramBuckets buckets = data.getClassicBuckets();
    assertThat(buckets.getCount(0)).isEqualTo(0); // <= -20: none
    assertThat(buckets.getCount(1)).isEqualTo(1); // (-20, -10]: (-15.0)
    assertThat(buckets.getCount(2)).isEqualTo(1); // (-10, 0]: (-5.0)
    assertThat(buckets.getCount(3)).isEqualTo(1); // (0, 10]: (5.0)
    assertThat(buckets.getCount(4)).isEqualTo(1); // (10, 20]: (15.0)
    assertThat(buckets.getCount(5)).isEqualTo(1); // (20, 30]: (25.0)

    assertThat(data.getCount()).isEqualTo(5);
  }

  @Test
  void testCustomBucketsWithLabels() {
    // Verify that custom buckets work correctly with labeled histograms
    Histogram histogram =
        Histogram.builder()
            .name("api_request_duration_seconds")
            .help("API request duration with custom buckets")
            .classicUpperBounds(0.01, 0.1, 1.0, 10.0)
            .labelNames("method", "endpoint")
            .build();

    histogram.labelValues("GET", "/users").observe(0.05);
    histogram.labelValues("GET", "/users").observe(0.5);
    histogram.labelValues("POST", "/users").observe(2.0);

    HistogramSnapshot snapshot = histogram.collect();

    // Verify we have 2 data points (one for each unique label combination)
    assertThat(snapshot.getDataPoints()).hasSize(2);

    // Verify both data points have the correct custom buckets
    for (HistogramSnapshot.HistogramDataPointSnapshot data : snapshot.getDataPoints()) {
      List<Double> upperBounds =
          data.getClassicBuckets().stream()
              .map(ClassicHistogramBucket::getUpperBound)
              .collect(Collectors.toList());

      assertThat(upperBounds).containsExactly(0.01, 0.1, 1.0, 10.0, Double.POSITIVE_INFINITY);
    }

    // Verify GET /users data point
    HistogramSnapshot.HistogramDataPointSnapshot getData =
        getData(histogram, "method", "GET", "endpoint", "/users");

    assertThat(getData.getCount()).isEqualTo(2);

    // Verify POST /users data point
    HistogramSnapshot.HistogramDataPointSnapshot postData =
        getData(histogram, "method", "POST", "endpoint", "/users");

    assertThat(postData.getCount()).isEqualTo(1);
  }

  private HistogramSnapshot.HistogramDataPointSnapshot getData(
      Histogram histogram, String... labels) {
    return histogram.collect().getDataPoints().stream()
        .filter(d -> d.getLabels().equals(Labels.of(labels)))
        .findAny()
        .orElseThrow(() -> new RuntimeException("histogram with labels not found"));
  }

  @Test
  void testCustomBucketsBoundaryEdgeCases() {
    // Test edge cases: observations exactly on bucket boundaries
    Histogram histogram =
        Histogram.builder()
            .name("test_boundaries")
            .help("Test bucket boundary edge cases")
            .classicUpperBounds(1.0, 5.0, 10.0)
            .build();

    // Observe values exactly on the boundaries
    histogram.observe(1.0);
    histogram.observe(5.0);
    histogram.observe(10.0);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Values on boundaries should be included in their respective buckets
    // Buckets are inclusive of upper bound
    // Note: counts are non-cumulative
    ClassicHistogramBuckets buckets = data.getClassicBuckets();
    assertThat(buckets.getCount(0)).isEqualTo(1); // <= 1.0: (1.0)
    assertThat(buckets.getCount(1)).isEqualTo(1); // (1.0, 5.0]: (5.0)
    assertThat(buckets.getCount(2)).isEqualTo(1); // (5.0, 10.0]: (10.0)

    assertThat(data.getCount()).isEqualTo(3);
  }

  @Test
  void testCustomBucketsFineBoundaries() {
    // Test with very fine-grained custom bucket boundaries
    // This simulates a use case where precise bucket boundaries are needed
    Histogram histogram =
        Histogram.builder()
            .name("precise_measurement")
            .help("Histogram with fine-grained custom buckets")
            .classicUpperBounds(0.001, 0.002, 0.003, 0.004, 0.005, 0.006, 0.007, 0.008, 0.009, 0.01)
            .build();

    histogram.observe(0.0015);
    histogram.observe(0.0045);
    histogram.observe(0.0075);

    HistogramSnapshot snapshot = histogram.collect();
    HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);

    // Verify fine-grained buckets are set correctly
    List<Double> upperBounds =
        data.getClassicBuckets().stream()
            .map(ClassicHistogramBucket::getUpperBound)
            .collect(Collectors.toList());

    assertThat(upperBounds)
        .containsExactly(
            0.001,
            0.002,
            0.003,
            0.004,
            0.005,
            0.006,
            0.007,
            0.008,
            0.009,
            0.01,
            Double.POSITIVE_INFINITY);

    // Verify observations are in correct buckets
    // Note: counts are non-cumulative
    ClassicHistogramBuckets buckets = data.getClassicBuckets();
    assertThat(buckets.getCount(0)).isEqualTo(0); // <= 0.001: none
    assertThat(buckets.getCount(1)).isEqualTo(1); // (0.001, 0.002]: (0.0015)
    assertThat(buckets.getCount(4)).isEqualTo(1); // (0.004, 0.005]: (0.0045)
    assertThat(buckets.getCount(7)).isEqualTo(1); // (0.007, 0.008]: (0.0075)

    assertThat(data.getCount()).isEqualTo(3);
  }
}
