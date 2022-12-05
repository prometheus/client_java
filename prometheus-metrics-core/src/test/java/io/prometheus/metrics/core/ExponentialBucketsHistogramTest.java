package io.prometheus.metrics.core;

import com.google.protobuf.TextFormat;
import io.prometheus.expositionformat.protobuf.Protobuf;
import io.prometheus.expositionformat.protobuf.generated.Metrics;
import io.prometheus.metrics.model.ExponentialBucket;
import io.prometheus.metrics.model.ExponentialBucketsHistogramSnapshot;
import io.prometheus.metrics.model.Labels;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ExponentialBucketsHistogramTest {

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
    public void testGolangTests() throws NoSuchFieldException, IllegalAccessException {
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(0)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(3)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(-1)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(-2)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(1.4)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .build(),
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
                        Histogram.newBuilder()
                                .withName("test")
                                .withSchema(2)
                                .withZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NEGATIVE_INFINITY
                )
        };
        for (TestCase testCase : testCases) {
            for (double observation : testCase.observations) {
                testCase.histogram.observe(observation);
            }
            Metrics.MetricFamily protobufData = Protobuf.convert(testCase.histogram.collect());
            String expected = "name: \"test\" type: HISTOGRAM metric { histogram { " + testCase.expected + " } }";
            Assert.assertEquals("test \"" + testCase.name + "\" failed", expected, TextFormat.printer().shortDebugString(protobufData));
        }
    }

    @Test
    public void testI() {
        System.out.println(valueToIndex(3.11, 5));
        System.out.println(valueToIndex(2.11, 5));
        System.out.println(valueToIndex(2.12, 5));
        System.out.println(valueToIndex(3.12, 5));
        System.out.println(valueToIndex(3.13, 5));
    }

    /*
    @Test
    public void testExemplarSampler() {
        final Exemplar exemplar1 = new Exemplar(1.1, Labels.of("trace_id", "abc", "span_id", "123"), System.currentTimeMillis());
        final Exemplar exemplar2 = new Exemplar(2.1, Labels.of("trace_id", "def", "span_id", "456"), System.currentTimeMillis());
        final Exemplar exemplar3 = new Exemplar(2.2, Labels.of("trace_id", "123", "span_id", "abc"), System.currentTimeMillis());
        final AtomicReference<Integer> callNumber = new AtomicReference<>(0);

        // observed values:               3.11, 2.11, 2.12, 3.12
        // corresponding buckets indexes: 53,   35,   35,   53

        HistogramExemplarSampler exemplarSampler = (val, from, to, prev) -> {
            switch (callNumber.get()) {
                case 0:
                    Assert.assertEquals(3.11, val, 0.00001);
                    Assert.assertEquals(lowerBound(3.11, 5), from, 0.000000001);
                    Assert.assertEquals(upperBound(3.11, 5), to, 0.000000001);
                    assertNull(prev);
                    return exemplar1;
                case 1:
                    Assert.assertEquals(2.11, val,  0.00001);
                    Assert.assertEquals(lowerBound(2.11, 5), from, 0.000000001);
                    Assert.assertEquals(upperBound(2.11, 5), to, 0.000000001);
                    assertNull(prev);
                    return exemplar2;
                case 2:
                    Assert.assertEquals(2.12, val, 0.00001);
                    Assert.assertEquals(lowerBound(2.12, 5), from, 0.000000001);
                    Assert.assertEquals(upperBound(2.12, 5), to, 0.000000001);
                    Assert.assertEquals(exemplar1, prev);
                    return null;
                case 3:
                    Assert.assertEquals(3.12, val, 0.00001);
                    Assert.assertEquals(lowerBound(3.12, 5), from, 0.000000001);
                    Assert.assertEquals(upperBound(3.12, 5), to, 0.000000001);
                    Assert.assertEquals(exemplar2, prev);
                    return exemplar3;
                default:
                    throw new RuntimeException("Unexpected 5th call");
            }
        };
        Histogram histogram = Histogram.newBuilder()
                .withName("test")
                .withExemplarSampler(exemplarSampler)
                .build();

        // the test assumes 3.11, 3.12, and 3.13 fall into the same bucket
        Assert.assertEquals(valueToIndex(3.11, 5), valueToIndex(3.12, 5));
        Assert.assertEquals(valueToIndex(3.12, 5), valueToIndex(3.13, 5));
        // the test assumes 2.11 and 2.12 fall into the same bucket
        Assert.assertEquals(valueToIndex(2.11, 5), valueToIndex(2.12, 5));

        // observed values:               3.11, 2.11, 2.12, 3.12
        assertFalse(getBucket(histogram, valueToIndex(3.11, 5)).isPresent());
        assertFalse(getBucket(histogram, valueToIndex(2.11, 5)).isPresent());
        histogram.observe(3.11);
        callNumber.set(callNumber.get() + 1);
        assertExemplarEquals(exemplar1, getBucket(histogram, valueToIndex(3.11, 5)).get().getExemplar());
        assertFalse(getBucket(histogram, valueToIndex(2.11, 5)).isPresent());
        histogram.observe(2.1);
        callNumber.set(callNumber.get() + 1);
        assertExemplarEquals(exemplar1, getBucket(histogram, valueToIndex(3.11, 5)).get().getExemplar());
        assertExemplarEquals(exemplar2, getBucket(histogram, valueToIndex(2.11, 5)).get().getExemplar());
        histogram.observe(1.3);
        callNumber.set(callNumber.get() + 1);
        assertExemplarEquals(exemplar1, getBucket(histogram, valueToIndex(3.11, 5)).get().getExemplar());
        assertExemplarEquals(exemplar2, getBucket(histogram, valueToIndex(2.11, 5)).get().getExemplar());
        histogram.observe(2.2);
        callNumber.set(callNumber.get() + 1);
        assertExemplarEquals(exemplar1, getBucket(histogram, valueToIndex(3.11, 5)).get().getExemplar());
        assertExemplarEquals(exemplar3, getBucket(histogram, valueToIndex(2.11, 5)).get().getExemplar());
        histogram.observeWithExemplar(3.13, Labels.of("key1", "value1", "key2", "value2"));
        assertExemplarEquals(new Exemplar(3.13, Labels.of("key1", "value1", "key2", "value2"), System.currentTimeMillis()), getBucket(histogram, valueToIndex(3.13, 5)).get().getExemplar());
        assertExemplarEquals(exemplar3, getBucket(histogram, valueToIndex(3.13, 5)).get().getExemplar());
    }

     */

    private ExponentialBucketsHistogramSnapshot.ExponentialBucketsHistogramData getData(Histogram histogram, String... labels) {
        return ((ExponentialBucketsHistogramSnapshot) histogram.collect()).getData().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("histogram with labels " + labels + " not found"));
    }

    private Optional<ExponentialBucket> getBucket(Histogram histogram, int bucketIndex, String... labels) {
        return getData(histogram, labels).getBucketsForPositiveValues().stream()
                .filter(b -> b.getBucketIndex() == bucketIndex)
                .findAny();
    }

    private int valueToIndex(double value, int schema) {
        double base = Math.pow(2, Math.pow(2, -schema));
        return (int) Math.ceil(Math.log(value) / Math.log(base));
    }

    private double lowerBound(double value, int schema) {
        double base = Math.pow(2, Math.pow(2, -schema));
        int index = (int) Math.ceil(Math.log(value) / Math.log(base));
        return Math.pow(base, index - 1);
    }

    private double upperBound(double value, int schema) {
        double base = Math.pow(2, Math.pow(2, -schema));
        int index = (int) Math.ceil(Math.log(value) / Math.log(base));
        return Math.pow(base, index);
    }
}
