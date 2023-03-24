package io.prometheus.expositionformat.text;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.CounterSnapshot.CounterData;
import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Exemplars;
import io.prometheus.metrics.model.ClassicHistogramBuckets;
import io.prometheus.metrics.model.ClassicHistogramSnapshot;
import io.prometheus.metrics.model.ClassicHistogramSnapshot.ClassicHistogramData;
import io.prometheus.metrics.model.GaugeSnapshot;
import io.prometheus.metrics.model.GaugeSnapshot.GaugeData;
import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricSnapshot;
import io.prometheus.metrics.model.MetricSnapshots;
import io.prometheus.metrics.model.NativeHistogramBuckets;
import io.prometheus.metrics.model.NativeHistogramSnapshot;
import io.prometheus.metrics.model.NativeHistogramSnapshot.NativeHistogramData;
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

public class TextFormatWriterTest {

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
        String openMetrics = "" +
                "# TYPE service_time_seconds counter\n" +
                "# UNIT service_time_seconds seconds\n" +
                "# HELP service_time_seconds total time spent serving\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP service_time_seconds_total total time spent serving\n" +
                "# TYPE service_time_seconds_total counter\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + "\n" +
                "# HELP service_time_seconds_created total time spent serving\n" +
                "# TYPE service_time_seconds_created gauge\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsWithoutCreated = "" +
                "# TYPE service_time_seconds counter\n" +
                "# UNIT service_time_seconds seconds\n" +
                "# HELP service_time_seconds total time spent serving\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        String prometheusWithoutCreated = "" +
                "# HELP service_time_seconds_total total time spent serving\n" +
                "# TYPE service_time_seconds_total counter\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + "\n";

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
        assertOpenMetrics(openMetrics, counter);
        assertPrometheus(prometheus, counter);
        assertOpenMetricsWithoutCreated(openMetricsWithoutCreated, counter);
        assertPrometheusWithoutCreated(prometheusWithoutCreated, counter);
    }

    @Test
    public void testCounterMinimal() throws IOException {
        String openMetrics = "" +
                "# TYPE my_counter counter\n" +
                "my_counter_total 1.1\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE my_counter_total counter\n" +
                "my_counter_total 1.1\n";
        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("my_counter")
                .addCounterData(CounterData.newBuilder().withValue(1.1).build())
                .build();
        assertOpenMetrics(openMetrics, counter);
        assertPrometheus(prometheus, counter);
        assertOpenMetricsWithoutCreated(openMetrics, counter);
        assertPrometheusWithoutCreated(prometheus, counter);
    }

    @Test
    public void testGaugeComplete() throws IOException {
        String openMetrics = "" +
                "# TYPE disk_usage_ratio gauge\n" +
                "# UNIT disk_usage_ratio ratio\n" +
                "# HELP disk_usage_ratio percentage used\n" +
                "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP disk_usage_ratio percentage used\n" +
                "# TYPE disk_usage_ratio gauge\n" +
                "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + scrapeTimestamp1s + "\n" +
                "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + scrapeTimestamp2s + "\n";
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
        assertOpenMetrics(openMetrics, gauge);
        assertPrometheus(prometheus, gauge);
        assertOpenMetricsWithoutCreated(openMetrics, gauge);
        assertPrometheusWithoutCreated(prometheus, gauge);
    }

    @Test
    public void testGaugeMinimal() throws IOException {
        String openMetrics = "" +
                "# TYPE temperature_centigrade gauge\n" +
                "temperature_centigrade 22.3\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE temperature_centigrade gauge\n" +
                "temperature_centigrade 22.3\n";
        GaugeSnapshot gauge = GaugeSnapshot.newBuilder()
                .withName("temperature_centigrade")
                .addGaugeData(GaugeData.newBuilder()
                        .withValue(22.3)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, gauge);
        assertPrometheus(prometheus, gauge);
        assertOpenMetricsWithoutCreated(openMetrics, gauge);
        assertPrometheusWithoutCreated(prometheus, gauge);
    }

    @Test
    public void testSummaryComplete() throws IOException {
        String openMetrics = "" +
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
        String prometheus = "" +
                "# HELP http_request_duration_seconds request duration\n" +
                "# TYPE http_request_duration_seconds summary\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds_count{status=\"200\"} 3 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds_sum{status=\"200\"} 1.2 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds_count{status=\"500\"} 7 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds_sum{status=\"500\"} 2.2 " + scrapeTimestamp2s + "\n" +
                "# HELP http_request_duration_seconds_created request duration\n" +
                "# TYPE http_request_duration_seconds_created gauge\n" +
                "http_request_duration_seconds_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsWithoutCreated = "" +
                "# TYPE http_request_duration_seconds summary\n" +
                "# UNIT http_request_duration_seconds seconds\n" +
                "# HELP http_request_duration_seconds request duration\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds_count{status=\"200\"} 3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds_sum{status=\"200\"} 1.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds_count{status=\"500\"} 7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "http_request_duration_seconds_sum{status=\"500\"} 2.2 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        String prometheusWithoutCreated = "" +
                "# HELP http_request_duration_seconds request duration\n" +
                "# TYPE http_request_duration_seconds summary\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"200\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds_count{status=\"200\"} 3 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds_sum{status=\"200\"} 1.2 " + scrapeTimestamp1s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.5\"} 225.3 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.9\"} 240.7 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds{status=\"500\",quantile=\"0.95\"} 245.1 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds_count{status=\"500\"} 7 " + scrapeTimestamp2s + "\n" +
                "http_request_duration_seconds_sum{status=\"500\"} 2.2 " + scrapeTimestamp2s + "\n";
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
        assertOpenMetrics(openMetrics, summary);
        assertPrometheus(prometheus, summary);
        assertOpenMetricsWithoutCreated(openMetricsWithoutCreated, summary);
        assertPrometheusWithoutCreated(prometheusWithoutCreated, summary);
    }

    @Test
    public void testSummaryWithoutQuantiles() throws IOException {
        String openMetrics = "" +
                "# TYPE latency_seconds summary\n" +
                "# UNIT latency_seconds seconds\n" +
                "# HELP latency_seconds latency\n" +
                "latency_seconds_count 3\n" +
                "latency_seconds_sum 1.2\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP latency_seconds latency\n" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count 3\n" +
                "latency_seconds_sum 1.2\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .withHelp("latency")
                .withUnit(Unit.SECONDS)
                .addSummaryData(SummaryData.newBuilder()
                        .withCount(3)
                        .withSum(1.2)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, summary);
        assertPrometheus(prometheus, summary);
        assertOpenMetricsWithoutCreated(openMetrics, summary);
        assertPrometheusWithoutCreated(prometheus, summary);
    }

    @Test
    public void testSummaryNoCountAndSum() throws IOException {
        String openMetrics = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds{quantile=\"0.95\"} 200.0\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds{quantile=\"0.95\"} 200.0\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addSummaryData(SummaryData.newBuilder()
                        .withQuantiles(Quantiles.newBuilder().addQuantile(0.95, 200.0).build())
                        .build())
                .build();
        assertOpenMetrics(openMetrics, summary);
        assertPrometheus(prometheus, summary);
        assertOpenMetricsWithoutCreated(openMetrics, summary);
        assertPrometheusWithoutCreated(prometheus, summary);
    }

    @Test
    public void testSummaryJustCount() throws IOException {
        String openMetrics = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count 1\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count 1\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addSummaryData(SummaryData.newBuilder()
                        .withCount(1)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, summary);
        assertPrometheus(prometheus, summary);
        assertOpenMetricsWithoutCreated(openMetrics, summary);
        assertPrometheusWithoutCreated(prometheus, summary);
    }

    @Test
    public void testSummaryJustSum() throws IOException {
        String openMetrics = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_sum 12.3\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_sum 12.3\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addSummaryData(SummaryData.newBuilder()
                        .withSum(12.3)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, summary);
        assertPrometheus(prometheus, summary);
        assertOpenMetricsWithoutCreated(openMetrics, summary);
        assertPrometheusWithoutCreated(prometheus, summary);
    }

    @Test
    public void testSummaryEmptyData() throws IOException {
        // SummaryData can be present but empty (no count, no sum, no quantiles).
        // This should be treated like no data is present.
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .withHelp("latency")
                .withUnit(Unit.SECONDS)
                .addSummaryData(SummaryData.newBuilder().build())
                .build();
        assertOpenMetrics("# EOF\n", summary);
        assertPrometheus("", summary);
        assertOpenMetricsWithoutCreated("# EOF\n", summary);
        assertPrometheusWithoutCreated("", summary);
    }

    @Test
    public void testSummaryEmptyAndNonEmpty() throws IOException {
        String openMetrics = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count{path=\"/v2\"} 2\n" +
                "latency_seconds_sum{path=\"/v2\"} 10.7\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count{path=\"/v2\"} 2\n" +
                "latency_seconds_sum{path=\"/v2\"} 10.7\n";
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addSummaryData(SummaryData.newBuilder()
                        .withLabels(Labels.of("path", "/v1"))
                        .build())
                .addSummaryData(SummaryData.newBuilder()
                        .withLabels(Labels.of("path", "/v2"))
                        .withCount(2)
                        .withSum(10.7)
                        .build())
                .addSummaryData(SummaryData.newBuilder()
                        .withLabels(Labels.of("path", "/v3"))
                        .build())
                .build();
        assertOpenMetrics(openMetrics, summary);
        assertPrometheus(prometheus, summary);
        assertOpenMetricsWithoutCreated(openMetrics, summary);
        assertPrometheusWithoutCreated(prometheus, summary);
    }

    @Test
    public void testClassicHistogramComplete() throws Exception {
        String openMetrics = "" +
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
        String prometheus = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 4 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# HELP response_size_bytes_created help\n" +
                "# TYPE response_size_bytes_created gauge\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsWithoutCreated = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"200\"} 4 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 2 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 2 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"500\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusWithoutCreated = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 4 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n";
        ClassicHistogramSnapshot histogram = ClassicHistogramSnapshot.newBuilder()
                .withName("response_size_bytes")
                .withHelp("help")
                .withUnit(Unit.BYTES)
                .addData(ClassicHistogramData.newBuilder()
                        .withSum(3.2)
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(2.2, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .withLabels(Labels.of("status", "500"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addData(ClassicHistogramData.newBuilder()
                        .withSum(4.1)
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(2.2, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 4)
                                .build())
                        .withLabels(Labels.of("status", "200"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, histogram);
        assertPrometheus(prometheus, histogram);
        assertOpenMetricsWithoutCreated(openMetricsWithoutCreated, histogram);
        assertPrometheusWithoutCreated(prometheusWithoutCreated, histogram);
    }

    @Test
    public void testClassicHistogramMinimal() throws Exception {
        // In OpenMetrics a histogram can have a _count if and only if it has a _sum.
        // In Prometheus format, a histogram can have a _count without a _sum.
        String openMetrics = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n";
        ClassicHistogramSnapshot histogram = ClassicHistogramSnapshot.newBuilder()
                .withName("request_latency_seconds")
                .addData(ClassicHistogramData.newBuilder()
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .build())
                .build();
        assertOpenMetrics(openMetrics, histogram);
        assertPrometheus(prometheus, histogram);
        assertOpenMetricsWithoutCreated(openMetrics, histogram);
        assertPrometheusWithoutCreated(prometheus, histogram);
    }

    @Test
    public void testClassicHistogramCountAndSum() throws Exception {
        String openMetrics = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n" +
                "request_latency_seconds_sum 3.2\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n" +
                "request_latency_seconds_sum 3.2\n";
        ClassicHistogramSnapshot histogram = ClassicHistogramSnapshot.newBuilder()
                .withName("request_latency_seconds")
                .addData(ClassicHistogramData.newBuilder()
                        .withSum(3.2)
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .build())
                .build();
        assertOpenMetrics(openMetrics, histogram);
        assertPrometheus(prometheus, histogram);
        assertOpenMetricsWithoutCreated(openMetrics, histogram);
        assertPrometheusWithoutCreated(prometheus, histogram);
    }

    @Test
    public void testClassicGaugeHistogramComplete() throws IOException {
        String openMetrics = "" +
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
        String prometheus = "" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "# TYPE cache_size_bytes histogram\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 7 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 8 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gcount number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gcount gauge\n" +
                "cache_size_bytes_gcount{db=\"items\"} 7 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 8 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gsum number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gsum gauge\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_created number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_created gauge\n" +
                "cache_size_bytes_created{db=\"items\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_created{db=\"options\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsWithoutCreated = "" +
                "# TYPE cache_size_bytes gaugehistogram\n" +
                "# UNIT cache_size_bytes bytes\n" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 7 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"items\"} 7 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 8 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 8 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusWithoutCreated = "" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "# TYPE cache_size_bytes histogram\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 7 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 8 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gcount number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gcount gauge\n" +
                "cache_size_bytes_gcount{db=\"items\"} 7 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 8 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gsum number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gsum gauge\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n";
        ClassicHistogramSnapshot gaugeHistogram = ClassicHistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("cache_size_bytes")
                .withHelp("number of bytes in the cache")
                .withUnit(Unit.BYTES)
                .addData(ClassicHistogramData.newBuilder()
                        .withSum(17)
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(2.0, 3)
                                .addBucket(Double.POSITIVE_INFINITY, 7)
                                .build())
                        .withLabels(Labels.of("db", "items"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .addData(ClassicHistogramData.newBuilder()
                        .withSum(18)
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
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
        assertOpenMetrics(openMetrics, gaugeHistogram);
        assertPrometheus(prometheus, gaugeHistogram);
        assertOpenMetricsWithoutCreated(openMetricsWithoutCreated, gaugeHistogram);
        assertPrometheusWithoutCreated(prometheusWithoutCreated, gaugeHistogram);
    }

    @Test
    public void testClassicGaugeHistogramMinimal() throws IOException {
        // In OpenMetrics a histogram can have a _count if and only if it has a _sum.
        // In Prometheus format, a histogram can have a _count without a _sum.
        String openMetrics = "" +
                "# TYPE queue_size_bytes gaugehistogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE queue_size_bytes histogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "# TYPE queue_size_bytes_gcount gauge\n" +
                "queue_size_bytes_gcount 130\n";
        ClassicHistogramSnapshot gaugeHistogram = ClassicHistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("queue_size_bytes")
                .addData(ClassicHistogramData.newBuilder()
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 130)
                                .build())
                        .build())
                .build();
        assertOpenMetrics(openMetrics, gaugeHistogram);
        assertPrometheus(prometheus, gaugeHistogram);
        assertOpenMetricsWithoutCreated(openMetrics, gaugeHistogram);
        assertPrometheusWithoutCreated(prometheus, gaugeHistogram);
     }

    @Test
    public void testClassicGaugeHistogramCountAndSum() throws IOException {
        String openMetrics = "" +
                "# TYPE queue_size_bytes gaugehistogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "queue_size_bytes_gcount 130\n" +
                "queue_size_bytes_gsum 27000.0\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE queue_size_bytes histogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "# TYPE queue_size_bytes_gcount gauge\n" +
                "queue_size_bytes_gcount 130\n" +
                "# TYPE queue_size_bytes_gsum gauge\n" +
                "queue_size_bytes_gsum 27000.0\n";
        ClassicHistogramSnapshot gaugeHistogram = ClassicHistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("queue_size_bytes")
                .addData(ClassicHistogramData.newBuilder()
                        .withSum(27000)
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 130)
                                .build())
                        .build())
                .build();
        assertOpenMetrics(openMetrics, gaugeHistogram);
        assertPrometheus(prometheus, gaugeHistogram);
        assertOpenMetricsWithoutCreated(openMetrics, gaugeHistogram);
        assertPrometheusWithoutCreated(prometheus, gaugeHistogram);
    }

    @Test
    public void testNativeHistogram() throws IOException {
        String openMetrics = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_count{status=\"200\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 9 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_count{status=\"500\"} 9 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 9 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 9 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# HELP response_size_bytes_created help\n" +
                "# TYPE response_size_bytes_created gauge\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        NativeHistogramSnapshot nativeHistogram = NativeHistogramSnapshot.newBuilder()
                .withName("response_size_bytes")
                .withHelp("help")
                .withUnit(Unit.BYTES)
                .addData(NativeHistogramData.newBuilder()
                        .withSum(3.2)
                        .withSchema(5)
                        .withZeroCount(1)
                        .withBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(2, 3)
                                .addBucket(3, 7)
                                .build())
                        .withBucketsForNegativeValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(0, 1)
                                .build())
                        .withLabels(Labels.of("status", "500"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addData(NativeHistogramData.newBuilder()
                        .withSum(4.2)
                        .withSchema(5)
                        .withBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(0, 2)
                                .build())
                        .withLabels(Labels.of("status", "200"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, nativeHistogram);
        assertPrometheus(prometheus, nativeHistogram);
    }

    @Test
    public void testInfo() throws IOException {
        String openMetrics = "" +
                "# TYPE version info\n" +
                "# HELP version version information\n" +
                "version_info{version=\"1.2.3\"} 1\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP version_info version information\n" +
                "# TYPE version_info gauge\n" +
                "version_info{version=\"1.2.3\"} 1\n";
        InfoSnapshot info = InfoSnapshot.newBuilder()
                .withName("version")
                .withHelp("version information")
                .addInfoData(InfoSnapshot.InfoData.newBuilder()
                        .withLabels(Labels.of("version", "1.2.3"))
                        .build())
                .build();
        assertOpenMetrics(openMetrics, info);
        assertPrometheus(prometheus, info);
        assertOpenMetricsWithoutCreated(openMetrics, info);
        assertPrometheusWithoutCreated(prometheus, info);
    }

    @Test
    public void testStateSetComplete() throws IOException {
        String openMetrics = "" +
                "# TYPE state stateset\n" +
                "# HELP state complete state set example\n" +
                "state{env=\"dev\",state=\"state1\"} 1 " + scrapeTimestamp1s + "\n" +
                "state{env=\"dev\",state=\"state2\"} 0 " + scrapeTimestamp1s + "\n" +
                "state{env=\"prod\",state=\"state1\"} 0 " + scrapeTimestamp2s + "\n" +
                "state{env=\"prod\",state=\"state2\"} 1 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP state complete state set example\n" +
                "# TYPE state gauge\n" +
                "state{env=\"dev\",state=\"state1\"} 1 " + scrapeTimestamp1s + "\n" +
                "state{env=\"dev\",state=\"state2\"} 0 " + scrapeTimestamp1s + "\n" +
                "state{env=\"prod\",state=\"state1\"} 0 " + scrapeTimestamp2s + "\n" +
                "state{env=\"prod\",state=\"state2\"} 1 " + scrapeTimestamp2s + "\n";
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
        assertOpenMetrics(openMetrics, stateSet);
        assertPrometheus(prometheus, stateSet);
        assertOpenMetricsWithoutCreated(openMetrics, stateSet);
        assertPrometheusWithoutCreated(prometheus, stateSet);
    }

    @Test
    public void testStateSetMinimal() throws IOException {
        String openMetrics = "" +
                "# TYPE state stateset\n" +
                "state{state=\"a\"} 1\n" +
                "state{state=\"bb\"} 0\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE state gauge\n" +
                "state{state=\"a\"} 1\n" +
                "state{state=\"bb\"} 0\n";
        StateSetSnapshot stateSet = StateSetSnapshot.newBuilder()
                .withName("state")
                .addStateSetData(StateSetSnapshot.StateSetData.newBuilder()
                        .addState("a", true)
                        .addState("bb", false)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, stateSet);
        assertPrometheus(prometheus, stateSet);
        assertOpenMetricsWithoutCreated(openMetrics, stateSet);
        assertPrometheusWithoutCreated(prometheus, stateSet);
    }

    @Test
    public void testUnknownComplete() throws IOException {
        String openMetrics = "" +
                "# TYPE my_special_thing_bytes unknown\n" +
                "# UNIT my_special_thing_bytes bytes\n" +
                "# HELP my_special_thing_bytes help message\n" +
                "my_special_thing_bytes{env=\"dev\"} 0.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "my_special_thing_bytes{env=\"prod\"} 0.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP my_special_thing_bytes help message\n" +
                "# TYPE my_special_thing_bytes untyped\n" +
                "my_special_thing_bytes{env=\"dev\"} 0.2 " + scrapeTimestamp1s + "\n" +
                "my_special_thing_bytes{env=\"prod\"} 0.7 " + scrapeTimestamp2s + "\n";
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
        assertOpenMetrics(openMetrics, unknown);
        assertPrometheus(prometheus, unknown);
        assertOpenMetricsWithoutCreated(openMetrics, unknown);
        assertPrometheusWithoutCreated(prometheus, unknown);
    }

    @Test
    public void testUnknownMinimal() throws IOException {
        String openMetrics = "" +
                "# TYPE other unknown\n" +
                "other 22.3\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE other untyped\n" +
                "other 22.3\n";
        UnknownSnapshot unknown = UnknownSnapshot.newBuilder()
                .withName("other")
                .addUnknownData(UnknownData.newBuilder()
                        .withValue(22.3)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, unknown);
        assertPrometheus(prometheus, unknown);
        assertOpenMetricsWithoutCreated(openMetrics, unknown);
        assertPrometheusWithoutCreated(prometheus, unknown);
    }

    @Test
    public void testHelpEscape() throws IOException {
        String openMetrics = "" +
                "# TYPE test counter\n" +
                "# HELP test Some text and \\n some \\\" escaping\n" +
                "test_total 1.0\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP test_total Some text and \\n some \" escaping\n" +
                "# TYPE test_total counter\n" +
                "test_total 1.0\n";
        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("test")
                .withHelp("Some text and \n some \" escaping") // example from https://openMetrics.io
                .addCounterData(CounterData.newBuilder().withValue(1.0).build())
                .build();
        assertOpenMetrics(openMetrics, counter);
        assertPrometheus(prometheus, counter);
        assertOpenMetricsWithoutCreated(openMetrics, counter);
        assertPrometheusWithoutCreated(prometheus, counter);
    }

    @Test
    public void testLabelValueEscape() throws IOException {
        String openMetrics = "" +
                "# TYPE test counter\n" +
                "test_total{a=\"x\",b=\"escaping\\\" example \\n \"} 1.0\n" +
                "# EOF\n";
        String prometheus = "" +
                "# TYPE test_total counter\n" +
                "test_total{a=\"x\",b=\"escaping\\\" example \\n \"} 1.0\n";
        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("test")
                .addCounterData(CounterData.newBuilder()
                        // example from https://openMetrics.io
                        .withLabels(Labels.of("a", "x", "b", "escaping\" example \n "))
                        .withValue(1.0)
                        .build())
                .build();
        assertOpenMetrics(openMetrics, counter);
        assertPrometheus(prometheus, counter);
    }

    private void assertOpenMetrics(String openMetrics, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(openMetrics, out.toString());
    }

    private void assertOpenMetricsWithoutCreated(String openMetrics, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(false);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(openMetrics, out.toString());
    }

    private void assertPrometheus(String prometheus, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrometheusTextFormatWriter writer = new PrometheusTextFormatWriter(true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(prometheus, out.toString());
    }

    private void assertPrometheusWithoutCreated(String prometheus, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrometheusTextFormatWriter writer = new PrometheusTextFormatWriter(false);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(prometheus, out.toString());
    }
}
