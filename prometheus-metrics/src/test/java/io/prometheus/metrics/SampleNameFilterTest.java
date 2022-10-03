package io.prometheus.metrics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SampleNameFilterTest {

    private CollectorRegistry registry;

    @Before
    public void setUp() {
        registry = new CollectorRegistry(true);
    }

    @Test
    public void testCounter() {
        // It should not make any difference whether a counter is created as "my_counter" or as "my_counter_total".
        // We test both ways here and expect the same output.
        for (String suffix : new String[] { "", "_total"}) {
            registry.clear();
            Counter counter1 = Counter.build()
                    .name("counter1" + suffix)
                    .help("test counter 1")
                    .labelNames("path")
                    .register(registry);
            counter1.labels("/hello").inc();
            counter1.labels("/goodbye").inc();
            Counter counter2 = Counter.build()
                    .name("counter2" + suffix)
                    .help("test counter 2")
                    .register(registry);
            counter2.inc();

            SampleNameFilter filter = new SampleNameFilter.Builder().build();
            List<Collector.MetricFamilySamples> mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertSamplesInclude(mfsList, "counter1_total", 2);
            assertSamplesInclude(mfsList, "counter1_created", 2);
            assertSamplesInclude(mfsList, "counter2_total", 1);
            assertSamplesInclude(mfsList, "counter2_created", 1);
            assertTotalNumberOfSamples(mfsList, 6);

            filter = new SampleNameFilter.Builder().nameMustStartWith("counter1").build();
            mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertSamplesInclude(mfsList, "counter1_total", 2);
            assertSamplesInclude(mfsList, "counter1_created", 2);
            assertTotalNumberOfSamples(mfsList, 4);

            filter = new SampleNameFilter.Builder().nameMustNotStartWith("counter1").build();
            mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertSamplesInclude(mfsList, "counter2_total", 1);
            assertSamplesInclude(mfsList, "counter2_created", 1);
            assertTotalNumberOfSamples(mfsList, 2);

            filter = new SampleNameFilter.Builder()
                    .nameMustBeEqualTo("counter2_total")
                    .nameMustBeEqualTo("counter1_total")
                    .build();
            mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertSamplesInclude(mfsList, "counter1_total", 2);
            assertSamplesInclude(mfsList, "counter2_total", 1);
            assertTotalNumberOfSamples(mfsList, 3);

            filter = new SampleNameFilter.Builder()
                    .nameMustStartWith("counter1")
                    .nameMustNotStartWith("counter1_created")
                    .build();
            mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertSamplesInclude(mfsList, "counter1_total", 2);
            assertTotalNumberOfSamples(mfsList, 2);

            // The following filter would be weird in practice, but let's test this anyways :)
            filter = new SampleNameFilter.Builder()
                    .nameMustBeEqualTo("counter1_created")
                    .nameMustBeEqualTo("counter2_created")
                    .nameMustNotStartWith("counter1")
                    .build();
            mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertSamplesInclude(mfsList, "counter2_created", 1);
            assertTotalNumberOfSamples(mfsList, 1);

            // And finally one that should not match anything
            filter = new SampleNameFilter.Builder()
                    .nameMustBeEqualTo("counter1_total")
                    .nameMustNotBeEqualTo("counter1_total")
                    .build();
            mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
            assertTotalNumberOfSamples(mfsList, 0);

        }
    }

    @Test
    public void testCustomCollector() {
        Collector myCollector = new Collector() {
            @Override
            public List<MetricFamilySamples> collect() {
                List<MetricFamilySamples> result = new ArrayList<MetricFamilySamples>();

                String name = "temperature_centigrade";
                List<String> labelNames = Collections.singletonList("location");
                GaugeMetricFamily temperatureCentigrade = new GaugeMetricFamily(name, "temperature centigrade", labelNames);
                temperatureCentigrade.samples.add(new MetricFamilySamples.Sample(name, labelNames, Collections.singletonList("outside"), 26.0));
                temperatureCentigrade.samples.add(new MetricFamilySamples.Sample(name, labelNames, Collections.singletonList("inside"), 22.0));
                result.add(temperatureCentigrade);

                name = "temperature_fahrenheit";
                GaugeMetricFamily temperatureFahrenheit = new GaugeMetricFamily(name, "temperature fahrenheit", labelNames);
                temperatureFahrenheit.samples.add(new MetricFamilySamples.Sample(name, labelNames, Collections.singletonList("outside"), 78.8));
                temperatureFahrenheit.samples.add(new MetricFamilySamples.Sample(name, labelNames, Collections.singletonList("inside"), 71.6));
                result.add(temperatureFahrenheit);

                return result;
            }
        };
        registry.register(myCollector);

        SampleNameFilter filter = new SampleNameFilter.Builder()
                .nameMustStartWith("temperature_centigrade")
                .build();
        List<Collector.MetricFamilySamples> mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "temperature_centigrade", 2);
        assertTotalNumberOfSamples(mfsList, 2);

        filter = new SampleNameFilter.Builder()
                .nameMustNotStartWith("temperature_centigrade")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "temperature_fahrenheit", 2);
        assertTotalNumberOfSamples(mfsList, 2);

        filter = new SampleNameFilter.Builder()
                .nameMustNotStartWith("temperature")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertTotalNumberOfSamples(mfsList, 0);

        filter = new SampleNameFilter.Builder()
                .nameMustBeEqualTo("temperature_centigrade")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "temperature_centigrade", 2);
        assertTotalNumberOfSamples(mfsList, 2);

        filter = new SampleNameFilter.Builder()
                .nameMustNotBeEqualTo("temperature_centigrade")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "temperature_fahrenheit", 2);
        assertTotalNumberOfSamples(mfsList, 2);

        filter = new SampleNameFilter.Builder()
                .nameMustStartWith("temperature_c")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "temperature_centigrade", 2);
        assertTotalNumberOfSamples(mfsList, 2);
    }

    @Test
    public void testHistogram() {
        Histogram histogram = Histogram.build()
                .name("test_histogram")
                .help("test histogram")
                .create();
        histogram.observe(100);
        histogram.observe(200);
        registry.register(histogram);

        SampleNameFilter filter = new SampleNameFilter.Builder().build();
        List<Collector.MetricFamilySamples> mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "test_histogram_bucket", 15);
        assertSamplesInclude(mfsList, "test_histogram_count", 1);
        assertSamplesInclude(mfsList, "test_histogram_sum", 1);
        assertSamplesInclude(mfsList, "test_histogram_created", 1);
        assertTotalNumberOfSamples(mfsList, 18);

        filter = new SampleNameFilter.Builder()
                .nameMustStartWith("test_histogram")
                // nameMustStartWith() is for the names[] query parameter in HTTP exporters.
                // If histogram_created is missing in the names[] list, it should not be exported
                // even though includePrefixes matches histogram_created.
                .nameMustBeEqualTo("test_histogram_bucket")
                .nameMustBeEqualTo("test_histogram_count")
                .nameMustBeEqualTo("test_histogram_sum")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "test_histogram_bucket", 15);
        assertSamplesInclude(mfsList, "test_histogram_count", 1);
        assertSamplesInclude(mfsList, "test_histogram_sum", 1);
        assertTotalNumberOfSamples(mfsList, 17);

        filter = new SampleNameFilter.Builder()
                .nameMustStartWith("test_histogram")
                // histogram without buckets
                .nameMustNotStartWith("test_histogram_bucket")
                .build();
        mfsList = Collections.list(registry.filteredMetricFamilySamples(filter));
        assertSamplesInclude(mfsList, "test_histogram_count", 1);
        assertSamplesInclude(mfsList, "test_histogram_sum", 1);
        assertSamplesInclude(mfsList, "test_histogram_created", 1);
        assertTotalNumberOfSamples(mfsList, 3);
    }

    /**
     * Before {@link SampleNameFilter} was introduced, the {@link CollectorRegistry#filteredMetricFamilySamples(Set)}
     * method could be used to pass included names directly. That method still there for compatibility.
     * This is the original test copied over from {@link CollectorRegistryTest}.
     */
    @Test
    public void testLegacyApi() {
        Gauge.build().name("g").help("h").register(registry);
        Counter.build().name("c").help("h").register(registry);
        Summary.build().name("s").help("h").register(registry);
        new EmptyCollector().register(registry);
        SkippedCollector sr = new SkippedCollector().register(registry);
        PartiallyFilterCollector pfr = new PartiallyFilterCollector().register(registry);
        HashSet<String> metrics = new HashSet<String>();
        HashSet<String> series = new HashSet<String>();
        Set<String> includedNames = new HashSet<String>(Arrays.asList("", "s_sum", "c_total", "part_filter_a", "part_filter_c"));
        List<Collector.MetricFamilySamples> mfsList = Collections.list(registry.filteredMetricFamilySamples(includedNames));
        for (Collector.MetricFamilySamples metricFamilySamples : mfsList) {
            metrics.add(metricFamilySamples.name);
            for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
                series.add(sample.name);
            }
        }
        assertEquals(1, sr.collectCallCount);
        assertEquals(2, pfr.collectCallCount);
        assertEquals(new HashSet<String>(Arrays.asList("s", "c", "part_filter_a", "part_filter_c")), metrics);
        assertEquals(new HashSet<String>(Arrays.asList("s_sum", "c_total", "part_filter_a", "part_filter_c")), series);
    }

    private static class EmptyCollector extends Collector {
        public List<MetricFamilySamples> collect() {
            return new ArrayList<MetricFamilySamples>();
        }
    }

    private static class SkippedCollector extends Collector implements Collector.Describable {
        public int collectCallCount = 0;

        @Override
        public List<MetricFamilySamples> collect() {
            collectCallCount++;
            List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
            mfs.add(new GaugeMetricFamily("slow_gauge", "help", 123));
            return mfs;
        }

        @Override
        public List<MetricFamilySamples> describe() {
            return collect();
        }
    }

    private static class PartiallyFilterCollector extends Collector implements Collector.Describable {
        public int collectCallCount = 0;

        @Override
        public List<MetricFamilySamples> collect() {
            collectCallCount++;
            List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
            mfs.add(new GaugeMetricFamily("part_filter_a", "help", 123));
            mfs.add(new GaugeMetricFamily("part_filter_b", "help", 123));
            mfs.add(new GaugeMetricFamily("part_filter_c", "help", 123));
            return mfs;
        }

        @Override
        public List<MetricFamilySamples> describe() {
            return collect();
        }
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