package io.prometheus.expositionformat.text;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.FixedHistogramBuckets;
import io.prometheus.metrics.model.FixedHistogramSnapshot;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricData;
import io.prometheus.metrics.model.MetricMetadata;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.Quantile;
import io.prometheus.metrics.model.StateSetSnapshot;
import io.prometheus.metrics.model.SummarySnapshot;
import io.prometheus.metrics.model.UnknownSnapshot;
import sun.misc.FloatingDecimal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class PrometheusTextFormatWriter {

    public final static String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";

    private final boolean writeCreatedTimestamps;

    public PrometheusTextFormatWriter(boolean writeCreatedTimestamps) {
        this.writeCreatedTimestamps = writeCreatedTimestamps;
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
                } else if (snapshot instanceof FixedHistogramSnapshot) {
                    writeHistogram(writer, (FixedHistogramSnapshot) snapshot);
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
                    } else if (snapshot instanceof FixedHistogramSnapshot) {
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

    private void writeHistogram(OutputStreamWriter writer, FixedHistogramSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "", "histogram", metadata);
        for (FixedHistogramSnapshot.FixedHistogramData data : snapshot.getData()) {
            FixedHistogramBuckets buckets = data.getBuckets();
            for (int i = 0; i < buckets.size(); i++) {
                writeNameAndLabels(writer, metadata.getName(), "_bucket", data.getLabels(), "le", buckets.getUpperBound(i));
                writeLong(writer, buckets.getCumulativeCount(i));
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

    private void writeGaugeCountSum(OutputStreamWriter writer, FixedHistogramSnapshot snapshot, MetricMetadata metadata) throws IOException {
        // Prometheus text format does not support gaugehistogram's _gcount and _gsum.
        // So we append _gcount and _gsum as gauge metrics.
        boolean metadataWritten = false;
        for (FixedHistogramSnapshot.FixedHistogramData data : snapshot.getData()) {
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
        for (FixedHistogramSnapshot.FixedHistogramData data : snapshot.getData()) {
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

    private void writeEscapedLabelValue(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\"':
                    writer.append("\\\"");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }


    private void writeLabels(OutputStreamWriter writer, Labels labels, String additionalLabelName, double additionalLabelValue) throws IOException {
        writer.write('{');
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                writer.write(",");
            }
            writer.write(labels.getName(i));
            writer.write("=\"");
            writeEscapedLabelValue(writer, labels.getValue(i));
            writer.write("\"");
        }
        if (additionalLabelName != null) {
            if (!labels.isEmpty()) {
                writer.write(",");
            }
            writer.write(additionalLabelName);
            writer.write("=\"");
            writeDouble(writer, additionalLabelValue);
            writer.write("\"");
        }
        writer.write('}');
    }

    private void writeLong(OutputStreamWriter writer, long value) throws IOException {
        writer.append(Long.toString(value));
    }

    private void writeDouble(OutputStreamWriter writer, double d) throws IOException {
        if (d == Double.POSITIVE_INFINITY) {
            writer.write("+Inf");
        } else if (d == Double.NEGATIVE_INFINITY) {
            writer.write("-Inf");
        } else {
            writer.write(Double.toString(d));
            FloatingDecimal.getBinaryToASCIIConverter(d).appendTo(writer);
        }
    }

    private void writeTimestamp(OutputStreamWriter writer, long timestampMs) throws IOException {
        writer.write(Long.toString(timestampMs / 1000L));
        writer.write(".");
        long ms = timestampMs % 1000;
        if (ms < 100) {
            writer.write("0");
        }
        if (ms < 10) {
            writer.write("0");
        }
        writer.write(Long.toString(ms));
    }

    private void writeScrapeTimestampAndNewline(OutputStreamWriter writer, MetricData data) throws IOException {
        if (data.hasScrapeTimestamp()) {
            writer.write(' ');
            writeTimestamp(writer, data.getScrapeTimestampMillis());
        }
        writer.write('\n');
    }
}
