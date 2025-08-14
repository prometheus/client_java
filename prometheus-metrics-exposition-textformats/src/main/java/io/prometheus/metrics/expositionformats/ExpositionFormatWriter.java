package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

public interface ExpositionFormatWriter {
  boolean accepts(@Nullable String acceptHeader);

  /** Writes the given metric snapshots to the output stream using the specified escaping scheme. */
  void write(OutputStream out, MetricSnapshots metricSnapshots, EscapingScheme escapingScheme)
      throws IOException;

  /** Writes the given metric snapshots to the output stream using the default escaping scheme. */
  default void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
    write(out, metricSnapshots, EscapingScheme.DEFAULT);
  }

  /** Converts the metric snapshots to a debug string using the specified escaping scheme. */
  default String toDebugString(MetricSnapshots metricSnapshots, EscapingScheme escapingScheme) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      write(out, metricSnapshots, escapingScheme);
      return out.toString("UTF-8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Converts the metric snapshots to a debug string using the default escaping scheme. */
  default String toDebugString(MetricSnapshots metricSnapshots) {
    return toDebugString(metricSnapshots, EscapingScheme.DEFAULT);
  }

  String getContentType();

  /**
   * Returns true if the writer is available. If false, the writer will throw an exception if used.
   */
  default boolean isAvailable() {
    return true;
  }
}
