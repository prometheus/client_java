package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import java.util.Optional;

class PrometheusNamingTest {

  @Test
  public void testSanitizeMetricName() {
    // nameValidationScheme is now final and initialized from properties
    assertThat(prometheusName(sanitizeMetricName("0abc.def"))).isEqualTo("_abc_def");
    assertThat(prometheusName(sanitizeMetricName("___ab.:c0"))).isEqualTo("___ab__c0");
    assertThat(sanitizeMetricName("my_prefix/my_metric")).isEqualTo("my_prefix_my_metric");
    assertThat(prometheusName(sanitizeMetricName("my_counter_total"))).isEqualTo("my_counter");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("jvm_info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("a.b")).isEqualTo("a.b");
    assertThat(sanitizeMetricName("_total")).isEqualTo("total");
    assertThat(sanitizeMetricName("total")).isEqualTo("total");
  }

  @Test
  public void testSanitizeMetricNameWithUnit() {
    
    assertThat(prometheusName(sanitizeMetricName("0abc.def", Unit.RATIO)))
        .isEqualTo("_abc_def_" + Unit.RATIO);
    assertThat(prometheusName(sanitizeMetricName("___ab.:c0", Unit.RATIO)))
        .isEqualTo("___ab__c0_" + Unit.RATIO);
    assertThat(sanitizeMetricName("my_prefix/my_metric", Unit.RATIO))
        .isEqualTo("my_prefix_my_metric_" + Unit.RATIO);
    assertThat(prometheusName(sanitizeMetricName("my_counter_total", Unit.RATIO)))
        .isEqualTo("my_counter_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm.info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm_info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm.info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("a.b", Unit.RATIO)).isEqualTo("a.b_" + Unit.RATIO);
    assertThat(sanitizeMetricName("_total", Unit.RATIO)).isEqualTo("total_" + Unit.RATIO);
    assertThat(sanitizeMetricName("total", Unit.RATIO)).isEqualTo("total_" + Unit.RATIO);
  }

  @Test
  public void testSanitizeLabelName() {
    
    assertThat(prometheusName(sanitizeLabelName("0abc.def"))).isEqualTo("_abc_def");
    assertThat(prometheusName(sanitizeLabelName("_abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("__abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("___abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("_.abc"))).isEqualTo("_abc");
    assertThat(sanitizeLabelName("abc.def")).isEqualTo("abc.def");
    assertThat(sanitizeLabelName("abc.def2")).isEqualTo("abc.def2");
  }

  @Test
  public void testValidateUnitName() {
    assertThat(validateUnitName("secondstotal")).isNotNull();
    assertThat(validateUnitName("total")).isNotNull();
    assertThat(validateUnitName("seconds_total")).isNotNull();
    assertThat(validateUnitName("_total")).isNotNull();
    assertThat(validateUnitName("")).isNotNull();

    assertThat(validateUnitName("seconds")).isNull();
    assertThat(validateUnitName("2")).isNull();
  }

  @Test
  public void testSanitizeUnitName() {
    assertThat(sanitizeUnitName("seconds")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("seconds_total")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("seconds_total_total")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("m/s")).isEqualTo("m_s");
    assertThat(sanitizeUnitName("secondstotal")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("2")).isEqualTo("2");
  }

  @Test
  public void testInvalidUnitName1() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("total"));
  }

  @Test
  public void testInvalidUnitName2() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("_total"));
  }

  @Test
  public void testInvalidUnitName3() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("%"));
  }

