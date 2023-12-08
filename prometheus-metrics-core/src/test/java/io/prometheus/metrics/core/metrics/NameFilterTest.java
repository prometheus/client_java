package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class NameFilterTest {
    @Test
    public void testCounterWithCallback() {
        AtomicInteger accessCount = new AtomicInteger();
        CounterWithCallback metrics = CounterWithCallback.builder()
                .name("my_counter")
                .callback(cb -> {
                    accessCount.incrementAndGet();
                    cb.call(1.0);
                })
                .build();


        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(metrics);

        var result1 = registry.scrape(MetricNameFilter.builder().nameMustBeEqualTo("XXX").build());
        Assert.assertEquals(accessCount.get(), 0);
        Assert.assertTrue(result1.stream().toList().isEmpty());

        var result2 = registry.scrape(MetricNameFilter.builder().nameMustBeEqualTo("my_counter").build());
        Assert.assertEquals(accessCount.get(), 1);
        Assert.assertEquals(result2.stream().toList().size(), 1);
        Assert.assertEquals(result2.get(0).getMetadata().getPrometheusName(), "my_counter");
    }

    @Test
    public void testGaugeWithCallback() {
        AtomicInteger accessCount = new AtomicInteger();
        GaugeWithCallback metrics = GaugeWithCallback.builder()
                .name("my_gauge")
                .callback(cb -> {
                    accessCount.incrementAndGet();
                    cb.call(1.0);
                })
                .build();


        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(metrics);

        var result1 = registry.scrape(MetricNameFilter.builder().nameMustBeEqualTo("XXX").build());
        Assert.assertEquals(accessCount.get(), 0);
        Assert.assertTrue(result1.stream().toList().isEmpty());

        var result2 = registry.scrape(MetricNameFilter.builder().nameMustBeEqualTo("my_gauge").build());
        Assert.assertEquals(accessCount.get(), 1);
        Assert.assertEquals(result2.stream().toList().size(), 1);
        Assert.assertEquals(result2.get(0).getMetadata().getPrometheusName(), "my_gauge");
    }

}
