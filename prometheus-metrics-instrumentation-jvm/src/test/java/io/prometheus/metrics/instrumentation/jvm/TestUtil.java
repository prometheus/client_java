package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestUtil {

  static String convertToOpenMetricsFormat(MetricSnapshots snapshots) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer =
        OpenMetricsTextFormatWriter.builder()
            .setCreatedTimestampsEnabled(true)
            .setExemplarsOnAllMetricTypesEnabled(true)
            .build();
    writer.write(out, snapshots, EscapingScheme.ALLOW_UTF8);
    return out.toString(StandardCharsets.UTF_8);
  }
}
