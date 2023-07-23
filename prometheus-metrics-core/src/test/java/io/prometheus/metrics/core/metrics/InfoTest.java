package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.com_google_protobuf_3_21_7.TextFormat;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_21_7.Metrics;
import org.junit.Test;

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
                info.infoLabelValues("value");
                Metrics.MetricFamily protobufData = new PrometheusProtobufWriter().convert(info.collect());
                assertEquals("name: \"jvm_runtime_info\" type: GAUGE metric { label { name: \"my_key\" value: \"value\" } gauge { value: 1.0 } }", TextFormat.printer().shortDebugString(protobufData));
            }
        }
    }
}
