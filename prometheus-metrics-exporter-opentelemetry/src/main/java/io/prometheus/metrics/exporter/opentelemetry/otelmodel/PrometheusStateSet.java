package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.AggregationTemporality;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoublePointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SumData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PrometheusStateSet extends PrometheusData<DoublePointData> implements SumData<DoublePointData> {

    private final List<DoublePointData> points;
    public PrometheusStateSet(StateSetSnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.DOUBLE_SUM);
        this.points = new ArrayList<>();
        for (StateSetSnapshot.StateSetDataPointSnapshot dataPoint : snapshot.getDataPoints()) {
            for (int i=0; i<dataPoint.size(); i++) {
                this.points.add(toOtelDataPoint(snapshot, dataPoint, i, currentTimeMillis));
            }
        }
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

    private DoublePointData toOtelDataPoint(StateSetSnapshot snapshot, StateSetSnapshot.StateSetDataPointSnapshot dataPoint, int i, long currentTimeMillis) {
        return new DoublePointDataImpl(
                dataPoint.isTrue(i) ? 1.0 : 0.0,
                getStartEpochNanos(dataPoint),
                getEpochNanos(dataPoint, currentTimeMillis),
                labelsToAttributes(dataPoint.getLabels().merge(Labels.of(snapshot.getMetadata().getName(), dataPoint.getName(i)))),
                Collections.emptyList()
        );
    }
}
