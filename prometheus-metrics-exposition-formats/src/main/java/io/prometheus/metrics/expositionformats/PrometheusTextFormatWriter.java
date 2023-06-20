package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricData;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeDouble;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeEscapedLabelValue;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLabels;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLong;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeTimestamp;

/**
 * Write the Prometheus text format. This is the default if you view a Prometheus endpoint with your Web browser.
 */
public class PrometheusTextFormatWriter implements ExpositionFormatWriter {

    public final static String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";

    private final boolean writeCreatedTimestamps;

    public PrometheusTextFormatWriter(boolean writeCreatedTimestamps) {
        this.writeCreatedTimestamps = writeCreatedTimestamps;
    }

    @Override
    public boolean accepts(String acceptHeader) {
        if (acceptHeader == null) {
            return false;
        } else {
        return acceptHeader.contains("text/plain");
        }
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
        // See https://prometheus.io/docs/instrumenting/exposition_formats/
        // "unknown", "gauge", "counter", "stateset", "info", "histogram", "gaugehistogram", and "summary".
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        for (MetricSnapshot snapshot : metricSnapshots) {
            if (snapshot.getData().size() > 0) {
                if (snapshot instanceof CounterSnapshot) {
                    writeCounter(writer, (CounterSnapshot) snapshot);
                } else if (snapshot instanceof GaugeSnapshot) {
                    writeGauge(writer, (GaugeSnapshot) snapshot);
                } else if (snapshot instanceof HistogramSnapshot) {
                    writeHistogram(writer, (HistogramSnapshot) snapshot);
                } else if (snapshot instanceof SummarySnapshot) {
                    writeSummary(writer, (SummarySnapshot) snapshot);
                } else if (snapshot instanceof InfoSnapshot) {
                    writeInfo(writer, (InfoSnapshot) snapshot);
                } else if (snapshot instanceof StateSetSnapshot) {
                    writeStateSet(writer, (StateSetSnapshot) snapshot);
                } else if (snapshot instanceof UnknownSnapshot) {
                    writeUnknown(writer, (UnknownSnapshot) snapshot);
                }
            }
        }
        if (writeCreatedTimestamps) {
            for (MetricSnapshot snapshot : metricSnapshots) {
                if (snapshot.getData().size() > 0) {
                    if (snapshot instanceof CounterSnapshot) {
                        writeCreated(writer, snapshot);
                    } else if (snapshot instanceof HistogramSnapshot) {
                        writeCreated(writer, snapshot);
                    } else if (snapshot instanceof SummarySnapshot) {
                        writeCreated(writer, snapshot);
                    }
                }
            }
        }
        writer.flush();
    }

    public void writeCreated(OutputStreamWriter writer, MetricSnapshot snapshot) throws IOException {
            boolean metadataWritten = false;
            MetricMetadata metadata = snapshot.getMetadata();
            for (MetricData data : snapshot.getData()) {
                if (data.hasCreatedTimestamp()) {
                    if (!metadataWritten) {
                        writeMetadata(writer, "_created", "gauge", metadata);
                        metadataWritten = true;
                    }
                    writeNameAndLabels(writer, metadata.getName(), "_created", data.getLabels());
                    writeTimestamp(writer, data.getCreatedTimestampMillis());
                    writeScrapeTimestampAndNewline(writer, data);
                }
            }

    }

    private void writeCounter(OutputStreamWriter writer, CounterSnapshot snapshot) throws IOException {
        if (snapshot.getData().size() > 0) {
            MetricMetadata metadata = snapshot.getMetadata();
            writeMetadata(writer, "_total", "counter", metadata);
            for (CounterSnapshot.CounterData data : snapshot.getData()) {
                writeNameAndLabels(writer, metadata.getName(), "_total", data.getLabels());
                writeDouble(writer, data.getValue());
                writeScrapeTimestampAndNewline(writer, data);
            }
        }
    }

