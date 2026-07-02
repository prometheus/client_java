package io.prometheus.metrics.it.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.client.it.common.ExporterTest;
import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.generated.Metrics;
import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ApplicationTest {
  @Test
  void testPrometheusProtobufFormat() throws IOException {
    ExporterTest.Response response =
        ExporterTest.scrape(
            "GET",
            URI.create("http://localhost:8080/actuator/prometheus"),
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
                + " encoding=delimited");
    assertThat(response.status).isEqualTo(200);

    List<Metrics.MetricFamily> metrics = response.protoBody();
    Optional<Metrics.MetricFamily> metric =
        metrics.stream()
            .filter(m -> m.getName().equals("application_started_time_seconds"))
            .findFirst();
    assertThat(metric).isPresent();
  }

  @Test
  void testPrometheusProtobufDebugFormat() throws IOException {
    HistogramSnapshot histogram =
        HistogramSnapshot.builder()
            .name("native_debug_repro_seconds")
            .help("native debug repro")
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .sum(0.123)
                    .nativeSchema(5)
                    .nativeZeroThreshold(2.938735877055719E-39)
                    .nativeZeroCount(0)
                    .nativeBucketsForPositiveValues(
                        NativeHistogramBuckets.builder().bucket(-96, 1).build())
                    .build())
            .build();

    String debugString =
        new PrometheusProtobufWriterImpl()
            .toDebugString(MetricSnapshots.of(histogram), EscapingScheme.UNDERSCORE_ESCAPING);

    assertThat(debugString)
        .contains(
            "name: \"native_debug_repro_seconds\"",
            "type: HISTOGRAM",
            "schema: 5",
            "positive_span");
  }
}
