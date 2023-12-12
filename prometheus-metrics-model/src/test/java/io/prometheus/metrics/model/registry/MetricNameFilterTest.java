package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricNameFilterTest {

    private PrometheusRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PrometheusRegistry();
    }

    @Test
    void testCounter() {
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
        Assertions.assertEquals(2, registry.scrape(filter).size());

        filter = MetricNameFilter.builder().nameMustStartWith("counter1").build();
        MetricSnapshots snapshots = registry.scrape(filter);
        Assertions.assertEquals(1, snapshots.size());
        Assertions.assertEquals("counter1", snapshots.get(0).getMetadata().getName());

        filter = MetricNameFilter.builder().nameMustNotStartWith("counter1").build();
        snapshots = registry.scrape(filter);
        Assertions.assertEquals(1, snapshots.size());
        Assertions.assertEquals("counter2", snapshots.get(0).getMetadata().getName());

        filter = MetricNameFilter.builder()
                .nameMustBeEqualTo("counter2_total", "counter1_total")
                .build();
        snapshots = registry.scrape(filter);
        Assertions.assertEquals(2, snapshots.size());

        filter = MetricNameFilter.builder()
                .nameMustBeEqualTo("counter1_total")
                .nameMustNotBeEqualTo("counter1_total")
                .build();
        Assertions.assertEquals(0, registry.scrape(filter).size());
    }
}
