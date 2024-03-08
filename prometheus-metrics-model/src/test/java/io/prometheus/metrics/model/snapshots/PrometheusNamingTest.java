package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.*;

public class PrometheusNamingTest {

    @Test
    public void testSanitizeMetricName() {
        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
        Assert.assertEquals("_abc_def", prometheusName(sanitizeMetricName("0abc.def")));
        Assert.assertEquals("___ab_:c0", prometheusName(sanitizeMetricName("___ab.:c0")));
        Assert.assertEquals("my_prefix_my_metric", sanitizeMetricName("my_prefix/my_metric"));
        Assert.assertEquals("my_counter", prometheusName(sanitizeMetricName("my_counter_total")));
        Assert.assertEquals("jvm", sanitizeMetricName("jvm.info"));
        Assert.assertEquals("jvm", sanitizeMetricName("jvm_info"));
        Assert.assertEquals("jvm", sanitizeMetricName("jvm.info"));
        Assert.assertEquals("a.b", sanitizeMetricName("a.b"));
    }

    @Test
    public void testSanitizeLabelName() {
        PrometheusNaming.nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
        Assert.assertEquals("_abc_def", prometheusName(sanitizeLabelName("0abc.def")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("_abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("__abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("___abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("_.abc")));
        Assert.assertEquals("abc.def", sanitizeLabelName("abc.def"));
        Assert.assertEquals("abc.def2", sanitizeLabelName("abc.def2"));
    }

    @Test
    public void testMetricNameIsValid() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
        Assert.assertNull(validateMetricName("Avalid_23name"));
        Assert.assertNull(validateMetricName("_Avalid_23name"));
        Assert.assertNull(validateMetricName("1valid_23name"));
        Assert.assertNull(validateMetricName("avalid_23name"));
        Assert.assertNull(validateMetricName("Ava:lid_23name"));
        Assert.assertNull(validateMetricName("a lid_23name"));
        Assert.assertNull(validateMetricName(":leading_colon"));
        Assert.assertNull(validateMetricName("colon:in:the:middle"));
        Assert.assertEquals("The metric name contains unsupported characters", validateMetricName(""));
        Assert.assertEquals("The metric name contains unsupported characters", validateMetricName("a\ud800z"));
        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
        Assert.assertNull(validateMetricName("Avalid_23name"));
        Assert.assertNull(validateMetricName("_Avalid_23name"));
        Assert.assertEquals("The metric name contains unsupported characters", validateMetricName("1valid_23name"));
        Assert.assertNull(validateMetricName("avalid_23name"));
        Assert.assertNull(validateMetricName("Ava:lid_23name"));
        Assert.assertEquals("The metric name contains unsupported characters", validateMetricName("a lid_23name"));
        Assert.assertNull(validateMetricName(":leading_colon"));
        Assert.assertNull(validateMetricName("colon:in:the:middle"));
        Assert.assertEquals("The metric name contains unsupported characters", validateMetricName(""));
        Assert.assertEquals("The metric name contains unsupported characters", validateMetricName("a\ud800z"));
    }

    @Test
    public void testLabelNameIsValid() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
        Assert.assertTrue(isValidLabelName("Avalid_23name"));
        Assert.assertTrue(isValidLabelName("_Avalid_23name"));
        Assert.assertTrue(isValidLabelName("1valid_23name"));
        Assert.assertTrue(isValidLabelName("avalid_23name"));
        Assert.assertTrue(isValidLabelName("Ava:lid_23name"));
        Assert.assertTrue(isValidLabelName("a lid_23name"));
        Assert.assertTrue(isValidLabelName(":leading_colon"));
        Assert.assertTrue(isValidLabelName("colon:in:the:middle"));
        Assert.assertFalse(isValidLabelName("a\ud800z"));
        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
        Assert.assertTrue(isValidLabelName("Avalid_23name"));
        Assert.assertTrue(isValidLabelName("_Avalid_23name"));
        Assert.assertFalse(isValidLabelName("1valid_23name"));
        Assert.assertTrue(isValidLabelName("avalid_23name"));
        Assert.assertFalse(isValidLabelName("Ava:lid_23name"));
        Assert.assertFalse(isValidLabelName("a lid_23name"));
        Assert.assertFalse(isValidLabelName(":leading_colon"));
        Assert.assertFalse(isValidLabelName("colon:in:the:middle"));
        Assert.assertFalse(isValidLabelName("a\ud800z"));
    }

    @Test
    public void testEscapeName() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;

        // empty string
        String got = escapeName("", EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("", got);
        got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("", got);

        got = escapeName("", EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("", got);
        got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("", got);

        got = escapeName("", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("", got);
        got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("", got);

        // legacy valid name
        got = escapeName("no:escaping_required", EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("no:escaping_required", got);
        got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("no:escaping_required", got);

        got = escapeName("no:escaping_required", EscapingScheme.DOTS_ESCAPING);
        // Dots escaping will escape underscores even though it's not strictly
        // necessary for compatibility.
        Assert.assertEquals("no:escaping__required", got);
        got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("no:escaping_required", got);

        got = escapeName("no:escaping_required", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("no:escaping_required", got);
        got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("no:escaping_required", got);

        // name with dots
        got = escapeName("mysystem.prod.west.cpu.load", EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("mysystem_prod_west_cpu_load", got);
        got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("mysystem_prod_west_cpu_load", got);

        got = escapeName("mysystem.prod.west.cpu.load", EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("mysystem_dot_prod_dot_west_dot_cpu_dot_load", got);
        got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("mysystem.prod.west.cpu.load", got);

        got = escapeName("mysystem.prod.west.cpu.load", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__mysystem_2e_prod_2e_west_2e_cpu_2e_load", got);
        got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("mysystem.prod.west.cpu.load", got);

        // name with dots and colon
        got = escapeName("http.status:sum", EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("http_status:sum", got);
        got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("http_status:sum", got);

        got = escapeName("http.status:sum", EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("http_dot_status:sum", got);
        got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("http.status:sum", got);

        got = escapeName("http.status:sum", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__http_2e_status:sum", got);
        got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("http.status:sum", got);

        // name with unicode characters > 0x100
        got = escapeName("花火", EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("__", got);
        got = unescapeName(got, EscapingScheme.UNDERSCORE_ESCAPING);
        Assert.assertEquals("__", got);

        got = escapeName("花火", EscapingScheme.DOTS_ESCAPING);
        Assert.assertEquals("__", got);
        got = unescapeName(got, EscapingScheme.DOTS_ESCAPING);
        // Dots-replacement does not know the difference between two replaced
        // characters and a single underscore.
        Assert.assertEquals("_", got);

        got = escapeName("花火", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U___82b1__706b_", got);
        got = unescapeName(got, EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("花火", got);

        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }

    @Test
    public void testValueUnescapeErrors() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
        String got;

        // empty string
        got = unescapeName("", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("", got);

        // basic case, no error
        got = unescapeName("U__no:unescapingrequired", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("no:unescapingrequired", got);

        // capitals ok, no error
        got = unescapeName("U__capitals_2E_ok", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("capitals.ok", got);

        // underscores, no error
        got = unescapeName("U__underscores__doubled__", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("underscores_doubled_", got);

        // invalid single underscore
        got = unescapeName("U__underscores_doubled_", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__underscores_doubled_", got);

        // invalid single underscore, 2
        got = unescapeName("U__underscores__doubled_", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__underscores__doubled_", got);

        // giant fake UTF-8 code
        got = unescapeName("U__my__hack_2e_attempt_872348732fabdabbab_", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__my__hack_2e_attempt_872348732fabdabbab_", got);

        // trailing UTF-8
        got = unescapeName("U__my__hack_2e", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__my__hack_2e", got);

        // invalid UTF-8 value
        got = unescapeName("U__bad__utf_2eg_", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__bad__utf_2eg_", got);

        // surrogate UTF-8 value
        got = unescapeName("U__bad__utf_D900_", EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("U__bad__utf_D900_", got);

        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }

    @Test
    public void testEscapeMetricSnapshotEmpty() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
        MetricSnapshot original = CounterSnapshot.builder().name("empty").build();
        MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);
        Assert.assertEquals("empty", got.getMetadata().getName());
        Assert.assertEquals("empty", original.getMetadata().getName());
        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }

    @Test
    public void testEscapeMetricSnapshotSimpleNoEscapingNeeded() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
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

        Assert.assertEquals("my_metric", got.getMetadata().getName());
        Assert.assertEquals("some help text", got.getMetadata().getHelp());
        Assert.assertEquals(1, got.getDataPoints().size());
        CounterSnapshot.CounterDataPointSnapshot data = (CounterSnapshot.CounterDataPointSnapshot) got.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "my_metric")
                .label("some_label", "labelvalue")
                .build(), data.getLabels());
        Assert.assertEquals("my_metric", original.getMetadata().getName());
        Assert.assertEquals("some help text", original.getMetadata().getHelp());
        Assert.assertEquals(1, original.getDataPoints().size());
        data = (CounterSnapshot.CounterDataPointSnapshot) original.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "my_metric")
                .label("some_label", "labelvalue")
                .build(), data.getLabels());

        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }

    @Test
    public void testEscapeMetricSnapshotLabelNameEscapingNeeded() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
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

        Assert.assertEquals("my_metric", got.getMetadata().getName());
        Assert.assertEquals("some help text", got.getMetadata().getHelp());
        Assert.assertEquals(1, got.getDataPoints().size());
        CounterSnapshot.CounterDataPointSnapshot data = (CounterSnapshot.CounterDataPointSnapshot) got.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "my_metric")
                .label("U__some_2e_label", "labelvalue")
                .build(), data.getLabels());
        Assert.assertEquals("my_metric", original.getMetadata().getName());
        Assert.assertEquals("some help text", original.getMetadata().getHelp());
        Assert.assertEquals(1, original.getDataPoints().size());
        data = (CounterSnapshot.CounterDataPointSnapshot) original.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "my_metric")
                .label("some.label", "labelvalue")
                .build(), data.getLabels());

        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }

    @Test
    public void testEscapeMetricSnapshotCounterEscapingNeeded() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
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

        Assert.assertEquals("U__my_2e_metric", got.getMetadata().getName());
        Assert.assertEquals("some help text", got.getMetadata().getHelp());
        Assert.assertEquals(1, got.getDataPoints().size());
        CounterSnapshot.CounterDataPointSnapshot data = (CounterSnapshot.CounterDataPointSnapshot) got.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "U__my_2e_metric")
                .label("U__some_3f_label", "label??value")
                .build(), data.getLabels());
        Assert.assertEquals("my.metric", original.getMetadata().getName());
        Assert.assertEquals("some help text", original.getMetadata().getHelp());
        Assert.assertEquals(1, original.getDataPoints().size());
        data = (CounterSnapshot.CounterDataPointSnapshot) original.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "my.metric")
                .label("some?label", "label??value")
                .build(), data.getLabels());

        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }

    @Test
    public void testEscapeMetricSnapshotGaugeEscapingNeeded() {
        nameValidationScheme = ValidationScheme.UTF_8_VALIDATION;
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

        Assert.assertEquals("unicode_dot_and_dot_dots_dot___", got.getMetadata().getName());
        Assert.assertEquals("some help text", got.getMetadata().getHelp());
        Assert.assertEquals(1, got.getDataPoints().size());
        GaugeSnapshot.GaugeDataPointSnapshot data = (GaugeSnapshot.GaugeDataPointSnapshot) got.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "unicode_dot_and_dot_dots_dot___")
                .label("some_label", "label??value")
                .build(), data.getLabels());
        Assert.assertEquals("unicode.and.dots.花火", original.getMetadata().getName());
        Assert.assertEquals("some help text", original.getMetadata().getHelp());
        Assert.assertEquals(1, original.getDataPoints().size());
        data = (GaugeSnapshot.GaugeDataPointSnapshot) original.getDataPoints().get(0);
        Assert.assertEquals(34.2, data.getValue(), 0.0);
        Assert.assertEquals(Labels.builder()
                .label("__name__", "unicode.and.dots.花火")
                .label("some_label", "label??value")
                .build(), data.getLabels());

        nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;
    }
}
