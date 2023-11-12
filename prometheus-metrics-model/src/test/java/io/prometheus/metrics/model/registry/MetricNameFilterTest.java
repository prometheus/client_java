package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class MetricNameFilterTest {

    private PrometheusRegistry registry;

    @Before
    public void setUp() {
        registry = new PrometheusRegistry();
    }

    @Test
    public void testCounter() {
        registry.register(() -> CounterSnapshot.builder()
                .name("counter1")
                .help("test counter 1")
                .dataPoint(CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/hello"))
                        .value(1.0)
                        .build()
                )
                .dataPoint(CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/goodbye"))
                        .value(2.0)
                        .build()
                )
                .build()
        );
        registry.register(() -> CounterSnapshot.builder()
                .name("counter2")
                .help("test counter 2")
                .dataPoint(CounterDataPointSnapshot.builder()
                        .value(1.0)
                        .build()
                )
                .build()
        );

        MetricNameFilter filter = MetricNameFilter.builder().build();
        Assert.assertEquals(2, registry.scrape(filter).size());

        filter = MetricNameFilter.builder().nameMustStartWith("counter1").build();
        MetricSnapshots snapshots = registry.scrape(filter);
        Assert.assertEquals(1, snapshots.size());
        Assert.assertEquals("counter1", snapshots.get(0).getMetadata().getName());

        filter = MetricNameFilter.builder().nameMustNotStartWith("counter1").build();
        snapshots = registry.scrape(filter);
        Assert.assertEquals(1, snapshots.size());
        Assert.assertEquals("counter2", snapshots.get(0).getMetadata().getName());

        filter = MetricNameFilter.builder()
                .nameMustBeEqualTo("counter2_total", "counter1_total")
                .build();
        snapshots = registry.scrape(filter);
        Assert.assertEquals(2, snapshots.size());

        filter = MetricNameFilter.builder()
                .nameMustBeEqualTo("counter1_total")
                .nameMustNotBeEqualTo("counter1_total")
                .build();
        Assert.assertEquals(0, registry.scrape(filter).size());
    }
}
