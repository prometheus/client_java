package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.config.EscapingScheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/** Immutable snapshot of an Unknown (Untyped) metric. */
public final class UnknownSnapshot extends MetricSnapshot {

  /**
   * To create a new {@link UnknownSnapshot}, you can either call the constructor directly or use
   * the builder with {@link UnknownSnapshot#builder()}.
   *
   * @param metadata required name and optional help and unit. See {@link MetricMetadata} for naming
   *     conventions.
   * @param data the constructor will create a sorted copy of the collection.
   */
  public UnknownSnapshot(MetricMetadata metadata, Collection<UnknownDataPointSnapshot> data) {
    this(metadata, data, false);
  }

  private UnknownSnapshot(
      MetricMetadata metadata, Collection<UnknownDataPointSnapshot> data, boolean internal) {
    super(metadata, data, internal);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<UnknownDataPointSnapshot> getDataPoints() {
    return (List<UnknownDataPointSnapshot>) dataPoints;
  }

  @SuppressWarnings("unchecked")
  @Override
  MetricSnapshot escape(
      EscapingScheme escapingScheme, List<? extends DataPointSnapshot> dataPointSnapshots) {
    return new UnknownSnapshot(
        getMetadata().escape(escapingScheme),
        (List<UnknownDataPointSnapshot>) dataPointSnapshots,
        true);
  }

  public static final class UnknownDataPointSnapshot extends DataPointSnapshot {

    private final double value;
    @Nullable private final Exemplar exemplar;

    /**
     * To create a new {@link UnknownDataPointSnapshot}, you can either call the constructor
     * directly or use the Builder with {@link UnknownDataPointSnapshot#builder()}.
     *
     * @param value the value.
     * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
     * @param exemplar may be null.
     */
    public UnknownDataPointSnapshot(double value, Labels labels, @Nullable Exemplar exemplar) {
      this(value, labels, exemplar, 0);
    }

    /**
     * Constructor with an additional scrape timestamp. This is only useful in rare cases as the
     * scrape timestamp is usually set by the Prometheus server during scraping. Exceptions include
     * mirroring metrics with given timestamps from other metric sources.
     */
    public UnknownDataPointSnapshot(
        double value, Labels labels, @Nullable Exemplar exemplar, long scrapeTimestampMillis) {
      this(value, labels, exemplar, scrapeTimestampMillis, false);
    }

    private UnknownDataPointSnapshot(
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
      return new UnknownDataPointSnapshot(
          value,
          SnapshotEscaper.escapeLabels(getLabels(), escapingScheme),
          SnapshotEscaper.escapeExemplar(exemplar, escapingScheme),
          getScrapeTimestampMillis(),
          true);
    }

    public static class Builder extends DataPointSnapshot.Builder<Builder> {

      @Nullable private Exemplar exemplar = null;
      @Nullable private Double value = null;

      private Builder() {}

      /** required. */
      public Builder value(double value) {
        this.value = value;
        return this;
      }

      /** Optional */
      public Builder exemplar(@Nullable Exemplar exemplar) {
        this.exemplar = exemplar;
        return this;
      }

      public UnknownDataPointSnapshot build() {
        if (value == null) {
          throw new IllegalArgumentException("Missing required field: value is null.");
        }
        return new UnknownDataPointSnapshot(value, labels, exemplar, scrapeTimestampMillis);
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

    private final List<UnknownDataPointSnapshot> dataPoints = new ArrayList<>();

    private Builder() {}

    /** Add a data point. Call multiple times to add multiple data points. */
    public Builder dataPoint(UnknownDataPointSnapshot data) {
      dataPoints.add(data);
      return this;
    }

    @Override
    public UnknownSnapshot build() {
      return new UnknownSnapshot(buildMetadata(), dataPoints);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
