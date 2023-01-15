package io.prometheus.expositionformat.text;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.FixedBucket;
import io.prometheus.metrics.model.FixedBuckets;
import io.prometheus.metrics.model.FixedBucketsHistogramSnapshot;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Label;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricData;
import io.prometheus.metrics.model.MetricMetadata;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.Quantile;
import io.prometheus.metrics.model.SummarySnapshot;
import sun.misc.FloatingDecimal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class OpenMetricsTextFormatWriter {

    public final static String CONTENT_TYPE = "application/openmetrics-text; version=1.0.0; charset=utf-8";
    private final boolean writeCreatedTimestamps;

    public OpenMetricsTextFormatWriter(boolean writeCreatedTimestamps) {
        this.writeCreatedTimestamps = writeCreatedTimestamps;
    }

    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        for (MetricSnapshot snapshot : metricSnapshots) {
            if (snapshot instanceof CounterSnapshot) {
                writeCounter(writer, (CounterSnapshot) snapshot);
            } else if (snapshot instanceof GaugeSnapshot) {
                writeGauge(writer, (GaugeSnapshot) snapshot);
            } else if (snapshot instanceof FixedBucketsHistogramSnapshot) {
                writeHistogram(writer, (FixedBucketsHistogramSnapshot) snapshot);
            } else if (snapshot instanceof SummarySnapshot) {
                writeSummary(writer, (SummarySnapshot) snapshot);
            } else if (snapshot instanceof InfoSnapshot) {
                writeInfo(writer, (InfoSnapshot) snapshot);
            }
        }
        writer.write("# EOF\n");
        writer.flush();
    }

    private void writeCounter(OutputStreamWriter writer, CounterSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "counter", metadata);
        for (CounterSnapshot.CounterData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), "_total", data.getLabels());
            writeDouble(writer, data.getValue());
            writeTimestampAndExemplar(writer, data, data.getExemplar());
            if (writeCreatedTimestamps && data.hasCreatedTimestamp()) {
                writeNameAndLabels(writer, metadata.getName(), "_created", data.getLabels());
                writeTimestamp(writer, data.getCreatedTimestampMillis());
                writer.write('\n');
            }
        }
    }

    private void writeGauge(OutputStreamWriter writer, GaugeSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "gauge", metadata);
        for (GaugeSnapshot.GaugeData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), null, data.getLabels());
            writeDouble(writer, data.getValue());
            writeTimestampAndExemplar(writer, data, data.getExemplar());
        }
    }

    private void writeHistogram(OutputStreamWriter writer, FixedBucketsHistogramSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "histogram", metadata);
        for (FixedBucketsHistogramSnapshot.FixedBucketsHistogramData data : snapshot.getData()) {
            FixedBuckets buckets = data.getBuckets();
            Exemplars exemplars = data.getExemplars();
            for (int i=0; i<buckets.size(); i++) {
                writeNameAndLabels(writer, metadata.getName(), "_bucket", data.getLabels(), "le", buckets.getUpperBound(i));
                writeLong(writer, buckets.getCumulativeCount(i));
                Exemplar exemplar;
                if (i == 0) {
                    exemplar = exemplars.get(Double.NEGATIVE_INFINITY, buckets.getUpperBound(i));
                } else {
                    exemplar = exemplars.get(buckets.getUpperBound(i-1), buckets.getUpperBound(i));
                }
                writeTimestampAndExemplar(writer, data, exemplar);
            }
            writeCountSumCreated(writer, metadata, data, data.getCount(), data.getSum(), Exemplars.EMPTY);
        }
    }

    private void writeSummary(OutputStreamWriter writer, SummarySnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "summary", metadata);
        int exemplarIndex = 0;
        for (SummarySnapshot.SummaryData data : snapshot.getData()) {
            Exemplars exemplars = data.getExemplars();
            for (Quantile quantile : data.getQuantiles()) {
                writeNameAndLabels(writer, metadata.getName(), null, data.getLabels(), "quantile", quantile.getQuantile());
                writeDouble(writer, quantile.getValue());
                exemplarIndex = exemplarIndex + 1 % exemplars.size();
                writeTimestampAndExemplar(writer, data, exemplars.size() > 0 ? exemplars.get(exemplarIndex) : null);
            }
            writeCountSumCreated(writer, metadata, data, data.getCount(), data.getSum(), exemplars);
        }
    }

    private void writeInfo(OutputStreamWriter writer, InfoSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "info", metadata);
        for (InfoSnapshot.InfoData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), "_info", data.getLabels());
            writer.write("1.0");
            writeTimestampAndExemplar(writer, data, null);
        }
    }

    private void writeCountSumCreated(OutputStreamWriter writer, MetricMetadata metadata, MetricData data, long count, double sum, Exemplars exemplars) throws IOException {
        writeNameAndLabels(writer, metadata.getName(), "_count", data.getLabels());
        writeLong(writer, count);
        writeTimestampAndExemplar(writer, data, exemplars.size() > 0 ? exemplars.get(0) : null);
        writeNameAndLabels(writer, metadata.getName(), "_sum", data.getLabels());
        writeDouble(writer, sum);
        writeTimestampAndExemplar(writer, data, exemplars.size() > 0 ? exemplars.get(exemplars.size()-1) : null);
        if (writeCreatedTimestamps && data.hasCreatedTimestamp()) {
            writeNameAndLabels(writer, metadata.getName(), "_created", data.getLabels());
            writeTimestamp(writer, data.getCreatedTimestampMillis());
            writer.write('\n');
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

    private void writeTimestampAndExemplar(OutputStreamWriter writer, MetricData data, Exemplar exemplar) throws IOException {
        if (data.hasTimestamp()) {
            writer.write(' ');
            writeTimestamp(writer, data.getTimestampMillis());
        }
        if (exemplar != null) {
            writer.write(" # ");
            writeLabels(writer, exemplar.getLabels(), null, 0);
            writer.write(' ');
            writeDouble(writer, exemplar.getValue());
            if (exemplar.getTimestampMillis() != null) {
                writer.write(' ');
                writeTimestamp(writer, exemplar.getTimestampMillis());
            }
        }
        writer.write('\n');
    }

    private void writeMetadata(OutputStreamWriter writer, String typeName, MetricMetadata metadata) throws IOException {
        writer.write("# TYPE ");
        writer.write(metadata.getName());
        writer.write(' ');
        writer.write(typeName);
        writer.write('\n');
        if (metadata.getUnit() != null) {
            writer.write("# UNIT ");
            writer.write(metadata.getName());
            writer.write(' ');
            writer.write(metadata.getUnit().toString());
            writer.write('\n');
        }
        if (metadata.getHelp() != null && !metadata.getHelp().isEmpty()) {
            writer.write("# HELP ");
            writer.write(metadata.getName());
            writer.write(' ');
            writeEscapedLabelValue(writer, metadata.getHelp());
            writer.write('\n');
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
        for (int i=0; i<labels.size(); i++) {
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
}
