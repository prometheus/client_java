package io.prometheus.client.kafka;

import io.prometheus.client.CollectorRegistry;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public class PrometheusMetricsReporter implements MetricsReporter {
    private final static Logger logger = LoggerFactory.getLogger("PrometheusMetricsReporter");

    private final static IdentityHashMap<CollectorRegistry, Map<FullName, KafkaGauge>> metricsPerRegistry =
            new IdentityHashMap<CollectorRegistry, Map<FullName, KafkaGauge>>();

    private final CollectorRegistry registry;

    public PrometheusMetricsReporter(CollectorRegistry registry) {
        this.registry = registry;
    }

    public PrometheusMetricsReporter() {
        this(CollectorRegistry.defaultRegistry);
    }

    @Override
    public void init(List<KafkaMetric> metrics) {
        synchronized (metricsPerRegistry) {
            if (metricsPerRegistry.get(registry) == null) {
                metricsPerRegistry.put(registry, new HashMap<FullName, KafkaGauge>());
            }
        }

        for (KafkaMetric metric : metrics) {
            metricChange(metric);
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

    @Override
    public void metricChange(KafkaMetric metric) {
        Map<FullName, KafkaGauge> namesToMetrics = getNamesToMetrics();
        synchronized (namesToMetrics) {
            FullName fullName = FullName.of(metric);
            KafkaGauge existing = namesToMetrics.get(fullName);
            if (existing == null) {
                KafkaGauge gauge = new KafkaGauge(fullName.group, fullName.name, metric);
                namesToMetrics.put(fullName, gauge);
                registry.register(gauge);
            } else {
                existing.add(metric);
            }
        }
    }

    @Override
    public void metricRemoval(KafkaMetric metric) {
        Map<FullName, KafkaGauge> namesToMetrics = getNamesToMetrics();
        synchronized (namesToMetrics) {
            FullName fullName = FullName.of(metric);
            KafkaGauge existing = namesToMetrics.get(fullName);
            if (existing != null) {
                existing.remove(metric);
                if (existing.isEmpty()) {
                    namesToMetrics.remove(fullName);
                    registry.unregister(existing);
                }
            } else {
                logger.error("Attempted to remove non-existing metric {}", metric.metricName());
            }
        }
    }

    @Override
    public void close() {
        synchronized (metricsPerRegistry) {
            metricsPerRegistry.remove(registry);
        }
    }

    private Map<FullName, KafkaGauge> getNamesToMetrics() {
        synchronized (metricsPerRegistry) {
            return metricsPerRegistry.get(registry);
        }
    }

    private static class FullName {
        final String group;
        final String name;

        FullName(String group, String name) {
            this.group = group;
            this.name = name;
        }

        static FullName of(KafkaMetric metric) {
            return new FullName(metric.metricName().group(), metric.metricName().name());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FullName fullName = (FullName) o;

            if (group != null ? !group.equals(fullName.group) : fullName.group != null) return false;
            return name != null ? name.equals(fullName.name) : fullName.name == null;
        }

        @Override
        public int hashCode() {
            int result = group != null ? group.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
