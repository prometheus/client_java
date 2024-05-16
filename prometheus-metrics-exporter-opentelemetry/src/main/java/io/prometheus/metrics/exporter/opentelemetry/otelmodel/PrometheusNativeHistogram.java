package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.AggregationTemporality;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ExponentialHistogramBuckets;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ExponentialHistogramData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ExponentialHistogramPointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class PrometheusNativeHistogram extends PrometheusData<ExponentialHistogramPointData> implements ExponentialHistogramData {

    private final List<ExponentialHistogramPointData> points;

    PrometheusNativeHistogram(HistogramSnapshot snapshot, long currentTimeMillis) {
        super(MetricDataType.EXPONENTIAL_HISTOGRAM);
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
    public Collection<ExponentialHistogramPointData> getPoints() {
        return points;
    }

    private ExponentialHistogramPointData toOtelDataPoint(HistogramSnapshot.HistogramDataPointSnapshot dataPoint, long currentTimeMillis) {
        if (!dataPoint.hasNativeHistogramData()) {
            return null;
        }
        return new ExponentialHistogramPointDataImpl(
                dataPoint.getNativeSchema(),
                dataPoint.hasSum() ? dataPoint.getSum() : Double.NaN,
                dataPoint.hasCount() ? dataPoint.getCount() : calculateCount(dataPoint),
                dataPoint.getNativeZeroCount(),
                Double.NaN,
                Double.NaN,
                convertBuckets(dataPoint.getNativeSchema(), dataPoint.getNativeBucketsForPositiveValues()),
                convertBuckets(dataPoint.getNativeSchema(), dataPoint.getNativeBucketsForNegativeValues()),
                getStartEpochNanos(dataPoint),
                getEpochNanos(dataPoint, currentTimeMillis),
                labelsToAttributes(dataPoint.getLabels()),
                convertExemplars(dataPoint.getExemplars())
        );
    }

    private ExponentialHistogramBuckets convertBuckets(int scale, NativeHistogramBuckets buckets) {
        if (buckets.size() == 0) {
            return new ExponentialHistogramBucketsImpl(scale, 0);
        }
        int offset = buckets.getBucketIndex(0);
        ExponentialHistogramBucketsImpl result = new ExponentialHistogramBucketsImpl(scale, offset-1);
        int currentBucket = 0;
        for (int i=offset; i<=buckets.getBucketIndex(buckets.size()-1); i++) {
            if (buckets.getBucketIndex(currentBucket) == i) {
                result.addCount(buckets.getCount(currentBucket));
                currentBucket++;
            } else {
                result.addCount(0);
            }
        }
        return result;
    }

    private long calculateCount(HistogramSnapshot.HistogramDataPointSnapshot dataPoint) {
        long result = 0L;
        for (int i=0; i<dataPoint.getNativeBucketsForPositiveValues().size(); i++) {
            result += dataPoint.getNativeBucketsForPositiveValues().getCount(i);
        }
        for (int i=0; i<dataPoint.getNativeBucketsForNegativeValues().size(); i++) {
            result += dataPoint.getNativeBucketsForNegativeValues().getCount(i);
        }
        result += dataPoint.getNativeZeroCount();
        return result;
    }
}
