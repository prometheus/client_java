package io.prometheus.metrics.expositionformats.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;
import com.google.protobuf.Timestamp;
import io.prometheus.metrics.expositionformats.generated.Metrics;

@SuppressWarnings("NonCanonicalType")
class PrometheusProtobufDebugFormat {

  private static final String INDENT = "  ";

  static String toDebugString(Metrics.MetricFamily metricFamily) {
    StringBuilder result = new StringBuilder();
    appendMetricFamily(result, "", metricFamily);
    return result.toString();
  }

  private static void appendMetricFamily(
      StringBuilder result, String indent, Metrics.MetricFamily metricFamily) {
    appendString(result, indent, "name", metricFamily.hasName(), metricFamily.getName());
    appendString(result, indent, "help", metricFamily.hasHelp(), metricFamily.getHelp());
    if (metricFamily.hasType()) {
      appendScalar(result, indent, "type", metricFamily.getType().name());
    }
    for (Metrics.Metric metric : metricFamily.getMetricList()) {
      appendMessage(result, indent, "metric", () -> appendMetric(result, indent + INDENT, metric));
    }
    appendString(result, indent, "unit", metricFamily.hasUnit(), metricFamily.getUnit());
  }

  private static void appendMetric(StringBuilder result, String indent, Metrics.Metric metric) {
    for (Metrics.LabelPair label : metric.getLabelList()) {
      appendMessage(result, indent, "label", () -> appendLabel(result, indent + INDENT, label));
    }
    if (metric.hasGauge()) {
      appendMessage(
          result, indent, "gauge", () -> appendGauge(result, indent + INDENT, metric.getGauge()));
    }
    if (metric.hasCounter()) {
      appendMessage(
          result,
          indent,
          "counter",
          () -> appendCounter(result, indent + INDENT, metric.getCounter()));
    }
    if (metric.hasSummary()) {
      appendMessage(
          result,
          indent,
          "summary",
          () -> appendSummary(result, indent + INDENT, metric.getSummary()));
    }
    if (metric.hasUntyped()) {
      appendMessage(
          result,
          indent,
          "untyped",
          () -> appendUntyped(result, indent + INDENT, metric.getUntyped()));
    }
    if (metric.hasHistogram()) {
      appendMessage(
          result,
          indent,
          "histogram",
          () -> appendHistogram(result, indent + INDENT, metric.getHistogram()));
    }
    appendScalar(result, indent, "timestamp_ms", metric.hasTimestampMs(), metric.getTimestampMs());
  }

  private static void appendLabel(StringBuilder result, String indent, Metrics.LabelPair label) {
    appendString(result, indent, "name", label.hasName(), label.getName());
    appendString(result, indent, "value", label.hasValue(), label.getValue());
  }

  private static void appendGauge(StringBuilder result, String indent, Metrics.Gauge gauge) {
    appendScalar(result, indent, "value", gauge.hasValue(), gauge.getValue());
  }

  private static void appendCounter(StringBuilder result, String indent, Metrics.Counter counter) {
    appendScalar(result, indent, "value", counter.hasValue(), counter.getValue());
    if (counter.hasExemplar()) {
      appendMessage(
          result,
          indent,
          "exemplar",
          () -> appendExemplar(result, indent + INDENT, counter.getExemplar()));
    }
    appendTimestamp(
        result,
        indent,
        "created_timestamp",
        counter.hasCreatedTimestamp(),
        counter.getCreatedTimestamp());
  }

  private static void appendSummary(StringBuilder result, String indent, Metrics.Summary summary) {
    appendScalar(
        result, indent, "sample_count", summary.hasSampleCount(), summary.getSampleCount());
    appendScalar(result, indent, "sample_sum", summary.hasSampleSum(), summary.getSampleSum());
    for (Metrics.Quantile quantile : summary.getQuantileList()) {
      appendMessage(
          result, indent, "quantile", () -> appendQuantile(result, indent + INDENT, quantile));
    }
    appendTimestamp(
        result,
        indent,
        "created_timestamp",
        summary.hasCreatedTimestamp(),
        summary.getCreatedTimestamp());
  }

  private static void appendQuantile(
      StringBuilder result, String indent, Metrics.Quantile quantile) {
    appendScalar(result, indent, "quantile", quantile.hasQuantile(), quantile.getQuantile());
    appendScalar(result, indent, "value", quantile.hasValue(), quantile.getValue());
  }

  private static void appendUntyped(StringBuilder result, String indent, Metrics.Untyped untyped) {
    appendScalar(result, indent, "value", untyped.hasValue(), untyped.getValue());
  }

