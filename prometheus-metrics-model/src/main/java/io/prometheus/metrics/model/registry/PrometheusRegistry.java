package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class PrometheusRegistry {

    public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

    private final Set<String> names = ConcurrentHashMap.newKeySet();
    private final List<Collector> collectors = new CopyOnWriteArrayList<>();
    private final List<MultiCollector> multiCollectors = new CopyOnWriteArrayList<>();

    public void register(Collector collector) {
        String name = collector.getName();
        if (!names.add(name)) {
            throw new IllegalStateException("Can't register " + name + " because that name is already registered.");
        }
        collectors.add(collector);
    }

    public void register(MultiCollector collector) {
        for (String name : collector.getNames()) {
            if (!names.add(name)) {
                throw new IllegalStateException("Can't register " + name + " because that name is already registered.");
            }
        }
        multiCollectors.add(collector);
    }

    public void unregister(Collector collector) {
        collectors.remove(collector);
        names.remove(collector.getName());
    }

    public void unregister(MultiCollector collector) {
        multiCollectors.remove(collector);
        for (String name : collector.getNames()) {
            names.remove(name);
        }
    }

    public MetricSnapshots scrape() {
        MetricSnapshots.Builder result = MetricSnapshots.newBuilder();
        for (Collector collector : collectors) {
            result.addMetricSnapshot(collector.collect());
        }
        for (MultiCollector collector : multiCollectors) {
            for (MetricSnapshot snapshot : collector.collect()) {
                result.addMetricSnapshot(snapshot);
            }
        }
        return result.build();
    }

    public MetricSnapshots scrape(Predicate<String> excludedNames) {
        MetricSnapshots.Builder result = MetricSnapshots.newBuilder();
        for (Collector collector : collectors) {
            String name = collector.getName();
            if (name == null || !excludedNames.test(name)) {
                MetricSnapshot snapshot = collector.collect();
                if (!excludedNames.test(snapshot.getMetadata().getName())) {
                    result.addMetricSnapshot(snapshot);
                }
            }
        }
        for (MultiCollector collector : multiCollectors) {
            List<String> names = collector.getNames();
            boolean excluded = true;
            for (String name : names) {
                if (!excludedNames.test(name)) {
                    excluded = false;
                    break;
                }
            }
            if (!excluded) {
                for (MetricSnapshot snapshot : collector.collect()) {
                    if (!excludedNames.test(snapshot.getMetadata().getName())) {
                        result.addMetricSnapshot(snapshot);
                    }
                }
            }
        }
        return result.build();
    }
}
