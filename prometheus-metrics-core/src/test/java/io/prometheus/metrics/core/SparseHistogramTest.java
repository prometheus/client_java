package io.prometheus.metrics.core;

import com.google.protobuf.TextFormat;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SparseHistogramTest {

    private static class TestCase {
        final String name;
        final String expected;
        final Histogram histogram;
        final double[] observations;

        private TestCase(String name, String expected, Histogram histogram, double... observations) {
            this.name = name;
            this.expected = expected;
            this.histogram = histogram;
            this.observations = observations;
        }
    }

    /**
     * This includes the test cases from histogram_test.go in client_golang.
     */
    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        TestCase[] testCases = new TestCase[]{
                new TestCase("observed values are exactly at bucket boundaries",
                        "sample_count: 3 " +
                                "sample_sum: 1.5 " +
                                "schema: 0 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: -1 length: 2 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 0",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(0)
                                .withZeroThreshold(0)
                                .create(),
                        0.0, 0.5, 1.0),
                new TestCase("'factor 1.1 results in schema 3' from client_golang",
                        "sample_count: 4 " +
                                "sample_sum: 6.0 " +
                                "schema: 3 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 1 } " +
                                "positive_span { offset: 7 length: 1 } " +
                                "positive_span { offset: 4 length: 1 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 0 " +
                                "positive_delta: 0",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(3)
                                .withZeroThreshold(0)
                                .create(),
                        0.0, 1.0, 2.0, 3.0),
                new TestCase("'factor 1.2 results in schema 2' from client_golang",
                        "sample_count: 6 " +
                                "sample_sum: 7.4 " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .create(),
                        0, 1, 1.2, 1.4, 1.8, 2),
                new TestCase("'factor 4 results in schema -1' from client_golang",
                        "sample_count: 10 " +
                                "sample_sum: 62.83 " +
                                "schema: -1 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 0 " +
                                "positive_span { offset: 0 length: 4 } " +
                                "positive_delta: 2 " +
                                "positive_delta: 2 " +
                                "positive_delta: -1 " +
                                "positive_delta: -2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(-1)
                                .withZeroThreshold(0)
                                .create(),
                        0.5, 1, // Bucket 0: (0.25, 1]
                        1.5, 2, 3, 3.5, // Bucket 1: (1, 4]
                        5, 6, 7, // Bucket 2: (4, 16]
                        33.33 // Bucket 3: (16, 64]
                ),
                new TestCase("'factor 17 results in schema -2' from client_golang",
                        "sample_count: 10 " +
                                "sample_sum: 62.83 " +
                                "schema: -2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 0 " +
                                "positive_span { offset: 0 length: 3 } " +
                                "positive_delta: 2 " +
                                "positive_delta: 5 " +
                                "positive_delta: -6",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(-2)
                                .withZeroThreshold(0)
                                .create(),
                        0.5, 1, // Bucket 0: (0.0625, 1]
                        1.5, 2, 3, 3.5, 5, 6, 7, // Bucket 1: (1, 16]
                        33.33 // Bucket 2: (16, 256]
                ),
                new TestCase("'negative buckets' from client_golang",
                        "sample_count: 6 " +
                                "sample_sum: -7.4 " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "negative_span { offset: 0 length: 5 } " +
                                "negative_delta: 1 " +
                                "negative_delta: -1 " +
                                "negative_delta: 2 " +
                                "negative_delta: -2 " +
                                "negative_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .create(),
                        0, -1, -1.2, -1.4, -1.8, -2
                ),
                new TestCase("'negative and positive buckets' from client_golang",
                        "sample_count: 11 " +
                                "sample_sum: 0.0 " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "negative_span { offset: 0 length: 5 } " +
                                "negative_delta: 1 " +
                                "negative_delta: -1 " +
                                "negative_delta: 2 " +
                                "negative_delta: -2 " +
                                "negative_delta: 2 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .create(),
                        0, -1, -1.2, -1.4, -1.8, -2, 1, 1.2, 1.4, 1.8, 2
                ),
                new TestCase("'wide zero bucket' from client_golang",
                        "sample_count: 11 " +
                                "sample_sum: 0.0 " +
                                "schema: 2 " +
                                "zero_threshold: 1.4 " +
                                "zero_count: 7 " +
                                "negative_span { offset: 4 length: 1 } " +
                                "negative_delta: 2 " +
                                "positive_span { offset: 4 length: 1 } " +
                                "positive_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(1.4)
                                .create(),
                        0, -1, -1.2, -1.4, -1.8, -2, 1, 1.2, 1.4, 1.8, 2
                ),
                new TestCase("'NaN observation' from client_golang",
                        "sample_count: 7 " +
                                "sample_sum: NaN " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .create(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NaN
                ),
                new TestCase("'+Inf observation' from client_golang",
                        "sample_count: 7 " +
                                "sample_sum: Infinity " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .create(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.POSITIVE_INFINITY
                ),
                new TestCase("'-Inf observation' from client_golang",
                        "sample_count: 7 " +
                                "sample_sum: -Infinity " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2",
                        SparseHistogram.Builder.build("test", "test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .create(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NEGATIVE_INFINITY
                )
        };
        for (TestCase testCase : testCases) {
            for (double observation : testCase.observations) {
                testCase.histogram.noLabelsChild.observe(observation);
            }
            Metrics.Histogram protobufData = convertToProtobuf(testCase.histogram.noLabelsChild);
            Assert.assertEquals("test \"" + testCase.name + "\" failed", testCase.expected, TextFormat.printer().shortDebugString(protobufData));
        }
    }

    private <T> T getField(SparseHistogram.Child child, String name, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = SparseHistogram.Child.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(child);
    }

    private Metrics.Histogram convertToProtobuf(SparseHistogram.Child child) throws NoSuchFieldException, IllegalAccessException {
        Metrics.Histogram.Builder histogramBuilder = Metrics.Histogram.newBuilder();
        histogramBuilder.setSchema(getField(child, "schema", Integer.class));
        histogramBuilder.setZeroThreshold(getField(child, "zeroThreshold", Double.class));
        histogramBuilder.setSampleCount(getField(child, "count", DoubleAdder.class).longValue());
        histogramBuilder.setSampleSum(getField(child, "sum", DoubleAdder.class).doubleValue());
        histogramBuilder.setZeroCount(getField(child, "zeroCount", DoubleAdder.class).longValue());
        ConcurrentHashMap<Integer, DoubleAdder> positiveBuckets = getField(child, "bucketsForPositiveValues", ConcurrentHashMap.class);
        addBuckets(histogramBuilder, positiveBuckets, +1);
        ConcurrentHashMap<Integer, DoubleAdder> negativeBuckets = getField(child, "bucketsForNegativeValues", ConcurrentHashMap.class);
        addBuckets(histogramBuilder, negativeBuckets, -1);
        return histogramBuilder.build();
    }

    private void addBuckets(Metrics.Histogram.Builder histogramBuilder, ConcurrentHashMap<Integer, DoubleAdder> buckets, int sgn) {
        if (!buckets.isEmpty()) {
            List<Integer> bucketIndexes = new ArrayList<Integer>(buckets.keySet());
            Collections.sort(bucketIndexes);
            Metrics.BucketSpan.Builder currentSpan = Metrics.BucketSpan.newBuilder();
            currentSpan.setOffset(bucketIndexes.get(0));
            currentSpan.setLength(0);
            int previousIndex = currentSpan.getOffset();
            long previousCount = 0;
            for (int bucketIndex : bucketIndexes) {
                if (bucketIndex > previousIndex + 1) {
                    // If the gap between bucketIndex and previousIndex is just 1 or 2,
                    // we don't start a new span but continue the existing span and add 1 or 2 empty buckets.
                    if (bucketIndex < previousIndex + 3) {
                        while (bucketIndex > previousIndex + 1) {
                            currentSpan.setLength(currentSpan.getLength() + 1);
                            previousIndex++;
                            if (sgn > 0) {
                                histogramBuilder.addPositiveDelta(-previousCount);
                            } else {
                                histogramBuilder.addNegativeDelta(-previousCount);
                            }
                            previousCount = 0;
                        }
                    } else {
                        if (sgn > 0) {
                            histogramBuilder.addPositiveSpan(currentSpan.build());
                        } else {
                            histogramBuilder.addNegativeSpan(currentSpan.build());
                        }
                        currentSpan = Metrics.BucketSpan.newBuilder();
                        currentSpan.setOffset(bucketIndex - (previousIndex + 1));
                    }
                }
                currentSpan.setLength(currentSpan.getLength() + 1);
                previousIndex = bucketIndex;
                // TODO: Not relevant for the test, but buckets.get(bucketIndex) might return null
                // if the histogram is currently scaling down in another thread.
                if (sgn > 0) {
                    histogramBuilder.addPositiveDelta(buckets.get(bucketIndex).longValue() - previousCount);
                } else {
                    histogramBuilder.addNegativeDelta(buckets.get(bucketIndex).longValue() - previousCount);
                }
                previousCount = buckets.get(bucketIndex).longValue();
            }
            if (sgn > 0) {
                histogramBuilder.addPositiveSpan(currentSpan.build());
            } else {
                histogramBuilder.addNegativeSpan(currentSpan.build());
            }
        }
    }
}
