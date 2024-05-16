package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.AggregationTemporality;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.HistogramData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.HistogramPointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class PrometheusClassicHistogram extends PrometheusData<HistogramPointData> implements HistogramData {

    private final List<HistogramPointData> points;

    PrometheusClassicHistogram(HistogramSnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.HISTOGRAM);
        this.points = snapshot.getDataPoints().stream()
                .map(dataPoint -> toOtelDataPoint(dataPoint, currentTimeMillis))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public AggregationTemporality getAggregationTemporality() {
        return AggregationTemporality.CUMULATIVE;
    }

    @Override
    public Collection<HistogramPointData> getPoints() {
        return points;
    }

    private HistogramPointData toOtelDataPoint(HistogramSnapshot.HistogramDataPointSnapshot dataPoint, long currentTimeMillis) {
        if (!dataPoint.hasClassicHistogramData()) {
            return null;
        } else {
            return new HistogramPointDataImpl(
                    dataPoint.hasSum() ? dataPoint.getSum() : Double.NaN,
                    dataPoint.hasCount() ? dataPoint.getCount() : calculateCount(dataPoint.getClassicBuckets()),
                    Double.NaN,
                    Double.NaN,
                    makeBoundaries(dataPoint.getClassicBuckets()),
                    makeCounts(dataPoint.getClassicBuckets()),
                    getStartEpochNanos(dataPoint),
                    getEpochNanos(dataPoint, currentTimeMillis),
                    labelsToAttributes(dataPoint.getLabels()),
                    convertExemplars(dataPoint.getExemplars())
            );
        }
    }

    private long calculateCount(ClassicHistogramBuckets buckets) {
        int result = 0;
        for (int i=0; i<buckets.size(); i++ ) {
            result += buckets.getCount(i);
        }
        return result;
    }

    private List<Double> makeBoundaries(ClassicHistogramBuckets buckets) {
        List<Double> result = new ArrayList<>(buckets.size());
        for (int i=0; i<buckets.size(); i++) {
            result.add(buckets.getUpperBound(i));
        }
        return result;
    }

    private List<Long> makeCounts(ClassicHistogramBuckets buckets) {
        List<Long> result = new ArrayList<>(buckets.size());
        for (int i=0; i<buckets.size(); i++) {
            result.add(buckets.getCount(i));
        }
        return result;
    }
}
