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
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricMetadata;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.Quantile;
import io.prometheus.metrics.model.Quantiles;
import io.prometheus.metrics.model.StateSetSnapshot;
import io.prometheus.metrics.model.SummarySnapshot;
import io.prometheus.metrics.model.SummarySnapshot.SummaryData;
import io.prometheus.metrics.model.Unit;
import io.prometheus.metrics.model.UnknownSnapshot;
import io.prometheus.metrics.model.UnknownSnapshot.UnknownData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class OpenMetricsTextFormatWriterTest {

    private String name;
    private String expectedOutput;
    private MetricSnapshots snapshots;

    public OpenMetricsTextFormatWriterTest(String name, String expectedOutput, MetricSnapshot[] snapshots) {
        this.name = name;
        this.expectedOutput = expectedOutput;
        this.snapshots = new MetricSnapshots(snapshots);
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

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> testCases() {
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
                new Object[]{"counter", "" +
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
                        new MetricSnapshot[]{
                                new CounterSnapshot("service_time_seconds", "total time spent serving", Unit.SECONDS,
                                        new CounterData(0.8, Labels.of("path", "/hello", "status", "200"), exemplar1, timestampLongs[1], timestampLongs[0]),
                                        new CounterData(0.9, Labels.of("path", "/hello", "status", "500"), exemplar1, timestampLongs[1], timestampLongs[0])
                                ),
                                new CounterSnapshot("my_counter", new CounterData(1.1))
                        }},

                new Object[]{"gauge", "" +
                        "# TYPE disk_usage_ratio gauge\n" +
                        "# UNIT disk_usage_ratio ratio\n" +
                        "# HELP disk_usage_ratio percentage used\n" +
                        "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "# TYPE temperature_centigrade gauge\n" +
                        "temperature_centigrade 22.3\n" +
                        "# EOF\n",
                        new MetricSnapshot[]{
                                new GaugeSnapshot("disk_usage_ratio", "percentage used", Unit.RATIO,
                                        new GaugeData(0.7, Labels.of("device", "/dev/sda2"), exemplar1, timestampLongs[1], timestampLongs[0]),
                                        new GaugeData(0.2, Labels.of("device", "/dev/sda1"), exemplar1, timestampLongs[1], timestampLongs[0])
                                ),
                                new GaugeSnapshot("temperature_centigrade", new GaugeData(22.3))
                        }},
                new Object[]{"summary", "" +
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
                        new MetricSnapshot[]{
                                new SummarySnapshot("http_request_duration_seconds", "request duration", Unit.SECONDS,
                                        new SummaryData(7, 2.2, Quantiles.of(quantiles), Labels.of("status", "500"), Exemplars.of(exemplar1), timestampLongs[1], timestampLongs[0]),
                                        new SummaryData(3, 1.2, Quantiles.of(quantiles), Labels.of("status", "200"), Exemplars.of(exemplar1), timestampLongs[1], timestampLongs[0])
                                ),
                                new SummarySnapshot("latency_seconds", "latency", Unit.SECONDS, new SummaryData(3, 1.2))
                        }},
                new Object[]{"static_histogram", "" +
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
                        new MetricSnapshot[]{
                                new FixedBucketsHistogramSnapshot("request_latency_seconds", new FixedBucketsHistogramData(2, 3.2, FixedBuckets.newBuilder().addBucket(Double.POSITIVE_INFINITY, 2).build(), Labels.EMPTY, Exemplars.EMPTY, 0L)),
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
                                )}},
                new Object[]{"gauge_histogram", "" +
                        "# TYPE cache_size_bytes gaugehistogram\n" +
                        "# UNIT cache_size_bytes bytes\n" +
                        "# HELP cache_size_bytes number of bytes in the cache\n" +
                        "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 7 " + timestampStrings[0] + " # " + exemplar2String + "\n" +
                        "cache_size_bytes_gcount{db=\"items\"} 7 " + timestampStrings[0] + "\n" +
                        "cache_size_bytes_gsum{db=\"items\"} 17.0 " + timestampStrings[0] + "\n" +
                        "cache_size_bytes_created{db=\"items\"} " + timestampStrings[1] + "\n" +
                        "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + timestampStrings[0] + " # " + exemplar1String + "\n" +
                        "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 8 " + timestampStrings[0] + " # " + exemplar2String + "\n" +
                        "cache_size_bytes_gcount{db=\"options\"} 8 " + timestampStrings[0] + "\n" +
                        "cache_size_bytes_gsum{db=\"options\"} 18.0 " + timestampStrings[0] + "\n" +
                        "cache_size_bytes_created{db=\"options\"} " + timestampStrings[1] + "\n" +
                        "# TYPE queue_size_bytes gaugehistogram\n" +
                        "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                        "queue_size_bytes_gcount 130\n" +
                        "queue_size_bytes_gsum 27000.0\n" +
                        "# EOF\n",
                        new MetricSnapshot[]{
                                new FixedBucketsHistogramSnapshot(true, new MetricMetadata("queue_size_bytes"), Collections.singletonList(new FixedBucketsHistogramData(130, 27000, FixedBuckets.newBuilder().addBucket(Double.POSITIVE_INFINITY, 130).build(), Labels.EMPTY, Exemplars.EMPTY, 0L))),
                                new FixedBucketsHistogramSnapshot(true, new MetricMetadata("cache_size_bytes", "number of bytes in the cache", Unit.BYTES),
                                        Arrays.asList(
                                        new FixedBucketsHistogramData(7, 17, FixedBuckets.newBuilder()
                                                .addBucket(2.0, 3)
                                                .addBucket(Double.POSITIVE_INFINITY, 7)
                                                .build(),
                                                Labels.of("db", "items"),
                                                Exemplars.of(exemplar1, exemplar2),
                                                timestampLongs[1],
                                                timestampLongs[0]),
                                        new FixedBucketsHistogramData(8, 18, FixedBuckets.newBuilder()
                                                .addBucket(2.0, 4)
                                                .addBucket(Double.POSITIVE_INFINITY, 8)
                                                .build(),
                                                Labels.of("db", "options"),
                                                Exemplars.of(exemplar1, exemplar2),
                                                timestampLongs[1],
                                                timestampLongs[0])
                                        )
                                )}},
                new Object[]{"info", "" +
                        "# TYPE version info\n" +
                        "# HELP version version information\n" +
                        "version_info{version=\"1.2.3\"} 1.0\n" +
                        "# EOF\n",
                        new MetricSnapshot[]{
                                new InfoSnapshot("version", "version information", new InfoSnapshot.InfoData(Labels.of("version", "1.2.3")))
                        }},
                new Object[]{"stateset", "" +
                        "# TYPE more_complete stateset\n" +
                        "# HELP more_complete complete state set example\n" +
                        "more_complete{env=\"dev\",more_complete=\"state1\"} 1\n" +
                        "more_complete{env=\"dev\",more_complete=\"state2\"} 0\n" +
                        "more_complete{env=\"prod\",more_complete=\"state1\"} 0\n" +
                        "more_complete{env=\"prod\",more_complete=\"state2\"} 1\n" +
                        "# TYPE my_states stateset\n" +
                        "my_states{my_states=\"a\"} 1\n" +
                        "my_states{my_states=\"bb\"} 0\n" +
                        "# EOF\n",
                        new MetricSnapshot[]{
                                new StateSetSnapshot("my_states", StateSetSnapshot.StateSetData.newBuilder().addState("a", true).addState("bb", false).build()),
                                new StateSetSnapshot("more_complete", "complete state set example",
                                        StateSetSnapshot.StateSetData.newBuilder().withLabels(Labels.of("env", "prod")).addState("state1", false).addState("state2", true).build(),
                                        StateSetSnapshot.StateSetData.newBuilder().withLabels(Labels.of("env", "dev")).addState("state2", false).addState("state1", true).build()
                                )
                        }
                },
                new Object[]{"unknown", "" +
                        "# TYPE my_special_thing unknown\n" +
                        "# UNIT my_special_thing bytes\n" +
                        "# HELP my_special_thing help message\n" +
                        "my_special_thing{env=\"dev\"} 0.2 1672850685.829 # {env=\"prod\",span_id=\"12345\",trace_id=\"abcde\"} 1.7 1672850685.829\n" +
                        "my_special_thing{env=\"prod\"} 0.7 1672850685.829 # {env=\"prod\",span_id=\"12345\",trace_id=\"abcde\"} 1.7 1672850685.829\n" +
                        "# TYPE other unknown\n" +
                        "other 22.3\n" +
                        "# EOF\n",
                        new MetricSnapshot[]{
                                new UnknownSnapshot("my_special_thing", "help message", Unit.BYTES,
                                        new UnknownData(0.7, Labels.of("env", "prod"), exemplar1, timestampLongs[1], timestampLongs[0]),
                                        new UnknownData(0.2, Labels.of("env", "dev"), exemplar1, timestampLongs[1], timestampLongs[0])
                                ),
                                new UnknownSnapshot("other", new UnknownData(22.3))
                        }}
        );
    }

    @Test
    public void runTestCase() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true);
        writer.write(out, snapshots);
        Assert.assertEquals(expectedOutput, out.toString());
    }

}
