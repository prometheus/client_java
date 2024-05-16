package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.ExponentialHistogramBuckets;

import java.util.ArrayList;
import java.util.List;

class ExponentialHistogramBucketsImpl implements ExponentialHistogramBuckets {

    private final int scale;
    private final int offset;
    private final List<Long> bucketCounts = new ArrayList<>();

    ExponentialHistogramBucketsImpl(int scale, int offset) {
        this.scale = scale;
        this.offset = offset;
    }

    void addCount(long count) {
        bucketCounts.add(count);
    }

    @Override
    public int getScale() {
        return scale;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public List<Long> getBucketCounts() {
        return bucketCounts;
    }

    @Override
    public long getTotalCount() {
        long result = 0;
        for (Long count : bucketCounts) {
            result += count;
        }
        return result;
    }
}
