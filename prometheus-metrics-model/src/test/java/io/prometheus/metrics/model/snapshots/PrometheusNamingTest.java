package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;

public class PrometheusNamingTest {

    @Test
    public void testSanitizeMetricName() {
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
        Assert.assertEquals("_abc_def", prometheusName(sanitizeLabelName("0abc.def")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("_abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("__abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("___abc")));
        Assert.assertEquals("_abc", prometheusName(sanitizeLabelName("_.abc")));
        Assert.assertEquals("abc.def", sanitizeLabelName("abc.def"));
        Assert.assertEquals("abc.def2", sanitizeLabelName("abc.def2"));
    }
}
