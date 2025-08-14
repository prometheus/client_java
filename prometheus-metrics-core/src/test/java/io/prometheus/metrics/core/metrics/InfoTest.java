package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_31_1.Metrics;
import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import io.prometheus.metrics.expositionformats.internal.ProtobufUtil;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InfoTest {

  @ParameterizedTest
  @ValueSource(strings = {"jvm.runtime", "jvm.runtime.info"})
  public void testInfoStrippedFromName(String name) {
    Info info = Info.builder().name(name).labelNames("my.key").build();
    info.addLabelValues("value");
    Metrics.MetricFamily protobufData =
        new PrometheusProtobufWriterImpl().convert(info.collect(), EscapingScheme.ALLOW_UTF8);
    assertThat(ProtobufUtil.shortDebugString(protobufData))
        .isEqualTo(
            "name: \"jvm.runtime_info\" type: GAUGE metric { label { name: \"my.key\" value:"
                + " \"value\" } gauge { value: 1.0 } }");
  }

  @Test
  public void testAddAndRemove() {
    Info info = Info.builder().name("test_info").labelNames("a", "b").build();
    assertThat(info.collect().getDataPoints()).isEmpty();
    info.addLabelValues("val1", "val2");
    assertThat(info.collect().getDataPoints()).hasSize(1);
    info.addLabelValues("val1", "val2"); // already exist, so no change
    assertThat(info.collect().getDataPoints()).hasSize(1);
    info.addLabelValues("val2", "val2");
    assertThat(info.collect().getDataPoints()).hasSize(2);
    info.remove("val1", "val3"); // does not exist, so no change
    assertThat(info.collect().getDataPoints()).hasSize(2);
    info.remove("val1", "val2");
    assertThat(info.collect().getDataPoints()).hasSize(1);
    info.remove("val2", "val2");
    assertThat(info.collect().getDataPoints()).isEmpty();

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> info.addLabelValues("val1", "val2", "extra"))
        .withMessage(
            "Info test was created with 2 label names, but you called addLabelValues() with 3 label"
                + " values.");
  }

  @Test
  public void testSet() throws IOException {
    Info info =
        Info.builder()
            .name("target_info")
            .constLabels(Labels.of("service.name", "test", "service.instance.id", "123"))
            .labelNames("service.version")
            .build();
    info.setLabelValues("1.0.0");
    assertThat(info.collect().getDataPoints()).hasSize(1);
    info.setLabelValues("2.0.0");
    assertThat(info.collect().getDataPoints()).hasSize(1);
    assertTextFormat(
        "target_info{service_instance_id=\"123\",service_name=\"test\",service_version=\"2.0.0\"}"
            + " 1\n",
        info);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> info.setLabelValues("2.0.0", "extra"))
        .withMessage(
            "Info target was created with 1 label names, but you called setLabelValues() with 2"
                + " label values.");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> info.remove("2.0.0", "extra"))
        .withMessage(
            "Info target was created with 1 label names, but you called remove() with 2 label"
                + " values.");
  }

  @Test
  public void testConstLabelsOnly() throws IOException {
    Info info =
        Info.builder()
            .name("target_info")
            .constLabels(Labels.of("service.name", "test", "service.instance.id", "123"))
            .build();
    assertThat(info.collect().getDataPoints()).hasSize(1);
    assertTextFormat("target_info{service_instance_id=\"123\",service_name=\"test\"} 1\n", info);
  }

  @Test
  void unit() {
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> Info.builder().unit(Unit.BYTES).build())
        .withMessage("Info metrics cannot have a unit.");
  }

  @Test
  public void testConstLabelsDuplicate1() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> Info.builder().constLabels(Labels.of("a_1", "val1")).labelNames("a.1").build());
  }

  @Test
  public void testConstLabelsDuplicate2() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> Info.builder().labelNames("a_1").constLabels(Labels.of("a.1", "val1")).build());
  }

  private void assertTextFormat(String expected, Info info) throws IOException {
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(true, true);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    writer.write(
        outputStream, MetricSnapshots.of(info.collect()), EscapingScheme.UNDERSCORE_ESCAPING);
    String result = outputStream.toString(StandardCharsets.UTF_8.name());
    if (!result.contains(expected)) {
      throw new AssertionError(expected + " is not contained in the following output:\n" + result);
    }
  }
}
