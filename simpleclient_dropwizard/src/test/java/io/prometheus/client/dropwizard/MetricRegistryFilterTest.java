package io.prometheus.client.dropwizard;

import com.codahale.metrics.*;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class MetricRegistryFilterTest {

    private static final MetricFilter filter = new MetricFilter() {
        @Override
        public boolean matches(String name, Metric metric) {
            return !name.contains("skipme");
        }
    };

    private final MetricRegistry originalRegistry = new MetricRegistry();

    @Before
    public void initOriginalRegistry() {
        originalRegistry.register("some.guage", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 42;
            }
        });
        originalRegistry.register("some.guage.skipme", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 666;
            }
        });

        originalRegistry.counter("some.counter");
        originalRegistry.counter("some.counter.skipme");

        originalRegistry.histogram("some.histogram");
        originalRegistry.histogram("some.histogram.skipme");

        originalRegistry.timer("some.timer");
        originalRegistry.timer("some.timer.skipme");

        originalRegistry.meter("some.meter");
        originalRegistry.meter("some.meter.skipme");
    }

    @Test
    public void shouldCorrectlyApplyTheFilter() {
        MetricRegistry decoratedRegistry = MetricRegistryFilter.decorateMetricRegistryByFilter(originalRegistry, filter);

        assertTrue(decoratedRegistry.getMetrics().containsKey("some.guage"));
        assertTrue(decoratedRegistry.getMetrics().containsKey("some.counter"));
        assertTrue(decoratedRegistry.getMetrics().containsKey("some.histogram"));
        assertTrue(decoratedRegistry.getMetrics().containsKey("some.timer"));
        assertTrue(decoratedRegistry.getMetrics().containsKey("some.meter"));

        assertFalse(decoratedRegistry.getMetrics().containsKey("some.guage.skipme"));
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.counter.skipme"));
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.histogram.skipme"));
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.timer.skipme"));
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.meter.skipme"));
    }

    @Test
    public void shouldTransparentlyRemoveMetricsFromDecoratedRegistryWhenRemovalHappenFromOriginalRegistry() {
        MetricRegistry decoratedRegistry = MetricRegistryFilter.decorateMetricRegistryByFilter(originalRegistry, filter);

        originalRegistry.remove("some.guage");
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.guage"));

        originalRegistry.remove("some.counter");
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.counter"));

        originalRegistry.remove("some.histogram");
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.histogram"));

        originalRegistry.remove("some.timer");
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.timer"));

        originalRegistry.remove("some.meter");
        assertFalse(decoratedRegistry.getMetrics().containsKey("some.meter"));
    }


}