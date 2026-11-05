package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class NegativeBucketHistogramTest {

  @Test
  void histogramWithNegativeBucketThresholdOmitsCountAndSum() throws IOException {
    HistogramSnapshot histogram =
        HistogramSnapshot.builder()
            .name("my_histogram")
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .sum(0.5)
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(-1.0, 0)
                            .bucket(1.0, 1)
                            .bucket(Double.POSITIVE_INFINITY, 0)
                            .build())
                    .build())
            .build();

    assertThat(write(histogram))
        .isEqualTo(
            "# TYPE my_histogram histogram\n"
                + "my_histogram_bucket{le=\"-1.0\"} 0\n"
                + "my_histogram_bucket{le=\"1.0\"} 1\n"
                + "my_histogram_bucket{le=\"+Inf\"} 1\n"
                + "# EOF\n");
  }

  @Test
  void histogramWithNonnegativeBucketThresholdKeepsCountAndSum() throws IOException {
    HistogramSnapshot histogram =
        HistogramSnapshot.builder()
            .name("my_histogram")
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .sum(0.0)
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(0.0, 1)
                            .bucket(Double.POSITIVE_INFINITY, 0)
                            .build())
                    .build())
            .build();

    assertThat(write(histogram))
        .isEqualTo(
            "# TYPE my_histogram histogram\n"
                + "my_histogram_bucket{le=\"0.0\"} 1\n"
                + "my_histogram_bucket{le=\"+Inf\"} 1\n"
                + "my_histogram_count 1\n"
                + "my_histogram_sum 0.0\n"
                + "# EOF\n");
  }

  @Test
  void histogramWithPositiveBucketThresholdKeepsCountAndSum() throws IOException {
    HistogramSnapshot histogram =
        HistogramSnapshot.builder()
            .name("my_histogram")
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .sum(0.5)
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(1.0, 1)
                            .bucket(Double.POSITIVE_INFINITY, 0)
                            .build())
                    .build())
            .build();

    assertThat(write(histogram))
        .isEqualTo(
            "# TYPE my_histogram histogram\n"
                + "my_histogram_bucket{le=\"1.0\"} 1\n"
                + "my_histogram_bucket{le=\"+Inf\"} 1\n"
                + "my_histogram_count 1\n"
                + "my_histogram_sum 0.5\n"
                + "# EOF\n");
  }

  @Test
  void gaugeHistogramWithNegativeBucketThresholdKeepsNegativeSum() throws IOException {
    HistogramSnapshot histogram =
        HistogramSnapshot.builder()
            .name("my_gauge_histogram")
            .gaugeHistogram(true)
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .sum(-7.0)
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(-1.0, 4)
                            .bucket(Double.POSITIVE_INFINITY, 1)
                            .build())
                    .build())
            .build();

    assertThat(write(histogram))
        .isEqualTo(
            "# TYPE my_gauge_histogram gaugehistogram\n"
                + "my_gauge_histogram_bucket{le=\"-1.0\"} 4\n"
                + "my_gauge_histogram_bucket{le=\"+Inf\"} 5\n"
                + "my_gauge_histogram_gcount 5\n"
                + "my_gauge_histogram_gsum -7.0\n"
                + "# EOF\n");
  }

  private static String write(HistogramSnapshot histogram) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter.create().write(out, MetricSnapshots.of(histogram));
    return out.toString(StandardCharsets.UTF_8);
  }
}
