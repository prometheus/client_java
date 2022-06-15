package io.prometheus.client;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class EmptyDescribableTest {

    static class TestCollector extends Collector implements Collector.Describable {

        private final List<MetricFamilySamples> describeResult;

        TestCollector(List<MetricFamilySamples> describeResult) {
            this.describeResult = describeResult;
        }

        @Override
        public List<MetricFamilySamples> collect() {
            List<String> labelNames = Arrays.asList("label1", "label2");
            List<String> labelValues1 = Arrays.asList("a", "b");
            List<String> labelValues2 = Arrays.asList("c", "d");
            List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>(2);
            samples.add(new MetricFamilySamples.Sample("my_metric", labelNames, labelValues1, 1.0));
            samples.add(new MetricFamilySamples.Sample("my_metric", labelNames, labelValues2, 2.0));
            MetricFamilySamples mfs = new MetricFamilySamples("my_metric", "", Type.UNKNOWN, "help text", samples);
            return Collections.singletonList(mfs);
        }

        @Override
        public List<MetricFamilySamples> describe() {
            return describeResult;
        }
    }

    @Test
    public void testEmptyDescribe() {
        CollectorRegistry registry = new CollectorRegistry();
        TestCollector collector = new TestCollector(new ArrayList<Collector.MetricFamilySamples>());
        collector.register(registry);
        Set<String> includedNames = new HashSet<String>(Collections.singletonList("my_metric"));
        List<Collector.MetricFamilySamples> mfs = Collections.list(registry.filteredMetricFamilySamples(includedNames));
        assertEquals(1, mfs.size());
        assertEquals(2, mfs.get(0).samples.size());
    }
}
