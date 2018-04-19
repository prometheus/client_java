package io.prometheus.client.dropwizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.prometheus.client.Collector;

/**
 * Collect Dropwizard metrics from a MetricRegistry.
 */
public class DropwizardExports extends Collector implements Collector.Describable {
    private static final Logger LOGGER = Logger.getLogger(DropwizardExports.class.getName());

    private final MetricSource source;

    public DropwizardExports(SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        this(new StaticSource(gauges, counters, histograms, meters, timers));
    }

    /**
     * @param registry a metric registry to export in prometheus.
     */
    public DropwizardExports(MetricRegistry registry) {
        this(new RegistrySource(registry));
    }

    private DropwizardExports(MetricSource source) {
        this.source = source;
    }

    /**
     * Export counter as Prometheus <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
     */
    List<MetricFamilySamples> fromCounter(String dropwizardName, Counter counter) {
        String name = sanitizeMetricName(dropwizardName);
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name, new ArrayList<String>(), new ArrayList<String>(),
                new Long(counter.getCount()).doubleValue());
        return Arrays.asList(new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName, counter), Arrays.asList(sample)));
    }

    private static String getHelpMessage(String metricName, Metric metric){
        return String.format("Generated from Dropwizard metric import (metric=%s, type=%s)",
                metricName, metric.getClass().getName());
    }

    /**
     * Export gauge as a prometheus gauge.
     */
    List<MetricFamilySamples> fromGauge(String dropwizardName, Gauge gauge) {
        String name = sanitizeMetricName(dropwizardName);
        Object obj = gauge.getValue();
        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            value = ((Boolean) obj) ? 1 : 0;
        } else {
            LOGGER.log(Level.FINE, String.format("Invalid type for Gauge %s: %s", name,
                obj == null ? "null" : obj.getClass().getName()));
            return new ArrayList<MetricFamilySamples>();
        }
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name,
                new ArrayList<String>(), new ArrayList<String>(), value);
        return Arrays.asList(new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName, gauge), Arrays.asList(sample)));
    }

    /**
     * Export a histogram snapshot as a prometheus SUMMARY.
     *
     * @param dropwizardName metric name.
     * @param snapshot the histogram snapshot.
     * @param count the total sample count for this snapshot.
     * @param factor a factor to apply to histogram values.
     *
     */
    List<MetricFamilySamples> fromSnapshotAndCount(String dropwizardName, Snapshot snapshot, long count, double factor, String helpMessage) {
        String name = sanitizeMetricName(dropwizardName);
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
        String name = sanitizeMetricName(dropwizardName);
        return Arrays.asList(
                new MetricFamilySamples(name + "_total", Type.COUNTER, getHelpMessage(dropwizardName, meter),
                        Arrays.asList(new MetricFamilySamples.Sample(name + "_total",
                                new ArrayList<String>(),
                                new ArrayList<String>(),
                                meter.getCount())))

        );
    }

    private static final Pattern METRIC_NAME_RE = Pattern.compile("[^a-zA-Z0-9:_]");

    /**
     * Replace all unsupported chars with '_', prepend '_' if name starts with digit.
     *
     * @param dropwizardName
     *            original metric name.
     * @return the sanitized metric name.
     */
    public static String sanitizeMetricName(String dropwizardName) {
        String name = METRIC_NAME_RE.matcher(dropwizardName).replaceAll("_");
        if (!name.isEmpty() && Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        return name;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        ArrayList<MetricFamilySamples> mfSamples = new ArrayList<MetricFamilySamples>();
        for (SortedMap.Entry<String, Gauge> entry : source.getGauges().entrySet()) {
            mfSamples.addAll(fromGauge(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Counter> entry : source.getCounters().entrySet()) {
            mfSamples.addAll(fromCounter(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Histogram> entry : source.getHistograms().entrySet()) {
            mfSamples.addAll(fromHistogram(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Timer> entry : source.getTimers().entrySet()) {
            mfSamples.addAll(fromTimer(entry.getKey(), entry.getValue()));
        }
        for (SortedMap.Entry<String, Meter> entry : source.getMeters().entrySet()) {
            mfSamples.addAll(fromMeter(entry.getKey(), entry.getValue()));
        }
        return mfSamples;
    }

    @Override
    public List<MetricFamilySamples> describe() {
      return new ArrayList<MetricFamilySamples>();
    }

    private interface MetricSource {
        SortedMap<String, Gauge> getGauges();
        SortedMap<String, Counter> getCounters();
        SortedMap<String, Histogram> getHistograms();
        SortedMap<String, Meter> getMeters();
        SortedMap<String, Timer> getTimers();
    }

    private static class StaticSource implements MetricSource {
        private final SortedMap<String, Gauge> gauges;
        private final SortedMap<String, Counter> counters;
        private final SortedMap<String, Histogram> histograms;
        private final SortedMap<String, Meter> meters;
        private final SortedMap<String, Timer> timers;

        StaticSource(SortedMap<String, Gauge> gauges,
                SortedMap<String, Counter> counters,
                SortedMap<String, Histogram> histograms,
                SortedMap<String, Meter> meters,
                SortedMap<String, Timer> timers) {
            this.gauges = gauges;
            this.counters = counters;
            this.histograms = histograms;
            this.meters = meters;
            this.timers = timers;
        }

        @Override
        public SortedMap<String, Gauge> getGauges() {
            return gauges;
        }

        @Override
        public SortedMap<String, Counter> getCounters() {
            return counters;
        }

        @Override
        public SortedMap<String, Histogram> getHistograms() {
            return histograms;
        }

        @Override
        public SortedMap<String, Meter> getMeters() {
            return meters;
        }

        @Override
        public SortedMap<String, Timer> getTimers() {
            return timers;
        }
    }

    private static class RegistrySource implements MetricSource {

        private final MetricRegistry registry;

        RegistrySource(MetricRegistry registry) {
            this.registry = registry;
        }

        @Override
        public SortedMap<String, Gauge> getGauges() {
            return registry.getGauges();
        }

        @Override
        public SortedMap<String, Counter> getCounters() {
            return registry.getCounters();
        }

        @Override
        public SortedMap<String, Histogram> getHistograms() {
            return registry.getHistograms();
        }

        @Override
        public SortedMap<String, Meter> getMeters() {
            return registry.getMeters();
        }

        @Override
        public SortedMap<String, Timer> getTimers() {
            return registry.getTimers();
        }
    }
}
