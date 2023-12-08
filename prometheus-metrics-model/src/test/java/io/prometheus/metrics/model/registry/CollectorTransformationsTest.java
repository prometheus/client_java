package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import org.junit.Assert;
import org.junit.Test;

public class CollectorTransformationsTest {

    @Test
    public void testNamePrefix() {
        var metrics = CollectorBuilder.fromMetric(() -> CounterSnapshot.builder()
                        .name("counter1")
                        .dataPoint(CounterDataPointSnapshot.builder()
                                .labels(Labels.of("path", "/hello"))
                                .value(1.0)
                                .build()
                        )
                        .build()
                );

        var prefixed = CollectorBuilder.withNamePrefix(metrics, "my_");
        var labeled = CollectorBuilder.withLabels(metrics, Labels.of("l", "v"));

        Assert.assertEquals(metrics.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getMetadata().getPrometheusName(), "counter1");
        Assert.assertEquals(prefixed.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getMetadata().getPrometheusName(), "my_counter1");
        Assert.assertEquals(prefixed.collect(name -> name.equals("counter1"), null).size(), 0);
        Assert.assertEquals(prefixed.collect(name -> name.equals("my_counter1"), null).size(), 1);

        Assert.assertEquals(metrics.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getDataPoints().get(0).getLabels().size(), 1);
        Assert.assertEquals(labeled.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getDataPoints().get(0).getLabels().size(), 2);
        Assert.assertTrue(labeled.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getDataPoints().get(0).getLabels().contains("l"));
    }

    @Test
    public void testLabels() {
        var metrics = CollectorBuilder.fromMetric(() -> CounterSnapshot.builder()
                .name("counter1")
                .dataPoint(CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/hello"))
                        .value(1.0)
                        .build()
                )
                .build()
        );

        var labeled = CollectorBuilder.withLabels(metrics, Labels.of("l", "v"));
        Assert.assertEquals(metrics.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getDataPoints().get(0).getLabels().size(), 1);
        Assert.assertEquals(labeled.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getDataPoints().get(0).getLabels().size(), 2);
        Assert.assertTrue(labeled.collect(MetricNameFilter.ALLOW_ALL, null).get(0).getDataPoints().get(0).getLabels().contains("l"));
    }

}
