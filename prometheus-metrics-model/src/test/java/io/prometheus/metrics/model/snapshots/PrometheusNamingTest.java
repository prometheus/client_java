package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;

class PrometheusNamingTest {

    @Test
    void testSanitizeMetricName() {
        Assertions.assertEquals("_abc_def", prometheusName(sanitizeMetricName("0abc.def")));
        Assertions.assertEquals("___ab_:c0", prometheusName(sanitizeMetricName("___ab.:c0")));
        Assertions.assertEquals("my_prefix_my_metric", sanitizeMetricName("my_prefix/my_metric"));
        Assertions.assertEquals("my_counter", prometheusName(sanitizeMetricName("my_counter_total")));
        Assertions.assertEquals("jvm", sanitizeMetricName("jvm.info"));
        Assertions.assertEquals("jvm", sanitizeMetricName("jvm_info"));
        Assertions.assertEquals("jvm", sanitizeMetricName("jvm.info"));
        Assertions.assertEquals("a.b", sanitizeMetricName("a.b"));
    }

    @Test
    void testSanitizeLabelName() {
        Assertions.assertEquals("_abc_def", prometheusName(sanitizeLabelName("0abc.def")));
        Assertions.assertEquals("_abc", prometheusName(sanitizeLabelName("_abc")));
        Assertions.assertEquals("_abc", prometheusName(sanitizeLabelName("__abc")));
        Assertions.assertEquals("_abc", prometheusName(sanitizeLabelName("___abc")));
        Assertions.assertEquals("_abc", prometheusName(sanitizeLabelName("_.abc")));
        Assertions.assertEquals("abc.def", sanitizeLabelName("abc.def"));
        Assertions.assertEquals("abc.def2", sanitizeLabelName("abc.def2"));
    }
}
