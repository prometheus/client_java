package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class SnapshotEscaper {

  private SnapshotEscaper() {}

  /** Escapes the given metric names and labels with the given escaping scheme. */
  @Nullable
  public static MetricSnapshot escapeMetricSnapshot(
      @Nullable MetricSnapshot v, EscapingScheme scheme) {
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
        v, PrometheusNaming.escapeName(v.getMetadata().getName(), scheme), outDataPoints);
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
      outLabelsBuilder.label(PrometheusNaming.escapeName(l.getName(), scheme), l.getValue());
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
      if (PrometheusNaming.needsEscaping(l.getName(), scheme)) {
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
