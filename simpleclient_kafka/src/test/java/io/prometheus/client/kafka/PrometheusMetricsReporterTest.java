package io.prometheus.client.kafka;

import io.prometheus.client.CollectorRegistry;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.MetricConfig;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.common.metrics.stats.Value;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


public class PrometheusMetricsReporterTest {
    private CollectorRegistry registry;
    private Metrics kafkaMetrics;

    @Before
    public void before() {
        registry = new CollectorRegistry();
        MetricsReporter reporter = new PrometheusMetricsReporter(registry);
        kafkaMetrics = new Metrics();
        kafkaMetrics.addReporter(reporter);
    }

    @Test
    public void shouldRegisterMetricFromKafka() {
        kafkaMetrics.addMetric(metricName("hello", "world"), new Value());

        assertThat(registry.getSampleValue("kafka_hello_world"), is(0.0));
    }

    @Test
    public void shouldUnregisterMetricFromKafka() {
        MetricName name = metricName("g", "n");
        kafkaMetrics.addMetric(name, valueOf(0.0));
        assertThat(registry.getSampleValue("kafka_g_n"), is(0.0));
        kafkaMetrics.removeMetric(name);
        assertNull(registry.getSampleValue("kafka_g_n"));
    }

    @Test
    public void shouldRegisterWithLabels() {
        MetricName name1 = metricName("g", "n", Collections.singletonMap("tag-a", "A"));
        MetricName name2 = metricName("g", "n", Collections.singletonMap("tag-a", "B"));
        kafkaMetrics.addMetric(name1, valueOf(11.0));
        kafkaMetrics.addMetric(name2, valueOf(22.0));
        assertThat(registry.getSampleValue("kafka_g_n", new String[]{"tag_a"}, new String[]{"A"}), is(11.0));
        assertThat(registry.getSampleValue("kafka_g_n", new String[]{"tag_a"}, new String[]{"B"}), is(22.0));
    }

    @Test
    public void shouldUnregisterWithLabels() {
        MetricName name1 = metricName("g", "n", Collections.singletonMap("tag-a", "A"));
        MetricName name2 = metricName("g", "n", Collections.singletonMap("tag-a", "B"));
        kafkaMetrics.addMetric(name1, valueOf(11.0));
        kafkaMetrics.addMetric(name2, valueOf(22.0));
        assertThat(registry.getSampleValue("kafka_g_n", new String[]{"tag_a"}, new String[]{"A"}), is(11.0));
        assertThat(registry.getSampleValue("kafka_g_n", new String[]{"tag_a"}, new String[]{"B"}), is(22.0));
        kafkaMetrics.removeMetric(name1);
        assertNull(registry.getSampleValue("kafka_g_n", new String[]{"tag_a"}, new String[]{"A"}));
    }

    private MetricName metricName(String group, String name) {
        return metricName(group, name, Collections.<String, String>emptyMap());
    }

    private MetricName metricName(String group, String name, Map<String, String> tags) {
        return new MetricName(name, group, "description", tags);
    }

    private Value valueOf(double init) {
        Value value = new Value();
        value.record(new MetricConfig(), init, 0L);
        return value;
    }
}
