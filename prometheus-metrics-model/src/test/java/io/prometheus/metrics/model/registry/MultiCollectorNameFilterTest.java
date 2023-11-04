package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MultiCollectorNameFilterTest {

    private PrometheusRegistry registry;
    private boolean[] collectCalled = {false};
    private Predicate<String> includedNames = null;
    List<String> prometheusNames = new ArrayList<>();

    @Before
    public void setUp() {
        registry = new PrometheusRegistry();
        collectCalled[0] = false;
        includedNames = null;
        prometheusNames = Collections.emptyList();

        registry.register(new MultiCollector() {
            @Override
            public MetricSnapshots collect() {
                collectCalled[0] = true;
                return MetricSnapshots.builder()
                        .metricSnapshot(CounterSnapshot.builder()
                                .name("counter_1")
                                .dataPoint(CounterDataPointSnapshot.builder().value(1.0).build())
                                .build()
                        )
                        .metricSnapshot(GaugeSnapshot.builder()
                                .name("gauge_2")
                                .dataPoint(GaugeDataPointSnapshot.builder().value(1.0).build())
                                .build()
                        )
                        .build();
            }

            @Override
            public List<String> getPrometheusNames() {
                return prometheusNames;
            }
        });
    }

    @Test
    public void testPartialFilter() {

        includedNames = name -> name.equals("counter_1");

        MetricSnapshots snapshots = registry.scrape(includedNames);
        Assert.assertTrue(collectCalled[0]);
        Assert.assertEquals(1, snapshots.size());
        Assert.assertEquals("counter_1", snapshots.get(0).getMetadata().getName());
    }

    @Test
    public void testPartialFilterWithPrometheusNames() {

        includedNames = name -> name.equals("counter_1");
        prometheusNames = Arrays.asList("counter_1", "gauge_2");

        MetricSnapshots snapshots = registry.scrape(includedNames);
        Assert.assertTrue(collectCalled[0]);
        Assert.assertEquals(1, snapshots.size());
        Assert.assertEquals("counter_1", snapshots.get(0).getMetadata().getName());
    }

    @Test
    public void testCompleteFilter_CollectCalled() {

        includedNames = name -> !name.equals("counter_1") && !name.equals("gauge_2");

        MetricSnapshots snapshots = registry.scrape(includedNames);
        Assert.assertTrue(collectCalled[0]);
        Assert.assertEquals(0, snapshots.size());
    }

    @Test
    public void testCompleteFilter_CollectNotCalled() {

        includedNames = name -> !name.equals("counter_1") && !name.equals("gauge_2");
        prometheusNames = Arrays.asList("counter_1", "gauge_2");

        MetricSnapshots snapshots = registry.scrape(includedNames);
        Assert.assertFalse(collectCalled[0]);
        Assert.assertEquals(0, snapshots.size());
    }
}
