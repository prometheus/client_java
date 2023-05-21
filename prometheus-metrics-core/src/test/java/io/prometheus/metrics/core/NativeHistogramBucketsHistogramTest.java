package io.prometheus.metrics.core;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.com_google_protobuf_3_21_7.TextFormat;
import io.prometheus.expositionformat.PrometheusProtobufWriter;
import io.prometheus.expositionformat.protobuf.generated.com_google_protobuf_3_21_7.Metrics;
import io.prometheus.metrics.exemplars.ExemplarConfig;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.HistogramSnapshot;
import io.prometheus.metrics.model.NativeHistogramBucket;
import io.prometheus.metrics.model.Labels;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static io.prometheus.metrics.core.TestUtil.assertExemplarEquals;
import static org.junit.Assert.assertEquals;

public class NativeHistogramBucketsHistogramTest {

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

        private void run() {
            for (double observation : observations) {
                histogram.observe(observation);
            }
            Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(histogram.collect());
            String expectedWithMetadata = "name: \"test\" type: HISTOGRAM metric { histogram { " + expected + " } }";
            assertEquals("test \"" + name + "\" failed", expectedWithMetadata, TextFormat.printer().shortDebugString(protobufData));
        }
    }

    /**
     * This includes the test cases from histogram_test.go in client_golang.
     */
    @Test
    public void testGolangTests() throws NoSuchFieldException, IllegalAccessException {
        TestCase[] testCases = new TestCase[]{
                new TestCase("'no sparse buckets' from client_golang",
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asClassicHistogram()
                                .build(),
                        1.0, 2.0, 3.0),
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
                                .asNativeHistogram()
                                .withNativeSchema(3)
                                .withNativeMaxZeroThreshold(0)
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
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
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
                                .asNativeHistogram()
                                .withNativeSchema(-1)
                                .withNativeMaxZeroThreshold(0)
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
                                .asNativeHistogram()
                                .withNativeSchema(-2)
                                .withNativeMaxZeroThreshold(0)
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
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
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
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
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
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(1.4)
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NaN
                ),
                */
                new TestCase("'+Inf observation' from client_golang",
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.POSITIVE_INFINITY
                ),
                new TestCase("'-Inf observation' from client_golang",
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2, Double.NEGATIVE_INFINITY
                ),
                new TestCase("'limited buckets but nothing triggered' from client_golang",
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
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
                                .withNativeMaxBuckets(4)
                                .build(),
                        0, 1, 1.2, 1.4, 1.8, 2
                ),
                new TestCase("'buckets limited by halving resolution' from client_golang",
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(0)
                                .withNativeMaxBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3
                ),
                new TestCase("'buckets limited by widening the zero bucket' from client_golang",
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(1.2)
                                .withNativeMaxBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3
                ),
                new TestCase("'buckets limited by widening the zero bucket twice' from client_golang",
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
                        Histogram.newBuilder()
                                .withName("test")
                                .asNativeHistogram()
                                .withNativeSchema(2)
                                .withNativeMaxZeroThreshold(1.2)
                                .withNativeMaxBuckets(4)
                                .build(),
                        0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3, 4)

                /*
                {
		{
			name:             "buckets limited by reset",
			observations:     []float64{0, 1, 1.1, 1.2, 1.4, 1.8, 2, 3, 4},
			factor:           1.2,
			maxBuckets:       4,
			maxZeroThreshold: 1.2,
			minResetDuration: 5 * time.Minute,
			want:             `sample_count:2 sample_sum:7 schema:2 zero_threshold:2.938735877055719e-39 zero_count:0 positive_span:<offset:7 length:2 > positive_delta:1 positive_delta:0 `,
		},
		{
			name:         "limited buckets but nothing triggered, negative observations",
			observations: []float64{0, -1, -1.2, -1.4, -1.8, -2},
			factor:       1.2,
			maxBuckets:   4,
			want:         `sample_count:6 sample_sum:-7.4 schema:2 zero_threshold:2.938735877055719e-39 zero_count:1 negative_span:<offset:0 length:5 > negative_delta:1 negative_delta:-1 negative_delta:2 negative_delta:-2 negative_delta:2 `,
		},
		{
			name:         "buckets limited by halving resolution, negative observations",
			observations: []float64{0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3},
			factor:       1.2,
			maxBuckets:   4,
			want:         `sample_count:8 sample_sum:-11.5 schema:1 zero_threshold:2.938735877055719e-39 zero_count:1 negative_span:<offset:0 length:5 > negative_delta:1 negative_delta:2 negative_delta:-1 negative_delta:-2 negative_delta:1 `,
		},
		{
			name:             "buckets limited by widening the zero bucket, negative observations",
			observations:     []float64{0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3},
			factor:           1.2,
			maxBuckets:       4,
			maxZeroThreshold: 1.2,
			want:             `sample_count:8 sample_sum:-11.5 schema:2 zero_threshold:1 zero_count:2 negative_span:<offset:1 length:7 > negative_delta:1 negative_delta:1 negative_delta:-2 negative_delta:2 negative_delta:-2 negative_delta:0 negative_delta:1 `,
		},
		{
			name:             "buckets limited by widening the zero bucket twice, negative observations",
			observations:     []float64{0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3, -4},
			factor:           1.2,
			maxBuckets:       4,
			maxZeroThreshold: 1.2,
			want:             `sample_count:9 sample_sum:-15.5 schema:2 zero_threshold:1.189207115002721 zero_count:3 negative_span:<offset:2 length:7 > negative_delta:2 negative_delta:-2 negative_delta:2 negative_delta:-2 negative_delta:0 negative_delta:1 negative_delta:0 `,
		},
		{
			name:             "buckets limited by reset, negative observations",
			observations:     []float64{0, -1, -1.1, -1.2, -1.4, -1.8, -2, -3, -4},
			factor:           1.2,
			maxBuckets:       4,
			maxZeroThreshold: 1.2,
			minResetDuration: 5 * time.Minute,
			want:             `sample_count:2 sample_sum:-7 schema:2 zero_threshold:2.938735877055719e-39 zero_count:0 negative_span:<offset:7 length:2 > negative_delta:1 negative_delta:0 `,
		},
		{
			name:             "buckets limited by halving resolution, then reset",
			observations:     []float64{0, 1, 1.1, 1.2, 1.4, 1.8, 2, 5, 5.1, 3, 4},
			factor:           1.2,
			maxBuckets:       4,
			minResetDuration: 9 * time.Minute,
			want:             `sample_count:2 sample_sum:7 schema:2 zero_threshold:2.938735877055719e-39 zero_count:0 positive_span:<offset:7 length:2 > positive_delta:1 positive_delta:0 `,
		},
		{
			name:             "buckets limited by widening the zero bucket, then reset",
			observations:     []float64{0, 1, 1.1, 1.2, 1.4, 1.8, 2, 5, 5.1, 3, 4},
			factor:           1.2,
			maxBuckets:       4,
			maxZeroThreshold: 1.2,
			minResetDuration: 9 * time.Minute,
			want:             `sample_count:2 sample_sum:7 schema:2 zero_threshold:2.938735877055719e-39 zero_count:0 positive_span:<offset:7 length:2 > positive_delta:1 positive_delta:0 `,
		},
		}
                 */
        };
        for (TestCase testCase : testCases) {
            if (testCase.name.contains("widening")) {
                testCase.run();
            }
        }
    }

    /**
     * Additional tests that are not part of client_golang's test suite.
     */
    @Test
    public void testAdditional() throws NoSuchFieldException, IllegalAccessException {
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
                                .asNativeHistogram()
                                .withNativeSchema(0)
                                .withNativeMaxZeroThreshold(0)
                                .build(),
                        0.0, 0.5, 1.0)
        };
        for (TestCase testCase : testCases) {
            testCase.run();
        }
    }

    @Test
    public void testNativeBucketIndexToUpperBound() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // test data from client_golang's TestGetLe
        int[] indexes = new int[]{-1, 0, 1, 512, 513, -1, 0, 1, 1024, 1025, -1, 0, 1, 4096, 4097};
        int[] schemas = new int[]{-1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2};
        double[] expectedUpperBounds = new double[]{0.25, 1, 4, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                0.5, 1, 2, Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                0.8408964152537144, 1, 1.189207115002721, Double.MAX_VALUE, Double.POSITIVE_INFINITY};
        Method method = Histogram.HistogramData.class.getDeclaredMethod("nativeBucketIndexToUpperBound", int.class, int.class);
        method.setAccessible(true);
        for (int i = 0; i < indexes.length; i++) {
            Histogram histogram = Histogram.newBuilder()
                    .withName("test")
                    .withNativeSchema(schemas[i])
                    .build();
            Histogram.HistogramData histogramData = histogram.newMetricData();
            double result = (double) method.invoke(histogramData, schemas[i], indexes[i]);
            Assert.assertEquals("index=" + indexes[i] + ", schema=" + schemas[i], expectedUpperBounds[i], result, 0.0000000000001);
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

    @Test
    public void testExemplarSampler() {

        SpanContextSupplier spanContextSupplier = new SpanContextSupplier() {
            int callCount = 0;

            @Override
            public String getTraceId() {
                return "traceId-" + callCount;
            }

            @Override
            public String getSpanId() {
                return "spanId-" + callCount;
            }

            @Override
            public boolean isSampled() {
                callCount++;
                return true;
            }
        };
        long sampleIntervalMillis = 10;
        Histogram histogram = Histogram.newBuilder()
                .withName("test")
                .withExemplarConfig(ExemplarConfig.newBuilder()
                        .withSpanContextSupplier(spanContextSupplier)
                        .withSampleIntervalMillis(sampleIntervalMillis)
                        .build())
                .withLabelNames("path")
                .build();

        Exemplar ex1 = Exemplar.newBuilder()
                .withValue(3.11)
                .withSpanId("spanId-1")
                .withTraceId("traceId-1")
                .build();
        Exemplar ex2 = Exemplar.newBuilder()
                .withValue(3.12)
                .withSpanId("spanId-2")
                .withTraceId("traceId-2")
                .build();
        Exemplar ex3 = Exemplar.newBuilder()
                .withValue(3.13)
                .withSpanId("spanId-3")
                .withTraceId("traceId-3")
                .withLabels(Labels.of("key1", "value1", "key2", "value2"))
                .build();

        histogram.withLabels("/hello").observe(3.11);
        histogram.withLabels("/world").observe(3.12);
        assertEquals(1, getData(histogram, "path", "/hello").getExemplars().size());
        assertExemplarEquals(ex1, getData(histogram, "path", "/hello").getExemplars().iterator().next());
        assertEquals(1, getData(histogram, "path", "/world").getExemplars().size());
        assertExemplarEquals(ex2, getData(histogram, "path", "/world").getExemplars().iterator().next());
        histogram.withLabels("/world").observeWithExemplar(3.13, Labels.of("key1", "value1", "key2", "value2"));
        assertEquals(1, getData(histogram, "path", "/hello").getExemplars().size());
        assertExemplarEquals(ex1, getData(histogram, "path", "/hello").getExemplars().iterator().next());
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

    private HistogramSnapshot.HistogramData getData(Histogram histogram, String... labels) {
        return ((HistogramSnapshot) histogram.collect()).getData().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("histogram with labels " + labels + " not found"));
    }

    private Optional<NativeHistogramBucket> getBucket(Histogram histogram, int bucketIndex, String... labels) {
        return getData(histogram, labels).getNativeBucketsForPositiveValues().stream()
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
