package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.com_google_protobuf_3_21_7.TextFormat;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_21_7.Metrics;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot.SummaryDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot.UnknownDataPointSnapshot;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExpositionFormatsTest {

    private final String exemplar1String = "{env=\"prod\",span_id=\"12345\",trace_id=\"abcde\"} 1.7 1672850685.829";
    private final String exemplar2String = "{env=\"dev\",span_id=\"23456\",trace_id=\"bcdef\"} 2.4 1672850685.830";
    private final String exemplarWithDotsString = "{some_exemplar_key=\"some value\"} 3.0 1690298864.383";

    private final String exemplar1protoString = "exemplar { " +
            "label { name: \"env\" value: \"prod\" } " +
            "label { name: \"span_id\" value: \"12345\" } " +
            "label { name: \"trace_id\" value: \"abcde\" } " +
            "value: 1.7 " +
            "timestamp { seconds: 1672850685 nanos: 829000000 } }";

    private final String exemplar2protoString = "exemplar { " +
            "label { name: \"env\" value: \"dev\" } " +
            "label { name: \"span_id\" value: \"23456\" } " +
            "label { name: \"trace_id\" value: \"bcdef\" } " +
            "value: 2.4 " +
            "timestamp { seconds: 1672850685 nanos: 830000000 } }";

    private final String exemplarWithDotsProtoString = "exemplar { " +
            "label { name: \"some_exemplar_key\" value: \"some value\" } " +
            "value: 3.0 " +
            "timestamp { seconds: 1690298864 nanos: 383000000 } }";

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

    private final Exemplar exemplarWithDots = Exemplar.newBuilder()
            .withLabels(Labels.of("some.exemplar.key", "some value"))
            .withValue(3.0)
            .withTimestampMillis(1690298864383L)
            .build();

    @Test
    public void testCounterComplete() throws IOException {
        String openMetricsText = "" +
                "# TYPE service_time_seconds counter\n" +
                "# UNIT service_time_seconds seconds\n" +
                "# HELP service_time_seconds total time spent serving\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP service_time_seconds_total total time spent serving\n" +
                "# TYPE service_time_seconds_total counter\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + "\n" +
                "# HELP service_time_seconds_created total time spent serving\n" +
                "# TYPE service_time_seconds_created gauge\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_created{path=\"/hello\",status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsTextWithoutCreated = "" +
                "# TYPE service_time_seconds counter\n" +
                "# UNIT service_time_seconds seconds\n" +
                "# HELP service_time_seconds total time spent serving\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        String prometheusTextWithoutCreated = "" +
                "# HELP service_time_seconds_total total time spent serving\n" +
                "# TYPE service_time_seconds_total counter\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"200\"} 0.8 " + scrapeTimestamp1s + "\n" +
                "service_time_seconds_total{path=\"/hello\",status=\"500\"} 0.9 " + scrapeTimestamp2s + "\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"service_time_seconds_total\" " +
                "help: \"total time spent serving\" " +
                "type: COUNTER " +
                "metric { " +
                    "label { name: \"path\" value: \"/hello\" } " +
                    "label { name: \"status\" value: \"200\" } " +
                    "counter { " +
                        "value: 0.8 " +
                        exemplar1protoString + " " +
                    "} " +
                    "timestamp_ms: 1672850685829 " +
                "} " +
                "metric { " +
                    "label { name: \"path\" value: \"/hello\" } " +
                    "label { name: \"status\" value: \"500\" } " +
                    "counter { " +
                        "value: 0.9 " +
                        exemplar2protoString + " " +
                    "} " +
                    "timestamp_ms: 1672850585820 " +
                "}";
                //@formatter:on

        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("service_time_seconds")
                .withHelp("total time spent serving")
                .withUnit(Unit.SECONDS)
                .addDataPoint(CounterDataPointSnapshot.newBuilder()
                        .withValue(0.8)
                        .withLabels(Labels.newBuilder()
                                .addLabel("path", "/hello")
                                .addLabel("status", "200")
                                .build())
                        .withExemplar(exemplar1)
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .addDataPoint(CounterDataPointSnapshot.newBuilder()
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
        assertOpenMetricsText(openMetricsText, counter);
        assertPrometheusText(prometheusText, counter);
        assertOpenMetricsTextWithoutCreated(openMetricsTextWithoutCreated, counter);
        assertPrometheusTextWithoutCreated(prometheusTextWithoutCreated, counter);
        assertPrometheusProtobuf(prometheusProtobuf, counter);
    }

    @Test
    public void testCounterMinimal() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_counter counter\n" +
                "my_counter_total 1.1\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE my_counter_total counter\n" +
                "my_counter_total 1.1\n";
        String prometheusProtobuf = "" +
                "name: \"my_counter_total\" type: COUNTER metric { counter { value: 1.1 } }";
        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("my_counter")
                .addDataPoint(CounterDataPointSnapshot.newBuilder().withValue(1.1).build())
                .build();
        assertOpenMetricsText(openMetricsText, counter);
        assertPrometheusText(prometheusText, counter);
        assertOpenMetricsTextWithoutCreated(openMetricsText, counter);
        assertPrometheusTextWithoutCreated(prometheusText, counter);
        assertPrometheusProtobuf(prometheusProtobuf, counter);
    }

    @Test
    public void testCounterWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_request_count counter\n" +
                "my_request_count_total{http_path=\"/hello\"} 3.0 # " + exemplarWithDotsString + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE my_request_count_total counter\n" +
                "my_request_count_total{http_path=\"/hello\"} 3.0\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"my_request_count_total\" " +
                "type: COUNTER " +
                "metric { " +
                    "label { name: \"http_path\" value: \"/hello\" } " +
                    "counter { " +
                        "value: 3.0 " + exemplarWithDotsProtoString + " " +
                    "} " +
                "}";
                //@formatter:on

        CounterSnapshot counter = CounterSnapshot.newBuilder()
                .withName("my.request.count")
                .addDataPoint(CounterDataPointSnapshot.newBuilder()
                        .withValue(3.0)
                        .withLabels(Labels.newBuilder()
                                .addLabel("http.path", "/hello")
                                .build())
                        .withExemplar(exemplarWithDots)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, counter);
        assertPrometheusText(prometheusText, counter);
        assertPrometheusProtobuf(prometheusProtobuf, counter);
    }

    @Test
    public void testGaugeComplete() throws IOException {
        String openMetricsText = "" +
                "# TYPE disk_usage_ratio gauge\n" +
                "# UNIT disk_usage_ratio ratio\n" +
                "# HELP disk_usage_ratio percentage used\n" +
                "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP disk_usage_ratio percentage used\n" +
                "# TYPE disk_usage_ratio gauge\n" +
                "disk_usage_ratio{device=\"/dev/sda1\"} 0.2 " + scrapeTimestamp1s + "\n" +
                "disk_usage_ratio{device=\"/dev/sda2\"} 0.7 " + scrapeTimestamp2s + "\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"disk_usage_ratio\" " +
                "help: \"percentage used\" " +
                "type: GAUGE " +
                "metric { " +
                    "label { name: \"device\" value: \"/dev/sda1\" } " +
                    "gauge { value: 0.2 } " +
                    "timestamp_ms: 1672850685829 " +
                "} metric { " +
                    "label { name: \"device\" value: \"/dev/sda2\" } " +
                    "gauge { value: 0.7 } " +
                    "timestamp_ms: 1672850585820 " +
                "}";
                //@formatter:on
        GaugeSnapshot gauge = GaugeSnapshot.newBuilder()
                .withName("disk_usage_ratio")
                .withHelp("percentage used")
                .withUnit(new Unit("ratio"))
                .addDataPoint(GaugeDataPointSnapshot.newBuilder()
                        .withValue(0.7)
                        .withLabels(Labels.newBuilder()
                                .addLabel("device", "/dev/sda2")
                                .build())
                        .withExemplar(exemplar2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addDataPoint(GaugeDataPointSnapshot.newBuilder()
                        .withValue(0.2)
                        .withLabels(Labels.newBuilder()
                                .addLabel("device", "/dev/sda1")
                                .build())
                        .withExemplar(exemplar1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, gauge);
        assertPrometheusText(prometheusText, gauge);
        assertOpenMetricsTextWithoutCreated(openMetricsText, gauge);
        assertPrometheusTextWithoutCreated(prometheusText, gauge);
        assertPrometheusProtobuf(prometheusProtobuf, gauge);
    }

    @Test
    public void testGaugeMinimal() throws IOException {
        String openMetricsText = "" +
                "# TYPE temperature_centigrade gauge\n" +
                "temperature_centigrade 22.3\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE temperature_centigrade gauge\n" +
                "temperature_centigrade 22.3\n";
        String prometheusProtobuf = "" +
                "name: \"temperature_centigrade\" type: GAUGE metric { gauge { value: 22.3 } }";
        GaugeSnapshot gauge = GaugeSnapshot.newBuilder()
                .withName("temperature_centigrade")
                .addDataPoint(GaugeDataPointSnapshot.newBuilder()
                        .withValue(22.3)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, gauge);
        assertPrometheusText(prometheusText, gauge);
        assertOpenMetricsTextWithoutCreated(openMetricsText, gauge);
        assertPrometheusTextWithoutCreated(prometheusText, gauge);
        assertPrometheusProtobuf(prometheusProtobuf, gauge);
    }

    @Test
    public void testGaugeWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_temperature_celsius gauge\n" +
                "# UNIT my_temperature_celsius celsius\n" +
                "# HELP my_temperature_celsius Temperature\n" +
                "my_temperature_celsius{location_id=\"data-center-1\"} 23.0 # " + exemplarWithDotsString + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP my_temperature_celsius Temperature\n" +
                "# TYPE my_temperature_celsius gauge\n" +
                "my_temperature_celsius{location_id=\"data-center-1\"} 23.0\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"my_temperature_celsius\" " +
                "help: \"Temperature\" " +
                "type: GAUGE " +
                "metric { " +
                    "label { name: \"location_id\" value: \"data-center-1\" } " +
                    "gauge { " +
                        "value: 23.0 " +
                    "} " +
                "}";
                //@formatter:on

        GaugeSnapshot gauge = GaugeSnapshot.newBuilder()
                .withName("my.temperature.celsius")
                .withHelp("Temperature")
                .withUnit(Unit.CELSIUS)
                .addDataPoint(GaugeDataPointSnapshot.newBuilder()
                        .withValue(23.0)
                        .withLabels(Labels.newBuilder()
                                .addLabel("location.id", "data-center-1")
                                .build())
                        .withExemplar(exemplarWithDots)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, gauge);
        assertPrometheusText(prometheusText, gauge);
        assertPrometheusProtobuf(prometheusProtobuf, gauge);
    }

    @Test
    public void testSummaryComplete() throws IOException {
        String openMetricsText = "" +
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
        String prometheusText = "" +
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
        String openMetricsTextWithoutCreated = "" +
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
        String prometheusTextWithoutCreated = "" +
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
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"http_request_duration_seconds\" " +
                "help: \"request duration\" " +
                "type: SUMMARY " +
                "metric { " +
                    "label { name: \"status\" value: \"200\" } " +
                    "summary { " +
                        "sample_count: 3 " +
                        "sample_sum: 1.2 " +
                        "quantile { quantile: 0.5 value: 225.3 } " +
                        "quantile { quantile: 0.9 value: 240.7 } " +
                        "quantile { quantile: 0.95 value: 245.1 } " +
                    "} " +
                    "timestamp_ms: 1672850685829 " +
                "} metric { " +
                    "label { name: \"status\" value: \"500\" } " +
                    "summary { " +
                        "sample_count: 7 " +
                        "sample_sum: 2.2 " +
                        "quantile { quantile: 0.5 value: 225.3 } " +
                        "quantile { quantile: 0.9 value: 240.7 } " +
                        "quantile { quantile: 0.95 value: 245.1 } " +
                    "} " + "" +
                    "timestamp_ms: 1672850585820 " +
                "}";
                //@formatter:on
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("http_request_duration_seconds")
                .withHelp("request duration")
                .withUnit(Unit.SECONDS)
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
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
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
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
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertOpenMetricsTextWithoutCreated(openMetricsTextWithoutCreated, summary);
        assertPrometheusTextWithoutCreated(prometheusTextWithoutCreated, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testSummaryWithoutQuantiles() throws IOException {
        String openMetricsText = "" +
                "# TYPE latency_seconds summary\n" +
                "# UNIT latency_seconds seconds\n" +
                "# HELP latency_seconds latency\n" +
                "latency_seconds_count 3\n" +
                "latency_seconds_sum 1.2\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP latency_seconds latency\n" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count 3\n" +
                "latency_seconds_sum 1.2\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"latency_seconds\" " +
                "help: \"latency\" " +
                "type: SUMMARY " +
                "metric { " +
                    "summary { " +
                        "sample_count: 3 " +
                        "sample_sum: 1.2 " +
                    "} " +
                "}";
                //@formatter:on
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .withHelp("latency")
                .withUnit(Unit.SECONDS)
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withCount(3)
                        .withSum(1.2)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertOpenMetricsTextWithoutCreated(openMetricsText, summary);
        assertPrometheusTextWithoutCreated(prometheusText, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testSummaryNoCountAndSum() throws IOException {
        String openMetricsText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds{quantile=\"0.95\"} 200.0\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds{quantile=\"0.95\"} 200.0\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"latency_seconds\" " +
                "type: SUMMARY " +
                "metric { " +
                    "summary { " +
                        "quantile { quantile: 0.95 value: 200.0 } " +
                    "} " +
                "}";
                //@formatter:on
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withQuantiles(Quantiles.newBuilder().addQuantile(0.95, 200.0).build())
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertOpenMetricsTextWithoutCreated(openMetricsText, summary);
        assertPrometheusTextWithoutCreated(prometheusText, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testSummaryJustCount() throws IOException {
        String openMetricsText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count 1\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count 1\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"latency_seconds\" " +
                "type: SUMMARY " +
                "metric { " +
                    "summary { " +
                        "sample_count: 1 " +
                    "} " +
                "}";
        //@formatter:on
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withCount(1)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertOpenMetricsTextWithoutCreated(openMetricsText, summary);
        assertPrometheusTextWithoutCreated(prometheusText, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testSummaryJustSum() throws IOException {
        String openMetricsText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_sum 12.3\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_sum 12.3\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"latency_seconds\" " +
                "type: SUMMARY " +
                "metric { " +
                    "summary { " +
                        "sample_sum: 12.3 " +
                    "} " +
                "}";
                //@formatter:on
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withSum(12.3)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertOpenMetricsTextWithoutCreated(openMetricsText, summary);
        assertPrometheusTextWithoutCreated(prometheusText, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testSummaryEmptyData() throws IOException {
        // SummaryData can be present but empty (no count, no sum, no quantiles).
        // This should be treated like no data is present.
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .withHelp("latency")
                .withUnit(Unit.SECONDS)
                .addDataPoint(SummaryDataPointSnapshot.newBuilder().build())
                .build();
        assertOpenMetricsText("# EOF\n", summary);
        assertPrometheusText("", summary);
        assertOpenMetricsTextWithoutCreated("# EOF\n", summary);
        assertPrometheusTextWithoutCreated("", summary);
        assertPrometheusProtobuf("", summary);
    }

    @Test
    public void testSummaryEmptyAndNonEmpty() throws IOException {
        String openMetricsText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count{path=\"/v2\"} 2\n" +
                "latency_seconds_sum{path=\"/v2\"} 10.7\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE latency_seconds summary\n" +
                "latency_seconds_count{path=\"/v2\"} 2\n" +
                "latency_seconds_sum{path=\"/v2\"} 10.7\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"latency_seconds\" " +
                "type: SUMMARY " +
                "metric { " +
                    "label { name: \"path\" value: \"/v2\" } " +
                    "summary { " +
                        "sample_count: 2 " +
                        "sample_sum: 10.7 " +
                    "} " +
                "}";
                //@formatter:on
        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("path", "/v1"))
                        .build())
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("path", "/v2"))
                        .withCount(2)
                        .withSum(10.7)
                        .build())
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("path", "/v3"))
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertOpenMetricsTextWithoutCreated(openMetricsText, summary);
        assertPrometheusTextWithoutCreated(prometheusText, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testSummaryWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_request_duration_seconds summary\n" +
                "# UNIT my_request_duration_seconds seconds\n" +
                "# HELP my_request_duration_seconds Request duration in seconds\n" +
                "my_request_duration_seconds_count{http_path=\"/hello\"} 1 # " + exemplarWithDotsString + "\n" +
                "my_request_duration_seconds_sum{http_path=\"/hello\"} 0.03 # " + exemplarWithDotsString + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP my_request_duration_seconds Request duration in seconds\n" +
                "# TYPE my_request_duration_seconds summary\n" +
                "my_request_duration_seconds_count{http_path=\"/hello\"} 1\n" +
                "my_request_duration_seconds_sum{http_path=\"/hello\"} 0.03\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"my_request_duration_seconds\" " +
                "help: \"Request duration in seconds\" " +
                "type: SUMMARY " +
                "metric { " +
                    "label { name: \"http_path\" value: \"/hello\" } " +
                    "summary { sample_count: 1 sample_sum: 0.03 } " +
                "}";
                //@formatter:on

        SummarySnapshot summary = SummarySnapshot.newBuilder()
                .withName("my.request.duration.seconds")
                .withHelp("Request duration in seconds")
                .withUnit(Unit.SECONDS)
                .addDataPoint(SummaryDataPointSnapshot.newBuilder()
                        .withCount(1)
                        .withSum(0.03)
                        .withLabels(Labels.newBuilder()
                                .addLabel("http.path", "/hello")
                                .build())
                        .withExemplars(Exemplars.of(exemplarWithDots))
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, summary);
        assertPrometheusText(prometheusText, summary);
        assertPrometheusProtobuf(prometheusProtobuf, summary);
    }

    @Test
    public void testClassicHistogramComplete() throws Exception {
        String openMetricsText = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 3 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"200\"} 3 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"0.0\"} 3 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 5 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 5 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"500\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 3 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 3 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"0.0\"} 3 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# HELP response_size_bytes_created help\n" +
                "# TYPE response_size_bytes_created gauge\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsTextWithoutCreated = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 3 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"200\"} 3 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"0.0\"} 3 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 5 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 5 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "response_size_bytes_count{status=\"500\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusTextWithoutCreated = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"2.2\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 3 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 3 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.1 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"0.0\"} 3 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"2.2\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 5 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"response_size_bytes\" " +
                "help: \"help\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "label { name: \"status\" value: \"200\" } " +
                    "timestamp_ms: 1672850685829 " +
                    "histogram { " +
                        "sample_count: 3 " +
                        "sample_sum: 4.1 " +
                        "bucket { " +
                            "cumulative_count: 2 " +
                            "upper_bound: 2.2 " +
                            exemplar1protoString + " " +
                        "} bucket { " +
                            "cumulative_count: 3 " +
                            "upper_bound: Infinity " +
                            exemplar2protoString + " " +
                        "} " +
                    "} " +
                "} metric { " +
                    "label { name: \"status\" value: \"500\" } " +
                    "timestamp_ms: 1672850585820 " +
                    "histogram { " +
                        "sample_count: 5 " +
                        "sample_sum: 3.2 " +
                        "bucket { " +
                            "cumulative_count: 3 " +
                            "upper_bound: 0.0 " +
                        "} bucket { " +
                            "cumulative_count: 5 " +
                            "upper_bound: 2.2 " +
                            exemplar1protoString + " " +
                        "} bucket { " +
                            "cumulative_count: 5 " +
                            "upper_bound: Infinity " +
                            exemplar2protoString + " " +
                        "} " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot histogram = HistogramSnapshot.newBuilder()
                .withName("response_size_bytes")
                .withHelp("help")
                .withUnit(Unit.BYTES)
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(3.2)
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(0.0, 3)
                                .addBucket(2.2, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 0)
                                .build())
                        .withLabels(Labels.of("status", "500"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(4.1)
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(2.2, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 1)
                                .build())
                        .withLabels(Labels.of("status", "200"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, histogram);
        assertPrometheusText(prometheusText, histogram);
        assertOpenMetricsTextWithoutCreated(openMetricsTextWithoutCreated, histogram);
        assertPrometheusTextWithoutCreated(prometheusTextWithoutCreated, histogram);
        assertPrometheusProtobuf(prometheusProtobuf, histogram);
    }

    @Test
    public void testClassicHistogramMinimal() throws Exception {
        // In OpenMetrics a histogram can have a _count if and only if it has a _sum.
        // In Prometheus format, a histogram can have a _count without a _sum.
        String openMetricsText = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"request_latency_seconds\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "histogram { " +
                        "sample_count: 2 " +
                        "bucket { " +
                            "cumulative_count: 2 " +
                            "upper_bound: Infinity " +
                        "} " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot histogram = HistogramSnapshot.newBuilder()
                .withName("request_latency_seconds")
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, histogram);
        assertPrometheusText(prometheusText, histogram);
        assertOpenMetricsTextWithoutCreated(openMetricsText, histogram);
        assertPrometheusTextWithoutCreated(prometheusText, histogram);
        assertPrometheusProtobuf(prometheusProtobuf, histogram);
    }

    @Test
    public void testClassicHistogramCountAndSum() throws Exception {
        String openMetricsText = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n" +
                "request_latency_seconds_sum 3.2\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE request_latency_seconds histogram\n" +
                "request_latency_seconds_bucket{le=\"+Inf\"} 2\n" +
                "request_latency_seconds_count 2\n" +
                "request_latency_seconds_sum 3.2\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"request_latency_seconds\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "histogram { " +
                        "sample_count: 2 " +
                        "sample_sum: 3.2 " +
                        "bucket { " +
                            "cumulative_count: 2 " +
                            "upper_bound: Infinity " +
                        "} " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot histogram = HistogramSnapshot.newBuilder()
                .withName("request_latency_seconds")
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(3.2)
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 2)
                                .build())
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, histogram);
        assertPrometheusText(prometheusText, histogram);
        assertOpenMetricsTextWithoutCreated(openMetricsText, histogram);
        assertPrometheusTextWithoutCreated(prometheusText, histogram);
        assertPrometheusProtobuf(prometheusProtobuf, histogram);
    }

    @Test
    public void testClassicGaugeHistogramComplete() throws IOException {
        String openMetricsText = "" +
                "# TYPE cache_size_bytes gaugehistogram\n" +
                "# UNIT cache_size_bytes bytes\n" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"items\"} 4 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_created{db=\"items\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 4 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 4 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_created{db=\"options\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "# TYPE cache_size_bytes histogram\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 4 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gcount number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gcount gauge\n" +
                "cache_size_bytes_gcount{db=\"items\"} 4 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 4 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gsum number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gsum gauge\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_created number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_created gauge\n" +
                "cache_size_bytes_created{db=\"items\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_created{db=\"options\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsTextWithoutCreated = "" +
                "# TYPE cache_size_bytes gaugehistogram\n" +
                "# UNIT cache_size_bytes bytes\n" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"items\"} 4 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 4 " + scrapeTimestamp2s + " # " + exemplar2String + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 4 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusTextWithoutCreated = "" +
                "# HELP cache_size_bytes number of bytes in the cache\n" +
                "# TYPE cache_size_bytes histogram\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"2.0\"} 3 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"items\",le=\"+Inf\"} 4 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"2.0\"} 4 " + scrapeTimestamp2s + "\n" +
                "cache_size_bytes_bucket{db=\"options\",le=\"+Inf\"} 4 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gcount number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gcount gauge\n" +
                "cache_size_bytes_gcount{db=\"items\"} 4 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gcount{db=\"options\"} 4 " + scrapeTimestamp2s + "\n" +
                "# HELP cache_size_bytes_gsum number of bytes in the cache\n" +
                "# TYPE cache_size_bytes_gsum gauge\n" +
                "cache_size_bytes_gsum{db=\"items\"} 17.0 " + scrapeTimestamp1s + "\n" +
                "cache_size_bytes_gsum{db=\"options\"} 18.0 " + scrapeTimestamp2s + "\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"cache_size_bytes\" " +
                "help: \"number of bytes in the cache\" " +
                "type: GAUGE_HISTOGRAM " +
                "metric { " +
                    "label { name: \"db\" value: \"items\" } " +
                    "timestamp_ms: 1672850685829 " +
                    "histogram { " +
                        "sample_count: 4 " +
                        "sample_sum: 17.0 " +
                        "bucket { " +
                            "cumulative_count: 3 " +
                            "upper_bound: 2.0 " +
                            exemplar1protoString + " " +
                        "} bucket { " +
                            "cumulative_count: 4 " +
                            "upper_bound: Infinity " +
                            exemplar2protoString + " " +
                        "} " +
                    "} " +
                "} metric { " +
                    "label { name: \"db\" value: \"options\" } " +
                    "timestamp_ms: 1672850585820 " +
                    "histogram { " +
                        "sample_count: 4 " +
                        "sample_sum: 18.0 " +
                        "bucket { " +
                            "cumulative_count: 4 " +
                            "upper_bound: 2.0 " +
                            exemplar1protoString + " " +
                        "} bucket { " +
                            "cumulative_count: 4 " +
                            "upper_bound: Infinity " +
                            exemplar2protoString + " " +
                        "} " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot gaugeHistogram = HistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("cache_size_bytes")
                .withHelp("number of bytes in the cache")
                .withUnit(Unit.BYTES)
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(17)
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(2.0, 3)
                                .addBucket(Double.POSITIVE_INFINITY, 1)
                                .build())
                        .withLabels(Labels.of("db", "items"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(18)
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(2.0, 4)
                                .addBucket(Double.POSITIVE_INFINITY, 0)
                                .build()
                        )
                        .withLabels(Labels.of("db", "options"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, gaugeHistogram);
        assertPrometheusText(prometheusText, gaugeHistogram);
        assertOpenMetricsTextWithoutCreated(openMetricsTextWithoutCreated, gaugeHistogram);
        assertPrometheusTextWithoutCreated(prometheusTextWithoutCreated, gaugeHistogram);
        assertPrometheusProtobuf(prometheusProtobuf, gaugeHistogram);
    }

    @Test
    public void testClassicGaugeHistogramMinimal() throws IOException {
        // In OpenMetrics a histogram can have a _count if and only if it has a _sum.
        // In Prometheus format, a histogram can have a _count without a _sum.
        String openMetricsText = "" +
                "# TYPE queue_size_bytes gaugehistogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE queue_size_bytes histogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "# TYPE queue_size_bytes_gcount gauge\n" +
                "queue_size_bytes_gcount 130\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"queue_size_bytes\" " +
                "type: GAUGE_HISTOGRAM " +
                "metric { " +
                    "histogram { " +
                        "sample_count: 130 " +
                        "bucket { " +
                            "cumulative_count: 130 " +
                            "upper_bound: Infinity " +
                        "} " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot gaugeHistogram = HistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("queue_size_bytes")
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 130)
                                .build())
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, gaugeHistogram);
        assertPrometheusText(prometheusText, gaugeHistogram);
        assertOpenMetricsTextWithoutCreated(openMetricsText, gaugeHistogram);
        assertPrometheusTextWithoutCreated(prometheusText, gaugeHistogram);
        assertPrometheusProtobuf(prometheusProtobuf, gaugeHistogram);
    }

    @Test
    public void testClassicGaugeHistogramCountAndSum() throws IOException {
        String openMetricsText = "" +
                "# TYPE queue_size_bytes gaugehistogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "queue_size_bytes_gcount 130\n" +
                "queue_size_bytes_gsum 27000.0\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE queue_size_bytes histogram\n" +
                "queue_size_bytes_bucket{le=\"+Inf\"} 130\n" +
                "# TYPE queue_size_bytes_gcount gauge\n" +
                "queue_size_bytes_gcount 130\n" +
                "# TYPE queue_size_bytes_gsum gauge\n" +
                "queue_size_bytes_gsum 27000.0\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"queue_size_bytes\" " +
                "type: GAUGE_HISTOGRAM " +
                "metric { " +
                    "histogram { " +
                        "sample_count: 130 " +
                        "sample_sum: 27000.0 " +
                        "bucket { " +
                            "cumulative_count: 130 " +
                            "upper_bound: Infinity " +
                        "} " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot gaugeHistogram = HistogramSnapshot.newBuilder()
                .asGaugeHistogram()
                .withName("queue_size_bytes")
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(27000)
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 130)
                                .build())
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, gaugeHistogram);
        assertPrometheusText(prometheusText, gaugeHistogram);
        assertOpenMetricsTextWithoutCreated(openMetricsText, gaugeHistogram);
        assertPrometheusTextWithoutCreated(prometheusText, gaugeHistogram);
        assertPrometheusProtobuf(prometheusProtobuf, gaugeHistogram);
    }

    @Test
    public void testClassicHistogramWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_request_duration_seconds histogram\n" +
                "# UNIT my_request_duration_seconds seconds\n" +
                "# HELP my_request_duration_seconds Request duration in seconds\n" +
                "my_request_duration_seconds_bucket{http_path=\"/hello\",le=\"+Inf\"} 130 # " + exemplarWithDotsString + "\n" +
                "my_request_duration_seconds_count{http_path=\"/hello\"} 130\n" +
                "my_request_duration_seconds_sum{http_path=\"/hello\"} 0.01\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP my_request_duration_seconds Request duration in seconds\n" +
                "# TYPE my_request_duration_seconds histogram\n" +
                "my_request_duration_seconds_bucket{http_path=\"/hello\",le=\"+Inf\"} 130\n" +
                "my_request_duration_seconds_count{http_path=\"/hello\"} 130\n" +
                "my_request_duration_seconds_sum{http_path=\"/hello\"} 0.01\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"my_request_duration_seconds\" " +
                "help: \"Request duration in seconds\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "label { name: \"http_path\" value: \"/hello\" } " +
                    "histogram { " +
                        "sample_count: 130 " +
                        "sample_sum: 0.01 " +
                        "bucket { cumulative_count: 130 upper_bound: Infinity " + exemplarWithDotsProtoString + " } " +
                    "} " +
                "}";
                //@formatter:on

        HistogramSnapshot histogram = HistogramSnapshot.newBuilder()
                .withName("my.request.duration.seconds")
                .withHelp("Request duration in seconds")
                .withUnit(Unit.SECONDS)
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(0.01)
                        .withLabels(Labels.newBuilder()
                                .addLabel("http.path", "/hello")
                                .build())
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(Double.POSITIVE_INFINITY, 130)
                                .build())
                        .withExemplars(Exemplars.of(exemplarWithDots))
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, histogram);
        assertPrometheusText(prometheusText, histogram);
        assertPrometheusProtobuf(prometheusProtobuf, histogram);
    }

    @Test
    public void testNativeHistogramComplete() throws IOException {
        String openMetricsText = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_count{status=\"200\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 55 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_count{status=\"500\"} 55 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 55 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 55 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# HELP response_size_bytes_created help\n" +
                "# TYPE response_size_bytes_created gauge\n" +
                "response_size_bytes_created{status=\"200\"} " + createdTimestamp1s + " " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_created{status=\"500\"} " + createdTimestamp2s + " " + scrapeTimestamp2s + "\n";
        String openMetricsTextWithoutCreated = "" +
                "# TYPE response_size_bytes histogram\n" +
                "# UNIT response_size_bytes bytes\n" +
                "# HELP response_size_bytes help\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 2 " + scrapeTimestamp1s + " # " + exemplar1String + "\n" +
                "response_size_bytes_count{status=\"200\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 55 " + scrapeTimestamp2s + " # " + exemplar1String + "\n" +
                "response_size_bytes_count{status=\"500\"} 55 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n" +
                "# EOF\n";
        String prometheusTextWithoutCreated = "" +
                "# HELP response_size_bytes help\n" +
                "# TYPE response_size_bytes histogram\n" +
                "response_size_bytes_bucket{status=\"200\",le=\"+Inf\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_count{status=\"200\"} 2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_sum{status=\"200\"} 4.2 " + scrapeTimestamp1s + "\n" +
                "response_size_bytes_bucket{status=\"500\",le=\"+Inf\"} 55 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_count{status=\"500\"} 55 " + scrapeTimestamp2s + "\n" +
                "response_size_bytes_sum{status=\"500\"} 3.2 " + scrapeTimestamp2s + "\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"response_size_bytes\" " +
                "help: \"help\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "label { name: \"status\" value: \"200\" } " +
                    "timestamp_ms: 1672850685829 " +
                    "histogram { " +
                        "sample_count: 2 " +
                        "sample_sum: 4.2 " +
                        "bucket { cumulative_count: 2 upper_bound: Infinity " + exemplar2protoString + " } " +
                        "schema: 5 " +
                        "zero_threshold: 0.0 " +
                        "zero_count: 0 " +
                        "positive_span { offset: 0 length: 1 } " +
                        "positive_delta: 2 " +
                    "} " +
                "} metric { " +
                    "label { name: \"status\" value: \"500\" } " +
                    "timestamp_ms: 1672850585820 " +
                    "histogram { " +
                        "sample_count: 55 " + // bucket counts + zero count
                        "sample_sum: 3.2 " +
                        "bucket { cumulative_count: 55 upper_bound: Infinity " + exemplar2protoString + " } " +
                        "schema: 5 " +
                        "zero_threshold: 0.0 " +
                        "zero_count: 1 " +
                        "negative_span { offset: 0 length: 1 } " +
                        "negative_span { offset: 9 length: 1 } " +
                        "negative_delta: 1 " +
                        "negative_delta: -1 " + // span with count 0
                        "positive_span { offset: 2 length: 3 } " + // span with 3 buckets (indexes 2-4)
                        "positive_span { offset: 7 length: 1 } " + // span with 1 bucket (index 12)
                        "positive_span { offset: 9 length: 4 } " + // span with gap of size 1 (indexes 22-25)
                        "positive_span { offset: 6 length: 5 } " + // span with gap of size 2 (indexes 32-36)
                        "positive_span { offset: 4 length: 2 } " + // span with gap of size 3 part 1 (indexes 41-42)
                        "positive_span { offset: 3 length: 2 } " + // span with gap of size 3 part 2 (indexes 46-47)
                        "positive_delta: 3 " + // index 2, count 3
                        "positive_delta: 2 " + // index 3, count 5
                        "positive_delta: -1 " + // index 4, count 4
                        "positive_delta: 2 " + // index 12, count 6
                        "positive_delta: -4 " + // index 22, count 2
                        "positive_delta: -2 " + // index 23, gap
                        "positive_delta: 1 " + // index 24, count 1
                        "positive_delta: 2 " + // index 25, count 3
                        "positive_delta: 1 " + // index 32, count 4
                        "positive_delta: -1 " + // index 33, count 3
                        "positive_delta: -3 " + // index 34, gap
                        "positive_delta: 0 " + // index 35, gap
                        "positive_delta: 7 " + // index 36, count 7
                        "positive_delta: -4 " + // index 41, count 3
                        "positive_delta: 6 " + // index 42, count 9
                        "positive_delta: -7 " + // index 46, count 2
                        "positive_delta: -1 " + // index 47, count 1
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot nativeHistogram = HistogramSnapshot.newBuilder()
                .withName("response_size_bytes")
                .withHelp("help")
                .withUnit(Unit.BYTES)
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(3.2)
                        .withNativeSchema(5)
                        .withNativeZeroCount(1)
                        .withNativeBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                // span with 3 buckets
                                .addBucket(2, 3)
                                .addBucket(3, 5)
                                .addBucket(4, 4)
                                // span with just 1 bucket
                                .addBucket(12, 6)
                                // span with gap of size 1
                                .addBucket(22, 2)
                                .addBucket(24, 1)
                                .addBucket(25, 3)
                                // span with gap of size 2
                                .addBucket(32, 4)
                                .addBucket(33, 3)
                                .addBucket(36, 7)
                                // span with gap of size 3
                                .addBucket(41, 3)
                                .addBucket(42, 9)
                                .addBucket(46, 2)
                                .addBucket(47, 1)
                                .build())
                        .withNativeBucketsForNegativeValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(0, 1)
                                .addBucket(10, 0) // bucket with count 0
                                .build())
                        .withLabels(Labels.of("status", "500"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withSum(4.2)
                        .withNativeSchema(5)
                        .withNativeBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(0, 2)
                                .build())
                        .withLabels(Labels.of("status", "200"))
                        .withExemplars(Exemplars.of(exemplar1, exemplar2))
                        .withCreatedTimestampMillis(createdTimestamp1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, nativeHistogram);
        assertPrometheusText(prometheusText, nativeHistogram);
        assertOpenMetricsTextWithoutCreated(openMetricsTextWithoutCreated, nativeHistogram);
        assertPrometheusTextWithoutCreated(prometheusTextWithoutCreated, nativeHistogram);
        assertPrometheusProtobuf(prometheusProtobuf, nativeHistogram);
    }

    @Test
    public void testNativeHistogramMinimal() throws IOException {
        String openMetricsText = "" +
                "# TYPE latency_seconds histogram\n" +
                "latency_seconds_bucket{le=\"+Inf\"} 0\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# TYPE latency_seconds histogram\n" +
                "latency_seconds_bucket{le=\"+Inf\"} 0\n" +
                "latency_seconds_count 0\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"latency_seconds\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "histogram { " +
                        "sample_count: 0 " +
                        "schema: 5 " +
                        "zero_threshold: 0.0 " +
                        "zero_count: 0 " +
                    "} " +
                "}";
                //@formatter:on
        HistogramSnapshot nativeHistogram = HistogramSnapshot.newBuilder()
                .withName("latency_seconds")
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withNativeSchema(5)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, nativeHistogram);
        assertPrometheusText(prometheusText, nativeHistogram);
        assertPrometheusProtobuf(prometheusProtobuf, nativeHistogram);
    }

    @Test
    public void testNativeHistogramWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_request_duration_seconds histogram\n" +
                "# UNIT my_request_duration_seconds seconds\n" +
                "# HELP my_request_duration_seconds Request duration in seconds\n" +
                "my_request_duration_seconds_bucket{http_path=\"/hello\",le=\"+Inf\"} 4 # " + exemplarWithDotsString + "\n" +
                "my_request_duration_seconds_count{http_path=\"/hello\"} 4\n" +
                "my_request_duration_seconds_sum{http_path=\"/hello\"} 3.2\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP my_request_duration_seconds Request duration in seconds\n" +
                "# TYPE my_request_duration_seconds histogram\n" +
                "my_request_duration_seconds_bucket{http_path=\"/hello\",le=\"+Inf\"} 4\n" +
                "my_request_duration_seconds_count{http_path=\"/hello\"} 4\n" +
                "my_request_duration_seconds_sum{http_path=\"/hello\"} 3.2\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"my_request_duration_seconds\" " +
                "help: \"Request duration in seconds\" " +
                "type: HISTOGRAM " +
                "metric { " +
                    "label { name: \"http_path\" value: \"/hello\" } " +
                    "histogram { " +
                        "sample_count: 4 " +
                        "sample_sum: 3.2 " +
                        "bucket { cumulative_count: 4 upper_bound: Infinity " + exemplarWithDotsProtoString + " } " +
                        "schema: 5 " +
                        "zero_threshold: 0.0 " +
                        "zero_count: 1 " +
                        "positive_span { offset: 2 length: 1 } " +
                        "positive_delta: 3 " +
                    "} " +
                "}";
                //@formatter:on

        HistogramSnapshot histogram = HistogramSnapshot.newBuilder()
                .withName("my.request.duration.seconds")
                .withHelp("Request duration in seconds")
                .withUnit(Unit.SECONDS)
                .addDataPoint(HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                        .withLabels(Labels.newBuilder()
                                .addLabel("http.path", "/hello")
                                .build())
                        .withSum(3.2)
                        .withNativeSchema(5)
                        .withNativeZeroCount(1)
                        .withNativeBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(2, 3)
                                .build()
                        )
                        .withExemplars(Exemplars.of(exemplarWithDots))
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, histogram);
        assertPrometheusText(prometheusText, histogram);
        assertPrometheusProtobuf(prometheusProtobuf, histogram);
    }
    // TODO: Gauge Native Histogram

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
                .addDataPoint(InfoSnapshot.InfoDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("version", "1.2.3"))
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, info);
        assertPrometheusText(prometheus, info);
        assertOpenMetricsTextWithoutCreated(openMetrics, info);
        assertPrometheusTextWithoutCreated(prometheus, info);
    }

    @Test
    public void testInfoWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE jvm_status info\n" +
                "# HELP jvm_status JVM status info\n" +
                "jvm_status_info{jvm_version=\"1.2.3\"} 1\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP jvm_status_info JVM status info\n" +
                "# TYPE jvm_status_info gauge\n" +
                "jvm_status_info{jvm_version=\"1.2.3\"} 1\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"jvm_status_info\" " +
                "help: \"JVM status info\" " +
                "type: GAUGE " +
                "metric { " + "" +
                    "label { name: \"jvm_version\" value: \"1.2.3\" } " +
                    "gauge { value: 1.0 } " +
                "}";
                //@formatter:on
        InfoSnapshot info = InfoSnapshot.newBuilder()
                .withName("jvm.status")
                .withHelp("JVM status info")
                .addDataPoint(InfoSnapshot.InfoDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("jvm.version", "1.2.3"))
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, info);
        assertPrometheusText(prometheusText, info);
        assertPrometheusProtobuf(prometheusProtobuf, info);
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
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("env", "prod"))
                        .addState("state1", false)
                        .addState("state2", true)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("env", "dev"))
                        .addState("state2", false)
                        .addState("state1", true)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, stateSet);
        assertPrometheusText(prometheus, stateSet);
        assertOpenMetricsTextWithoutCreated(openMetrics, stateSet);
        assertPrometheusTextWithoutCreated(prometheus, stateSet);
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
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .addState("a", true)
                        .addState("bb", false)
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, stateSet);
        assertPrometheusText(prometheus, stateSet);
        assertOpenMetricsTextWithoutCreated(openMetrics, stateSet);
        assertPrometheusTextWithoutCreated(prometheus, stateSet);
    }

    @Test
    public void testStateSetWithDots() throws IOException {
        String openMetricsText = "" +
                "# TYPE my_application_state stateset\n" +
                "# HELP my_application_state My application state\n" +
                "my_application_state{data_center=\"us east\",my_application_state=\"feature.enabled\"} 1\n" +
                "my_application_state{data_center=\"us east\",my_application_state=\"is.alpha.version\"} 0\n" +
                "# EOF\n";
        String prometheusText = "" +
                "# HELP my_application_state My application state\n" +
                "# TYPE my_application_state gauge\n" +
                "my_application_state{data_center=\"us east\",my_application_state=\"feature.enabled\"} 1\n" +
                "my_application_state{data_center=\"us east\",my_application_state=\"is.alpha.version\"} 0\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"my_application_state\" " +
                "help: \"My application state\" " +
                "type: GAUGE " +
                "metric { " +
                    "label { name: \"data_center\" value: \"us east\" } " +
                    "label { name: \"my_application_state\" value: \"feature.enabled\" } " +
                    "gauge { value: 1.0 } " +
                "} metric { " +
                    "label { name: \"data_center\" value: \"us east\" } " +
                    "label { name: \"my_application_state\" value: \"is.alpha.version\" } " +
                    "gauge { value: 0.0 } " +
                "}";
                //@formatter:on
        StateSetSnapshot stateSet = StateSetSnapshot.newBuilder()
                .withName("my.application.state")
                .withHelp("My application state")
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("data.center", "us east"))
                        .addState("feature.enabled", true)
                        .addState("is.alpha.version", false)
                        .build())
                .build();
        assertOpenMetricsText(openMetricsText, stateSet);
        assertPrometheusText(prometheusText, stateSet);
        assertPrometheusProtobuf(prometheusProtobuf, stateSet);
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
                .addDataPoint(UnknownDataPointSnapshot.newBuilder()
                        .withValue(0.7)
                        .withLabels(Labels.of("env", "prod"))
                        .withExemplar(exemplar2)
                        .withScrapeTimestampMillis(scrapeTimestamp2)
                        .build())
                .addDataPoint(UnknownDataPointSnapshot.newBuilder()
                        .withValue(0.2)
                        .withLabels(Labels.of("env", "dev"))
                        .withExemplar(exemplar1)
                        .withScrapeTimestampMillis(scrapeTimestamp1)
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, unknown);
        assertPrometheusText(prometheus, unknown);
        assertOpenMetricsTextWithoutCreated(openMetrics, unknown);
        assertPrometheusTextWithoutCreated(prometheus, unknown);
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
                .addDataPoint(UnknownDataPointSnapshot.newBuilder()
                        .withValue(22.3)
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, unknown);
        assertPrometheusText(prometheus, unknown);
        assertOpenMetricsTextWithoutCreated(openMetrics, unknown);
        assertPrometheusTextWithoutCreated(prometheus, unknown);
    }

    @Test
    public void testUnknownWithDots() throws IOException {
        String openMetrics = "" +
                "# TYPE some_unknown_metric unknown\n" +
                "# UNIT some_unknown_metric bytes\n" +
                "# HELP some_unknown_metric help message\n" +
                "some_unknown_metric{test_env=\"7\"} 0.7 # " + exemplarWithDotsString + "\n" +
                "# EOF\n";
        String prometheus = "" +
                "# HELP some_unknown_metric help message\n" +
                "# TYPE some_unknown_metric untyped\n" +
                "some_unknown_metric{test_env=\"7\"} 0.7\n";
        String prometheusProtobuf = "" +
                //@formatter:off
                "name: \"some_unknown_metric\" " +
                "help: \"help message\" " +
                "type: UNTYPED " +
                "metric { " +
                    "label { name: \"test_env\" value: \"7\" } " +
                    "untyped { value: 0.7 } " +
                "}";
                //@formatter:on
        UnknownSnapshot unknown = UnknownSnapshot.newBuilder()
                .withName("some.unknown.metric")
                .withHelp("help message")
                .withUnit(Unit.BYTES)
                .addDataPoint(UnknownDataPointSnapshot.newBuilder()
                        .withValue(0.7)
                        .withLabels(Labels.of("test.env", "7"))
                        .withExemplar(exemplarWithDots)
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, unknown);
        assertPrometheusText(prometheus, unknown);
        assertPrometheusProtobuf(prometheusProtobuf, unknown);
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
                .addDataPoint(CounterDataPointSnapshot.newBuilder().withValue(1.0).build())
                .build();
        assertOpenMetricsText(openMetrics, counter);
        assertPrometheusText(prometheus, counter);
        assertOpenMetricsTextWithoutCreated(openMetrics, counter);
        assertPrometheusTextWithoutCreated(prometheus, counter);
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
                .addDataPoint(CounterDataPointSnapshot.newBuilder()
                        // example from https://openMetrics.io
                        .withLabels(Labels.of("a", "x", "b", "escaping\" example \n "))
                        .withValue(1.0)
                        .build())
                .build();
        assertOpenMetricsText(openMetrics, counter);
        assertPrometheusText(prometheus, counter);
    }

    private void assertOpenMetricsText(String expected, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(expected, out.toString());
    }

    private void assertOpenMetricsTextWithoutCreated(String expected, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(false, true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(expected, out.toString());
    }

    private void assertPrometheusText(String expected, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrometheusTextFormatWriter writer = new PrometheusTextFormatWriter(true);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(expected, out.toString());
    }

    private void assertPrometheusTextWithoutCreated(String expected, MetricSnapshot snapshot) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrometheusTextFormatWriter writer = new PrometheusTextFormatWriter(false);
        writer.write(out, MetricSnapshots.of(snapshot));
        Assert.assertEquals(expected, out.toString());
    }

    private void assertPrometheusProtobuf(String expected, MetricSnapshot snapshot) {
        PrometheusProtobufWriter writer = new PrometheusProtobufWriter();
        Metrics.MetricFamily protobufData = writer.convert(snapshot);
        String actual = TextFormat.printer().shortDebugString(protobufData);
        Assert.assertEquals(expected, actual);
    }
}
