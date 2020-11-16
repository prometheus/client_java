package io.prometheus.client.dropwizard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;

public class MetricRegistryFilter {

    public static MetricRegistry decorateMetricRegistryByFilter(MetricRegistry sourceRegistry, final MetricFilter filter) {
        final MetricRegistry decoratedRegistry = new MetricRegistry();

        MetricRegistryListener listener = new MetricRegistryListener() {
            @Override
            public void onGaugeAdded(String name, Gauge<?> gauge) {
                if (filter.matches(name, gauge)) {
                    decoratedRegistry.register(name, gauge);
                }
            }

            @Override
            public void onGaugeRemoved(String name) {
                decoratedRegistry.remove(name);
            }

            @Override
            public void onCounterAdded(String name, com.codahale.metrics.Counter counter) {
                if (filter.matches(name, counter)) {
                    decoratedRegistry.register(name, counter);
                }
            }

            @Override
            public void onCounterRemoved(String name) {
                decoratedRegistry.remove(name);
            }

            @Override
            public void onHistogramAdded(String name, Histogram histogram) {
                if (filter.matches(name, histogram)) {
                    decoratedRegistry.register(name, histogram);
                }
            }

            @Override
            public void onHistogramRemoved(String name) {
                decoratedRegistry.remove(name);
            }

            @Override
            public void onMeterAdded(String name, Meter meter) {
                if (filter.matches(name, meter)) {
                    decoratedRegistry.register(name, meter);
                }
            }

            @Override
            public void onMeterRemoved(String name) {
                decoratedRegistry.remove(name);
            }

            @Override
            public void onTimerAdded(String name, Timer timer) {
                if (filter.matches(name, timer)) {
                    decoratedRegistry.register(name, timer);
                }
            }

            @Override
            public void onTimerRemoved(String name) {
                decoratedRegistry.remove(name);
            }
        };
        sourceRegistry.addListener(listener);

        return decoratedRegistry;
    }

}
