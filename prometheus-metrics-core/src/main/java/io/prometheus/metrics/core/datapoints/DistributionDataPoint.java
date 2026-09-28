package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.annotations.StableApi;
import io.prometheus.metrics.model.snapshots.Labels;

/**
 * Represents a single data point of a histogram or a summary metric.
 *
 * <p>Single data point means identified label values like {@code {method="GET", path="/",
 * status_code="200"}}, ignoring the {@code "le"} label for histograms or the {@code "quantile"}
 * label for summaries.
 *
 * <p>This interface is named <i>DistributionDataPoint</i> because both histograms and summaries are
 * used to observe distributions, like latency distributions or distributions of request sizes.
 * Therefore <i>DistributionDataPoint</i> is a good name for a common interface implemented by
 * histogram data points and summary data points.
 *
 * <p>See JavaDoc of {@link CounterDataPoint} on how using data points directly can improve
 * performance.
 */
@StableApi
public interface DistributionDataPoint extends DataPoint, TimerApi {

  /** Get the count of observations. */
  long getCount();

  /** Get the sum of all observed values. */
  double getSum();

  /** Observe {@code value}. */
  void observe(double value);

  /** Observe {@code value}, and create a custom exemplar with the given labels. */
  void observeWithExemplar(double value, Labels labels);

  /**
   * Observe {@code value} {@code count} times, as a single operation.
   *
   * <p>Use this to record pre-aggregated data ("this value occurred {@code count} times") without
   * paying the per-observation cost of calling {@link #observe(double)} in a loop. Buckets and the
   * observation count end up exactly as if {@link #observe(double)} had been called {@code count}
   * times. The implementations in this library additionally guarantee that
   *
   * <ul>
   *   <li>the batch is applied atomically with respect to scrapes, so a snapshot contains either
   *       all of it or none of it,
   *   <li>the sum is increased by the correctly rounded product {@code value * count} rather than
   *       by {@code count} successive floating point additions (the product is at least as
   *       accurate, and for {@code count == 1} the two are identical),
   *   <li>at most one exemplar is sampled for the batch.
   * </ul>
   *
   * <p>{@code count == 0} is a no-op. A negative {@code count} throws {@link
   * IllegalArgumentException}. {@code NaN} values are ignored, as in {@link #observe(double)}.
   *
   * <p>The default implementation loops over {@link #observe(double)}. Histograms and summaries
   * override it with an implementation whose cost does not depend on {@code count}.
   */
  default void observe(double value, long count) {
    if (count < 0) {
      throw new IllegalArgumentException("Negative count " + count + " is illegal.");
    }
    for (long i = 0; i < count; i++) {
      observe(value);
    }
  }

  @Override
  default Timer startTimer() {
    return new Timer(this::observe);
  }
}
