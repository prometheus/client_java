package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.model.snapshots.Labels;

/**
 * Represents a single data point of a histogram or a summary metric.
 * <p>
 * Single data point means identified label values like {@code {method="GET", path="/", status_code="200"}},
 * ignoring the {@code "le"} label for histograms or the {@code "quantile"} label for summaries.
 * <p>
 * This interface is named <i>DistributionDataPoint</i> because both histograms and summaries are used to observe
 * distributions, like latency distributions or distributions of request sizes. Therefore
 * <i>DistributionDataPoint</i> is a good name for a common interface implemented by histogram data points
 * and summary data points.
 * <p>
 * See JavaDoc of {@link CounterDataPoint} on how using data points directly can improve performance.
 */
public interface DistributionDataPoint extends DataPoint, TimerApi {

    /**
     * Observe {@code value}.
     */
    void observe(double value);

    /**
     * Observe {@code value}, and create a custom exemplar with the given labels.
     */
    void observeWithExemplar(double value, Labels labels);

    /**
     * {@inheritDoc}
     */
    default Timer startTimer() {
        return new Timer(this::observe);
    }
}
