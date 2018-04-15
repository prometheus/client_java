package io.prometheus.client.dropwizard;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DropwizardPrometheusReporterTest {

    private final PrometheusSender prometheus = mock(PrometheusSender.class);

    @Test
    public void shouldReportsAllMetrics() throws Exception {

        MetricRegistry registry = new MetricRegistry();
        DropwizardPrometheusReporter reporter = DropwizardPrometheusReporter.forRegistry(registry)
                .build(prometheus);

        registry.counter("counter-1");
        registry.meter("meter-1");
        registry.timer("timer-1");
        registry.histogram("histogram-1");
        registry.register("gauge-1", gauge(1));

        reporter.report();

        ArgumentCaptor<Collector> captor = ArgumentCaptor.forClass(Collector.class);
        verify(prometheus).send(captor.capture());
        verifyNoMoreInteractions(prometheus);

        List<MetricFamilySamples> metrics = captor.getValue().collect();
        assertThat(metrics.size(), is(5));
        assertThat(namesOf(metrics), hasItems("counter_1", "gauge_1",
                "meter_1_total", "timer_1", "histogram_1"));
    }

    @Test
    public void shouldReportsFilteredMetrics() throws Exception {

        MetricRegistry registry = new MetricRegistry();
        DropwizardPrometheusReporter reporter = DropwizardPrometheusReporter.forRegistry(registry)
                .filter(new MetricFilter() {
                    @Override
                    public boolean matches(String name, Metric metric) {
                        return name.endsWith("-2");
                    }
                })
                .build(prometheus);

        registry.counter("counter-1");
        registry.counter("counter-2");
        registry.meter("meter-1");
        registry.timer("timer-1");
        registry.timer("timer-2");
        registry.histogram("histogram-1");
        registry.register("gauge-1", gauge(2));

        reporter.report();

        ArgumentCaptor<Collector> captor = ArgumentCaptor.forClass(Collector.class);
        verify(prometheus).send(captor.capture());
        verifyNoMoreInteractions(prometheus);

        List<MetricFamilySamples> metrics = captor.getValue().collect();
        assertThat(metrics.size(), is(2));
        assertThat(namesOf(metrics), hasItems("timer_2", "counter_2"));
    }

    @Test
    public void shouldLogErrorAndContinue() throws Exception {
        doThrow(new ConnectException("connection refused")).when(prometheus).send(any(Collector.class));

        MetricRegistry registry = new MetricRegistry();
        DropwizardPrometheusReporter reporter = DropwizardPrometheusReporter.forRegistry(registry)
                .build(prometheus);

        reporter.report();
        reporter.report();
        reporter.report();

        verify(prometheus, times(3)).send(any(Collector.class));
        verifyNoMoreInteractions(prometheus);
    }

    private List<String> namesOf(List<MetricFamilySamples> metrics) {
        List<String> names = new ArrayList<String>(metrics.size());
        for (MetricFamilySamples metric: metrics) {
            names.add(metric.name);
        }
        return names;
    }

    private <T> Gauge gauge(T value) {
        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(value);
        return gauge;
    }

}
