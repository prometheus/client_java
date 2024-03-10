package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.shaded.com_google_protobuf_3_21_7.TextFormat;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_21_7.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InfoTest {

    @Test
    void testInfoStrippedFromName() {
        for (String name : new String[]{
                "jvm.runtime", "jvm_runtime",
                "jvm.runtime.info", "jvm_runtime_info"}) {
            for (String labelName : new String[]{"my.key", "my_key"}) {
                Info info = Info.builder()
                        .name(name)
                        .labelNames(labelName)
                        .build();
                info.addLabelValues("value");
                Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(info.collect());
                assertEquals("name: \"jvm_runtime_info\" type: GAUGE metric { label { name: \"my_key\" value: \"value\" } gauge { value: 1.0 } }", TextFormat.printer().shortDebugString(protobufData));
            }
        }
    }

    @Test
    void testAddAndRemove() throws IOException {
        Info info = Info.builder()
                .name("test_info")
                .labelNames("a", "b")
                .build();
        Assertions.assertEquals(0, info.collect().getDataPoints().size());
        info.addLabelValues("val1", "val2");
        Assertions.assertEquals(1, info.collect().getDataPoints().size());
        info.addLabelValues("val1", "val2"); // already exist, so no change
        Assertions.assertEquals(1, info.collect().getDataPoints().size());
        info.addLabelValues("val2", "val2");
        Assertions.assertEquals(2, info.collect().getDataPoints().size());
        info.remove("val1", "val3"); // does not exist, so no change
        Assertions.assertEquals(2, info.collect().getDataPoints().size());
        info.remove("val1", "val2");
        Assertions.assertEquals(1, info.collect().getDataPoints().size());
        info.remove("val2", "val2");
        Assertions.assertEquals(0, info.collect().getDataPoints().size());
    }

    @Test
    void testSet() throws IOException {
        Info info = Info.builder()
                .name("target_info")
                .constLabels(Labels.of("service.name", "test", "service.instance.id", "123"))
                .labelNames("service.version")
                .build();
        info.setLabelValues("1.0.0");
        Assertions.assertEquals(1, info.collect().getDataPoints().size());
        info.setLabelValues("2.0.0");
        Assertions.assertEquals(1, info.collect().getDataPoints().size());
        assertTextFormat("target_info{service_instance_id=\"123\",service_name=\"test\",service_version=\"2.0.0\"} 1\n", info);
    }

    @Test
    void testConstLabelsOnly() throws IOException {
        Info info = Info.builder()
                .name("target_info")
                .constLabels(Labels.of("service.name", "test", "service.instance.id", "123"))
                .build();
        Assertions.assertEquals(1, info.collect().getDataPoints().size());
        assertTextFormat("target_info{service_instance_id=\"123\",service_name=\"test\"} 1\n", info);
    }

    @Test
    void testConstLabelsDuplicate1() {
        assertThrows(IllegalArgumentException.class,
                () -> Info.builder()
                        .constLabels(Labels.of("a_1", "val1"))
                        .labelNames("a.1")
                        .build());
    }

    @Test
    void testConstLabelsDuplicate2() {
        assertThrows(IllegalArgumentException.class,
                () -> Info.builder()
                        .labelNames("a_1")
                        .constLabels(Labels.of("a.1", "val1"))
                        .build());
    }

    private void assertTextFormat(String expected, Info info) throws IOException {
        OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.write(outputStream, MetricSnapshots.of(info.collect()));
        String result = outputStream.toString(StandardCharsets.UTF_8.name());
        if (!result.contains(expected)) {
            throw new AssertionError(expected + " is not contained in the following output:\n" + result);
        }
    }
}
