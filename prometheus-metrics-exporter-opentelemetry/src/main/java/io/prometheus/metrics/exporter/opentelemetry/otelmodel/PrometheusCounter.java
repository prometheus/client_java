package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.AggregationTemporality;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoublePointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SumData;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class PrometheusCounter extends PrometheusData<DoublePointData> implements SumData<DoublePointData> {

    private final List<DoublePointData> points;

    public PrometheusCounter(CounterSnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.DOUBLE_SUM);
        this.points = snapshot.getDataPoints().stream()
                .map(dataPoint -> toOtelDataPoint(dataPoint, currentTimeMillis))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMonotonic() {
        return true;
    }

    @Override
    public AggregationTemporality getAggregationTemporality() {
        return AggregationTemporality.CUMULATIVE;
    }

    @Override
    public Collection<DoublePointData> getPoints() {
        return points;
    }

    private DoublePointData toOtelDataPoint(CounterSnapshot.CounterDataPointSnapshot dataPoint, long currentTimeMillis) {
        return new DoublePointDataImpl(
                dataPoint.getValue(),
                getStartEpochNanos(dataPoint),
                getEpochNanos(dataPoint, currentTimeMillis),
                labelsToAttributes(dataPoint.getLabels()),
                convertExemplar(dataPoint.getExemplar())
        );
    }
}
