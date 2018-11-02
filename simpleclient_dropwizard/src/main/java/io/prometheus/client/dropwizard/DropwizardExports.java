package io.prometheus.client.dropwizard;

import com.codahale.metrics.*;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.impl.DefaultSampleBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collect Dropwizard metrics from a MetricRegistry.
 */
public class DropwizardExports extends io.prometheus.client.Collector implements io.prometheus.client.Collector.Describable {
    private static final Logger LOGGER = Logger.getLogger(DropwizardExports.class.getName());
    private MetricRegistry registry;
    private SampleBuilder sampleBuilder;

    /**
     * Creates a new DropwizardExports with a {@link DefaultSampleBuilder}.
     *
     * @param registry a metric registry to export in prometheus.
     */
    public DropwizardExports(MetricRegistry registry) {
        this.registry = registry;
        this.sampleBuilder = new DefaultSampleBuilder();
    }

    /**
     * @param registry      a metric registry to export in prometheus.
     * @param sampleBuilder sampleBuilder to use to create prometheus samples.
     */
    public DropwizardExports(MetricRegistry registry, SampleBuilder sampleBuilder) {
        this.registry = registry;
        this.sampleBuilder = sampleBuilder;
    }

    private static String getHelpMessage(String metricName, Metric metric) {
        return String.format("Generated from Dropwizard metric import (metric=%s, type=%s)",
                metricName, metric.getClass().getName());
    }

    /**
     * Export counter as Prometheus <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
     */
    List<MetricFamilySamples> fromCounter(String dropwizardName, Counter counter) {
        MetricFamilySamples.Sample sample = sampleBuilder.createSample(dropwizardName, "", new ArrayList<String>(), new ArrayList<String>(),
                new Long(counter.getCount()).doubleValue());
        return Arrays.asList(new MetricFamilySamples(sample.name, Type.GAUGE, getHelpMessage(dropwizardName, counter), Arrays.asList(sample)));
    }

    /**
     * Export gauge as a prometheus gauge.
     */
    List<MetricFamilySamples> fromGauge(String dropwizardName, Gauge gauge) {
        Object obj = gauge.getValue();
        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            value = ((Boolean) obj) ? 1 : 0;
        } else {
            LOGGER.log(Level.FINE, String.format("Invalid type for Gauge %s: %s", sanitizeMetricName(dropwizardName),
                    obj == null ? "null" : obj.getClass().getName()));
            return new ArrayList<MetricFamilySamples>();
        }
        MetricFamilySamples.Sample sample = sampleBuilder.createSample(dropwizardName, "",
                new ArrayList<String>(), new ArrayList<String>(), value);
        return Arrays.asList(new MetricFamilySamples(sample.name, Type.GAUGE, getHelpMessage(dropwizardName, gauge), Arrays.asList(sample)));
    }

    /**
     * Export a histogram snapshot as a prometheus SUMMARY.
     *
     * @param dropwizardName metric name.
     * @param snapshot       the histogram snapshot.
     * @param count          the total sample count for this snapshot.
     * @param factor         a factor to apply to histogram values.
     */
    List<MetricFamilySamples> fromSnapshotAndCount(String dropwizardName, Snapshot snapshot, long count, double factor, String helpMessage) {
        List<MetricFamilySamples.Sample> samples = Arrays.asList(
                sampleBuilder.createSample(dropwizardName, "", Arrays.asList("quantile"), Arrays.asList("0.5"), snapshot.getMedian() * factor),
                sampleBuilder.createSample(dropwizardName, "", Arrays.asList("quantile"), Arrays.asList("0.75"), snapshot.get75thPercentile() * factor),
                sampleBuilder.createSample(dropwizardName, "", Arrays.asList("quantile"), Arrays.asList("0.95"), snapshot.get95thPercentile() * factor),
                sampleBuilder.createSample(dropwizardName, "", Arrays.asList("quantile"), Arrays.asList("0.98"), snapshot.get98thPercentile() * factor),
                sampleBuilder.createSample(dropwizardName, "", Arrays.asList("quantile"), Arrays.asList("0.99"), snapshot.get99thPercentile() * factor),
                sampleBuilder.createSample(dropwizardName, "", Arrays.asList("quantile"), Arrays.asList("0.999"), snapshot.get999thPercentile() * factor),
                sampleBuilder.createSample(dropwizardName, "_count", new ArrayList<String>(), new ArrayList<String>(), count)
        );
        return Arrays.asList(
                new MetricFamilySamples(samples.get(0).name, Type.SUMMARY, helpMessage, samples)
        );
    }

    /**
     * Convert histogram snapshot.
     */
    List<MetricFamilySamples> fromHistogram(String dropwizardName, Histogram histogram) {
        return fromSnapshotAndCount(dropwizardName, histogram.getSnapshot(), histogram.getCount(), 1.0,
                getHelpMessage(dropwizardName, histogram));
    }

    /**
     * Export Dropwizard Timer as a histogram. Use TIME_UNIT as time unit.
     */
    List<MetricFamilySamples> fromTimer(String dropwizardName, Timer timer) {
        return fromSnapshotAndCount(dropwizardName, timer.getSnapshot(), timer.getCount(),
                1.0D / TimeUnit.SECONDS.toNanos(1L), getHelpMessage(dropwizardName, timer));
    }

    /**
     * Export a Meter as as prometheus COUNTER.
     */
    List<MetricFamilySamples> fromMeter(String dropwizardName, Meter meter) {
        final MetricFamilySamples.Sample sample = sampleBuilder.createSample(dropwizardName, "_total",
                new ArrayList<String>(),
                new ArrayList<String>(),
                meter.getCount());
        return Arrays.asList(
                new MetricFamilySamples(sample.name, Type.COUNTER, getHelpMessage(dropwizardName, meter),
                        Arrays.asList(sample))

        );
    }

    @Override
    public List<MetricFamilySamples> collect() {
        ArrayList<MetricFamilySamples> mfSamples = new ArrayList<MetricFamilySamples>();
        for (SortedMap.Entry<String, Gauge> entry : registry.getGauges().entrySet()) {
            mfSamples.addAll(fromGauge(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
            mfSamples.addAll(fromCounter(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Histogram> entry : registry.getHistograms().entrySet()) {
            mfSamples.addAll(fromHistogram(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            mfSamples.addAll(fromTimer(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Meter> entry : registry.getMeters().entrySet()) {
            mfSamples.addAll(fromMeter(entry.getKey(), entry.getValue()));
        }
        return mfSamples;
    }

    @Override
    public List<MetricFamilySamples> describe() {
        return new ArrayList<MetricFamilySamples>();
    }
}