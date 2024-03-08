package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.ValidationScheme;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

enum NameType {
    Metric,
    Label
}

public class TextFormatUtil {

    static void writeLong(OutputStreamWriter writer, long value) throws IOException {
        writer.append(Long.toString(value));
    }

    static void writeDouble(OutputStreamWriter writer, double d) throws IOException {
        if (d == Double.POSITIVE_INFINITY) {
            writer.write("+Inf");
        } else if (d == Double.NEGATIVE_INFINITY) {
            writer.write("-Inf");
        } else {
            writer.write(Double.toString(d));
            // FloatingDecimal.getBinaryToASCIIConverter(d).appendTo(writer);
        }
    }

    static void writeTimestamp(OutputStreamWriter writer, long timestampMs) throws IOException {
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

    static void writeEscapedString(Writer writer, String s) throws IOException {
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

    static void writeLabels(OutputStreamWriter writer, Labels labels, String additionalLabelName, double additionalLabelValue, boolean metricInsideBraces) throws IOException {
        if (!metricInsideBraces) {
            writer.write('{');
        }
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0 || metricInsideBraces) {
                writer.write(",");
            }
            writeName(writer, labels.getPrometheusName(i), NameType.Label);
            writer.write("=\"");
            writeEscapedString(writer, labels.getValue(i));
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

    static void writeName(OutputStreamWriter writer, String name, NameType nameType) throws IOException {
        switch (nameType) {
            case Metric:
                if (PrometheusNaming.isValidLegacyMetricName(name)) {
                    writer.write(name);
                    return;
                }
                break;
            case Label:
                if (PrometheusNaming.isValidLegacyLabelName(name) && PrometheusNaming.nameValidationScheme == ValidationScheme.LEGACY_VALIDATION) {
                    writer.write(name);
                    return;
                }
                break;
            default:
                throw new RuntimeException("Invalid name type requested: " + nameType);
        }
        writer.write('"');
        writeEscapedString(writer, name);
        writer.write('"');
    }
}
