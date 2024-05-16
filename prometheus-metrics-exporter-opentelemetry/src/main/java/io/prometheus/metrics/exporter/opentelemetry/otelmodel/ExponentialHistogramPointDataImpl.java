package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ExponentialHistogramBuckets;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ExponentialHistogramPointData;

import java.util.List;

public class ExponentialHistogramPointDataImpl extends PointDataImpl implements ExponentialHistogramPointData {

    private final int scale;
    private final double sum;
    private final long count;
    private final long zeroCount;
    private final double min;
    private final double max;

    private final ExponentialHistogramBuckets positiveBuckets;
    private final ExponentialHistogramBuckets negativeBuckets;

    ExponentialHistogramPointDataImpl(int scale, double sum, long count, long zeroCount, double min, double max,
                                      ExponentialHistogramBuckets positiveBuckets, ExponentialHistogramBuckets negativeBuckets,
                                      long startEpochNanos, long epochNanos, Attributes attributes, List<DoubleExemplarData> exemplars) {
        super(startEpochNanos, epochNanos, attributes, exemplars);
        this.scale = scale;
        this.sum = sum;
        this.count = count;
        this.zeroCount = zeroCount;
        this.min = min;
        this.max = max;
        this.positiveBuckets = positiveBuckets;
        this.negativeBuckets = negativeBuckets;
    }

    @Override
    public int getScale() {
        return scale;
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
    public long getZeroCount() {
        return zeroCount;
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
    public ExponentialHistogramBuckets getPositiveBuckets() {
        return positiveBuckets;
    }

    @Override
    public ExponentialHistogramBuckets getNegativeBuckets() {
        return negativeBuckets;
    }
}
