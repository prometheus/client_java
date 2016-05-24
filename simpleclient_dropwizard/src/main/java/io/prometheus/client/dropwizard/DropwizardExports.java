package io.prometheus.client.dropwizard;

import com.codahale.metrics.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collect dropwizard metrics from a MetricRegistry.
 */
public class DropwizardExports extends io.prometheus.client.Collector {
    private MetricRegistry registry;
    private static final Logger LOGGER = Logger.getLogger(DropwizardExports.class.getName());

    /**
     * @param registry a metric registry to export in prometheus.
     */
    public DropwizardExports(MetricRegistry registry) {
        this.registry = registry;
    }

    /**
     * Export counter as Prometheus <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
     */
    List<MetricFamilySamples> fromCounter(String name, Counter counter) {
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name, new ArrayList<String>(), new ArrayList<String>(),
                new Long(counter.getCount()).doubleValue());
        return Arrays.asList(new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(name, counter), Arrays.asList(sample)));
    }

    private static String getHelpMessage(String metricName, Metric metric){
        return String.format("Generated from dropwizard metric import (metric=%s, type=%s)",
                metricName, metric.getClass().getName());
    }

    /**
     * Export gauge as a prometheus gauge.
     */
    List<MetricFamilySamples> fromGauge(String name, Gauge gauge) {
        Object obj = gauge.getValue();
        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            value = ((Boolean) obj) ? 1 : 0;
        } else {
            LOGGER.log(Level.FINE, String.format("Invalid type for Gauge %s: %s", name,
                    obj.getClass().getName()));
            return new ArrayList<MetricFamilySamples>();
        }
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name,
                new ArrayList<String>(), new ArrayList<String>(), value);
        return Arrays.asList(new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(name, gauge), Arrays.asList(sample)));
    }

    /**
     * Export a histogram snapshot as a prometheus SUMMARY.
     *
     * @param name metric name.
     * @param snapshot the histogram snapshot.
     * @param count the total sample count for this snapshot.
     * @param factor a factor to apply to histogram values.
     *
     */
    List<MetricFamilySamples> fromSnapshotAndCount(String name, Snapshot snapshot, long count, double factor, String helpMessage) {
        List<MetricFamilySamples.Sample> samples = Arrays.asList(
                new MetricFamilySamples.Sample(name, Arrays.asList("quantile"), Arrays.asList("0.5"), snapshot.getMedian() * factor),
                new MetricFamilySamples.Sample(name, Arrays.asList("quantile"), Arrays.asList("0.75"), snapshot.get75thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Arrays.asList("quantile"), Arrays.asList("0.95"), snapshot.get95thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Arrays.asList("quantile"), Arrays.asList("0.98"), snapshot.get98thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Arrays.asList("quantile"), Arrays.asList("0.99"), snapshot.get99thPercentile() * factor),
                new MetricFamilySamples.Sample(name, Arrays.asList("quantile"), Arrays.asList("0.999"), snapshot.get999thPercentile() * factor),
                new MetricFamilySamples.Sample(name + "_count", new ArrayList<String>(), new ArrayList<String>(), count)
        );
        return Arrays.asList(
                new MetricFamilySamples(name, Type.SUMMARY, helpMessage, samples)
        );
    }

    /**
     * Convert histogram snapshot.
     */
    List<MetricFamilySamples> fromHistogram(String name, Histogram histogram) {
        return fromSnapshotAndCount(name, histogram.getSnapshot(), histogram.getCount(), 1.0,
                getHelpMessage(name, histogram));
    }

    /**
     * Export dropwizard Timer as a histogram. Use TIME_UNIT as time unit.
     */
    List<MetricFamilySamples> fromTimer(String name, Timer timer) {
        return fromSnapshotAndCount(name, timer.getSnapshot(), timer.getCount(),
                1.0D / TimeUnit.SECONDS.toNanos(1L), getHelpMessage(name, timer));
    }

    /**
     * Export a Meter as as prometheus COUNTER.
     */
    List<MetricFamilySamples> fromMeter(String name, Meter meter) {
        return Arrays.asList(
                new MetricFamilySamples(name + "_total", Type.COUNTER, getHelpMessage(name, meter),
                        Arrays.asList(new MetricFamilySamples.Sample(name + "_total",
                                new ArrayList<String>(),
                                new ArrayList<String>(),
                                meter.getCount())))

        );
    }

    /**
     * Replace all unsupported chars with '_'.
     *
     * @param name metric name.
     * @return the sanitized metric name.
     */
    public static String sanitizeMetricName(String name){
        return name.replaceAll("[^a-zA-Z0-9:_]", "_");
    }

    @Override
    public List<MetricFamilySamples> collect() {
        ArrayList<MetricFamilySamples> mfSamples = new ArrayList<MetricFamilySamples>();
        for (SortedMap.Entry<String, Gauge> entry : registry.getGauges().entrySet()) {
            mfSamples.addAll(fromGauge(sanitizeMetricName(entry.getKey()), entry.getValue()));
        }
        for (SortedMap.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
            mfSamples.addAll(fromCounter(sanitizeMetricName(entry.getKey()), entry.getValue()));
        }
        for (SortedMap.Entry<String, Histogram> entry : registry.getHistograms().entrySet()) {
            mfSamples.addAll(fromHistogram(sanitizeMetricName(entry.getKey()), entry.getValue()));
        }
        for (SortedMap.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            mfSamples.addAll(fromTimer(sanitizeMetricName(entry.getKey()), entry.getValue()));
        }
        for (SortedMap.Entry<String, Meter> entry : registry.getMeters().entrySet()) {
            mfSamples.addAll(fromMeter(sanitizeMetricName(entry.getKey()), entry.getValue()));
        }
        return mfSamples;
    }
}
