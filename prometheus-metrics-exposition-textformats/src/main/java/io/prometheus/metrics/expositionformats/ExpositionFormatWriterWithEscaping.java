package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An exposition format writer paired with the escaping scheme selected from an Accept header.
 *
 * <p>Instances are immutable and delegate to the underlying format writer. The content type is the
 * format's standard content type and does not include an escaping parameter.
 */
@StableApi
public final class ExpositionFormatWriterWithEscaping {

  private final ExpositionFormatWriter writer;
  private final EscapingScheme escapingScheme;

  ExpositionFormatWriterWithEscaping(ExpositionFormatWriter writer, EscapingScheme escapingScheme) {
    this.writer = writer;
    this.escapingScheme = escapingScheme;
  }

  /** Writes the given metric snapshots using the negotiated escaping scheme. */
  public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
    writer.write(out, metricSnapshots, escapingScheme);
  }

  /** Converts the metric snapshots to a debug string using the negotiated escaping scheme. */
  public String toDebugString(MetricSnapshots metricSnapshots) {
    return writer.toDebugString(metricSnapshots, escapingScheme);
  }

  /** Returns the standard content type for the selected exposition format. */
  public String getContentType() {
    return writer.getContentType();
  }

  /** Returns the escaping scheme selected from the Accept header. */
  public EscapingScheme getEscapingScheme() {
    return escapingScheme;
  }
}
