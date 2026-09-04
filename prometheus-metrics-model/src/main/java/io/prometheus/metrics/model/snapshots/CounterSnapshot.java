package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.config.EscapingScheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/** Immutable snapshot of a Counter. */
@StableApi
public class CounterSnapshot extends MetricSnapshot {

  /**
   * To create a new {@link CounterSnapshot}, you can either call the constructor directly or use
   * the builder with {@link CounterSnapshot#builder()}.
   *
   * @param metadata the metric name in metadata must not include the {@code _total} suffix. See
   *     {@link MetricMetadata} for more naming conventions.
   * @param dataPoints the constructor will create a sorted copy of the collection.
   */
  public CounterSnapshot(MetricMetadata metadata, Collection<CounterDataPointSnapshot> dataPoints) {
    this(metadata, dataPoints, false);
  }

  private CounterSnapshot(
      MetricMetadata metadata, Collection<CounterDataPointSnapshot> dataPoints, boolean internal) {
    super(metadata, dataPoints, internal);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<CounterDataPointSnapshot> getDataPoints() {
    return (List<CounterDataPointSnapshot>) dataPoints;
  }

  @SuppressWarnings("unchecked")
  @Override
  MetricSnapshot escape(
      EscapingScheme escapingScheme, List<? extends DataPointSnapshot> dataPointSnapshots) {
    return new CounterSnapshot(
        getMetadata().escape(escapingScheme),
        (List<CounterDataPointSnapshot>) dataPointSnapshots,
        true);
  }

  public static class CounterDataPointSnapshot extends DataPointSnapshot {

    private final double value;
    @Nullable private final Exemplar exemplar;
    /** Optional metric name used only in validation error messages. */
    @Nullable private final String metricName;

    /**
     * To create a new {@link CounterDataPointSnapshot}, you can either call the constructor
     * directly or use the Builder with {@link CounterDataPointSnapshot#builder()}.
     *
     * @param value the counter value. Must not be negative.
     * @param labels must not be null. Use {@link Labels#EMPTY} if there are no labels.
     * @param exemplar may be null.
     * @param createdTimestampMillis timestamp (as in {@link System#currentTimeMillis()}) when the
     *     time series (this specific set of labels) was created (or reset to zero). It's optional.
     *     Use {@code 0L} if there is no created timestamp.
     */
    public CounterDataPointSnapshot(
        double value, Labels labels, @Nullable Exemplar exemplar, long createdTimestampMillis) {
      this(value, labels, exemplar, createdTimestampMillis, 0, null);
    }

    /**
     * Same as {@link #CounterDataPointSnapshot(double, Labels, Exemplar, long)} with an optional
     * metric name included in validation error messages when the value is negative.
     */
    public CounterDataPointSnapshot(
        double value,
        Labels labels,
        @Nullable Exemplar exemplar,
        long createdTimestampMillis,
        @Nullable String metricName) {
      this(value, labels, exemplar, createdTimestampMillis, 0, metricName);
    }

    /**
     * Constructor with an additional scrape timestamp. This is only useful in rare cases as the
     * scrape timestamp is usually set by the Prometheus server during scraping. Exceptions include
     * mirroring metrics with given timestamps from other metric sources.
     */
    @SuppressWarnings("this-escape")
    public CounterDataPointSnapshot(
        double value,
        Labels labels,
        @Nullable Exemplar exemplar,
        long createdTimestampMillis,
        long scrapeTimestampMillis) {
      this(value, labels, exemplar, createdTimestampMillis, scrapeTimestampMillis, false, null);
    }

    /**
     * Constructor with scrape timestamp and optional metric name for validation messages.
     *
     * @see #CounterDataPointSnapshot(double, Labels, Exemplar, long, long)
     */
    @SuppressWarnings("this-escape")
    public CounterDataPointSnapshot(
        double value,
        Labels labels,
        @Nullable Exemplar exemplar,
        long createdTimestampMillis,
        long scrapeTimestampMillis,
        @Nullable String metricName) {
      this(
          value,
          labels,
          exemplar,
          createdTimestampMillis,
          scrapeTimestampMillis,
          false,
          metricName);
    }

    @SuppressWarnings("this-escape")
    public CounterDataPointSnapshot(
        double value,
        Labels labels,
        @Nullable Exemplar exemplar,
        long createdTimestampMillis,
        long scrapeTimestampMillis,
        boolean internal) {
      this(value, labels, exemplar, createdTimestampMillis, scrapeTimestampMillis, internal, null);
    }

    @SuppressWarnings("this-escape")
    private CounterDataPointSnapshot(
        double value,
        Labels labels,
        @Nullable Exemplar exemplar,
        long createdTimestampMillis,
        long scrapeTimestampMillis,
        boolean internal,
        @Nullable String metricName) {
      super(labels, createdTimestampMillis, scrapeTimestampMillis, internal);
      this.value = value;
      this.exemplar = exemplar;
      this.metricName = metricName;
      if (!internal) {
        validate();
      }
    }

    public double getValue() {
      return value;
    }

    @Nullable
    public Exemplar getExemplar() {
      return exemplar;
    }

    protected void validate() {
      if (value < 0.0) {
        StringBuilder message = new StringBuilder();
        if (metricName != null && !metricName.isEmpty()) {
          message.append(metricName).append('=');
        }
        message.append(value).append(": counters cannot have a negative value");
        Labels labels = getLabels();
        if (labels != null && !labels.isEmpty()) {
          message.append(" (labels=").append(labels).append(')');
        }
        throw new IllegalArgumentException(message.toString());
      }
    }

    @Override
    DataPointSnapshot escape(EscapingScheme escapingScheme) {
      return new CounterSnapshot.CounterDataPointSnapshot(
          value,
          SnapshotEscaper.escapeLabels(getLabels(), escapingScheme),
          SnapshotEscaper.escapeExemplar(exemplar, escapingScheme),
          getCreatedTimestampMillis(),
          getScrapeTimestampMillis(),
          true,
          metricName);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder extends DataPointSnapshot.Builder<Builder> {

      @Nullable private Exemplar exemplar = null;
      @Nullable private Double value = null;
      private long createdTimestampMillis = 0L;
      @Nullable private String metricName = null;

      private Builder() {}

      /** Counter value. This is required. The value must not be negative. */
      public Builder value(double value) {
        this.value = value;
        return this;
      }

      public Builder exemplar(@Nullable Exemplar exemplar) {
        this.exemplar = exemplar;
        return this;
      }

      public Builder createdTimestampMillis(long createdTimestampMillis) {
        this.createdTimestampMillis = createdTimestampMillis;
        return this;
      }

      /**
       * Optional metric name included in the exception message when {@link #value(double)} is
       * negative. Does not change the snapshot identity.
       */
      public Builder metricName(@Nullable String metricName) {
        this.metricName = metricName;
        return this;
      }

      public CounterDataPointSnapshot build() {
        if (value == null) {
          throw new IllegalArgumentException("Missing required field: value is null.");
        }
        return new CounterDataPointSnapshot(
            value,
            labels,
            exemplar,
            createdTimestampMillis,
            scrapeTimestampMillis,
            metricName);
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

    private final List<CounterDataPointSnapshot> dataPoints = new ArrayList<>();

    private Builder() {}

    /** Add a data point. Can be called multiple times to add multiple data points. */
    public Builder dataPoint(CounterDataPointSnapshot dataPoint) {
      dataPoints.add(dataPoint);
      return this;
    }

    @Override
    protected MetricMetadata buildMetadata() {
      if (name == null) {
        throw new IllegalArgumentException("Missing required field: name is null");
      }
      return MetricMetadataSupport.counterMetadata(name, help, unit);
    }

    @Override
    public CounterSnapshot build() {
      return new CounterSnapshot(buildMetadata(), dataPoints);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
