package io.prometheus.metrics.simpleclient.bridge;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Info;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleclientCollectorTest {

    private CollectorRegistry origRegistry;
    private PrometheusRegistry newRegistry;

    @Before
    public void setUp() {
        origRegistry = new CollectorRegistry();
        newRegistry = new PrometheusRegistry();
        SimpleclientCollector.builder()
                .collectorRegistry(origRegistry)
                .register(newRegistry);
    }

    @Test
    public void testCounterComplete() throws IOException, InterruptedException {
        Counter counter = Counter.build()
                .name("service_time_seconds_total")
                .help("total time spent serving")
                .labelNames("path", "status")
                .register(origRegistry);
        counter.labels("/hello", "200").incWithExemplar(0.8, "trace_id", "12345", "span_id", "abcde");
        Thread.sleep(3); // make timestamps a bit different
        counter.labels("/hello", "500").incWithExemplar(2.4, "trace_id", "23446", "span_id", "bcdef");

        Assert.assertEquals(fixTimestamps(sort(origOpenMetrics())), sort(newOpenMetrics()));
    }

    @Test
    public void testCounterMinimal() throws IOException {
        Counter.build()
                .name("events")
                .help("total number of events")
                .register(origRegistry);

        Assert.assertEquals(fixTimestamps(sort(origOpenMetrics())), sort(newOpenMetrics()));
    }

    @Test
    public void testGaugeComplete() throws IOException, InterruptedException {
        Gauge gauge = Gauge.build()
                .name("disk_usage_ratio")
                .help("percentage used")
                .unit("ratio")
                .labelNames("device")
                .register(origRegistry);
        gauge.labels("/dev/sda1").set(0.2);
        Thread.sleep(3);
        gauge.labels("/dev/sda2").set(0.7);

        Assert.assertEquals(sort(origOpenMetrics()), sort(newOpenMetrics()));
    }

    @Test
    public void testGaugeMinimal() throws IOException, InterruptedException {
        Gauge gauge = Gauge.build()
                .name("temperature_centigrade")
                .help("temperature")
                .unit("celsius")
                .register(origRegistry);
        gauge.set(22.3);

        Assert.assertEquals(sort(origOpenMetrics()), sort(newOpenMetrics()));
    }

    @Test
    public void testHistogramComplete() throws IOException, InterruptedException {
        Histogram histogram = Histogram.build()
                .name("response_size_bytes")
                .help("response size in Bytes")
                .labelNames("status")
                .buckets(64, 256, 512.1)
                .register(origRegistry);
        histogram.labels("200").observeWithExemplar(38, "trace_id", "1", "span_id", "2");
        histogram.labels("200").observeWithExemplar(127, "trace_id", "3", "span_id", "4");
        histogram.labels("200").observeWithExemplar(130, "trace_id", "5", "span_id", "6");
        histogram.labels("200").observeWithExemplar(40, "trace_id", "7", "span_id", "8");
        histogram.labels("200").observeWithExemplar(41, "trace_id", "9", "span_id", "10");
        Thread.sleep(3); // make timestamps a bit different
        histogram.labels("500").observeWithExemplar(10000, "trace_id", "11", "span_id", "12");

        Assert.assertEquals(fixCounts(fixTimestamps(sort(origOpenMetrics()))), sort(newOpenMetrics()));
    }

    @Test
    public void testHistogramMinimal() throws IOException, InterruptedException {
        Histogram.build()
                .name("request_latency")
                .help("request latency")
                .register(origRegistry);

        Assert.assertEquals(fixCounts(fixTimestamps(sort(origOpenMetrics()))), sort(newOpenMetrics()));
    }

    @Test
    public void testSummaryComplete() throws IOException, InterruptedException {
        Summary summary = Summary.build()
                .name("http_request_duration_seconds")
                .help("request duration")
                .labelNames("path", "status")
                .quantile(0.5, 0.01)
                .quantile(0.95, 0.01)
                .quantile(0.99, 0.001)
                .register(origRegistry);
        summary.labels("/", "200").observe(0.2);
        Thread.sleep(3);
        summary.labels("/info", "200").observe(0.7);
        summary.labels("/info", "200").observe(0.8);
        summary.labels("/info", "200").observe(0.9);
        Thread.sleep(3);
        summary.labels("/", "500").observe(0.3);
        summary.labels("/", "500").observe(0.31);
        summary.labels("/", "500").observe(0.32);

        Assert.assertEquals(fixCounts(fixTimestamps(sort(origOpenMetrics()))), sort(newOpenMetrics()));
    }

    @Test
    public void testSummaryMinimal() throws IOException, InterruptedException {
        Summary summary = Summary.build()
                .name("request_size")
                .help("request size")
                .register(origRegistry);

        Assert.assertEquals(fixCounts(fixTimestamps(sort(origOpenMetrics()))), sort(newOpenMetrics()));
    }

    @Test
    public void testInfoComplete() throws IOException, InterruptedException {
        Info info = Info.build()
                .name("version")
                .help("version information")
                .labelNames("env")
                .register(origRegistry);
        info.labels("prod").info("major_version", "12", "minor_version", "3");
        Thread.sleep(3);
        info.labels("dev").info("major_version", "13", "minor_version", "1");

        Assert.assertEquals(fixBoolean(sort(origOpenMetrics())), sort(newOpenMetrics()));
    }

    @Test
    public void testInfoMinimal() throws IOException, InterruptedException {
        Info info = Info.build()
                .name("jvm")
                .help("JVM info")
                .register(origRegistry);
        info.info("version", "17");

        Assert.assertEquals(fixBoolean(sort(origOpenMetrics())), sort(newOpenMetrics()));
    }

    @Test
    public void testStateSetComplete() throws IOException {
        Collector stateSet = new Collector() {
            @Override
            public List<MetricFamilySamples> collect() {
                List<Collector.MetricFamilySamples.Sample> samples = new ArrayList<>();
                samples.add(new Collector.MetricFamilySamples.Sample("state", Arrays.asList("env", "state"), Arrays.asList("dev", "state1"), 1.0));
                samples.add(new Collector.MetricFamilySamples.Sample("state", Arrays.asList("env", "state"), Arrays.asList("dev", "state2"), 0.0));
                return Collections.singletonList(new Collector.MetricFamilySamples("state", Collector.Type.STATE_SET, "my state", samples));
            }
        };
        origRegistry.register(stateSet);

        Assert.assertEquals(fixBoolean(sort(origOpenMetrics())), sort(newOpenMetrics()));
    }

    @Test
    public void testUnknownComplete() throws IOException {
        Collector unknown = new Collector() {
            @Override
            public List<MetricFamilySamples> collect() {
                List<Collector.MetricFamilySamples.Sample> samples = new ArrayList<>();
                samples.add(new Collector.MetricFamilySamples.Sample("my_unknown_metric_seconds", Arrays.asList("env", "status"), Arrays.asList("dev", "ok"), 3.0));
                samples.add(new Collector.MetricFamilySamples.Sample("my_unknown_metric_seconds", Arrays.asList("env", "status"), Arrays.asList("prod", "error"), 0.0));
                return Collections.singletonList(new Collector.MetricFamilySamples("my_unknown_metric_seconds", "seconds", Type.UNKNOWN, "test metric of type unknown", samples));
            }
        };
        origRegistry.register(unknown);

        Assert.assertEquals(sort(origOpenMetrics()), sort(newOpenMetrics()));
    }

    private String fixBoolean(String s) {
        return s.replaceAll(" 1.0", " 1").replaceAll(" 0.0", " 0");
    }

    private String sort(String s) {
        String[] lines = s.split("\n");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }

    private String fixTimestamps(String s) {
        // Example of a "_created" timestamp in orig format: 1.694464002939E9
        // Example of a "_created" timestamp in new format: 1694464002.939
        // The following regex translates the orig timestamp to the new timestamp
        return s
                .replaceAll("1\\.([0-9]{9})([0-9]{3})E9", "1$1.$2")   // Example: 1.694464002939E9
                .replaceAll("1\\.([0-9]{9})([0-9]{2})E9", "1$1.$20")  // Example: 1.69460725747E9
                .replaceAll("1\\.([0-9]{9})([0-9])E9", "1$1.$200") // Example: 1.6946072574E9
                .replaceAll("1\\.([0-9]{9})E9", "1$1.000")  // Example: 1.712332231E9
                .replaceAll("1\\.([0-9]{8})E9", "1$10.000") // Example: 1.71233242E9
                .replaceAll("1\\.([0-9]{7})E9", "1$100.000") // Example: 1.7123324E9
                .replaceAll("1\\.([0-9]{6})E9", "1$1000.000")
                .replaceAll("1\\.([0-9]{5})E9", "1$10000.000")
                .replaceAll("1\\.([0-9]{4})E9", "1$100000.000")
                .replaceAll("1\\.([0-9]{3})E9", "1$1000000.000")
                .replaceAll("1\\.([0-9]{2})E9", "1$10000000.000");
    }

    private String fixCounts(String s) {
        // Example of a "_count" or "_bucket" in orig format: 3.0
        // Example of a "_count" or "_bucket" in new format: 3
        // The following regex translates the orig bucket counts to the new bucket counts
        return s.replaceAll("((_count|_bucket)(\\{[^}]*})? [0-9])\\.0", "$1");
    }

    private String origOpenMetrics() throws IOException {
        StringWriter out = new StringWriter();
        TextFormat.writeOpenMetrics100(out, origRegistry.metricFamilySamples());
        return out.toString();
    }

    private String newOpenMetrics() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, false);
        writer.write(out, newRegistry.scrape());
        return out.toString(StandardCharsets.UTF_8.name());
    }
}
