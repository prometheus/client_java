package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.config.EscapingScheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/** Immutable snapshot of a Gauge. */
public final class GaugeSnapshot extends MetricSnapshot {

  /**
   * To create a new {@link GaugeSnapshot}, you can either call the constructor directly or use the
   * builder with {@link GaugeSnapshot#builder()}.
   *
   * @param metadata see {@link MetricMetadata} for naming conventions.
   * @param data the constructor will create a sorted copy of the collection.
   */
  public GaugeSnapshot(MetricMetadata metadata, Collection<GaugeDataPointSnapshot> data) {
    this(metadata, data, false);
  }

  private GaugeSnapshot(
      MetricMetadata metadata, Collection<GaugeDataPointSnapshot> data, boolean internal) {
    super(metadata, data, internal);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<GaugeDataPointSnapshot> getDataPoints() {
    return (List<GaugeDataPointSnapshot>) dataPoints;
  }

  @SuppressWarnings("unchecked")
  @Override
  MetricSnapshot escape(
      EscapingScheme escapingScheme, List<? extends DataPointSnapshot> dataPointSnapshots) {
    return new GaugeSnapshot(
        getMetadata().escape(escapingScheme),
        (List<GaugeDataPointSnapshot>) dataPointSnapshots,
        true);
  }

  public static final class GaugeDataPointSnapshot extends DataPointSnapshot {

    private final double value;
    @Nullable private final Exemplar exemplar;

    /**
     * To create a new {@link GaugeDataPointSnapshot}, you can either call the constructor directly
     * or use the Builder with {@link GaugeDataPointSnapshot#builder()}.
     *
     * @param value the gauge value.
     * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
     * @param exemplar may be null.
     */
    public GaugeDataPointSnapshot(double value, Labels labels, @Nullable Exemplar exemplar) {
      this(value, labels, exemplar, 0);
    }

    /**
     * Constructor with an additional scrape timestamp. This is only useful in rare cases as the
     * scrape timestamp is usually set by the Prometheus server during scraping. Exceptions include
     * mirroring metrics with given timestamps from other metric sources.
     */
    public GaugeDataPointSnapshot(
        double value, Labels labels, @Nullable Exemplar exemplar, long scrapeTimestampMillis) {
      this(value, labels, exemplar, scrapeTimestampMillis, false);
    }

    private GaugeDataPointSnapshot(
        double value,
        Labels labels,
        @Nullable Exemplar exemplar,
        long scrapeTimestampMillis,
        boolean internal) {
      super(labels, 0L, scrapeTimestampMillis, internal);
      this.value = value;
      this.exemplar = exemplar;
    }

    public double getValue() {
      return value;
    }

    @Nullable
    public Exemplar getExemplar() {
      return exemplar;
    }

    public static Builder builder() {
      return new Builder();
    }

    @Override
    DataPointSnapshot escape(EscapingScheme escapingScheme) {
      return new GaugeSnapshot.GaugeDataPointSnapshot(
          value,
          SnapshotEscaper.escapeLabels(getLabels(), escapingScheme),
          SnapshotEscaper.escapeExemplar(exemplar, escapingScheme),
          getCreatedTimestampMillis(),
          true);
    }

    public static class Builder extends DataPointSnapshot.Builder<Builder> {

      @Nullable private Exemplar exemplar = null;
      @Nullable private Double value = null;

      private Builder() {}

      /** Gauge value. This is required. */
      public Builder value(double value) {
        this.value = value;
        return this;
      }

      /** Optional */
      public Builder exemplar(Exemplar exemplar) {
        this.exemplar = exemplar;
        return this;
      }

      public GaugeDataPointSnapshot build() {
        if (value == null) {
          throw new IllegalArgumentException("Missing required field: value is null.");
        }
        return new GaugeDataPointSnapshot(value, labels, exemplar, scrapeTimestampMillis);
      }

      @Override
      protected Builder self() {
        return this;
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends MetricSnapshot.Builder<Builder> {

    private final List<GaugeDataPointSnapshot> dataPoints = new ArrayList<>();

    private Builder() {}

    /** Add a data point. This can be called multiple times to add multiple data points. */
    public Builder dataPoint(GaugeDataPointSnapshot dataPoint) {
      dataPoints.add(dataPoint);
      return this;
    }

    @Override
    public GaugeSnapshot build() {
      return new GaugeSnapshot(buildMetadata(), dataPoints);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