  @Test
  public void testEmptyUnitName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName(""));
  }

  @Test
  public void testMetricNameIsValid() {
    // nameValidationScheme is now final and initialized from properties (defaults to LEGACY_VALIDATION)
    assertThat(validateMetricName("Avalid_23name")).isNull();
    assertThat(validateMetricName("_Avalid_23name")).isNull();
    assertThat(validateMetricName("1valid_23name")).isEqualTo("The metric name contains unsupported characters");
    assertThat(validateMetricName("avalid_23name")).isNull();
    assertThat(validateMetricName("Ava:lid_23name")).isNull();
    assertThat(validateMetricName("a lid_23name")).isEqualTo("The metric name contains unsupported characters");
    assertThat(validateMetricName(":leading_colon")).isNull();
    assertThat(validateMetricName("colon:in:the:middle")).isNull();
    assertThat(validateMetricName("")).isEqualTo("The metric name contains unsupported characters");
    assertThat(validateMetricName("a\ud800z")).isEqualTo("The metric name contains unsupported characters");
  }

  @Test
  public void testLabelNameIsValid() {
    // nameValidationScheme is now final and initialized from properties
    assertThat(isValidLabelName("Avalid_23name")).isTrue();
    assertThat(isValidLabelName("_Avalid_23name")).isTrue();
    assertThat(isValidLabelName("1valid_23name")).isTrue();
    assertThat(isValidLabelName("avalid_23name")).isTrue();
    assertThat(isValidLabelName("Ava:lid_23name")).isTrue();
    assertThat(isValidLabelName("a lid_23name")).isTrue();
    assertThat(isValidLabelName(":leading_colon")).isTrue();
    assertThat(isValidLabelName("colon:in:the:middle")).isTrue();
    assertThat(isValidLabelName("a\ud800z")).isFalse();
    
    assertThat(isValidLabelName("Avalid_23name")).isTrue();
    assertThat(isValidLabelName("_Avalid_23name")).isTrue();
    assertThat(isValidLabelName("1valid_23name")).isFalse();
    assertThat(isValidLabelName("avalid_23name")).isTrue();
    assertThat(isValidLabelName("Ava:lid_23name")).isFalse();
    assertThat(isValidLabelName("a lid_23name")).isFalse();
    assertThat(isValidLabelName(":leading_colon")).isFalse();
    assertThat(isValidLabelName("colon:in:the:middle")).isFalse();
    assertThat(isValidLabelName("a\ud800z")).isFalse();
  }

  @Test
  public void testEscapeName() {
    // nameValidationScheme is now final and initialized from properties

    // empty string
    String got = escapeName("", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("");

    got = escapeName("", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("");

    got = escapeName("", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("");

    // legacy valid name
    got = escapeName("no:escaping_required", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("no:escaping_required");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("no:escaping_required");

    got = escapeName("no:escaping_required", EscapingScheme.DOTS_ESCAPING);
    // Dots escaping will escape underscores even though it's not strictly
    // necessary for compatibility.
    assertThat(got).isEqualTo("no:escaping__required");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("no:escaping_required");

    got = escapeName("no:escaping_required", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("no:escaping_required");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("no:escaping_required");

    // name with dots
    got = escapeName("mysystem.prod.west.cpu.load", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("mysystem_prod_west_cpu_load");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("mysystem_prod_west_cpu_load");

    got = escapeName("mysystem.prod.west.cpu.load", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("mysystem_dot_prod_dot_west_dot_cpu_dot_load");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("mysystem.prod.west.cpu.load");

    got = escapeName("mysystem.prod.west.cpu.load", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__mysystem_2e_prod_2e_west_2e_cpu_2e_load");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("mysystem.prod.west.cpu.load");

    // name with dots and underscore
    got = escapeName("mysystem.prod.west.cpu.load_total", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("mysystem_prod_west_cpu_load_total");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("mysystem_prod_west_cpu_load_total");

    got = escapeName("mysystem.prod.west.cpu.load_total", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("mysystem_dot_prod_dot_west_dot_cpu_dot_load__total");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("mysystem.prod.west.cpu.load_total");

    got = escapeName("mysystem.prod.west.cpu.load_total", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__mysystem_2e_prod_2e_west_2e_cpu_2e_load__total");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("mysystem.prod.west.cpu.load_total");

    // name with dots and colon
    got = escapeName("http.status:sum", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("http_status:sum");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("http_status:sum");

    got = escapeName("http.status:sum", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("http_dot_status:sum");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("http.status:sum");

    got = escapeName("http.status:sum", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__http_2e_status:sum");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("http.status:sum");

    // name with spaces and emoji
    got = escapeName("label with 😱", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("label_with__");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("label_with__");

    got = escapeName("label with 😱", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("label__with____");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("label_with__");

    got = escapeName("label with 😱", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__label_20_with_20__1f631_");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("label with 😱");

    // name with unicode characters > 0x100
    got = escapeName("花火", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("__");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("__");

    got = escapeName("花火", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("____");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    // Dots-replacement does not know the difference between two replaced
    // characters and a single underscore.
    assertThat(got).isEqualTo("__");

    got = escapeName("花火", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U___82b1__706b_");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("花火");

    // name with spaces and edge-case value
    got = escapeName("label with Ā", EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("label_with__");
    got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(got).isEqualTo("label_with__");

    got = escapeName("label with Ā", EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("label__with____");
    got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
    assertThat(got).isEqualTo("label_with__");

    got = escapeName("label with Ā", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__label_20_with_20__100_");
    got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("label with Ā");

    
  }

  @Test
  public void testValueUnescapeErrors() {
    // nameValidationScheme is now final and initialized from properties
    String got;

    // empty string
    got = unescapeName("", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("");

    // basic case, no error
    got = unescapeName("U__no:unescapingrequired", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("no:unescapingrequired");

    // capitals ok, no error
    got = unescapeName("U__capitals_2E_ok", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("capitals.ok");

    // underscores, no error
    got = unescapeName("U__underscores__doubled__", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("underscores_doubled_");

    // invalid single underscore
    got = unescapeName("U__underscores_doubled_", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__underscores_doubled_");

    // invalid single underscore, 2
    got = unescapeName("U__underscores__doubled_", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__underscores__doubled_");

    // giant fake UTF-8 code
    got = unescapeName("U__my__hack_2e_attempt_872348732fabdabbab_", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__my__hack_2e_attempt_872348732fabdabbab_");

    // trailing UTF-8
    got = unescapeName("U__my__hack_2e", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__my__hack_2e");

    // invalid UTF-8 value
    got = unescapeName("U__bad__utf_2eg_", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__bad__utf_2eg_");

    // surrogate UTF-8 value
    got = unescapeName("U__bad__utf_D900_", EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got).isEqualTo("U__bad__utf_D900_");

    
  }

  @Test
  public void testEscapeMetricSnapshotEmpty() {
    // nameValidationScheme is now final and initialized from properties
    MetricSnapshot original = CounterSnapshot.builder().name("empty").build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got.getMetadata().getName()).isEqualTo("empty");
    assertThat(original.getMetadata().getName()).isEqualTo("empty");
    
  }

  @Test
  public void testEscapeMetricSnapshotSimpleNoEscapingNeeded() {
    // nameValidationScheme is now final and initialized from properties
    MetricSnapshot original = CounterSnapshot.builder()
      .name("my_metric")
      .help("some help text")
      .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder()
        .value(34.2)
        .labels(Labels.builder()
          .label("__name__", "my_metric")
          .label("some_label", "labelvalue")
          .build())
        .build()
      )
      .build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);

    assertThat(got.getMetadata().getName()).isEqualTo("my_metric");
    assertThat(got.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(got.getDataPoints().size()).isEqualTo(1);
    CounterSnapshot.CounterDataPointSnapshot data = (CounterSnapshot.CounterDataPointSnapshot) got.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "my_metric")
      .label("some_label", "labelvalue")
      .build());
    assertThat(original.getMetadata().getName()).isEqualTo("my_metric");
    assertThat(original.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(original.getDataPoints().size()).isEqualTo(1);
    data = (CounterSnapshot.CounterDataPointSnapshot) original.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "my_metric")
      .label("some_label", "labelvalue")
      .build());

    
  }

  @Test
  public void testEscapeMetricSnapshotLabelNameEscapingNeeded() {
    // nameValidationScheme is now final and initialized from properties
    MetricSnapshot original = CounterSnapshot.builder()
      .name("my_metric")
      .help("some help text")
      .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder()
        .value(34.2)
        .labels(Labels.builder()
          .label("__name__", "my_metric")
          .label("some.label", "labelvalue")
          .build())
        .build()
      )
      .build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);

    assertThat(got.getMetadata().getName()).isEqualTo("my_metric");
    assertThat(got.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(got.getDataPoints().size()).isEqualTo(1);
    CounterSnapshot.CounterDataPointSnapshot data = (CounterSnapshot.CounterDataPointSnapshot) got.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "my_metric")
      .label("U__some_2e_label", "labelvalue")
      .build());
    assertThat(original.getMetadata().getName()).isEqualTo("my_metric");
    assertThat(original.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(original.getDataPoints().size()).isEqualTo(1);
    data = (CounterSnapshot.CounterDataPointSnapshot) original.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "my_metric")
      .label("some.label", "labelvalue")
      .build());

    
  }

  @Test
  public void testEscapeMetricSnapshotCounterEscapingNeeded() {
    // nameValidationScheme is now final and initialized from properties
    MetricSnapshot original = CounterSnapshot.builder()
      .name("my.metric")
      .help("some help text")
      .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder()
        .value(34.2)
        .labels(Labels.builder()
          .label("__name__", "my.metric")
          .label("some?label", "label??value")
          .build())
        .build()
      )
      .build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);

    assertThat(got.getMetadata().getName()).isEqualTo("U__my_2e_metric");
    assertThat(got.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(got.getDataPoints().size()).isEqualTo(1);
    CounterSnapshot.CounterDataPointSnapshot data = (CounterSnapshot.CounterDataPointSnapshot) got.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "U__my_2e_metric")
      .label("U__some_3f_label", "label??value")
      .build());
    assertThat(original.getMetadata().getName()).isEqualTo("my.metric");
    assertThat(original.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(original.getDataPoints().size()).isEqualTo(1);
    data = (CounterSnapshot.CounterDataPointSnapshot) original.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "my.metric")
      .label("some?label", "label??value")
      .build());

    
  }

  @Test
  public void testEscapeMetricSnapshotGaugeEscapingNeeded() {
    // nameValidationScheme is now final and initialized from properties
    MetricSnapshot original = GaugeSnapshot.builder()
      .name("unicode.and.dots.花火")
      .help("some help text")
      .dataPoint(GaugeSnapshot.GaugeDataPointSnapshot.builder()
        .value(34.2)
        .labels(Labels.builder()
          .label("__name__", "unicode.and.dots.花火")
          .label("some_label", "label??value")
          .build())
        .build()
      )
      .build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.DOTS_ESCAPING);

    assertThat(got.getMetadata().getName()).isEqualTo("unicode_dot_and_dot_dots_dot_____");
    assertThat(got.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(got.getDataPoints().size()).isEqualTo(1);
    GaugeSnapshot.GaugeDataPointSnapshot data = (GaugeSnapshot.GaugeDataPointSnapshot) got.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "unicode_dot_and_dot_dots_dot_____")
      .label("some_label", "label??value")
      .build());
    assertThat(original.getMetadata().getName()).isEqualTo("unicode.and.dots.花火");
    assertThat(original.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(original.getDataPoints().size()).isEqualTo(1);
    data = (GaugeSnapshot.GaugeDataPointSnapshot) original.getDataPoints().get(0);
    assertThat(data.getValue()).isEqualTo(34.2);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.builder()
      .label("__name__", "unicode.and.dots.花火")
      .label("some_label", "label??value")
      .build());

    
  }
}
