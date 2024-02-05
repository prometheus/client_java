package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.*;

public class PrometheusNamingTest {

    @Test
    public void testSanitizeMetricName() {
        nameValidationScheme = ValidationScheme.LegacyValidation;
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
        PrometheusNaming.nameValidationScheme = ValidationScheme.LegacyValidation;
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
        PrometheusNaming.nameValidationScheme = ValidationScheme.LegacyValidation;
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
        PrometheusNaming.nameValidationScheme = ValidationScheme.UTF8Validation;
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
    }

    @Test
    public void testLabelNameIsValid() {
        PrometheusNaming.nameValidationScheme = ValidationScheme.LegacyValidation;
        Assert.assertTrue(isValidLabelName("Avalid_23name"));
        Assert.assertTrue(isValidLabelName("_Avalid_23name"));
        Assert.assertFalse(isValidLabelName("1valid_23name"));
        Assert.assertTrue(isValidLabelName("avalid_23name"));
        Assert.assertFalse(isValidLabelName("Ava:lid_23name"));
        Assert.assertFalse(isValidLabelName("a lid_23name"));
        Assert.assertFalse(isValidLabelName(":leading_colon"));
        Assert.assertFalse(isValidLabelName("colon:in:the:middle"));
        Assert.assertFalse(isValidLabelName("a\ud800z"));
        PrometheusNaming.nameValidationScheme = ValidationScheme.UTF8Validation;
        Assert.assertTrue(isValidLabelName("Avalid_23name"));
        Assert.assertTrue(isValidLabelName("_Avalid_23name"));
        Assert.assertTrue(isValidLabelName("1valid_23name"));
        Assert.assertTrue(isValidLabelName("avalid_23name"));
        Assert.assertTrue(isValidLabelName("Ava:lid_23name"));
        Assert.assertTrue(isValidLabelName("a lid_23name"));
        Assert.assertTrue(isValidLabelName(":leading_colon"));
        Assert.assertTrue(isValidLabelName("colon:in:the:middle"));
        Assert.assertFalse(isValidLabelName("a\ud800z"));
    }
}
