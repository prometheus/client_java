package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoublePointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.GaugeData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class PrometheusUnknown extends PrometheusData<DoublePointData> implements GaugeData<DoublePointData> {

    private final List<DoublePointData> points;

    public PrometheusUnknown(UnknownSnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.DOUBLE_GAUGE);
        this.points = snapshot.getDataPoints().stream()
                .map(dataPoint -> toOtelDataPoint(dataPoint, currentTimeMillis))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<DoublePointData> getPoints() {
        return points;
    }

    private DoublePointData toOtelDataPoint(UnknownSnapshot.UnknownDataPointSnapshot dataPoint, long currentTimeMillis) {
        return new DoublePointDataImpl(
                dataPoint.getValue(),
                getStartEpochNanos(dataPoint),
                getEpochNanos(dataPoint, currentTimeMillis),
                labelsToAttributes(dataPoint.getLabels()),
                convertExemplar(dataPoint.getExemplar())
        );
    }
}
