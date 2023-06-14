package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DistributionData;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeDouble;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeEscapedLabelValue;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLabels;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeLong;
import static io.prometheus.metrics.expositionformats.TextFormatUtil.writeTimestamp;

/**
 * Write the OpenMetrics text format as defined on <a href="https://openmetrics.io/">https://openmetrics.io</a>.
 */
public class OpenMetricsTextFormatWriter {

    public final static String CONTENT_TYPE = "application/openmetrics-text; version=1.0.0; charset=utf-8";
    private final boolean createdTimestampsEnabled;

    /**
     * @param createdTimestampsEnabled defines if {@code _created} timestamps should be included in the output or not.
     */
    public OpenMetricsTextFormatWriter(boolean createdTimestampsEnabled) {
        this.createdTimestampsEnabled = createdTimestampsEnabled;
    }

    public void write(OutputStream out, MetricSnapshots metricSnapshots) throws IOException {
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
        writer.write("# EOF\n");
        writer.flush();
    }

    private void writeCounter(OutputStreamWriter writer, CounterSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "counter", metadata);
        for (CounterSnapshot.CounterData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), "_total", data.getLabels());
            writeDouble(writer, data.getValue());
            writeScrapeTimestampAndExemplar(writer, data, data.getExemplar());
            writeCreated(writer, metadata, data);
        }
    }

    private void writeGauge(OutputStreamWriter writer, GaugeSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "gauge", metadata);
        for (GaugeSnapshot.GaugeData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), null, data.getLabels());
            writeDouble(writer, data.getValue());
            writeScrapeTimestampAndExemplar(writer, data, data.getExemplar());
        }
    }

    private void writeHistogram(OutputStreamWriter writer, HistogramSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        if (snapshot.isGaugeHistogram()) {
            writeMetadata(writer, "gaugehistogram", metadata);
            writeClassicHistogramBuckets(writer, metadata, "_gcount", "_gsum", snapshot.getData());
        } else {
            writeMetadata(writer, "histogram", metadata);
            writeClassicHistogramBuckets(writer, metadata, "_count", "_sum", snapshot.getData());
        }
    }

    private void writeClassicHistogramBuckets(OutputStreamWriter writer, MetricMetadata metadata, String countSuffix, String sumSuffix, List<HistogramSnapshot.HistogramData> dataList) throws IOException {
        for (HistogramSnapshot.HistogramData data : dataList) {
            ClassicHistogramBuckets buckets = getClassicBuckets(data);
            Exemplars exemplars = data.getExemplars();
            long cumulativeCount = 0;
            for (int i = 0; i < buckets.size(); i++) {
                cumulativeCount += buckets.getCount(i);
                writeNameAndLabels(writer, metadata.getName(), "_bucket", data.getLabels(), "le", buckets.getUpperBound(i));
                writeLong(writer, cumulativeCount);
                Exemplar exemplar;
                if (i == 0) {
                    exemplar = exemplars.get(Double.NEGATIVE_INFINITY, buckets.getUpperBound(i));
                } else {
                    exemplar = exemplars.get(buckets.getUpperBound(i - 1), buckets.getUpperBound(i));
                }
                writeScrapeTimestampAndExemplar(writer, data, exemplar);
            }
            if (data.hasCount() && data.hasSum()) {
                // In OpenMetrics format, histogram _count and _sum are either both present or both absent.
                // While Prometheus allows Exemplars for histogram's _count and _sum now, we don't
                // use Exemplars here to be backwards compatible with previous behavior.
                writeCountAndSum(writer, metadata, data, countSuffix, sumSuffix, Exemplars.EMPTY);
            }
            writeCreated(writer, metadata, data);
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

    private void writeSummary(OutputStreamWriter writer, SummarySnapshot snapshot) throws IOException {
        boolean metadataWritten = false;
        MetricMetadata metadata = snapshot.getMetadata();
        for (SummarySnapshot.SummaryData data : snapshot.getData()) {
            if (data.getQuantiles().size() == 0 && !data.hasCount() && !data.hasSum()) {
                continue;
            }
            if (!metadataWritten) {
                writeMetadata(writer, "summary", metadata);
                metadataWritten = true;
            }
            Exemplars exemplars = data.getExemplars();
            // Exemplars for summaries are new, and there's no best practice yet which Exemplars to choose for which
            // time series. We select exemplars[0] for _count, exemplars[1] for _sum, and exemplars[2...] for the
            // quantiles, all indexes modulo exemplars.length.
            int exemplarIndex = 1;
            for (Quantile quantile : data.getQuantiles()) {
                writeNameAndLabels(writer, metadata.getName(), null, data.getLabels(), "quantile", quantile.getQuantile());
                writeDouble(writer, quantile.getValue());
                if (exemplars.size() > 0) {
                    exemplarIndex = (exemplarIndex + 1) % exemplars.size();
                    writeScrapeTimestampAndExemplar(writer, data, exemplars.get(exemplarIndex));
                } else {
                    writeScrapeTimestampAndExemplar(writer, data, null);
                }
            }
            // Unlike histograms, summaries can have only a count or only a sum according to OpenMetrics.
            writeCountAndSum(writer, metadata, data, "_count", "_sum", exemplars);
            writeCreated(writer, metadata, data);
        }
    }

    private void writeInfo(OutputStreamWriter writer, InfoSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "info", metadata);
        for (InfoSnapshot.InfoData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), "_info", data.getLabels());
            writer.write("1");
            writeScrapeTimestampAndExemplar(writer, data, null);
        }
    }

    private void writeStateSet(OutputStreamWriter writer, StateSetSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "stateset", metadata);
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
                writeScrapeTimestampAndExemplar(writer, data, null);
            }
        }
    }

    private void writeUnknown(OutputStreamWriter writer, UnknownSnapshot snapshot) throws IOException {
        MetricMetadata metadata = snapshot.getMetadata();
        writeMetadata(writer, "unknown", metadata);
        for (UnknownSnapshot.UnknownData data : snapshot.getData()) {
            writeNameAndLabels(writer, metadata.getName(), null, data.getLabels());
            writeDouble(writer, data.getValue());
            writeScrapeTimestampAndExemplar(writer, data, data.getExemplar());
        }
    }

    private void writeCountAndSum(OutputStreamWriter writer, MetricMetadata metadata, DistributionData data, String countSuffix, String sumSuffix, Exemplars exemplars) throws IOException {
        int exemplarIndex = 0;
        if (data.hasCount()) {
            writeNameAndLabels(writer, metadata.getName(), countSuffix, data.getLabels());
            writeLong(writer, data.getCount());
            if (exemplars.size() > 0) {
                writeScrapeTimestampAndExemplar(writer, data, exemplars.get(exemplarIndex));
                exemplarIndex = exemplarIndex + 1 % exemplars.size();
            } else {
                writeScrapeTimestampAndExemplar(writer, data, null);
            }
        }
        if (data.hasSum()) {
            writeNameAndLabels(writer, metadata.getName(), sumSuffix, data.getLabels());
            writeDouble(writer, data.getSum());
            if (exemplars.size() > 0) {
                writeScrapeTimestampAndExemplar(writer, data, exemplars.get(exemplarIndex));
            } else {
                writeScrapeTimestampAndExemplar(writer, data, null);
            }
        }
    }

    private void writeCreated(OutputStreamWriter writer, MetricMetadata metadata, MetricData data) throws IOException {
        if (createdTimestampsEnabled && data.hasCreatedTimestamp()) {
            writeNameAndLabels(writer, metadata.getName(), "_created", data.getLabels());
            writeTimestamp(writer, data.getCreatedTimestampMillis());
            if (data.hasScrapeTimestamp()) {
                writer.write(' ');
                writeTimestamp(writer, data.getScrapeTimestampMillis());
            }
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

    private void writeScrapeTimestampAndExemplar(OutputStreamWriter writer, MetricData data, Exemplar exemplar) throws IOException {
        if (data.hasScrapeTimestamp()) {
            writer.write(' ');
            writeTimestamp(writer, data.getScrapeTimestampMillis());
        }
        if (exemplar != null) {
            writer.write(" # ");
            writeLabels(writer, exemplar.getLabels(), null, 0);
            writer.write(' ');
            writeDouble(writer, exemplar.getValue());
            if (exemplar.hasTimestamp()) {
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
            writeEscapedLabelValue(writer, metadata.getUnit().toString());
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


}
