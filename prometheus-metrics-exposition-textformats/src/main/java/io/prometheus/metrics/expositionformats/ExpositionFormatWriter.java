package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nullable;

public interface ExpositionFormatWriter {
  boolean accepts(@Nullable String acceptHeader);

  /** Text formats use UTF-8 encoding. */
  void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException;

  default String toDebugString(MetricSnapshots metricSnapshots) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      write(out, metricSnapshots);
      return out.toString("UTF-8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  String getContentType();

  /**
   * Returns true if the writer is available. If false, the writer will throw an exception if used.
   */
  default boolean isAvailable() {
    return true;
  }
}