    private void writeGauge(OutputStreamWriter writer, GaugeSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "", "gauge", metadata);
        for (GaugeSnapshot.GaugeData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), null, data.getLabels());
            writeDouble(writer, data.getValue());
            writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeHistogram(OutputStreamWriter writer, HistogramSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "", "histogram", metadata);
        for (HistogramSnapshot.HistogramData data : snapshot.getData()) {
            ClassicHistogramBuckets buckets = getClassicBuckets(data);
            long cumulativeCount = 0;
            for (int i = 0; i < buckets.size(); i++) {
                cumulativeCount += buckets.getCount(i);
                writeNameAndLabels(writer, metadata.getName(), "_bucket", data.getLabels(), "le", buckets.getUpperBound(i));
                writeLong(writer, cumulativeCount);
                writeScrapeTimestampAndNewline(writer, data);
            }
            if (!snapshot.isGaugeHistogram()) {
                if (data.hasCount()) {
                    writeNameAndLabels(writer, metadata.getName(), "_count", data.getLabels());
                    writeLong(writer, data.getCount());
                    writeScrapeTimestampAndNewline(writer, data);
                }
                if (data.hasSum()) {
                    writeNameAndLabels(writer, metadata.getName(), "_sum", data.getLabels());
                    writeDouble(writer, data.getSum());
                    writeScrapeTimestampAndNewline(writer, data);
                }
            }
        }
        if (snapshot.isGaugeHistogram()) {
            writeGaugeCountSum(writer, snapshot, metadata);
        }
    }

    private ClassicHistogramBuckets getClassicBuckets(HistogramSnapshot.HistogramData data) {
        if (data.getClassicBuckets().isEmpty()) {
            return ClassicHistogramBuckets.of(
                    new double[]{Double.POSITIVE_INFINITY},
                    new long[]{data.getCount()}
            );
        } else {
            return data.getClassicBuckets();
        }
    }

    private void writeGaugeCountSum(OutputStreamWriter writer, HistogramSnapshot snapshot, MetricMetadata metadata) throws IOException {
        // Prometheus text format does not support gaugehistogram's _gcount and _gsum.
        // So we append _gcount and _gsum as gauge metrics.
        boolean metadataWritten = false;
        for (HistogramSnapshot.HistogramData data : snapshot.getData()) {
            if (data.hasCount()) {
                if (!metadataWritten) {
                    writeMetadata(writer, "_gcount", "gauge", metadata);
                    metadataWritten = true;
                }
                writeNameAndLabels(writer, metadata.getName(), "_gcount", data.getLabels());
                writeLong(writer, data.getCount());
                writeScrapeTimestampAndNewline(writer, data);
            }
        }
        metadataWritten = false;
        for (HistogramSnapshot.HistogramData data : snapshot.getData()) {
            if (data.hasSum()) {
                if (!metadataWritten) {
                    writeMetadata(writer, "_gsum", "gauge", metadata);
                    metadataWritten = true;
                }
                writeNameAndLabels(writer, metadata.getName(), "_gsum", data.getLabels());
                writeDouble(writer, data.getSum());
                writeScrapeTimestampAndNewline(writer, data);
            }
        }
    }

    private void writeSummary(OutputStreamWriter writer, SummarySnapshot snapshot) throws IOException {
        boolean metadataWritten = false;
        MetricMetadata metadata = snapshot.getMetadata();
        for (SummarySnapshot.SummaryData data : snapshot.getData()) {
            if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) {
                continue;
            }
            if (!metadataWritten) {
                writeMetadata(writer, "", "summary", metadata);
                metadataWritten = true;
            }
            for (Quantile quantile : data.getQuantiles()) {
                writeNameAndLabels(writer, metadata.getName(), null, data.getLabels(), "quantile", quantile.getQuantile());
                writeDouble(writer, quantile.getValue());
                writeScrapeTimestampAndNewline(writer, data);
            }
            if (data.hasCount()) {
                writeNameAndLabels(writer, metadata.getName(), "_count", data.getLabels());
                writeLong(writer, data.getCount());
                writeScrapeTimestampAndNewline(writer, data);
            }
            if (data.hasSum()) {
                writeNameAndLabels(writer, metadata.getName(), "_sum", data.getLabels());
                writeDouble(writer, data.getSum());
                writeScrapeTimestampAndNewline(writer, data);
            }
        }
    }

    private void writeInfo(OutputStreamWriter writer, InfoSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "_info", "gauge", metadata);
        for (InfoSnapshot.InfoData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), "_info", data.getLabels());
            writer.write("1");
            writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeStateSet(OutputStreamWriter writer, StateSetSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "", "gauge", metadata);
        for (StateSetSnapshot.StateSetData data : snapshot.getData()) {
            for (int i = 0; i < data.size(); i++) {
                writer.write(metadata.getName());
                writer.write('{');
                for (int j = 0; j < data.getLabels().size(); j++) {
                    if (j > 0) {
                        writer.write(",");
                    }
                    writer.write(data.getLabels().getName(j));
                    writer.write("=\"");
                    writeEscapedLabelValue(writer, data.getLabels().getValue(j));
                    writer.write("\"");
                }
                if (!data.getLabels().isEmpty()) {
                    writer.write(",");
                }
                writer.write(metadata.getName());
                writer.write("=\"");
                writeEscapedLabelValue(writer, data.getName(i));
                writer.write("\"} ");
                if (data.isTrue(i)) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }
                writeScrapeTimestampAndNewline(writer, data);
            }
        }
    }

    private void writeUnknown(OutputStreamWriter writer, UnknownSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "", "untyped", metadata);
        for (UnknownSnapshot.UnknownData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), null, data.getLabels());
            writeDouble(writer, data.getValue());
            writeScrapeTimestampAndNewline(writer, data);
        }
    }

    private void writeNameAndLabels(OutputStreamWriter writer, String name, String suffix, Labels labels) throws IOException {
        writeNameAndLabels(writer, name, suffix, labels, null, 0.0);
    }

    private void writeNameAndLabels(OutputStreamWriter writer, String name, String suffix, Labels labels,
                                    String additionalLabelName, double additionalLabelValue) throws IOException {
        writer.write(name);
        if (suffix != null) {
            writer.write(suffix);
        }
        if (!labels.isEmpty() || additionalLabelName != null) {
            writeLabels(writer, labels, additionalLabelName, additionalLabelValue);
        }
        writer.write(' ');
    }

    private void writeMetadata(OutputStreamWriter writer, String suffix, String typeString, MetricMetadata metadata) throws IOException {
        if (metadata.getHelp() != null && !metadata.getHelp().isEmpty()) {
            writer.write("# HELP ");
            writer.write(metadata.getName());
            if (suffix != null) {
                writer.write(suffix);
            }
            writer.write(' ');
            writeEscapedHelp(writer, metadata.getHelp());
            writer.write('\n');
        }
        writer.write("# TYPE ");
        writer.write(metadata.getName());
        if (suffix != null) {
            writer.write(suffix);
        }
        writer.write(' ');
        writer.write(typeString);
        writer.write('\n');
    }

    private void writeEscapedHelp(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    private void writeScrapeTimestampAndNewline(OutputStreamWriter writer, MetricData data) throws IOException {
        if (data.hasScrapeTimestamp()) {
            writer.write(' ');
            writeTimestamp(writer, data.getScrapeTimestampMillis());
        }
        writer.write('\n');
    }
}
