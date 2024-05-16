package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.HistogramPointData;

import java.util.List;

public class HistogramPointDataImpl extends PointDataImpl implements HistogramPointData {

    private final double sum;
    private final long count;
    private final double min;
    private final double max;
    private final List<Double> boundaries;
    private final List<Long> counts;

    public HistogramPointDataImpl(double sum, long count, double min, double max, List<Double> boundaries, List<Long> counts,
                                  long startEpochNanos, long epochNanos, Attributes attributes, List<DoubleExemplarData> exemplars) {
        super(startEpochNanos, epochNanos, attributes, exemplars);
        this.sum = sum;
        this.count = count;
        this.min = min;
        this.max = max;
        this.boundaries = boundaries;
        this.counts = counts;
    }

    @Override
    public double getSum() {
        return sum;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public boolean hasMin() {
        return !Double.isNaN(min);
    }

    @Override
    public double getMin() {
        return min;
    }

    @Override
    public boolean hasMax() {
        return !Double.isNaN(max);
    }

    @Override
    public double getMax() {
        return max;
    }

    @Override
    public List<Double> getBoundaries() {
        return boundaries;
    }

    @Override
    public List<Long> getCounts() {
        return counts;
    }
}
