package io.prometheus.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

public class DisableCreatedTest {

    private CollectorRegistry registry;

    @Before
    public void setUp() {
        registry = new CollectorRegistry(true);
    }

    @Test
    public void testDisableCreatedAll() throws Exception {
        EnvironmentVariables env = new EnvironmentVariables();

        try {
            env.set(Collector.PROMETHEUS_DISABLE_CREATED_SERIES, "true");
            env.setup();

            Collector.reset();

            Assert.assertEquals(true, Collector.DISABLE_CREATED_SERIES);

            Counter counter1 = Counter.build()
                    .name("counter1")
                    .help("test counter 1")
                    .labelNames("path")
                    .register(registry);
            counter1.labels("/hello").inc();
            counter1.labels("/goodbye").inc();

            Counter counter2 = Counter.build()
                    .name("counter2")
                    .help("test counter 2")
                    .register(registry);
            counter2.inc();

            Histogram histogram = Histogram.build()
                    .name("test_histogram")
                    .help("test histogram")
                    .create();
            histogram.observe(100);
            histogram.observe(200);

            registry.register(histogram);

            Summary noLabels = Summary.build().name("test_summary").help("test summary").register(registry);
            noLabels.observe(2);

            List<Collector.MetricFamilySamples> mfsList = Collections.list(registry.metricFamilySamples());

            assertTotalNumberOfSamples(mfsList, 22);
            assertSamplesInclude(mfsList, "counter1_total", 2);
            assertSamplesInclude(mfsList, "counter1_created", 0);
            assertSamplesInclude(mfsList, "counter2_total", 1);
            assertSamplesInclude(mfsList, "counter2_created", 0);
            assertSamplesInclude(mfsList, "test_histogram_bucket", 15);
            assertSamplesInclude(mfsList, "test_histogram_count", 1);
            assertSamplesInclude(mfsList, "test_histogram_sum", 1);
            assertSamplesInclude(mfsList, "test_histogram_created", 0);
            assertSamplesInclude(mfsList, "test_summary_count", 1);
            assertSamplesInclude(mfsList, "test_summary_sum", 1);
            assertSamplesInclude(mfsList, "test_summary_created", 0);
        } finally {
            env.teardown();
        }

        registry.clear();

        Collector.reset();

        Assert.assertEquals(false, Collector.DISABLE_CREATED_SERIES);

        Counter counter1 = Counter.build()
                .name("counter1")
                .help("test counter 1")
                .labelNames("path")
                .register(registry);
        counter1.labels("/hello").inc();
        counter1.labels("/goodbye").inc();

        List<Collector.MetricFamilySamples> mfsList = Collections.list(registry.metricFamilySamples());

        assertSamplesInclude(mfsList, "counter1_total", 2);
        assertSamplesInclude(mfsList, "counter1_created", 2);
    }

    private void assertSamplesInclude(List<Collector.MetricFamilySamples> mfsList, String name, int times) {
        int count = 0;
        for (Collector.MetricFamilySamples mfs : mfsList) {
            for (Collector.MetricFamilySamples.Sample sample : mfs.samples) {
                if (sample.name.equals(name)) {
                    count++;
                }
            }
        }
        Assert.assertEquals("Wrong number of samples for " + name, times, count);
    }

    private void assertTotalNumberOfSamples(List<Collector.MetricFamilySamples> mfsList, int n) {
        int count = 0;
        for (Collector.MetricFamilySamples mfs : mfsList) {
            count += mfs.samples.size();
        }
        Assert.assertEquals("Wrong total number of samples", n, count);
    }
}
