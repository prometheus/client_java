package io.prometheus.expositionformat;

import io.prometheus.metrics.model.ClassicHistogramBuckets;
import io.prometheus.metrics.model.ClassicHistogramSnapshot;
import io.prometheus.metrics.model.NativeHistogramSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TextFormatUtil {

    /**
     * Convert a native histogram to a classic histogram with just one +Inf bucket.
     */
    public static ClassicHistogramSnapshot nativeToClassic(NativeHistogramSnapshot nativeHistogramSnapshot) {
        // Convert to a classic histogram with just a +Inf bucket.
        List<ClassicHistogramSnapshot.ClassicHistogramData> classicData = new ArrayList<>(nativeHistogramSnapshot.getData().size());
        double[] upperBounds = new double[]{Double.POSITIVE_INFINITY};
        long[] counts = new long[1];
        for (NativeHistogramSnapshot.NativeHistogramData nativeData : nativeHistogramSnapshot.getData()) {
            counts[0] = nativeData.getCount();
            ClassicHistogramBuckets buckets = ClassicHistogramBuckets.of(upperBounds, counts);
            double sum = nativeData.hasSum() ? nativeData.getSum() : Double.NaN;
            long createdTimestamp = nativeData.hasCreatedTimestamp() ? nativeData.getCreatedTimestampMillis() : 0L;
            long scrapeTimestamp = nativeData.hasScrapeTimestamp() ? nativeData.getScrapeTimestampMillis() : 0L;
            classicData.add(new ClassicHistogramSnapshot.ClassicHistogramData(buckets, sum, nativeData.getLabels(), nativeData.getExemplars(), createdTimestamp, scrapeTimestamp));
        }
        return new ClassicHistogramSnapshot(nativeHistogramSnapshot.isGaugeHistogram(), nativeHistogramSnapshot.getMetadata(), classicData);
    }

}
