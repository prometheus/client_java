package io.prometheus.metrics.model.snapshots;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MAX_LOW_SURROGATE;
import static java.lang.Character.MIN_HIGH_SURROGATE;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class SnapshotEscaper {

  private SnapshotEscaper() {}

  /** Escapes the given metric names and labels with the given escaping scheme. */
  @Nullable
  public static MetricSnapshot escapeMetricSnapshot(@Nullable MetricSnapshot v, EscapingScheme scheme) {
    if (v == null) {
      return null;
    }

    if (scheme == EscapingScheme.NO_ESCAPING || scheme == EscapingScheme.UNDERSCORE_ESCAPING) {
      // we re-use the prometheus name for underscore escaping as an optimization
      return v;
    }

    List<DataPointSnapshot> outDataPoints = new ArrayList<>();

    for (DataPointSnapshot d : v.getDataPoints()) {
      if (!snapshotNeedsEscaping(d, scheme)) {
        outDataPoints.add(d);
        continue;
      }

      outDataPoints.add(
          createEscapedDataPointSnapshot(v, d, escapeLabels(d.getLabels(), scheme), scheme));
    }

    return createEscapedMetricSnapshot(
        v, escapeName(v.getMetadata().getName(), scheme), outDataPoints);
  }

  static MetricSnapshot createEscapedMetricSnapshot(
      MetricSnapshot v, String outName, List<DataPointSnapshot> outDataPoints) {
    if (v instanceof CounterSnapshot) {
      CounterSnapshot.Builder builder =
          CounterSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((CounterSnapshot.CounterDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof GaugeSnapshot) {
      GaugeSnapshot.Builder builder =
          GaugeSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((GaugeSnapshot.GaugeDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof HistogramSnapshot) {
      HistogramSnapshot.Builder builder =
          HistogramSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit())
              .gaugeHistogram(((HistogramSnapshot) v).isGaugeHistogram());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((HistogramSnapshot.HistogramDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof SummarySnapshot) {
      SummarySnapshot.Builder builder =
          SummarySnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((SummarySnapshot.SummaryDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof InfoSnapshot) {
      InfoSnapshot.Builder builder =
          InfoSnapshot.builder().name(outName).help(v.getMetadata().getHelp());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((InfoSnapshot.InfoDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof StateSetSnapshot) {
      StateSetSnapshot.Builder builder =
          StateSetSnapshot.builder().name(outName).help(v.getMetadata().getHelp());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((StateSetSnapshot.StateSetDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof UnknownSnapshot) {
      UnknownSnapshot.Builder builder =
          UnknownSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((UnknownSnapshot.UnknownDataPointSnapshot) d);
      }
      return builder.build();
    } else {
      throw new IllegalArgumentException("Unknown MetricSnapshot type: " + v.getClass());
    }
  }

  private static Labels escapeLabels(Labels labels, EscapingScheme scheme) {
    Labels.Builder outLabelsBuilder = Labels.builder();

    for (Label l : labels) {
      outLabelsBuilder.label(escapeName(l.getName(), scheme), l.getValue());
    }

    return outLabelsBuilder.build();
  }

  static boolean snapshotNeedsEscaping(DataPointSnapshot d, EscapingScheme scheme) {
    Labels labels = d.getLabels();
    if (labelsNeedsEscaping(labels, scheme)) {
      return true;
    }
    if (d instanceof SummarySnapshot.SummaryDataPointSnapshot) {
      return exemplarsNeedsEscaping(
          ((SummarySnapshot.SummaryDataPointSnapshot) d).getExemplars(), scheme);
    }
    if (d instanceof HistogramSnapshot.HistogramDataPointSnapshot) {
      return exemplarsNeedsEscaping(
          ((HistogramSnapshot.HistogramDataPointSnapshot) d).getExemplars(), scheme);
    }
    if (d instanceof CounterSnapshot.CounterDataPointSnapshot) {
      return exemplarNeedsEscaping(
          ((CounterSnapshot.CounterDataPointSnapshot) d).getExemplar(), scheme);
    }
    if (d instanceof UnknownSnapshot.UnknownDataPointSnapshot) {
      return exemplarNeedsEscaping(
          ((UnknownSnapshot.UnknownDataPointSnapshot) d).getExemplar(), scheme);
    }
    if (d instanceof GaugeSnapshot.GaugeDataPointSnapshot) {
      return exemplarNeedsEscaping(
          ((GaugeSnapshot.GaugeDataPointSnapshot) d).getExemplar(), scheme);
    }

    return false;
  }

  private static boolean labelsNeedsEscaping(Labels labels, EscapingScheme scheme) {
    for (Label l : labels) {
      if (needsEscaping(l.getName(), scheme)) {
        return true;
      }
    }
    return false;
  }

  private static boolean exemplarNeedsEscaping(@Nullable Exemplar exemplar, EscapingScheme scheme) {
    return exemplar != null && labelsNeedsEscaping(exemplar.getLabels(), scheme);
  }

  private static boolean exemplarsNeedsEscaping(Exemplars exemplars, EscapingScheme scheme) {
    for (Exemplar exemplar : exemplars) {
      if (labelsNeedsEscaping(exemplar.getLabels(), scheme)) {
        return true;
      }
    }
    return false;
  }

  private static DataPointSnapshot createEscapedDataPointSnapshot(
      MetricSnapshot v, DataPointSnapshot d, Labels outLabels, EscapingScheme scheme) {
    if (v instanceof CounterSnapshot) {
      return CounterSnapshot.CounterDataPointSnapshot.builder()
          .value(((CounterSnapshot.CounterDataPointSnapshot) d).getValue())
          .exemplar(
              escapeExemplar(((CounterSnapshot.CounterDataPointSnapshot) d).getExemplar(), scheme))
          .labels(outLabels)
          .createdTimestampMillis(d.getCreatedTimestampMillis())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof GaugeSnapshot) {
      return GaugeSnapshot.GaugeDataPointSnapshot.builder()
          .value(((GaugeSnapshot.GaugeDataPointSnapshot) d).getValue())
          .exemplar(
              escapeExemplar(((GaugeSnapshot.GaugeDataPointSnapshot) d).getExemplar(), scheme))
          .labels(outLabels)
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof HistogramSnapshot) {
      return HistogramSnapshot.HistogramDataPointSnapshot.builder()
          .classicHistogramBuckets(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d).getClassicBuckets())
          .nativeSchema(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeSchema())
          .nativeZeroCount(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeZeroCount())
          .nativeZeroThreshold(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeZeroThreshold())
          .nativeBucketsForPositiveValues(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d)
                  .getNativeBucketsForPositiveValues())
          .nativeBucketsForNegativeValues(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d)
                  .getNativeBucketsForNegativeValues())
          .count(((HistogramSnapshot.HistogramDataPointSnapshot) d).getCount())
          .sum(((HistogramSnapshot.HistogramDataPointSnapshot) d).getSum())
          .exemplars(
              escapeExemplars(
                  ((HistogramSnapshot.HistogramDataPointSnapshot) d).getExemplars(), scheme))
          .labels(outLabels)
          .createdTimestampMillis(d.getCreatedTimestampMillis())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof SummarySnapshot) {
      return SummarySnapshot.SummaryDataPointSnapshot.builder()
          .quantiles(((SummarySnapshot.SummaryDataPointSnapshot) d).getQuantiles())
          .count(((SummarySnapshot.SummaryDataPointSnapshot) d).getCount())
          .sum(((SummarySnapshot.SummaryDataPointSnapshot) d).getSum())
          .exemplars(
              escapeExemplars(
                  ((SummarySnapshot.SummaryDataPointSnapshot) d).getExemplars(), scheme))
          .labels(outLabels)
          .createdTimestampMillis(d.getCreatedTimestampMillis())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof InfoSnapshot) {
      return InfoSnapshot.InfoDataPointSnapshot.builder()
          .labels(outLabels)
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof StateSetSnapshot) {
      StateSetSnapshot.StateSetDataPointSnapshot.Builder builder =
          StateSetSnapshot.StateSetDataPointSnapshot.builder()
              .labels(outLabels)
              .scrapeTimestampMillis(d.getScrapeTimestampMillis());
      for (StateSetSnapshot.State state : ((StateSetSnapshot.StateSetDataPointSnapshot) d)) {
        builder.state(state.getName(), state.isTrue());
      }
      return builder.build();
    } else if (v instanceof UnknownSnapshot) {
      return UnknownSnapshot.UnknownDataPointSnapshot.builder()
          .labels(outLabels)
          .value(((UnknownSnapshot.UnknownDataPointSnapshot) d).getValue())
          .exemplar(
              escapeExemplar(((UnknownSnapshot.UnknownDataPointSnapshot) d).getExemplar(), scheme))
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else {
      throw new IllegalArgumentException("Unknown MetricSnapshot type: " + v.getClass());
    }
  }

  private static Exemplars escapeExemplars(Exemplars exemplars, EscapingScheme scheme) {
    List<Exemplar> escapedExemplars = new ArrayList<>(exemplars.size());
    for (Exemplar exemplar : exemplars) {
      escapedExemplars.add(escapeExemplar(exemplar, scheme));
    }
    return Exemplars.of(escapedExemplars);
  }

  private static Exemplar escapeExemplar(@Nullable Exemplar exemplar, EscapingScheme scheme) {
    if (exemplar == null) {
      return null;
    }
    return Exemplar.builder()
        .labels(escapeLabels(exemplar.getLabels(), scheme))
        .timestampMillis(exemplar.getTimestampMillis())
        .value(exemplar.getValue())
        .build();
  }

  /**
   * Escapes the incoming name according to the provided escaping scheme. Depending on the rules of
   * escaping, this may cause no change in the string that is returned (especially NO_ESCAPING,
   * which by definition is a noop). This method does not do any validation of the name.
   */
  public static String escapeName(String name, EscapingScheme scheme) {
    if (name.isEmpty() || !needsEscaping(name, scheme)) {
      return name;
    }

    StringBuilder escaped = new StringBuilder();
    switch (scheme) {
      case NO_ESCAPING:
        return name;
      case UNDERSCORE_ESCAPING:
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else {
            escaped.append('_');
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      case DOTS_ESCAPING:
        // Do not early return for legacy valid names, we still escape underscores.
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (c == '_') {
            escaped.append("__");
          } else if (c == '.') {
            escaped.append("_dot_");
          } else if (isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else {
            escaped.append("__");
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      case VALUE_ENCODING_ESCAPING:
        escaped.append("U__");
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (c == '_') {
            escaped.append("__");
          } else if (isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else if (!isValidUtf8Char(c)) {
            escaped.append("_FFFD_");
          } else {
            escaped.append('_');
            escaped.append(Integer.toHexString(c));
            escaped.append('_');
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      default:
        throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
    }
  }

  public static boolean needsEscaping(String name, EscapingScheme scheme) {
    return !PrometheusNaming.isValidLegacyMetricName(name)
        || (scheme == EscapingScheme.DOTS_ESCAPING && (name.contains(".") || name.contains("_")));
  }

  static boolean isValidLegacyChar(int c, int i) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || c == '_'
        || c == ':'
        || (c >= '0' && c <= '9' && i > 0);
  }

  private static boolean isValidUtf8Char(int c) {
    return (0 <= c && c < MIN_HIGH_SURROGATE) || (MAX_LOW_SURROGATE < c && c <= MAX_CODE_POINT);
  }

  public static String getSnapshotLabelName(Labels labels, int index, EscapingScheme scheme) {
    if (scheme == EscapingScheme.UNDERSCORE_ESCAPING) {
      return labels.getPrometheusName(index);
    } else {
      return labels.getName(index);
    }
  }

  public static String getMetadataName(MetricMetadata metadata, EscapingScheme scheme) {
    if (scheme == EscapingScheme.UNDERSCORE_ESCAPING) {
      return metadata.getPrometheusName();
    } else {
      return metadata.getName();
    }
  }
}
