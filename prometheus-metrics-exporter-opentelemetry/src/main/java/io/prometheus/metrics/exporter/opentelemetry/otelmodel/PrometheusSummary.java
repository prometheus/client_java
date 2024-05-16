package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SummaryData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SummaryPointData;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class PrometheusSummary extends PrometheusData<SummaryPointData> implements SummaryData {

    private final List<SummaryPointData> points;

    PrometheusSummary(SummarySnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.SUMMARY);
        this.points = snapshot.getDataPoints().stream()
                .map(dataPoint -> toOtelDataPoint(dataPoint, currentTimeMillis))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<SummaryPointData> getPoints() {
        return points;
    }

    private SummaryPointData toOtelDataPoint(SummarySnapshot.SummaryDataPointSnapshot dataPoint, long currentTimeMillis) {
            SummaryPointDataImpl result = new SummaryPointDataImpl(
                    dataPoint.hasSum() ? dataPoint.getSum() : Double.NaN,
                    dataPoint.hasCount() ? dataPoint.getCount() : 0,
                    getStartEpochNanos(dataPoint),
                    getEpochNanos(dataPoint, currentTimeMillis),
                    labelsToAttributes(dataPoint.getLabels()),
                    convertExemplars(dataPoint.getExemplars())
            );
            for (Quantile quantile : dataPoint.getQuantiles()) {
                result.addValue(quantile.getQuantile(), quantile.getValue());
            }
            return result;
    }
}
