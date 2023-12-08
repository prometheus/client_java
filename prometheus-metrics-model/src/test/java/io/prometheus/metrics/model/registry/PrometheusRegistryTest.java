package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Test;

public class PrometheusRegistryTest {

    Collector counterA1 = CollectorBuilder.fromMetric(() -> CounterSnapshot.builder().name("metric_a").build());
    Collector counterA2 = CollectorBuilder.fromMetric(() -> CounterSnapshot.builder().name("metric_a")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build()).build());
    Collector counterA3 = CollectorBuilder.fromMetric(() -> CounterSnapshot.builder().name("metric_a")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(2.0).build()).build());
    Collector counterB = CollectorBuilder.fromMetric(() -> CounterSnapshot.builder().name("metric_b").build());
    Collector gaugeA = CollectorBuilder.fromMetric(() -> GaugeSnapshot.builder().name("metric_a").build());

    @Test
    public void scrapeFailsOnTypeMismatch() {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(counterA1);
        registry.register(gaugeA);
        // However, at scrape time the collector has to provide a metric name, and then we'll get a duplicat name error.
        try {
            registry.scrape();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("invalid type") && e.getMessage().contains("metric_a"));
            return;
        }
        Assert.fail("Expected duplicate name exception");
    }

    @Test
    public void scrapeFailsOnDuplicateLabels() {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(counterA2);
        registry.register(counterA3);
        try {
            registry.scrape();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("Duplicate labels"));
            return;
        }
        Assert.fail("Expected duplicate labels exception");
    }


    /** It is allowed to register collectors with duplicate names.
     * Scrape will fail if metadata or metric types do not match.
     */
    @Test
    public void registerDuplicateName() {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(counterA1);
        registry.register(counterA2);
    }

    @Test
    public void registerOk() {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(counterA1);
        registry.register(counterB);
        MetricSnapshots snapshots = registry.scrape();
        Assert.assertEquals(2, snapshots.size());

        registry.unregister(counterB);
        snapshots = registry.scrape();
        Assert.assertEquals(1, snapshots.size());

        registry.register(counterB);
        snapshots = registry.scrape();
        Assert.assertEquals(2, snapshots.size());
    }
}
