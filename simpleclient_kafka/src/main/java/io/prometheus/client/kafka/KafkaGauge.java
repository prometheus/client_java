package io.prometheus.client.kafka;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.kafka.common.metrics.KafkaMetric;

import java.util.*;


final class KafkaGauge extends Collector implements Collector.Describable {
    private final List<KafkaMetric> metrics;
    private final String fullName;

    private KafkaGauge(String group, String name) {
        this.metrics = new ArrayList<KafkaMetric>();
        this.fullName = "kafka_" + sanitize(group) + "_" + sanitize(name);
    }

    KafkaGauge(String group, String name, KafkaMetric firstMetric) {
        this(group, name);
        this.add(firstMetric);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();

        for (KafkaMetric metric : metrics) {
            Map<String, String> labels = metric.metricName().tags();
            List<String> labelNames = new ArrayList<String>();
            List<String> labelValues = new ArrayList<String>();
            for (Map.Entry<String, String> label : labels.entrySet()) {
                labelNames.add(sanitize(label.getKey()));
                labelValues.add(sanitize(label.getValue()));
            }
            samples.add(new MetricFamilySamples.Sample(fullName, labelNames, labelValues, metric.value()));
        }

        return Collections.singletonList(new MetricFamilySamples(fullName, Type.GAUGE, help(), samples));
    }

    @Override
    public List<MetricFamilySamples> describe() {
        List<String> labelNames = new ArrayList<String>();
        for (KafkaMetric metric : metrics) {
            labelNames.addAll(metric.metricName().tags().keySet());
        }
        return Collections.singletonList((MetricFamilySamples) new GaugeMetricFamily(fullName, help(), labelNames));
    }

    boolean isEmpty() {
        return metrics.isEmpty();
    }

    void add(KafkaMetric metric) {
        metrics.add(metric);
    }

    void remove(KafkaMetric metric) {
        int at = -1;
        for (int i = 0; i < metrics.size(); i++) {
            if (metrics.get(i).metricName().equals(metric.metricName())) {
                at = i;
            }
        }
        if (at != -1)
            metrics.remove(at);
    }

    private String help() {
        for (KafkaMetric metric : metrics) {
            if (!metric.metricName().description().isEmpty())
                return metric.metricName().description();
        }
        return "";
    }

    private static String sanitize(String name) {
        return name
                .replace('-', '_')
                .replace('.', '_')
                .replace(':', '_')
                .replace(' ', '_');
    }
}