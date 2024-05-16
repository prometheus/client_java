package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.AggregationTemporality;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoublePointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SumData;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PrometheusInfo extends PrometheusData<DoublePointData> implements SumData<DoublePointData> {

    private final List<DoublePointData> points;

    public PrometheusInfo(InfoSnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.DOUBLE_SUM);
        this.points = snapshot.getDataPoints().stream()
                .map(dataPoint -> toOtelDataPoint(dataPoint, currentTimeMillis))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMonotonic() {
        return false;
    }

    @Override
    public AggregationTemporality getAggregationTemporality() {
        return AggregationTemporality.CUMULATIVE;
    }

    @Override
    public Collection<DoublePointData> getPoints() {
        return points;
    }

    private DoublePointData toOtelDataPoint(InfoSnapshot.InfoDataPointSnapshot dataPoint, long currentTimeMillis) {
        return new DoublePointDataImpl(
                1.0,
                getStartEpochNanos(dataPoint),
                getEpochNanos(dataPoint, currentTimeMillis),
                labelsToAttributes(dataPoint.getLabels()),
                Collections.emptyList()
        );
    }
}
