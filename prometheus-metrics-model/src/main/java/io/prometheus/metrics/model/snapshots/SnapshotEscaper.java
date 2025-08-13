package io.prometheus.metrics.model.snapshots;

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
      if (snapshotNeedsEscaping(d, scheme)) {
        outDataPoints.add(d.escape(scheme));
      } else {
        outDataPoints.add(d);
      }
    }

    return v.escape(scheme, outDataPoints);
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

  public static Labels escapeLabels(Labels labels, EscapingScheme scheme) {
    Labels.Builder outLabelsBuilder = Labels.builder();

    for (Label l : labels) {
      outLabelsBuilder.label(PrometheusNaming.escapeName(l.getName(), scheme), l.getValue());
    }

    return outLabelsBuilder.build();
  }

  public static Exemplars escapeExemplars(Exemplars exemplars, EscapingScheme scheme) {
    List<Exemplar> escapedExemplars = new ArrayList<>(exemplars.size());
    for (Exemplar exemplar : exemplars) {
      escapedExemplars.add(escapeExemplar(exemplar, scheme));
    }
    return Exemplars.of(escapedExemplars);
  }

  public static Exemplar escapeExemplar(@Nullable Exemplar exemplar, EscapingScheme scheme) {
    if (exemplar == null) {
      return null;
    }
    return Exemplar.builder()
        .labels(escapeLabels(exemplar.getLabels(), scheme))
        .timestampMillis(exemplar.getTimestampMillis())
        .value(exemplar.getValue())
        .build();
  }
}
