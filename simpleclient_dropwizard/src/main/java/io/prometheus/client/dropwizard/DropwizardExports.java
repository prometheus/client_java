package io.prometheus.client.dropwizard;

import com.codahale.metrics.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Collect Dropwizard metrics from a MetricRegistry.
 */
public class DropwizardExports extends io.prometheus.client.Collector implements io.prometheus.client.Collector.Describable {
    private MetricRegistry registry;
    private static final Logger LOGGER = Logger.getLogger(DropwizardExports.class.getName());

    private List<String> labelNames;
    private List<String> labelValues;
    /**
     * @param registry a metric registry to export in prometheus.
     */
    public DropwizardExports(MetricRegistry registry) {
        this.registry = registry;
        labelNames = new ArrayList<String>();
        labelValues = new ArrayList<String>();
    }

    /**
     * Add a label name and value to exported metrics
     *
     * @param labelName
     * @param labelValue
     */
    public void addLabel(String labelName, String labelValue) {
        labelNames.add(labelName);
        labelValues.add(labelValue);
    }

    /**
     * Export counter as Prometheus <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
     */
    List<MetricFamilySamples> fromCounter(String dropwizardName, Counter counter) {
        String name = sanitizeMetricName(dropwizardName);
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name, labelNames, labelValues,
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
                labelNames, labelValues, value);
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

        List<String> snapshotLabelNames = new ArrayList<String>(labelNames);
        snapshotLabelNames.add("quantile");

        List<String> snapshotLabelValues5 = new ArrayList<String>(labelValues);
        List<String> snapshotLabelValues75 = new ArrayList<String>(labelValues);
        List<String> snapshotLabelValues95 = new ArrayList<String>(labelValues);
        List<String> snapshotLabelValues98 = new ArrayList<String>(labelValues);
        List<String> snapshotLabelValues99 = new ArrayList<String>(labelValues);
        List<String> snapshotLabelValues999 = new ArrayList<String>(labelValues);

        snapshotLabelValues5.add("0.5");
        snapshotLabelValues75.add("0.75");
        snapshotLabelValues95.add("0.95");
        snapshotLabelValues98.add("0.98");
        snapshotLabelValues99.add("0.99");
        snapshotLabelValues999.add("0.999");

        List<MetricFamilySamples.Sample> samples = Arrays.asList(
                new MetricFamilySamples.Sample(name, snapshotLabelNames, snapshotLabelValues5, snapshot.getMedian() * factor),
                new MetricFamilySamples.Sample(name, snapshotLabelNames, snapshotLabelValues75, snapshot.get75thPercentile() * factor),
                new MetricFamilySamples.Sample(name, snapshotLabelNames, snapshotLabelValues95, snapshot.get95thPercentile() * factor),
                new MetricFamilySamples.Sample(name, snapshotLabelNames, snapshotLabelValues98, snapshot.get98thPercentile() * factor),
                new MetricFamilySamples.Sample(name, snapshotLabelNames, snapshotLabelValues99, snapshot.get99thPercentile() * factor),
                new MetricFamilySamples.Sample(name, snapshotLabelNames, snapshotLabelValues999, snapshot.get999thPercentile() * factor),
                new MetricFamilySamples.Sample(name + "_count", labelNames, labelValues, count)
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
                                labelNames,
                                labelValues,
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
