package io.prometheus.client.dropwizard;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {
    @Test
    public void testSanitizeMetricName() {
        Assert.assertEquals("Foo_Bar_metric_mame", Utils.sanitizeMetricName("Foo.Bar-metric,mame"));
    }

    @Test
    public void testSanitizeMetricNameStartingWithDigit() {
        Assert.assertEquals("_42Foo_Bar_metric_mame", Utils.sanitizeMetricName("42Foo.Bar-metric,mame"));
    }
}