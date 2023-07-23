package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

public class PrometheusRegistry {

    public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

    private final Set<String> prometheusNames = ConcurrentHashMap.newKeySet();
    private final List<Collector> collectors = new CopyOnWriteArrayList<>();
    private final List<MultiCollector> multiCollectors = new CopyOnWriteArrayList<>();

    public void register(Collector collector) {
        String prometheusName = collector.getPrometheusName();
        if (prometheusName != null) {
            if (!prometheusNames.add(prometheusName)) {
                throw new IllegalStateException("Can't register " + prometheusName + " because a metric with that name is already registered.");
            }
        }
        collectors.add(collector);
    }

    public void register(MultiCollector collector) {
        for (String prometheusName : collector.getPrometheusNames()) {
            if (!prometheusNames.add(prometheusName)) {
                throw new IllegalStateException("Can't register " + prometheusName + " because that name is already registered.");
            }
        }
        multiCollectors.add(collector);
    }

    public void unregister(Collector collector) {
        collectors.remove(collector);
        prometheusNames.remove(collector.getPrometheusName());
    }

    public void unregister(MultiCollector collector) {
        multiCollectors.remove(collector);
        for (String prometheusName : collector.getPrometheusNames()) {
            prometheusNames.remove(prometheusName(prometheusName));
        }
    }

    public MetricSnapshots scrape() {
        MetricSnapshots.Builder result = MetricSnapshots.newBuilder();
        for (Collector collector : collectors) {
            MetricSnapshot snapshot = collector.collect();
            if (snapshot != null) {
                if (result.containsMetricName(snapshot.getMetadata().getName())) {
                    throw new IllegalStateException(snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
                }
                result.addMetricSnapshot(snapshot);
            }
        }
        for (MultiCollector collector : multiCollectors) {
            for (MetricSnapshot snapshot : collector.collect()) {
                if (result.containsMetricName(snapshot.getMetadata().getName())) {
                    throw new IllegalStateException(snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
                }
                result.addMetricSnapshot(snapshot);
            }
        }
        return result.build();
    }

    public MetricSnapshots scrape(Predicate<String> includedNames) {
        if (includedNames == null) {
            return scrape();
        }
        MetricSnapshots.Builder result = MetricSnapshots.newBuilder();
        for (Collector collector : collectors) {
            String prometheusName = collector.getPrometheusName();
            if (prometheusName == null || includedNames.test(prometheusName)) {
                MetricSnapshot snapshot = collector.collect(includedNames);
                if (snapshot != null) {
                    result.addMetricSnapshot(snapshot);
                }
            }
        }
        for (MultiCollector collector : multiCollectors) {
            List<String> prometheusNames = collector.getPrometheusNames();
            boolean excluded = true; // the multi-collector is excluded unless at least one name matches
            for (String prometheusName : prometheusNames) {
                if (includedNames.test(prometheusName)) {
                    excluded = false;
                    break;
                }
            }
            if (!excluded) {
                for (MetricSnapshot snapshot : collector.collect(includedNames)) {
                    if (snapshot != null) {
                        result.addMetricSnapshot(snapshot);
                    }
                }
            }
        }
        return result.build();
    }
}
