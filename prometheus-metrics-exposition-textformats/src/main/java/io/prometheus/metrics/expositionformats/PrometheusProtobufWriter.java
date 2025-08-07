package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

/**
 * Write the Prometheus protobuf format as defined in <a
 * href="https://github.com/prometheus/client_model/tree/master/io/prometheus/client">github.com/prometheus/client_model</a>.
 *
 * <p>As of today, this is the only exposition format that supports native histograms.
 */
public class PrometheusProtobufWriter implements ExpositionFormatWriter {

  @Nullable private static final ExpositionFormatWriter DELEGATE = createProtobufWriter();

  public static final String CONTENT_TYPE =
      "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; "
          + "encoding=delimited";

  @Nullable
  private static ExpositionFormatWriter createProtobufWriter() {
    try {
      return Class.forName(
              "io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl")
          .asSubclass(ExpositionFormatWriter.class)
          .getDeclaredConstructor()
          .newInstance();
    } catch (Exception e) {
      // not in classpath
      return null;
    }
  }

  @Override
  public boolean accepts(String acceptHeader) {
    if (acceptHeader == null) {
      return false;
    } else {
      return acceptHeader.contains("application/vnd.google.protobuf")
          && acceptHeader.contains("proto=io.prometheus.client.MetricFamily");
    }
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public boolean isAvailable() {
    return DELEGATE != null;
  }

  @Override
  public String toDebugString(MetricSnapshots metricSnapshots, EscapingScheme escapingScheme) {
    checkAvailable();
    return DELEGATE.toDebugString(metricSnapshots, escapingScheme);
  }

  @Override
  public void write(
      OutputStream out, MetricSnapshots metricSnapshots, EscapingScheme escapingScheme)
      throws IOException {
    checkAvailable();
    DELEGATE.write(out, metricSnapshots, escapingScheme);
  }

  private void checkAvailable() {
    if (DELEGATE == null) {
      throw new UnsupportedOperationException("Prometheus protobuf writer not available");
    }
  }
}
