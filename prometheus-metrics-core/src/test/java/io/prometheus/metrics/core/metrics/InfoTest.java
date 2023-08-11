package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.shaded.com_google_protobuf_3_21_7.TextFormat;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_21_7.Metrics;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class InfoTest {

    @Test
    public void testInfoStrippedFromName() {
        for (String name : new String[]{
                "jvm.runtime", "jvm_runtime",
                "jvm.runtime.info", "jvm_runtime_info"}) {
            for (String labelName : new String[]{"my.key", "my_key"}) {
                Info info = Info.newBuilder()
                        .withName(name)
                        .withLabelNames(labelName)
                        .build();
                info.addLabelValues("value");
                Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(info.collect());
                assertEquals("name: \"jvm_runtime_info\" type: GAUGE metric { label { name: \"my_key\" value: \"value\" } gauge { value: 1.0 } }", TextFormat.printer().shortDebugString(protobufData));
            }
        }
    }

    @Test
    public void testAddAndRemove() throws IOException {
        Info info = Info.newBuilder()
                .withName("test_info")
                .withLabelNames("a", "b")
                .build();
        Assert.assertEquals(0, info.collect().getData().size());
        info.addLabelValues("val1", "val2");
        Assert.assertEquals(1, info.collect().getData().size());
        info.addLabelValues("val1", "val2"); // already exist, so no change
        Assert.assertEquals(1, info.collect().getData().size());
        info.addLabelValues("val2", "val2");
        Assert.assertEquals(2, info.collect().getData().size());
        info.remove("val1", "val3"); // does not exist, so no change
        Assert.assertEquals(2, info.collect().getData().size());
        info.remove("val1", "val2");
        Assert.assertEquals(1, info.collect().getData().size());
        info.remove("val2", "val2");
        Assert.assertEquals(0, info.collect().getData().size());
    }

    @Test
    public void testSet() throws IOException {
        Info info = Info.newBuilder()
                .withName("target_info")
                .withConstLabels(Labels.of("service.name", "test", "service.instance.id", "123"))
                .withLabelNames("service.version")
                .build();
        info.setLabelValues("1.0.0");
        Assert.assertEquals(1, info.collect().getData().size());
        info.setLabelValues("2.0.0");
        Assert.assertEquals(1, info.collect().getData().size());
        assertTextFormat("target_info{service_instance_id=\"123\",service_name=\"test\",service_version=\"2.0.0\"} 1\n", info);
    }

    @Test
    public void testConstLabelsOnly() throws IOException {
        Info info = Info.newBuilder()
                .withName("target_info")
                .withConstLabels(Labels.of("service.name", "test", "service.instance.id", "123"))
                .build();
        Assert.assertEquals(1, info.collect().getData().size());
        assertTextFormat("target_info{service_instance_id=\"123\",service_name=\"test\"} 1\n", info);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstLabelsDuplicate1() {
        Info.newBuilder()
                .withConstLabels(Labels.of("a_1", "val1"))
                .withLabelNames("a.1")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstLabelsDuplicate2() {
        Info.newBuilder()
                .withLabelNames("a_1")
                .withConstLabels(Labels.of("a.1", "val1"))
                .build();
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
