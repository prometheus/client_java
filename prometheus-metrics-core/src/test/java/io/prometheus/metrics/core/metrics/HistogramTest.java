package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.shaded.com_google_protobuf_3_25_3.TextFormat;
import io.prometheus.metrics.core.datapoints.DistributionDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfigTestUtil;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_25_3.Metrics;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBucket;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.tracer.common.SpanContext;
import io.prometheus.metrics.tracer.initializer.SpanContextSupplier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static io.prometheus.metrics.core.metrics.TestUtil.assertExemplarEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HistogramTest {

    private static final double RESET_DURATION_REACHED = -123.456; // just a random value indicating that we should simulate that the reset duration has been reached

    private SpanContext origSpanContext;

    @Before
    public void setUp() {
        origSpanContext = SpanContextSupplier.getSpanContext();
    }

    @After
    public void tearDown() {
        SpanContextSupplier.setSpanContext(origSpanContext);
    }

    /**
     * Mimic the tests in client_golang.
     */
    private static class GolangTestCase {
        final String name;
        final String expected;
        final Histogram histogram;
        final double[] observations;

        private GolangTestCase(String name, String expected, Histogram histogram, double... observations) {
            this.name = name;
            this.expected = expected;
            this.histogram = histogram;
            this.observations = observations;
        }

        private void run() throws NoSuchFieldException, IllegalAccessException {
            System.out.println("Running " + name + "...");
            for (double observation : observations) {
                if (observation == RESET_DURATION_REACHED) {
                    Field resetAllowed = Histogram.DataPoint.class.getDeclaredField("resetDurationExpired");
                    resetAllowed.setAccessible(true);
                    resetAllowed.set(histogram.getNoLabels(), true);
                } else {
                    histogram.observe(observation);
                }
            }
            Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(histogram.collect());
            String expectedWithMetadata = "name: \"test\" type: HISTOGRAM metric { histogram { " + expected + " } }";
            assertEquals("test \"" + name + "\" failed", expectedWithMetadata, TextFormat.printer().shortDebugString(protobufData));
        }
    }

    /**
     * Test cases copied from histogram_test.go in client_golang.
     */
    @Test
    public void testGolangTests() throws NoSuchFieldException, IllegalAccessException {
        GolangTestCase[] testCases = new GolangTestCase[]{
                new GolangTestCase("'no sparse buckets' from client_golang",
                        "sample_count: 3 " +
                                "sample_sum: 6.0 " +
                                "bucket { cumulative_count: 0 upper_bound: 0.005 } " +
                                "bucket { cumulative_count: 0 upper_bound: 0.01 } " +
                                "bucket { cumulative_count: 0 upper_bound: 0.025 } " +
                                "bucket { cumulative_count: 0 upper_bound: 0.05 } " +
                                "bucket { cumulative_count: 0 upper_bound: 0.1 } " +
                                "bucket { cumulative_count: 0 upper_bound: 0.25 } " +
                                "bucket { cumulative_count: 0 upper_bound: 0.5 } " +
                                "bucket { cumulative_count: 1 upper_bound: 1.0 } " +
                                "bucket { cumulative_count: 2 upper_bound: 2.5 } " +
                                "bucket { cumulative_count: 3 upper_bound: 5.0 } " +
                                "bucket { cumulative_count: 3 upper_bound: 10.0 } " +
                                "bucket { cumulative_count: 3 upper_bound: Infinity }",
                        Histogram.builder()
                                .name("test")
                                .classicOnly()
                                .build(),
                        1.0, 2.0, 3.0),
                new GolangTestCase("'factor 1.1 results in schema 3' from client_golang",
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
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(3)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0.0, 1.0, 2.0, 3.0),
                new GolangTestCase("'factor 1.2 results in schema 2' from client_golang",
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
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2),
                new GolangTestCase("'factor 4 results in schema -1' from client_golang",
                        "sample_count: 14 " +
                                "sample_sum: 63.2581251 " +
                                "schema: -1 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 0 " +
                                "positive_span { offset: -2 length: 6 } " +
                                "positive_delta: 2 " +
                                "positive_delta: 0 " +
                                "positive_delta: 0 " +
                                "positive_delta: 2 " +
                                "positive_delta: -1 " +
                                "positive_delta: -2",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(-1)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0.0156251, 0.0625, // Bucket -2: (0.015625, 0.0625)
                        0.1, 0.25, // Bucket -1: (0.0625, 0.25]
                        0.5, 1, // Bucket 0: (0.25, 1]
                        1.5, 2, 3, 3.5, // Bucket 1: (1, 4]
                        5, 6, 7, // Bucket 2: (4, 16]
                        33.33 // Bucket 3: (16, 64]
                ),
                new GolangTestCase("'factor 17 results in schema -2' from client_golang",
                        "sample_count: 14 " +
                                "sample_sum: 63.2581251 " +
                                "schema: -2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 0 " +
                                "positive_span { offset: -1 length: 4 } " +
                                "positive_delta: 2 " +
                                "positive_delta: 2 " +
                                "positive_delta: 3 " +
                                "positive_delta: -6",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(-2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0.0156251, 0.0625, // Bucket -1: (0.015625, 0.0625]
                        0.1, 0.25, 0.5, 1, // Bucket 0: (0.0625, 1]
                        1.5, 2, 3, 3.5, 5, 6, 7, // Bucket 1: (1, 16]
                        33.33 // Bucket 2: (16, 256]
                ),
                new GolangTestCase("'negative buckets' from client_golang",
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
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0, -1, -1.2, -1.4, -1.8, -2
                ),
                new GolangTestCase("'negative and positive buckets' from client_golang",
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
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0, -1, -1.2, -1.4, -1.8, -2, 1, 1.2, 1.4, 1.8, 2
                ),
                new GolangTestCase("'wide zero bucket' from client_golang",
                        "sample_count: 11 " +
                                "sample_sum: 0.0 " +
                                "schema: 2 " +
                                "zero_threshold: 1.4 " +
                                "zero_count: 7 " +
                                "negative_span { offset: 4 length: 1 } " +
                                "negative_delta: 2 " +
                                "positive_span { offset: 4 length: 1 } " +
                                "positive_delta: 2",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMinZeroThreshold(1.4)
                                .build(),
                        0, -1, -1.2, -1.4, -1.8, -2, 1, 1.2, 1.4, 1.8, 2
                ),
                /*
                // See https://github.com/prometheus/client_golang/issues/1275
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
                        Histogram.builder()
                                .name("test")
                                .nativeHistogram()
                                .nativeSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NaN
                ),
                */
                new GolangTestCase("'+Inf observation' from client_golang",
                        "sample_count: 7 " +
                                "sample_sum: Infinity " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_span { offset: 4092 length: 1 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2 " +
                                "positive_delta: -1",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.POSITIVE_INFINITY
                ),
                new GolangTestCase("'-Inf observation' from client_golang",
                        "sample_count: 7 " +
                                "sample_sum: -Infinity " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "negative_span { offset: 4097 length: 1 } " +
                                "negative_delta: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: -1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NEGATIVE_INFINITY
                ),
                new GolangTestCase("'limited buckets but nothing triggered' from client_golang",
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
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2
                ),
                new GolangTestCase("'buckets limited by halving resolution' from client_golang",
                        "sample_count: 8 " +
                                "sample_sum: 11.5 " +
                                "schema: 1 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: 0 length: 5 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 2 " +
                                "positive_delta: -1 " +
                                "positive_delta: -2 " +
                                "positive_delta: 1",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3
                ),
                new GolangTestCase("'buckets limited by widening the zero bucket' from client_golang",
                        "sample_count: 8 " +
                                "sample_sum: 11.5 " +
                                "schema: 2 " +
                                "zero_threshold: 1.0 " +
                                "zero_count: 2 " +
                                "positive_span { offset: 1 length: 7 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 1 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 0 " +
                                "positive_delta: 1",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3
                ),
                new GolangTestCase("'buckets limited by widening the zero bucket twice' from client_golang",
                        "sample_count: 9 " +
                                "sample_sum: 15.5 " +
                                "schema: 2 " +
                                "zero_threshold: 1.189207115002721 " +
                                "zero_count: 3 " +
                                "positive_span { offset: 2 length: 7 } " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 2 " +
                                "positive_delta: -2 " +
                                "positive_delta: 0 " +
                                "positive_delta: 1 " +
                                "positive_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3, 4),
                new GolangTestCase("'buckets limited by reset' from client_golang",
                        "sample_count: 2 " +
                                "sample_sum: 7.0 " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 0 " +
                                "positive_span { offset: 7 length: 2 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMinZeroThreshold(0)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, RESET_DURATION_REACHED, 3, 4),
                new GolangTestCase("'limited buckets but nothing triggered, negative observations' from client_golang",
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
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, -1, -1.2, -1.4, -1.8, -2),
                new GolangTestCase("'buckets limited by halving resolution, negative observations' from client_golang",
                        "sample_count: 8 " +
                                "sample_sum: -11.5 " +
                                "schema: 1 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "negative_span { offset: 0 length: 5 } " +
                                "negative_delta: 1 " +
                                "negative_delta: 2 " +
                                "negative_delta: -1 " +
                                "negative_delta: -2 " +
                                "negative_delta: 1",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3),
                new GolangTestCase("'buckets limited by widening the zero bucket, negative observations' from client_golang",
                        "sample_count: 8 " +
                                "sample_sum: -11.5 " +
                                "schema: 2 " +
                                "zero_threshold: 1.0 " +
                                "zero_count: 2 " +
                                "negative_span { offset: 1 length: 7 } " +
                                "negative_delta: 1 " +
                                "negative_delta: 1 " +
                                "negative_delta: -2 " +
                                "negative_delta: 2 " +
                                "negative_delta: -2 " +
                                "negative_delta: 0 " +
                                "negative_delta: 1",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3),
                new GolangTestCase("'buckets limited by widening the zero bucket twice, negative observations' from client_golang",
                        "sample_count: 9 " +
                                "sample_sum: -15.5 " +
                                "schema: 2 " +
                                "zero_threshold: 1.189207115002721 " +
                                "zero_count: 3 " +
                                "negative_span { offset: 2 length: 7 } " +
                                "negative_delta: 2 " +
                                "negative_delta: -2 " +
                                "negative_delta: 2 " +
                                "negative_delta: -2 " +
                                "negative_delta: 0 " +
                                "negative_delta: 1 " +
                                "negative_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3, -4),
                new GolangTestCase("'buckets limited by reset, negative observations' from client_golang",
                        "sample_count: 2 " +
                                "sample_sum: -7.0 " +
                                "schema: 2 " +
                                "zero_threshold: " + Math.pow(2.0, -128.0) + " " +
                                "zero_count: 0 " +
                                "negative_span { offset: 7 length: 2 } " +
                                "negative_delta: 1 " +
                                "negative_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, -1, -1.1, -1.2, -1.4, -1.8, -2, RESET_DURATION_REACHED, -3, -4),
                new GolangTestCase("'buckets limited by halving resolution, then reset' from client_golang",
                        "sample_count: 2 " +
                                "sample_sum: 7.0 " +
                                "schema: 2 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 0 " +
                                "positive_span { offset: 7 length: 2 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(0)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 5, 5.1, RESET_DURATION_REACHED, 3, 4),
                new GolangTestCase("'buckets limited by widening the zero bucket, then reset' from client_golang",
                        "sample_count: 2 " +
                                "sample_sum: 7.0 " +
                                "schema: 2 " +
                                "zero_threshold: " + Math.pow(2.0, -128.0) + " " +
                                "zero_count: 0 " +
                                "positive_span { offset: 7 length: 2 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(2)
                                .nativeMaxZeroThreshold(1.2)
                                .nativeMaxNumberOfBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 5, 5.1, RESET_DURATION_REACHED, 3, 4)
        };
        for (GolangTestCase testCase : testCases) {
            testCase.run();
        }
    }

    /**
     * Additional tests that are not part of client_golang's test suite.
     */
    @Test
    public void testAdditional() throws NoSuchFieldException, IllegalAccessException {
        GolangTestCase[] testCases = new GolangTestCase[]{
                new GolangTestCase("observed values are exactly at bucket boundaries",
                        "sample_count: 3 " +
                                "sample_sum: 1.5 " +
                                "schema: 0 " +
                                "zero_threshold: 0.0 " +
                                "zero_count: 1 " +
                                "positive_span { offset: -1 length: 2 } " +
                                "positive_delta: 1 " +
                                "positive_delta: 0",
                        Histogram.builder()
                                .name("test")
                                .nativeOnly()
                                .nativeInitialSchema(0)
                                .nativeMaxZeroThreshold(0)
                                .build(),
                        0.0, 0.5, 1.0)
        };
        for (GolangTestCase testCase : testCases) {
            testCase.run();
        }
    }

    /**
     * Tests HistogramData.nativeBucketIndexToUpperBound(int, int).
     * <p>
     * This test is ported from client_golang's TestGetLe().
     */
    @Test
    public void testNativeBucketIndexToUpperBound() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int[] indexes = new int[]{-1, 0, 1, 512, 513, -1, 0, 1, 1024, 1025, -1, 0, 1, 4096, 4097};
        int[] schemas = new int[]{-1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2};
        double[] expectedUpperBounds = new double[]{0.25, 1, 4, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                0.5, 1, 2, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                0.8408964152537144, 1, 1.189207115002721, Double.MAX_VALUE, Double.POSITIVE_INFINITY};
        Method method = Histogram.DataPoint.class.getDeclaredMethod("nativeBucketIndexToUpperBound", int.class, int.class);
        method.setAccessible(true);
        for (int i = 0; i < indexes.length; i++) {
            Histogram histogram = Histogram.builder()
                    .name("test")
                    .nativeInitialSchema(schemas[i])
                    .build();
            Histogram.DataPoint histogramData = histogram.newDataPoint();
            double result = (double) method.invoke(histogramData, schemas[i], indexes[i]);
            Assert.assertEquals("index=" + indexes[i] + ", schema=" + schemas[i], expectedUpperBounds[i], result, 0.0000000000001);
        }
    }

    /**
     * Test if lowerBound < value <= upperBound is true for the bucket index returned by findBucketIndex()
     */
    @Test
    public void testFindBucketIndex() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Random rand = new Random();
        Method findBucketIndex = Histogram.DataPoint.class.getDeclaredMethod("findBucketIndex", double.class);
        Method nativeBucketIndexToUpperBound = Histogram.DataPoint.class.getDeclaredMethod("nativeBucketIndexToUpperBound", int.class, int.class);
        findBucketIndex.setAccessible(true);
        nativeBucketIndexToUpperBound.setAccessible(true);
        for (int schema = -4; schema <= 8; schema++) {
            Histogram histogram = Histogram.builder()
                    .nativeOnly()
                    .name("test")
                    .nativeInitialSchema(schema)
                    .build();
            for (int i = 0; i < 10_000; i++) {
                for (int zeros = -5; zeros <= 10; zeros++) {
                    double value = rand.nextDouble() * Math.pow(10, zeros);
                    int bucketIndex = (int) findBucketIndex.invoke(histogram.getNoLabels(), value);
                    double lowerBound = (double) nativeBucketIndexToUpperBound.invoke(histogram.getNoLabels(), schema, bucketIndex - 1);
                    double upperBound = (double) nativeBucketIndexToUpperBound.invoke(histogram.getNoLabels(), schema, bucketIndex);
                    Assert.assertTrue("Bucket index " + bucketIndex + " with schema " + schema + " has range [" + lowerBound + ", " + upperBound + "]. Value " + value + " is outside of that range.", lowerBound < value && upperBound >= value);
                }
            }
        }
    }

    @Test
    public void testDefaults() throws IOException {
        Histogram histogram = Histogram.builder().name("test").build();
        histogram.observe(0.5);
        HistogramSnapshot snapshot = histogram.collect();
        String expectedProtobuf = "" +
                "name: \"test\" " +
                "type: HISTOGRAM " +
                "metric { " +
                "histogram { " +
                "sample_count: 1 " +
                "sample_sum: 0.5 " +
                // default has both, native and classic buckets
                "bucket { cumulative_count: 0 upper_bound: 0.005 } " +
                "bucket { cumulative_count: 0 upper_bound: 0.01 } " +
                "bucket { cumulative_count: 0 upper_bound: 0.025 } " +
                "bucket { cumulative_count: 0 upper_bound: 0.05 } " +
                "bucket { cumulative_count: 0 upper_bound: 0.1 } " +
                "bucket { cumulative_count: 0 upper_bound: 0.25 } " +
                "bucket { cumulative_count: 1 upper_bound: 0.5 } " +
                "bucket { cumulative_count: 1 upper_bound: 1.0 } " +
                "bucket { cumulative_count: 1 upper_bound: 2.5 } " +
                "bucket { cumulative_count: 1 upper_bound: 5.0 } " +
                "bucket { cumulative_count: 1 upper_bound: 10.0 } " +
                "bucket { cumulative_count: 1 upper_bound: Infinity } " +
                // default native schema is 5
                "schema: 5 " +
                // default zero threshold is 2^-128
                "zero_threshold: " + Math.pow(2.0, -128.0) + " " +
                "zero_count: 0 " +
                "positive_span { offset: -32 length: 1 } " +
                "positive_delta: 1 " +
                "} }";
        String expectedTextFormat = "" +
                // default classic buckets
                "# TYPE test histogram\n" +
                "test_bucket{le=\"0.005\"} 0\n" +
                "test_bucket{le=\"0.01\"} 0\n" +
                "test_bucket{le=\"0.025\"} 0\n" +
                "test_bucket{le=\"0.05\"} 0\n" +
                "test_bucket{le=\"0.1\"} 0\n" +
                "test_bucket{le=\"0.25\"} 0\n" +
                "test_bucket{le=\"0.5\"} 1\n" +
                "test_bucket{le=\"1.0\"} 1\n" +
                "test_bucket{le=\"2.5\"} 1\n" +
                "test_bucket{le=\"5.0\"} 1\n" +
                "test_bucket{le=\"10.0\"} 1\n" +
                "test_bucket{le=\"+Inf\"} 1\n" +
                "test_count 1\n" +
                "test_sum 0.5\n" +
                "# EOF\n";

        // protobuf
        Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(snapshot);
        Assert.assertEquals(expectedProtobuf, TextFormat.printer().shortDebugString(protobufData));

        // text
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(false, true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(expectedTextFormat, out.toString());
    }

    @Test
    public void testExemplarsClassicHistogram() throws Exception {
        SpanContext spanContext = new SpanContext() {
            int callCount = 0;

            @Override
            public String getCurrentTraceId() {
                return "traceId-" + callCount;
            }

            @Override
            public String getCurrentSpanId() {
                return "spanId-" + callCount;
            }

            @Override
            public boolean isCurrentSpanSampled() {
                callCount++;
                return true;
            }

            @Override
            public void markCurrentSpanAsExemplar() {
            }
        };
        Histogram histogram = Histogram.builder()
                .name("test")
                // The default number of Exemplars is 4.
                // Use 5 buckets to verify that the exemplar sample is configured with the buckets.
                .classicUpperBounds(1.0, 2.0, 3.0, 4.0, Double.POSITIVE_INFINITY)
                .labelNames("path")
                .build();

        long sampleIntervalMillis = 10;
        ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(histogram, sampleIntervalMillis);
        SpanContextSupplier.setSpanContext(spanContext);

        Exemplar ex1a = Exemplar.builder()
                .value(0.5)
                .spanId("spanId-1")
                .traceId("traceId-1")
                .build();
        Exemplar ex1b = Exemplar.builder()
                .value(0.5)
                .spanId("spanId-2")
                .traceId("traceId-2")
                .build();
        Exemplar ex2a = Exemplar.builder()
                .value(4.5)
                .spanId("spanId-3")
                .traceId("traceId-3")
                .build();
        Exemplar ex2b = Exemplar.builder()
                .value(4.5)
                .spanId("spanId-4")
                .traceId("traceId-4")
                .build();
        Exemplar ex3a = Exemplar.builder()
                .value(1.5)
                .spanId("spanId-5")
                .traceId("traceId-5")
                .build();
        Exemplar ex3b = Exemplar.builder()
                .value(1.5)
                .spanId("spanId-6")
                .traceId("traceId-6")
                .build();
        Exemplar ex4a = Exemplar.builder()
                .value(2.5)
                .spanId("spanId-7")
                .traceId("traceId-7")
                .build();
        Exemplar ex4b = Exemplar.builder()
                .value(2.5)
                .spanId("spanId-8")
                .traceId("traceId-8")
                .build();
        Exemplar ex5a = Exemplar.builder()
                .value(3.5)
                .spanId("spanId-9")
                .traceId("traceId-9")
                .build();
        Exemplar ex5b = Exemplar.builder()
                .value(3.5)
                .spanId("spanId-10")
                .traceId("traceId-10")
                .build();
        histogram.labelValues("/hello").observe(0.5);
        histogram.labelValues("/world").observe(0.5); // different labels are tracked independently, i.e. we don't need to wait for sampleIntervalMillis

        HistogramSnapshot snapshot = histogram.collect();
        assertExemplarEquals(ex1a, getExemplar(snapshot, 1.0, "path", "/hello"));
        assertExemplarEquals(ex1b, getExemplar(snapshot, 1.0, "path", "/world"));
        assertNull(getExemplar(snapshot, 2.0, "path", "/hello"));
        assertNull(getExemplar(snapshot, 2.0, "path", "/world"));
        assertNull(getExemplar(snapshot, 3.0, "path", "/hello"));
        assertNull(getExemplar(snapshot, 3.0, "path", "/world"));
        assertNull(getExemplar(snapshot, 4.0, "path", "/hello"));
        assertNull(getExemplar(snapshot, 4.0, "path", "/world"));
        assertNull(getExemplar(snapshot, Double.POSITIVE_INFINITY, "path", "/hello"));
        assertNull(getExemplar(snapshot, Double.POSITIVE_INFINITY, "path", "/world"));

        Thread.sleep(sampleIntervalMillis + 1);
        histogram.labelValues("/hello").observe(4.5);
        histogram.labelValues("/world").observe(4.5);

        snapshot = histogram.collect();
        assertExemplarEquals(ex1a, getExemplar(snapshot, 1.0, "path", "/hello"));
        assertExemplarEquals(ex1b, getExemplar(snapshot, 1.0, "path", "/world"));
        assertNull(getExemplar(snapshot, 2.0, "path", "/hello"));
        assertNull(getExemplar(snapshot, 2.0, "path", "/world"));
        assertNull(getExemplar(snapshot, 3.0, "path", "/hello"));
        assertNull(getExemplar(snapshot, 3.0, "path", "/world"));
        assertNull(getExemplar(snapshot, 4.0, "path", "/hello"));
        assertNull(getExemplar(snapshot, 4.0, "path", "/world"));
        assertExemplarEquals(ex2a, getExemplar(snapshot, Double.POSITIVE_INFINITY, "path", "/hello"));
        assertExemplarEquals(ex2b, getExemplar(snapshot, Double.POSITIVE_INFINITY, "path", "/world"));

        Thread.sleep(sampleIntervalMillis + 1);
        histogram.labelValues("/hello").observe(1.5);
        histogram.labelValues("/world").observe(1.5);
        Thread.sleep(sampleIntervalMillis + 1);
        histogram.labelValues("/hello").observe(2.5);
        histogram.labelValues("/world").observe(2.5);
        Thread.sleep(sampleIntervalMillis + 1);
        histogram.labelValues("/hello").observe(3.5);
        histogram.labelValues("/world").observe(3.5);

        snapshot = histogram.collect();
        assertExemplarEquals(ex1a, getExemplar(snapshot, 1.0, "path", "/hello"));
        assertExemplarEquals(ex1b, getExemplar(snapshot, 1.0, "path", "/world"));
        assertExemplarEquals(ex3a, getExemplar(snapshot, 2.0, "path", "/hello"));
        assertExemplarEquals(ex3b, getExemplar(snapshot, 2.0, "path", "/world"));
        assertExemplarEquals(ex4a, getExemplar(snapshot, 3.0, "path", "/hello"));
        assertExemplarEquals(ex4b, getExemplar(snapshot, 3.0, "path", "/world"));
        assertExemplarEquals(ex5a, getExemplar(snapshot, 4.0, "path", "/hello"));
        assertExemplarEquals(ex5b, getExemplar(snapshot, 4.0, "path", "/world"));
        assertExemplarEquals(ex2a, getExemplar(snapshot, Double.POSITIVE_INFINITY, "path", "/hello"));
        assertExemplarEquals(ex2b, getExemplar(snapshot, Double.POSITIVE_INFINITY, "path", "/world"));

        Exemplar custom = Exemplar.builder()
                .value(3.4)
                .labels(Labels.of("key2", "value2", "key1", "value1", "trace_id", "traceId-11", "span_id", "spanId-11"))
                .build();
        Thread.sleep(sampleIntervalMillis + 1);
        histogram.labelValues("/hello").observeWithExemplar(3.4, Labels.of("key1", "value1", "key2", "value2"));
        snapshot = histogram.collect();
        // custom exemplars have preference, so the automatic exemplar is replaced
        assertExemplarEquals(custom, getExemplar(snapshot, 4.0, "path", "/hello"));
    }

    private Exemplar getExemplar(HistogramSnapshot snapshot, double le, String... labels) {
        HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Labels not found"));
        double lowerBound = Double.NEGATIVE_INFINITY;
        for (ClassicHistogramBucket bucket : data.getClassicBuckets()) {
            if (bucket.getUpperBound() == le) {
                break;
            } else {
                lowerBound = bucket.getUpperBound();
            }
        }
        return data.getExemplars().get(lowerBound, le);
    }

    @Test
    public void testCustomExemplarsClassicHistogram() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        // TODO: This was copied from the old simpleclient, can probably be refactored.

        Histogram histogram = Histogram.builder()
                .name("test")
                .withExemplars()
                .build();

        long sampleIntervalMillis = 10;
        ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(histogram, sampleIntervalMillis);
        ExemplarSamplerConfigTestUtil.setMinRetentionPeriodMillis(histogram, 3 * sampleIntervalMillis);

        Labels labels = Labels.of("mapKey1", "mapValue1", "mapKey2", "mapValue2");

        histogram.observeWithExemplar(0.5, Labels.of("key", "value"));
        assertExemplar(histogram, 0.5, "key", "value");

        Thread.sleep(sampleIntervalMillis * 3 + 1);
        histogram.observeWithExemplar(0.5, Labels.EMPTY);
        assertExemplar(histogram, 0.5);

        Thread.sleep(sampleIntervalMillis * 3 + 1);
        histogram.observeWithExemplar(0.5, labels);
        assertExemplar(histogram, 0.5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

        // default buckets are {.005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10}
        Thread.sleep(sampleIntervalMillis * 3 + 1);
        histogram.observeWithExemplar(2.0, Labels.of("key1", "value1", "key2", "value2"));
        assertExemplar(histogram, 2.0, "key1", "value1", "key2", "value2");
        assertExemplar(histogram, 0.5, "mapKey1", "mapValue1", "mapKey2", "mapValue2");

        Thread.sleep(sampleIntervalMillis * 3 + 1);
        histogram.observeWithExemplar(0.4, Labels.EMPTY); // same bucket as 0.5
        assertExemplar(histogram, 0.4);
        assertExemplar(histogram, 2.0, "key1", "value1", "key2", "value2");
    }

    private void assertExemplar(Histogram histogram, double value, String... labels) {
        double lowerBound = Double.NEGATIVE_INFINITY;
        double upperBound = Double.POSITIVE_INFINITY;
        HistogramSnapshot snapshot = histogram.collect();
        HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().stream()
                .filter(d -> d.getLabels().isEmpty())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No data without labels found"));
        for (ClassicHistogramBucket bucket : data.getClassicBuckets()) {
            if (bucket.getUpperBound() >= value) {
                upperBound = bucket.getUpperBound();
                break;
            } else {
                lowerBound = bucket.getUpperBound();
            }
        }
        Exemplar exemplar = data.getExemplars().get(lowerBound, upperBound);
        Assert.assertNotNull("No exemplar found in bucket [" + lowerBound + ", " + upperBound + "]", exemplar);
        Assert.assertEquals(value, exemplar.getValue(), 0.0);
        Assert.assertEquals("" + exemplar.getLabels(), labels.length / 2, exemplar.getLabels().size());
        for (int i = 0; i < labels.length; i += 2) {
            Assert.assertEquals(labels[i], exemplar.getLabels().getName(i / 2));
            Assert.assertEquals(labels[i + 1], exemplar.getLabels().getValue(i / 2));
        }
    }


    @Test
    public void testExemplarsNativeHistogram() throws NoSuchFieldException, IllegalAccessException {

        SpanContext spanContext = new SpanContext() {
            int callCount = 0;

            @Override
            public String getCurrentTraceId() {
                return "traceId-" + callCount;
            }

            @Override
            public String getCurrentSpanId() {
                return "spanId-" + callCount;
            }

            @Override
            public boolean isCurrentSpanSampled() {
                callCount++;
                return true;
            }

            @Override
            public void markCurrentSpanAsExemplar() {
            }
        };
        Histogram histogram = Histogram.builder()
                .name("test")
                .nativeOnly()
                .labelNames("path")
                .build();

        long sampleIntervalMillis = 10;
        ExemplarSamplerConfigTestUtil.setSampleIntervalMillis(histogram, sampleIntervalMillis);
        SpanContextSupplier.setSpanContext(spanContext);

        Exemplar ex1 = Exemplar.builder()
                .value(3.11)
                .spanId("spanId-1")
                .traceId("traceId-1")
                .build();
        Exemplar ex2 = Exemplar.builder()
                .value(3.12)
                .spanId("spanId-2")
                .traceId("traceId-2")
                .build();
        Exemplar ex3 = Exemplar.builder()
                .value(3.13)
                .spanId("spanId-3")
                .traceId("traceId-3")
                .labels(Labels.of("key1", "value1", "key2", "value2"))
                .build();

        histogram.labelValues("/hello").observe(3.11);
        histogram.labelValues("/world").observe(3.12);
        assertEquals(1, getData(histogram, "path", "/hello").getExemplars().size());
        assertExemplarEquals(ex1, getData(histogram, "path", "/hello").getExemplars().iterator().next());
        assertEquals(1, getData(histogram, "path", "/world").getExemplars().size());
        assertExemplarEquals(ex2, getData(histogram, "path", "/world").getExemplars().iterator().next());

        histogram.labelValues("/world").observeWithExemplar(3.13, Labels.of("key1", "value1", "key2", "value2"));
        assertEquals(1, getData(histogram, "path", "/hello").getExemplars().size());
        assertExemplarEquals(ex1, getData(histogram, "path", "/hello").getExemplars().iterator().next());
        assertEquals(2, getData(histogram, "path", "/world").getExemplars().size());
        Exemplars exemplars = getData(histogram, "path", "/world").getExemplars();
        List<Exemplar> exemplarList = new ArrayList<>(exemplars.size());
        for (Exemplar exemplar : exemplars) {
            exemplarList.add(exemplar);
        }
        exemplarList.sort(Comparator.comparingDouble(Exemplar::getValue));
        assertEquals(2, exemplars.size());
        assertExemplarEquals(ex2, exemplarList.get(0));
        assertExemplarEquals(ex3, exemplarList.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLabelName() {
        Histogram.builder()
                .name("test")
                .labelNames("label", "le");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLabelNameConstLabels() {
        Histogram.builder()
                .name("test")
                .constLabels(Labels.of("label1", "value1", "le", "0.3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLabelNamePrefix() {
        Histogram.builder()
                .name("test")
                .labelNames("__hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalName() {
        Histogram.builder().name("my_namespace/server.durations");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoName() {
        Histogram.builder().build();
    }

    @Test(expected = NullPointerException.class)
    public void testNullName() {
        Histogram.builder()
                .name(null);
    }

    @Test
    public void testDuplicateClassicBuckets() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .classicUpperBounds(0, 3, 17, 3, 21)
                .build();
        List<Double> upperBounds = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getUpperBound)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(0.0, 3.0, 17.0, 21.0, Double.POSITIVE_INFINITY), upperBounds);
    }

    @Test
    public void testUnsortedBuckets() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .classicUpperBounds(0.2, 0.1)
                .build();
        List<Double> upperBounds = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getUpperBound)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(0.1, 0.2, Double.POSITIVE_INFINITY), upperBounds);
    }

    @Test
    public void testEmptyBuckets() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .classicUpperBounds()
                .build();
        List<Double> upperBounds = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getUpperBound)
                .collect(Collectors.toList());
        Assert.assertEquals(Collections.singletonList(Double.POSITIVE_INFINITY), upperBounds);
    }

    @Test
    public void testBucketsIncludePositiveInfinity() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .classicUpperBounds(0.01, 0.1, 1.0, Double.POSITIVE_INFINITY)
                .build();
        List<Double> upperBounds = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getUpperBound)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(0.01, 0.1, 1.0, Double.POSITIVE_INFINITY), upperBounds);
    }

    @Test
    public void testLinearBuckets() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .classicLinearUpperBounds(0.1, 0.1, 10)
                .build();
        List<Double> upperBounds = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getUpperBound)
                .collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, Double.POSITIVE_INFINITY), upperBounds);
    }

    @Test
    public void testExponentialBuckets() {
        Histogram histogram = Histogram.builder()
                .classicExponentialUpperBounds(2, 2.5, 3)
                .name("test")
                .build();
        List<Double> upperBounds = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getUpperBound)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(2.0, 5.0, 12.5, Double.POSITIVE_INFINITY), upperBounds);
    }

    @Test(expected = RuntimeException.class)
    public void testBucketsIncludeNaN() {
        Histogram.builder()
                .name("test")
                .classicUpperBounds(0.01, 0.1, 1.0, Double.NaN);
    }

    @Test
    public void testNoLabelsDefaultZeroValue() {
        Histogram noLabels = Histogram.builder().name("test").build();
        assertEquals(0.0, getBucket(noLabels, 0.005).getCount(), 0.0);
        assertEquals(0, getData(noLabels).getCount());
        assertEquals(0.0, getData(noLabels).getSum(), 0.0);
    }

    private ClassicHistogramBucket getBucket(Histogram histogram, double le, String... labels) {
        return getData(histogram, labels).getClassicBuckets().stream()
                .filter(b -> b.getUpperBound() == le)
                .findAny()
                .orElseThrow(() -> new RuntimeException("bucket with le=" + le + " not found."));
    }

    @Test
    public void testObserve() {
        Histogram noLabels = Histogram.builder()
                .name("test")
                .build();
        noLabels.observe(2);
        assertEquals(1, getData(noLabels).getCount());
        assertEquals(2.0, getData(noLabels).getSum(), .0);
        assertEquals(0.0, getBucket(noLabels, 1).getCount(), .0);
        assertEquals(1.0, getBucket(noLabels, 2.5).getCount(), .0);
        noLabels.observe(4);
        assertEquals(2.0, getData(noLabels).getCount(), .0);
        assertEquals(6.0, getData(noLabels).getSum(), .0);
        assertEquals(0.0, getBucket(noLabels, 1).getCount(), .0);
        assertEquals(1.0, getBucket(noLabels, 2.5).getCount(), .0);
        assertEquals(1.0, getBucket(noLabels, 5).getCount(), .0);
        assertEquals(0.0, getBucket(noLabels, 10).getCount(), .0);
        assertEquals(0.0, getBucket(noLabels, Double.POSITIVE_INFINITY).getCount(), .0);
    }

    @Test
    // See https://github.com/prometheus/client_java/issues/646
    public void testNegativeAmount() {
        Histogram histogram = Histogram.builder()
                .name("histogram")
                .help("test histogram for negative values")
                .classicUpperBounds(-10, -5, 0, 5, 10)
                .build();
        double expectedCount = 0;
        double expectedSum = 0;
        for (int i = 10; i >= -11; i--) {
            histogram.observe(i);
            expectedCount++;
            expectedSum += i;
            assertEquals(expectedSum, getData(histogram).getSum(), .001);
            assertEquals(expectedCount, getData(histogram).getCount(), .001);
        }
        List<Long> expectedBucketCounts = Arrays.asList(2L, 5L, 5L, 5L, 5L, 0L); // buckets -10, -5, 0, 5, 10, +Inf
        List<Long> actualBucketCounts = getData(histogram).getClassicBuckets().stream()
                .map(ClassicHistogramBucket::getCount)
                .collect(Collectors.toList());
        assertEquals(expectedBucketCounts, actualBucketCounts);
    }

    @Test
    public void testBoundaryConditions() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .build();
        histogram.observe(2.5);
        assertEquals(0, getBucket(histogram, 1).getCount());
        assertEquals(1, getBucket(histogram, 2.5).getCount());

        histogram.observe(Double.POSITIVE_INFINITY);
        assertEquals(0, getBucket(histogram, 1).getCount());
        assertEquals(1, getBucket(histogram, 2.5).getCount());
        assertEquals(0, getBucket(histogram, 5).getCount());
        assertEquals(0, getBucket(histogram, 10).getCount());
        assertEquals(1, getBucket(histogram, Double.POSITIVE_INFINITY).getCount());
    }

    @Test
    public void testObserveWithLabels() {
        Histogram histogram = Histogram.builder()
                .name("test")
                .constLabels(Labels.of("env", "prod"))
                .labelNames("path", "status")
                .build();
        histogram.labelValues("/hello", "200").observe(0.11);
        histogram.labelValues("/hello", "200").observe(0.2);
        histogram.labelValues("/hello", "500").observe(0.19);
        HistogramSnapshot.HistogramDataPointSnapshot data200 = getData(histogram, "env", "prod", "path", "/hello", "status", "200");
        HistogramSnapshot.HistogramDataPointSnapshot data500 = getData(histogram, "env", "prod", "path", "/hello", "status", "500");
        assertEquals(2, data200.getCount());
        assertEquals(0.31, data200.getSum(), 0.0000001);
        assertEquals(1, data500.getCount());
        assertEquals(0.19, data500.getSum(), 0.0000001);
        histogram.labelValues("/hello", "200").observe(0.13);
        data200 = getData(histogram, "env", "prod", "path", "/hello", "status", "200");
        data500 = getData(histogram, "env", "prod", "path", "/hello", "status", "500");
        assertEquals(3, data200.getCount());
        assertEquals(0.44, data200.getSum(), 0.0000001);
        assertEquals(1, data500.getCount());
        assertEquals(0.19, data500.getSum(), 0.0000001);
    }

    @Test
    public void testObserveMultithreaded() throws InterruptedException, ExecutionException, TimeoutException {
        // Hard to test concurrency, but let's run a couple of observations in parallel and assert none gets lost.
        Histogram histogram = Histogram.builder()
                .name("test")
                .labelNames("status")
                .build();
        int nThreads = 8;
        DistributionDataPoint obs = histogram.labelValues("200");
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CompletionService<List<HistogramSnapshot>> completionService = new ExecutorCompletionService<>(executor);
        CountDownLatch startSignal = new CountDownLatch(nThreads);
        for (int t = 0; t < nThreads; t++) {
            completionService.submit(() -> {
                List<HistogramSnapshot> snapshots = new ArrayList<>();
                startSignal.countDown();
                startSignal.await();
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 1000; j++) {
                        obs.observe(1.1);
                    }
                    snapshots.add(histogram.collect());
                }
                return snapshots;
            });
        }
        long maxCount = 0;
        for (int i = 0; i < nThreads; i++) {
            Future<List<HistogramSnapshot>> future = completionService.take();
            List<HistogramSnapshot> snapshots = future.get(5, TimeUnit.SECONDS);
            long count = 0;
            for (HistogramSnapshot snapshot : snapshots) {
                Assert.assertEquals(1, snapshot.getDataPoints().size());
                HistogramSnapshot.HistogramDataPointSnapshot data = snapshot.getDataPoints().stream().findFirst().orElseThrow(RuntimeException::new);
                Assert.assertTrue(data.getCount() >= (count + 1000)); // 1000 own observations plus the ones from other threads
                count = data.getCount();
            }
            if (count > maxCount) {
                maxCount = count;
            }
        }
        Assert.assertEquals(nThreads * 10_000, maxCount); // the last collect() has seen all observations
        Assert.assertEquals(getBucket(histogram, 2.5, "status", "200").getCount(), nThreads * 10_000);
        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }


    private HistogramSnapshot.HistogramDataPointSnapshot getData(Histogram histogram, String... labels) {
        return histogram.collect().getDataPoints().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("histogram with labels " + labels + " not found"));
    }
}
