package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.*;

public class PrometheusNamingTest {

    @Test
    public void testSanitizeMetricName() {
        Assert.assertEquals("_abc_def", prometheusName(sanitizeMetricName("0abc.def")));
        Assert.assertEquals("___ab__c0", prometheusName(sanitizeMetricName("___ab.:c0")));
        Assert.assertEquals("my_prefix_my_metric", sanitizeMetricName("my_prefix/my_metric"));
        Assert.assertEquals("my_counter", prometheusName(sanitizeMetricName("my_counter_total")));
        Assert.assertEquals("jvm", sanitizeMetricName("jvm.info"));
        Assert.assertEquals("jvm", sanitizeMetricName("jvm_info"));
        Assert.assertEquals("jvm", sanitizeMetricName("jvm.info"));
        Assert.assertEquals("a.b", sanitizeMetricName("a.b"));
        Assert.assertEquals("total", sanitizeMetricName("_total"));
        Assert.assertEquals("total", sanitizeMetricName("total"));
    }

    @Test
    public void testSanitizeMetricNameWithUnit() {
        Assert.assertEquals("_abc_def_" + Unit.RATIO, prometheusName(sanitizeMetricName("0abc.def", Unit.RATIO)));
        Assert.assertEquals("___ab__c0_" + Unit.RATIO, prometheusName(sanitizeMetricName("___ab.:c0", Unit.RATIO)));
        Assert.assertEquals("my_prefix_my_metric_" + Unit.RATIO, sanitizeMetricName("my_prefix/my_metric", Unit.RATIO));
        Assert.assertEquals("my_counter_" + Unit.RATIO, prometheusName(sanitizeMetricName("my_counter_total", Unit.RATIO)));
        Assert.assertEquals("jvm_" + Unit.RATIO, sanitizeMetricName("jvm.info", Unit.RATIO));
        Assert.assertEquals("jvm_" + Unit.RATIO, sanitizeMetricName("jvm_info", Unit.RATIO));
        Assert.assertEquals("jvm_" + Unit.RATIO, sanitizeMetricName("jvm.info", Unit.RATIO));
        Assert.assertEquals("a.b_" + Unit.RATIO, sanitizeMetricName("a.b", Unit.RATIO));
        Assert.assertEquals("total_" + Unit.RATIO, sanitizeMetricName("_total", Unit.RATIO));
        Assert.assertEquals("total_" + Unit.RATIO, sanitizeMetricName("total", Unit.RATIO));
    }

    @Test
    public void testSanitizeLabelName() {
        Assert.assertEquals("_abc_def", prometheusName(sanitizeLabelName("0abc.def")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("_abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("__abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("___abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("_.abc")));
        Assert.assertEquals("abc.def", sanitizeLabelName("abc.def"));
        Assert.assertEquals("abc.def2", sanitizeLabelName("abc.def2"));
    }

    @Test
    public void testValidateUnitName() {
        Assert.assertNotNull(validateUnitName("secondstotal"));
        Assert.assertNotNull(validateUnitName("total"));
        Assert.assertNotNull(validateUnitName("seconds_total"));
        Assert.assertNotNull(validateUnitName("_total"));
        Assert.assertNotNull(validateUnitName(""));

        Assert.assertNull(validateUnitName("seconds"));
        Assert.assertNull(validateUnitName("2"));
    }

    @Test
    public void testSanitizeUnitName() {
        Assert.assertEquals("seconds", sanitizeUnitName("seconds"));
        Assert.assertEquals("seconds", sanitizeUnitName("seconds_total"));
        Assert.assertEquals("seconds", sanitizeUnitName("seconds_total_total"));
        Assert.assertEquals("m_s", sanitizeUnitName("m/s"));
        Assert.assertEquals("seconds", sanitizeUnitName("secondstotal"));
        Assert.assertEquals("2", sanitizeUnitName("2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnitName1() {
        sanitizeUnitName("total");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnitName2() {
        sanitizeUnitName("_total");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnitName3() {
        sanitizeUnitName("%");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUnitName() {
        sanitizeUnitName("");
    }
}
