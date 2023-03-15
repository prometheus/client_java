package io.prometheus.expositionformat.text;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.CounterSnapshot.CounterData;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.FixedHistogramBuckets;
import io.prometheus.metrics.model.FixedHistogramSnapshot;
import io.prometheus.metrics.model.FixedHistogramSnapshot.FixedHistogramData;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.GaugeSnapshot.GaugeData;
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.Quantiles;
import io.prometheus.metrics.model.StateSetSnapshot;
import io.prometheus.metrics.model.SummarySnapshot;
import io.prometheus.metrics.model.SummarySnapshot.SummaryData;
import io.prometheus.metrics.model.Unit;
import io.prometheus.metrics.model.UnknownSnapshot;
import io.prometheus.metrics.model.UnknownSnapshot.UnknownData;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OpenMetricsTextFormatWriterTest {

    private final String exemplar1String = "{env=\"prod\",span_id=\"12345\",trace_id=\"abcde\"} 1.7 1672850685.829";
    private final String exemplar2String = "{env=\"dev\",span_id=\"23456\",trace_id=\"bcdef\"} 2.4 1672850685.830";

    private final String createdTimestamp1s = "1672850385.800";
    private final long createdTimestamp1 = (long) (1000 * Double.parseDouble(createdTimestamp1s));
    private final String createdTimestamp2s = "1672850285.000";
    private final long createdTimestamp2 = (long) (1000 * Double.parseDouble(createdTimestamp2s));
    private final String scrapeTimestamp1s = "1672850685.829";
    private final long scrapeTimestamp1 = (long) (1000 * Double.parseDouble(scrapeTimestamp1s));
    private final String scrapeTimestamp2s = "1672850585.820";
    private final long scrapeTimestamp2 = (long) (1000 * Double.parseDouble(scrapeTimestamp2s));

    private final Exemplar exemplar1 = Exemplar.newBuilder()
            .withSpanId("12345")
            .withTraceId("abcde")
            .withLabels(Labels.of("env", "prod"))
            .withValue(1.7)
            .withTimestampMillis(1672850685829L)
            .build();

    private final Exemplar exemplar2 = Exemplar.newBuilder()
            .withSpanId("23456")
            .withTraceId("bcdef")
            .withLabels(Labels.of("env", "dev"))
            .withValue(2.4)
            .withTimestampMillis(1672850685830L)
            .build();

    @Test
    public void testCounterComplete() throws IOException {
        String expected = "" +
                "# TYPE service_time_seconds counter\n" +
                "# UNIT service_time_seconds seconds\n" +
                "# HELP service_time_seconds total time spent serving\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("service_time_seconds")
                .withHelp("total time spent serving")
                .withUnit(Unit.SECONDS)
                .addCounterData(CounterData.newBuilder()
                        .withValue(0.8)
                        .withLabels(Labels.newBuilder()
                                .addLabel("path", "/hello")
                                .addLabel("status", "200")
                                .build())
                        .withExemplar(exemplar1)
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .addCounterData(CounterData.newBuilder()
                        .withValue(0.9)
                        .withLabels(Labels.newBuilder()
                                .addLabel("path", "/hello")
                                .addLabel("status", "500")
                                .build())
                        .withExemplar(exemplar2)
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .build();
        assertOutput(expected, counter);
    }

    @Test
    public void testCounterMinimal() throws IOException {
        String expected = "" +
                "# TYPE my_counter counter\n" +
                "my_counter_total 1.1\n" +
                "# EOF\n";
        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("my_counter")
                .addCounterData(CounterData.newBuilder().withValue(1.1).build())
                .build();
        assertOutput(expected, counter);
    }

    @Test
    public void testGaugeComplete() throws IOException {
        String expected = "" +
                "# TYPE disk_usage_ratio gauge\n" +
                "# UNIT disk_usage_ratio ratio\n" +
                "# HELP disk_usage_ratio percentage used\n" +
                "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        GaugeSnapshot gauge = GaugeSnapshot.newBuilder()
                .withName("disk_usage_ratio")
                .withHelp("percentage used")
                .withUnit(new Unit("ratio"))
                .addGaugeData(GaugeData.newBuilder()
                        .withValue(0.7)
                        .withLabels(Labels.newBuilder()
                                .addLabel("device", "/dev/sda2")
                                .build())
                        .withExemplar(exemplar2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addGaugeData(GaugeData.newBuilder()
                        .withValue(0.2)
                        .withLabels(Labels.newBuilder()
                                .addLabel("device", "/dev/sda1")
                                .build())
                        .withExemplar(exemplar1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOutput(expected, gauge);
    }

    @Test
    public void testGaugeMinimal() throws IOException {
        String expected = "" +
                "# TYPE temperature_centigrade gauge\n" +
                "temperature_centigrade 22.3\n" +
                "# EOF\n";
        GaugeSnapshot gauge = GaugeSnapshot.newBuilder()
                .withName("temperature_centigrade")
                .addGaugeData(GaugeData.newBuilder()
                        .withValue(22.3)
                        .build())
                .build();
        assertOutput(expected, gauge);
    }

    @Test
    public void testSummaryComplete() throws IOException {
        String expected = "" +
                "# TYPE http_request_duration_seconds summary\n" +
                "# UNIT http_request_duration_seconds seconds\n" +
                "# HELP http_request_duration_seconds request duration\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds_count{status=\"200\"} 3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds_sum{status=\"200\"} 1.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds_count{status=\"500\"} 7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds_sum{status=\"500\"} 2.2 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("http_request_duration_seconds")
                .withHelp("request duration")
                .withUnit(Unit.SECONDS)
                .addSummaryData(SummaryData.newBuilder()
                        .withCount(7)
                        .withSum(2.2)
                        .withQuantiles(Quantiles.newBuilder()
                                .addQuantile(0.5, 225.3)
                                .addQuantile(0.9, 240.7)
                                .addQuantile(0.95, 245.1)
                                .build())
                        .withLabels(Labels.newBuilder()
                                .addLabel("status", "500")
                                .build())
                        .withExemplars(Exemplars.of(exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addSummaryData(SummaryData.newBuilder()
                        .withCount(3)
                        .withSum(1.2)
                        .withQuantiles(Quantiles.newBuilder()
                                .addQuantile(0.5, 225.3)
                                .addQuantile(0.9, 240.7)
                                .addQuantile(0.95, 245.1)
                                .build())
                        .withLabels(Labels.newBuilder()
                                .addLabel("status", "200")
                                .build())
                        .withExemplars(Exemplars.of(exemplar1))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOutput(expected, summary);
    }

    @Test
    public void testSummaryMinimal() throws IOException {
        String expected = "" +
                "# TYPE latency_seconds summary\n" +
                "# UNIT latency_seconds seconds\n" +
                "# HELP latency_seconds latency\n" +
                "latency_seconds_count 3\n" +
                "latency_seconds_sum 1.2\n" +
                "# EOF\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .withHelp("latency")
                .withUnit(Unit.SECONDS)
                .addSummaryData(SummaryData.newBuilder()
                        .withCount(3)
                        .withSum(1.2)
                        .build())
                .build();
        assertOutput(expected, summary);
    }

    @Test
    public void testFixedHistogramComplete() throws Exception {
        String expected = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"200\"} 4 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 2 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 2 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"500\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        FixedHistogramSnapshot histogram = FixedHistogramSnapshot.newBuilder()
                .withName("response_size_bytes")
                .withHelp("help")
                .withUnit(Unit.BYTES)
                .addData(FixedHistogramData.newBuilder()
                        .withCount(2)
                        .withSum(3.2)
                        .withBuckets(FixedHistogramBuckets.newBuilder()
                                .addBucket(2.2, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .withLabels(Labels.of("status", "500"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addData(FixedHistogramData.newBuilder()
                        .withCount(4)
                        .withSum(4.1)
                        .withBuckets(FixedHistogramBuckets.newBuilder()
                                .addBucket(2.2, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 4)
                                .build())
                        .withLabels(Labels.of("status", "200"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOutput(expected, histogram);
    }

    @Test
    public void testFixedHistogramMinimal() throws Exception {
        String expected = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n" +
                "request_latency_seconds_sum 3.2\n" +
                "# EOF\n";
        FixedHistogramSnapshot histogram = FixedHistogramSnapshot.newBuilder()
                .withName("request_latency_seconds")
                .addData(FixedHistogramData.newBuilder()
                        .withCount(2)
                        .withSum(3.2)
                        .withBuckets(FixedHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .build())
                .build();
        assertOutput(expected, histogram);
    }

    @Test
    public void testFixedGaugeHistogramComplete() throws IOException {
        String expected = "" +
                "# TYPE cache_size_bytes gaugehistogram\n" +
                "# UNIT cache_size_bytes bytes\n" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 7 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"items\"} 7 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_created{db=\"items\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 8 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 8 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_created{db=\"options\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        FixedHistogramSnapshot gaugeHistogram = FixedHistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("cache_size_bytes")
                .withHelp("number of bytes in the cache")
                .withUnit(Unit.BYTES)
                .addData(FixedHistogramData.newBuilder()
                        .withCount(7)
                        .withSum(17)
                        .withBuckets(FixedHistogramBuckets.newBuilder()
                                .addBucket(2.0, 3)
                                .addBucket(Double.POSITIVE_INFINITY, 7)
                                .build())
                        .withLabels(Labels.of("db", "items"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .addData(FixedHistogramData.newBuilder()
                        .withCount(8)
                        .withSum(18)
                        .withBuckets(FixedHistogramBuckets.newBuilder()
                                .addBucket(2.0, 4)
                                .addBucket(Double.POSITIVE_INFINITY, 8)
                                .build()
                        )
                        .withLabels(Labels.of("db", "options"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .build();
        assertOutput(expected, gaugeHistogram);
    }

    @Test
    public void testFixedGaugeHistogramMinimal() throws IOException {
        String expected = "" +
                "# TYPE queue_size_bytes gaugehistogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "queue_size_bytes_gcount 130\n" +
                "queue_size_bytes_gsum 27000.0\n" +
                "# EOF\n";
        FixedHistogramSnapshot gaugeHistogram = FixedHistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("queue_size_bytes")
                .addData(FixedHistogramData.newBuilder()
                        .withCount(130)
                        .withSum(27000)
                        .withBuckets(FixedHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 130)
                                .build())
                        .build())
                .build();
        assertOutput(expected, gaugeHistogram);
    }

    @Test
    public void testInfo() throws IOException {
        String expected = "" +
                "# TYPE version info\n" +
                "# HELP version version information\n" +
                "version_info{version=\"1.2.3\"} 1.0\n" +
                "# EOF\n";
        InfoSnapshot info = InfoSnapshot.newBuilder()
                .withName("version")
                .withHelp("version information")
                .addInfoData(InfoSnapshot.InfoData.newBuilder()
                        .withLabels(Labels.of("version", "1.2.3"))
                        .build())
                .build();
        assertOutput(expected, info);
    }

    @Test
    public void testStateSetComplete() throws IOException {
        String expected = "" +
                "# TYPE state stateset\n" +
                "# HELP state complete state set example\n" +
                "state{env=\"dev\",state=\"state1\"} 1 " + scrapeTimestamp1s + "\n" +
                "state{env=\"dev\",state=\"state2\"} 0 " + scrapeTimestamp1s + "\n" +
                "state{env=\"prod\",state=\"state1\"} 0 " + scrapeTimestamp2s + "\n" +
                "state{env=\"prod\",state=\"state2\"} 1 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        StateSetSnapshot stateSet = StateSetSnapshot.newBuilder()
                .withName("state")
                .withHelp("complete state set example")
                .addStateSetData(StateSetSnapshot.StateSetData.newBuilder()
                        .withLabels(Labels.of("env", "prod"))
                        .addState("state1", false)
                        .addState("state2", true)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addStateSetData(StateSetSnapshot.StateSetData.newBuilder()
                        .withLabels(Labels.of("env", "dev"))
                        .addState("state2", false)
                        .addState("state1", true)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOutput(expected, stateSet);
    }

    @Test
    public void testStateSetMinimal() throws IOException {
        String expected = "" +
                "# TYPE state stateset\n" +
                "state{state=\"a\"} 1\n" +
                "state{state=\"bb\"} 0\n" +
                "# EOF\n";
        StateSetSnapshot stateSet = StateSetSnapshot.newBuilder()
                .withName("state")
                .addStateSetData(StateSetSnapshot.StateSetData.newBuilder()
                        .addState("a", true)
                        .addState("bb", false)
                        .build())
                .build();
        assertOutput(expected, stateSet);
    }

    @Test
    public void testUnknownComplete() throws IOException {
        String expected = "" +
                "# TYPE my_special_thing_bytes unknown\n" +
                "# UNIT my_special_thing_bytes bytes\n" +
                "# HELP my_special_thing_bytes help message\n" +
                "my_special_thing_bytes{env=\"dev\"} 0.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "my_special_thing_bytes{env=\"prod\"} 0.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        UnknownSnapshot unknown = UnknownSnapshot.newBuilder()
                .withName("my_special_thing_bytes")
                .withHelp("help message")
                .withUnit(Unit.BYTES)
                .addUnknownData(UnknownData.newBuilder()
                        .withValue(0.7)
                        .withLabels(Labels.of("env", "prod"))
                        .withExemplar(exemplar2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addUnknownData(UnknownData.newBuilder()
                        .withValue(0.2)
                        .withLabels(Labels.of("env", "dev"))
                        .withExemplar(exemplar1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOutput(expected, unknown);
    }

    @Test
    public void testUnknownMinimal() throws IOException {
        String expected = "" +
                "# TYPE other unknown\n" +
                "other 22.3\n" +
                "# EOF\n";
        UnknownSnapshot unknown = UnknownSnapshot.newBuilder()
                .withName("other")
                .addUnknownData(UnknownData.newBuilder()
                        .withValue(22.3)
                        .build())
                .build();
        assertOutput(expected, unknown);
    }

    private void assertOutput(String expected, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(expected, out.toString());
    }

}
