package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class TestUtil {

  static String convertToOpenMetricsFormat(MetricSnapshots snapshots) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
    writer.write(out, snapshots, EscapingScheme.NO_ESCAPING);
    return out.toString(StandardCharsets.UTF_8);
  }
}
