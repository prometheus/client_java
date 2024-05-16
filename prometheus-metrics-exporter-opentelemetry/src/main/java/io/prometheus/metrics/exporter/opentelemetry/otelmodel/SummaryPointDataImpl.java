package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoubleExemplarData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SummaryPointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ValueAtQuantile;

import java.util.ArrayList;
import java.util.List;

public class SummaryPointDataImpl extends PointDataImpl implements SummaryPointData {
    private final double sum;
    private final long count;
    private final List<ValueAtQuantile> values;

    public SummaryPointDataImpl(double sum, long count, long startEpochNanos, long epochNanos, Attributes attributes, List<DoubleExemplarData> exemplars) {
        super(startEpochNanos, epochNanos, attributes, exemplars);
        this.sum = sum;
        this.count = count;
        this.values = new ArrayList<>();
    }

    void addValue(double quantile, double value) {
        values.add(new ValueAtQuantileImpl(quantile, value));
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public double getSum() {
        return sum;
    }

    @Override
    public List<ValueAtQuantile> getValues() {
        return values;
    }
}
