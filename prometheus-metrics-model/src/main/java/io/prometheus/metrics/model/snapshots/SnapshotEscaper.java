package io.prometheus.metrics.model.snapshots;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MAX_LOW_SURROGATE;
import static java.lang.Character.MIN_HIGH_SURROGATE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

class SnapshotEscaper {
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

  /** Escapes the given metric names and labels with the given escaping scheme. */
  public static MetricSnapshot escapeMetricSnapshot(MetricSnapshot v, EscapingScheme scheme) {
    if (v == null) {
      return null;
    }

    if (scheme == EscapingScheme.NO_ESCAPING) {
      return v;
    }

    List<DataPointSnapshot> outDataPoints = new ArrayList<>();

    for (DataPointSnapshot d : v.getDataPoints()) {
      if (!snapshotNeedsEscaping(d, scheme)) {
        outDataPoints.add(d);
        continue;
      }

      DataPointSnapshot outDataPointSnapshot =
          createEscapedDataPointSnapshot(v, d, escapeLabels(d.getLabels(), scheme), scheme);
      outDataPoints.add(outDataPointSnapshot);
    }

    return createEscapedMetricSnapshot(
        v, escapeName(v.getMetadata().getName(), scheme), outDataPoints);
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

  private static boolean needsEscaping(String name, EscapingScheme scheme) {
    return !PrometheusNaming.isValidLegacyMetricName(name)
        || (scheme == EscapingScheme.DOTS_ESCAPING && (name.contains(".") || name.contains("_")));
  }

  /**
   * Unescapes the incoming name according to the provided escaping scheme if possible. Some schemes
   * are partially or totally non-roundtripable. If any error is encountered, returns the original
   * input.
   */
  @SuppressWarnings("IncrementInForLoopAndHeader")
  static String unescapeName(String name, EscapingScheme scheme) {
    if (name.isEmpty()) {
      return name;
    }
    switch (scheme) {
      case NO_ESCAPING:
        return name;
      case UNDERSCORE_ESCAPING:
        // It is not possible to unescape from underscore replacement.
        return name;
      case DOTS_ESCAPING:
        name = name.replaceAll("_dot_", ".");
        name = name.replaceAll("__", "_");
        return name;
      case VALUE_ENCODING_ESCAPING:
        Matcher matcher = Pattern.compile("U__").matcher(name);
        if (matcher.find()) {
          String escapedName = name.substring(matcher.end());
          StringBuilder unescaped = new StringBuilder();
          for (int i = 0; i < escapedName.length(); ) {
            // All non-underscores are treated normally.
            int c = escapedName.codePointAt(i);
            if (c != '_') {
              unescaped.appendCodePoint(c);
              i += Character.charCount(c);
              continue;
            }
            i++;
            if (i >= escapedName.length()) {
              return name;
            }
            // A double underscore is a single underscore.
            if (escapedName.codePointAt(i) == '_') {
              unescaped.append('_');
              i++;
              continue;
            }
            // We think we are in a UTF-8 code, process it.
            int utf8Val = 0;
            boolean foundClosingUnderscore = false;
            for (int j = 0; i < escapedName.length(); j++) {
              // This is too many characters for a UTF-8 value.
              if (j >= 6) {
                return name;
              }
              // Found a closing underscore, convert to a char, check validity, and append.
              if (escapedName.codePointAt(i) == '_') {
                // char utf8Char = (char) utf8Val;
                foundClosingUnderscore = true;
                if (!isValidUtf8Char(utf8Val)) {
                  return name;
                }
                unescaped.appendCodePoint(utf8Val);
                i++;
                break;
              }
              char r = Character.toLowerCase(escapedName.charAt(i));
              utf8Val *= 16;
              if (r >= '0' && r <= '9') {
                utf8Val += r - '0';
              } else if (r >= 'a' && r <= 'f') {
                utf8Val += r - 'a' + 10;
              } else {
                return name;
              }
              i++;
            }
            if (!foundClosingUnderscore) {
              return name;
            }
          }
          return unescaped.toString();
        } else {
          return name;
        }
      default:
        throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
    }
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
}