  private static void appendHistogram(
      StringBuilder result, String indent, Metrics.Histogram histogram) {
    appendScalar(
        result, indent, "sample_count", histogram.hasSampleCount(), histogram.getSampleCount());
    appendScalar(
        result,
        indent,
        "sample_count_float",
        histogram.hasSampleCountFloat(),
        histogram.getSampleCountFloat());
    appendScalar(result, indent, "sample_sum", histogram.hasSampleSum(), histogram.getSampleSum());
    for (Metrics.Bucket bucket : histogram.getBucketList()) {
      appendMessage(result, indent, "bucket", () -> appendBucket(result, indent + INDENT, bucket));
    }
    appendTimestamp(
        result,
        indent,
        "created_timestamp",
        histogram.hasCreatedTimestamp(),
        histogram.getCreatedTimestamp());
    appendScalar(result, indent, "schema", histogram.hasSchema(), histogram.getSchema());
    appendScalar(
        result,
        indent,
        "zero_threshold",
        histogram.hasZeroThreshold(),
        histogram.getZeroThreshold());
    appendScalar(result, indent, "zero_count", histogram.hasZeroCount(), histogram.getZeroCount());
    appendScalar(
        result,
        indent,
        "zero_count_float",
        histogram.hasZeroCountFloat(),
        histogram.getZeroCountFloat());
    for (Metrics.BucketSpan span : histogram.getNegativeSpanList()) {
      appendMessage(
          result,
          indent,
          "negative_span",
          () -> appendBucketSpan(result, indent + INDENT, span));
    }
    for (int i = 0; i < histogram.getNegativeDeltaCount(); i++) {
      appendScalar(result, indent, "negative_delta", histogram.getNegativeDelta(i));
    }
    for (int i = 0; i < histogram.getNegativeCountCount(); i++) {
      appendScalar(result, indent, "negative_count", histogram.getNegativeCount(i));
    }
    for (Metrics.BucketSpan span : histogram.getPositiveSpanList()) {
      appendMessage(
          result,
          indent,
          "positive_span",
          () -> appendBucketSpan(result, indent + INDENT, span));
    }
    for (int i = 0; i < histogram.getPositiveDeltaCount(); i++) {
      appendScalar(result, indent, "positive_delta", histogram.getPositiveDelta(i));
    }
    for (int i = 0; i < histogram.getPositiveCountCount(); i++) {
      appendScalar(result, indent, "positive_count", histogram.getPositiveCount(i));
    }
    for (Metrics.Exemplar exemplar : histogram.getExemplarsList()) {
      appendMessage(
          result,
          indent,
          "exemplars",
          () -> appendExemplar(result, indent + INDENT, exemplar));
    }
  }

  private static void appendBucket(StringBuilder result, String indent, Metrics.Bucket bucket) {
    appendScalar(
        result,
        indent,
        "cumulative_count",
        bucket.hasCumulativeCount(),
        bucket.getCumulativeCount());
    appendScalar(
        result,
        indent,
        "cumulative_count_float",
        bucket.hasCumulativeCountFloat(),
        bucket.getCumulativeCountFloat());
    appendScalar(result, indent, "upper_bound", bucket.hasUpperBound(), bucket.getUpperBound());
    if (bucket.hasExemplar()) {
      appendMessage(
          result,
          indent,
          "exemplar",
          () -> appendExemplar(result, indent + INDENT, bucket.getExemplar()));
    }
  }

  private static void appendBucketSpan(
      StringBuilder result, String indent, Metrics.BucketSpan span) {
    appendScalar(result, indent, "offset", span.hasOffset(), span.getOffset());
    appendScalar(result, indent, "length", span.hasLength(), span.getLength());
  }

  private static void appendExemplar(
      StringBuilder result, String indent, Metrics.Exemplar exemplar) {
    for (Metrics.LabelPair label : exemplar.getLabelList()) {
      appendMessage(result, indent, "label", () -> appendLabel(result, indent + INDENT, label));
    }
    appendScalar(result, indent, "value", exemplar.hasValue(), exemplar.getValue());
    appendTimestamp(result, indent, "timestamp", exemplar.hasTimestamp(), exemplar.getTimestamp());
  }

  private static void appendTimestamp(
      StringBuilder result, String indent, String fieldName, boolean present, Timestamp timestamp) {
    if (present) {
      appendMessage(
          result,
          indent,
          fieldName,
          () -> {
            appendScalar(result, indent + INDENT, "seconds", timestamp.getSeconds());
            appendScalar(result, indent + INDENT, "nanos", timestamp.getNanos());
          });
    }
  }

  private static void appendString(
      StringBuilder result, String indent, String fieldName, boolean present, String value) {
    if (present) {
      appendScalar(result, indent, fieldName, quote(value));
    }
  }

  private static void appendScalar(
      StringBuilder result, String indent, String fieldName, boolean present, double value) {
    if (present) {
      appendScalar(result, indent, fieldName, formatDouble(value));
    }
  }

  private static void appendScalar(
      StringBuilder result, String indent, String fieldName, boolean present, long value) {
    if (present) {
      appendScalar(result, indent, fieldName, Long.toString(value));
    }
  }

  private static void appendScalar(
      StringBuilder result, String indent, String fieldName, boolean present, int value) {
    if (present) {
      appendScalar(result, indent, fieldName, Integer.toString(value));
    }
  }

  private static void appendScalar(
      StringBuilder result, String indent, String fieldName, double value) {
    appendScalar(result, indent, fieldName, formatDouble(value));
  }

  private static void appendScalar(
      StringBuilder result, String indent, String fieldName, long value) {
    appendScalar(result, indent, fieldName, Long.toString(value));
  }

  private static void appendScalar(
      StringBuilder result, String indent, String fieldName, String value) {
    result.append(indent).append(fieldName).append(": ").append(value).append('\n');
  }

  private static void appendMessage(
      StringBuilder result, String indent, String fieldName, Runnable appendBody) {
    result.append(indent).append(fieldName).append(" {\n");
    appendBody.run();
    result.append(indent).append("}\n");
  }

  private static String quote(String value) {
    return "\"" + TextFormat.escapeBytes(ByteString.copyFromUtf8(value)) + "\"";
  }

  private static String formatDouble(double value) {
    if (Double.isNaN(value)) {
      return "nan";
    }
    if (value == Double.POSITIVE_INFINITY) {
      return "inf";
    }
    if (value == Double.NEGATIVE_INFINITY) {
      return "-inf";
    }
    return Double.toString(value);
  }
}
