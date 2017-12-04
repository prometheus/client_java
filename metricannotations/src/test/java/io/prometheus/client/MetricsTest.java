package io.prometheus.client;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;

import java.util.Enumeration;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class MetricsTest {
    @Rule
    public TestWatcher failureWatcher = new TestWatcher() {
        @Override
        protected void failed(final Throwable e, final Description description){
            printCounters();
        }
    };

    public static void printCounters() {
        final Enumeration<Collector.MetricFamilySamples> samples
                = defaultRegistry.metricFamilySamples();
        while (samples.hasMoreElements()) {
            final Collector.MetricFamilySamples element = samples.nextElement();
            System.out.println("name: " + element.name);
            System.out.println("help: " + element.help);
            System.out.println("type: " + element.type);
            System.out.println("samples: ");
            for (Collector.MetricFamilySamples.Sample sample : element.samples) {
                if (sample.labelNames.size() > 0) {
                    System.out.println("\t" + sample.labelNames);
                    System.out.println("\t" + sample.labelValues);
                }
                System.out.println("\t" + sample.name + " " + sample.value);
            }
        }
    }

}
