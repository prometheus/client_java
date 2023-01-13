package io.prometheus.expositionformat.text;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.CounterSnapshot.CounterData;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.FixedBuckets;
import io.prometheus.metrics.model.FixedBucketsHistogramSnapshot;
import io.prometheus.metrics.model.FixedBucketsHistogramSnapshot.FixedBucketsHistogramData;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.GaugeSnapshot.GaugeData;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.Quantile;
import io.prometheus.metrics.model.Quantiles;
import io.prometheus.metrics.model.SummarySnapshot;
import io.prometheus.metrics.model.SummarySnapshot.SummaryData;
import io.prometheus.metrics.model.Unit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class OpenMetricsTextFormatWriterTest {

    private static class TestCase {
        private final MetricSnapshot[] data;
        private final String expectedOutput;

        private TestCase(String expectedOutput, MetricSnapshot... data) {
            this.data = data;
            this.expectedOutput = expectedOutput;
        }
    }

    private TestCase testCase;

    public OpenMetricsTextFormatWriterTest(TestCase testCase) {
        this.testCase = testCase;
    }

    private static Exemplar exemplar1 = Exemplar.newBuilder()
            .withSpanId("12345")
            .withTraceId("abcde")
            .withLabels(Labels.of("env", "prod"))
            .withValue(1.7)
            .withTimestampMillis(1672850685829L)
            .build();

    private static Exemplar exemplar2 = Exemplar.newBuilder()
            .withSpanId("23456")
            .withTraceId("bcdef")
            .withLabels(Labels.of("env", "dev"))
            .withValue(2.4)
            .withTimestampMillis(1672850685830L)
            .build();

    private static String exemplar1String = "{env=\"prod\",span_id=\"12345\",trace_id=\"abcde\"} 1.7 1672850685.829";
    private static String exemplar2String = "{env=\"dev\",span_id=\"23456\",trace_id=\"bcdef\"} 2.4 1672850685.830";

    @Parameterized.Parameters
    public static List<TestCase> testCases() {
        String[] timestampStrings = new String[]{
                "1672850685.829", // sorted by age, youngest first
                "1672850585.820",
                "1672850385.800",
                "1672850285.000",
                "1672850180.000",
        };
        long[] timestampLongs = new long[timestampStrings.length];
        for (int i = 0; i < timestampStrings.length; i++) {
            timestampLongs[i] = (long) (1000L * Double.parseDouble(timestampStrings[i]));
        }
        Quantile[] quantiles = new Quantile[]{
                new Quantile(0.5, 225.3),
                new Quantile(0.9, 240.7),
                new Quantile(0.95, 245.1),
        };
        return Arrays.asList(
                new TestCase("" +
                        "# TYPE my_counter counter\n" +
                        "my_counter_total 1.1\n" +
                        "# TYPE service_time_seconds counter\n" +
                        "# UNIT service_time_seconds seconds\n" +
                        "# HELP service_time_seconds total time spent serving\n" +
                        "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "service_time_seconds_created{path=\"/hello\",status=\"200\"} " + timestampStrings[1] + "\n" +
                        "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "service_time_seconds_created{path=\"/hello\",status=\"500\"} " + timestampStrings[1] + "\n" +
                        "# EOF\n",
                        new CounterSnapshot("service_time_seconds", "total time spent serving", Unit.SECONDS,
                                new CounterData(0.8, Labels.of("path", "/hello", "status", "200"), exemplar1, timestampLongs[1], timestampLongs[0]),
                                new CounterData(0.9, Labels.of("path", "/hello", "status", "500"), exemplar1, timestampLongs[1], timestampLongs[0])
                        ),
                        new CounterSnapshot("my_counter", new CounterData(1.1))
                ),
                new TestCase("" +
                        "# TYPE disk_usage_ratio gauge\n" +
                        "# UNIT disk_usage_ratio ratio\n" +
                        "# HELP disk_usage_ratio percentage used\n" +
                        "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "# TYPE temperature_centigrade gauge\n" +
                        "temperature_centigrade 22.3\n" +
                        "# EOF\n",
                        new GaugeSnapshot("disk_usage_ratio", "percentage used", Unit.RATIO,
                                new GaugeData(0.7, Labels.of("device", "/dev/sda2"), exemplar1, timestampLongs[1], timestampLongs[0]),
                                new GaugeData(0.2, Labels.of("device", "/dev/sda1"), exemplar1, timestampLongs[1], timestampLongs[0])
                        ),
                        new GaugeSnapshot("temperature_centigrade", new GaugeData(22.3))
                ),
                new TestCase("" +
                        "# TYPE http_request_duration_seconds summary\n" +
                        "# UNIT http_request_duration_seconds seconds\n" +
                        "# HELP http_request_duration_seconds request duration\n" +
                        "http_request_duration_seconds{status=\"200\",quantile=\"0.5\"} 225.3 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds{status=\"200\",quantile=\"0.9\"} 240.7 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds{status=\"200\",quantile=\"0.95\"} 245.1 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds_count{status=\"200\"} 3 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds_sum{status=\"200\"} 1.2 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds_created{status=\"200\"} " + timestampStrings[1] + "\n" +
                        "http_request_duration_seconds{status=\"500\",quantile=\"0.5\"} 225.3 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds{status=\"500\",quantile=\"0.9\"} 240.7 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds{status=\"500\",quantile=\"0.95\"} 245.1 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds_count{status=\"500\"} 7 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds_sum{status=\"500\"} 2.2 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "http_request_duration_seconds_created{status=\"500\"} " + timestampStrings[1] + "\n" +
                        "# TYPE latency_seconds summary\n" +
                        "# UNIT latency_seconds seconds\n" +
                        "# HELP latency_seconds latency\n" +
                        "latency_seconds_count 3\n" +
                        "latency_seconds_sum 1.2\n" +
                        "# EOF\n",
                        new SummarySnapshot("http_request_duration_seconds", "request duration", Unit.SECONDS,
                                new SummaryData(7, 2.2, Quantiles.of(quantiles), Labels.of("status", "500"), Exemplars.of(exemplar1), timestampLongs[1], timestampLongs[0]),
                                new SummaryData(3, 1.2, Quantiles.of(quantiles), Labels.of("status", "200"), Exemplars.of(exemplar1), timestampLongs[1], timestampLongs[0])
                        ),
                        new SummarySnapshot("latency_seconds", "latency", Unit.SECONDS, new SummaryData(3, 1.2))
                ),
                new TestCase("" +
                        "# TYPE request_latency_seconds histogram\n" +
                        "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                        "request_latency_seconds_count 2\n" +
                        "request_latency_seconds_sum 3.2\n" +
                        "# TYPE response_size_bytes histogram\n" +
                        "# UNIT response_size_bytes bytes\n" +
                        "# HELP response_size_bytes help\n" +
                        "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 4 " + timestampStrings[0] + " # " + exemplar2String + "\n" +
                        "response_size_bytes_count{status=\"200\"} 4 " + timestampStrings[0] + "\n" +
                        "response_size_bytes_sum{status=\"200\"} 4.1 " + timestampStrings[0] + "\n" +
                        "response_size_bytes_created{status=\"200\"} " + timestampStrings[1] + "\n" +
                        "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 2 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 2 " + timestampStrings[0] + " # " + exemplar2String + "\n" +
                        "response_size_bytes_count{status=\"500\"} 2 " + timestampStrings[0] + "\n" +
                        "response_size_bytes_sum{status=\"500\"} 3.2 " + timestampStrings[0] + "\n" +
                        "response_size_bytes_created{status=\"500\"} " + timestampStrings[1] + "\n" +
                        "# EOF\n",
                        new FixedBucketsHistogramSnapshot("request_latency_seconds", new FixedBucketsHistogramData(2, 3.2, FixedBuckets.newBuilder().addBucket(Double.POSITIVE_INFINITY, 2).build())),
                        new FixedBucketsHistogramSnapshot("response_size_bytes", "help", Unit.BYTES,
                                new FixedBucketsHistogramData(2, 3.2, FixedBuckets.newBuilder()
                                        .addBucket(2.2, 2)
                                        .addBucket(Double.POSITIVE_INFINITY, 2)
                                        .build(),
                                        Labels.of("status", "500"),
                                        Exemplars.of(exemplar1, exemplar2),
                                        timestampLongs[1],
                                        timestampLongs[0]),
                                new FixedBucketsHistogramData(4, 4.1, FixedBuckets.newBuilder()
                                        .addBucket(2.2, 2)
                                        .addBucket(Double.POSITIVE_INFINITY, 4)
                                        .build(),
                                        Labels.of("status", "200"),
                                        Exemplars.of(exemplar1, exemplar2),
                                        timestampLongs[1],
                                        timestampLongs[0])
                        )
                )
        );
    }

    ;

    @Test
    public void runTestCase() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true);
        writer.write(out, MetricSnapshots.of(testCase.data));
        Assert.assertEquals(testCase.expectedOutput, out.toString());
    }

}
