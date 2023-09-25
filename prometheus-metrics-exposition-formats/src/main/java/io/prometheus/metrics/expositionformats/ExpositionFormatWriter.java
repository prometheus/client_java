package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.io.IOException;
import java.io.OutputStream;

public interface ExpositionFormatWriter {
    boolean accepts(String acceptHeader);

    /**
     * Text formats use UTF-8 encoding.
     */
    void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException;
    String getContentType();
}
