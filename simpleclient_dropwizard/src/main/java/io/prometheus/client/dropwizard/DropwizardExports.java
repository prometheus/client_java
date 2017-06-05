package io.prometheus.client.dropwizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * Collect Dropwizard metrics from a MetricRegistry.
 */
public class DropwizardExports extends io.prometheus.client.Collector implements io.prometheus.client.Collector.Describable {
    private MetricRegistry registry;
    private static final Logger LOGGER = Logger.getLogger(DropwizardExports.class.getName());

    /**
     * @param registry a metric registry to export in prometheus.
     */
    public DropwizardExports(MetricRegistry registry) {
        this.registry = registry;
    }

    protected DropwizardLabels findLabels(String dropwizardName) {
        Map<String, String> labels = new HashMap<String, String>();
        String nameWithoutLabels = dropwizardName;
        if (dropwizardName.indexOf("=\"") > 0) {
            Pattern pattern = Pattern.compile(".*_([^=]+)=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(dropwizardName);
            while (matcher.matches()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                labels.put(key, value);
                String keyValue = "_" + key + "=\"" + value + "\"";
                nameWithoutLabels = nameWithoutLabels.replace(keyValue, "");
                matcher = pattern.matcher(nameWithoutLabels);
            }
        }
        
        return new DropwizardLabels(nameWithoutLabels, labels);
    }

    /**
     * Export counter as Prometheus <a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Gauge</a>.
     */
    MetricFamilySamples fromCounter(String dropwizardName, Counter counter) {
        DropwizardLabels dkv = findLabels(dropwizardName);
        String name = sanitizeMetricName(dkv.dropwizardName());
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name, dkv.keys(), dkv.values(),
                new Long(counter.getCount()).doubleValue());
        List<MetricFamilySamples.Sample> sampleAsList = new ArrayList<MetricFamilySamples.Sample>();
        sampleAsList.add(sample);
        return new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName, counter), sampleAsList);
    }

    private static String getHelpMessage(String metricName, Metric metric){
        return String.format("Generated from Dropwizard metric import (metric=%s, type=%s)",
                metricName, metric.getClass().getName());
    }

    /**
     * Export gauge as a prometheus gauge.
     */
    MetricFamilySamples fromGauge(String dropwizardName, Gauge gauge) {
        DropwizardLabels dkv = findLabels(dropwizardName);
        String name = sanitizeMetricName(dkv.dropwizardName());
        Object obj = gauge.getValue();
        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            value = ((Boolean) obj) ? 1 : 0;
        } else {
            LOGGER.log(Level.FINE, String.format("Invalid type for Gauge %s: %s", name,
                    obj.getClass().getName()));
            return null;
        }
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name,
                dkv.keys(), dkv.values(), value);
        List<MetricFamilySamples.Sample> sampleAsList = new ArrayList<MetricFamilySamples.Sample>();
        sampleAsList.add(sample);
        return new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName, gauge), sampleAsList);
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
    MetricFamilySamples fromSnapshotAndCount(DropwizardLabels dkv, Type type, Snapshot snapshot, long count, double factor, String helpMessage) {
        String name = sanitizeMetricName(dkv.dropwizardName());
        List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>(Arrays.asList(
                new MetricFamilySamples.Sample(name, dkv.prependKey("quantile"), dkv.prependValue("0.5"), snapshot.getMedian() * factor),
                new MetricFamilySamples.Sample(name, dkv.prependKey("quantile"), dkv.prependValue("0.75"), snapshot.get75thPercentile() * factor),
                new MetricFamilySamples.Sample(name, dkv.prependKey("quantile"), dkv.prependValue("0.95"), snapshot.get95thPercentile() * factor),
                new MetricFamilySamples.Sample(name, dkv.prependKey("quantile"), dkv.prependValue("0.98"), snapshot.get98thPercentile() * factor),
                new MetricFamilySamples.Sample(name, dkv.prependKey("quantile"), dkv.prependValue("0.99"), snapshot.get99thPercentile() * factor),
                new MetricFamilySamples.Sample(name, dkv.prependKey("quantile"), dkv.prependValue("0.999"), snapshot.get999thPercentile() * factor),
                new MetricFamilySamples.Sample(name + "_mean", dkv.keys(), dkv.values(), snapshot.getMean()),
                new MetricFamilySamples.Sample(name + "_max", dkv.keys(), dkv.values(), snapshot.getMax()),
                new MetricFamilySamples.Sample(name + "_min", dkv.keys(), dkv.values(), snapshot.getMin()),
                new MetricFamilySamples.Sample(name + "_sum", dkv.keys(), dkv.values(), snapshot.getMean()*count),
                new MetricFamilySamples.Sample(name + "_count", dkv.keys(), dkv.values(), count)
        ));
        return new MetricFamilySamples(name, type, helpMessage, samples);
    }

    /**
     * Convert histogram snapshot.
     */
    MetricFamilySamples fromHistogram(String dropwizardName, Histogram histogram) {
        DropwizardLabels dkv = findLabels(dropwizardName);
        return fromSnapshotAndCount(dkv, Type.SUMMARY, histogram.getSnapshot(), histogram.getCount(), 1.0,
                getHelpMessage(dropwizardName, histogram));
    }

    /**
     * Export Dropwizard Timer as a histogram. Use TIME_UNIT as time unit.
     */
    MetricFamilySamples fromTimer(String dropwizardName, Timer timer) {
        DropwizardLabels dkv = findLabels(dropwizardName);
        MetricFamilySamples metrics = fromSnapshotAndCount(dkv, Type.SUMMARY, timer.getSnapshot(), timer.getCount(),
                1.0D / TimeUnit.SECONDS.toNanos(1L), getHelpMessage(dropwizardName, timer));
        String name = sanitizeMetricName(dkv.dropwizardName());
        metrics.samples.addAll(Arrays.asList(
                new MetricFamilySamples.Sample(name + "_meanRate", dkv.keys(), dkv.values(), timer.getMeanRate()),
                new MetricFamilySamples.Sample(name + "_fifteenMinuteRate", dkv.keys(), dkv.values(), timer.getFifteenMinuteRate()),
                new MetricFamilySamples.Sample(name + "_fiveMinuteRate", dkv.keys(), dkv.values(), timer.getFiveMinuteRate()),
                new MetricFamilySamples.Sample(name + "_oneMinuteRate", dkv.keys(), dkv.values(), timer.getOneMinuteRate())
            ));
        return metrics;
    }

    /**
     * Export a Meter as as prometheus COUNTER.
     */
    MetricFamilySamples fromMeter(String dropwizardName, Meter meter) {
        DropwizardLabels dkv = findLabels(dropwizardName);
        String name = sanitizeMetricName(dkv.dropwizardName());
        return new MetricFamilySamples(name + "_total", Type.COUNTER, getHelpMessage(dropwizardName, meter),
                        Arrays.asList(
                                new MetricFamilySamples.Sample(name + "_total", dkv.keys(), dkv.values(), meter.getCount()),
                                new MetricFamilySamples.Sample(name + "_meanRate", dkv.keys(), dkv.values(), meter.getMeanRate()),
                                new MetricFamilySamples.Sample(name + "_fifteenMinuteRate", dkv.keys(), dkv.values(), meter.getFifteenMinuteRate()),
                                new MetricFamilySamples.Sample(name + "_fiveMinuteRate", dkv.keys(), dkv.values(), meter.getFiveMinuteRate()),
                                new MetricFamilySamples.Sample(name + "_oneMinuteRate", dkv.keys(), dkv.values(), meter.getOneMinuteRate())
                        )
        );
    }

    /**
     * Replace all unsupported chars with '_'.
     *
     * @param dropwizardName original metric name.
     * @return the sanitized metric name.
     */
    public static String sanitizeMetricName(String dropwizardName){
        return dropwizardName.replaceAll("[^a-zA-Z0-9:_]", "_");
    }

    @Override
    public List<MetricFamilySamples> collect() {
        Map<String, MetricFamilySamples> uniqueSamples = new HashMap<String, MetricFamilySamples>();
        for (SortedMap.Entry<String, Gauge> entry : registry.getGauges().entrySet()) {
            MetricFamilySamples gaugeSamples = fromGauge(entry.getKey(), entry.getValue());
            mergeSample(uniqueSamples, gaugeSamples);
        }
        for (SortedMap.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
            MetricFamilySamples counterSamples = fromCounter(entry.getKey(), entry.getValue()); 
            mergeSample(uniqueSamples, counterSamples);
        }
        for (SortedMap.Entry<String, Histogram> entry : registry.getHistograms().entrySet()) {
            MetricFamilySamples histogramSamples = fromHistogram(entry.getKey(), entry.getValue()); 
            mergeSample(uniqueSamples, histogramSamples);
        }
        for (SortedMap.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
            MetricFamilySamples timerSamples = fromTimer(entry.getKey(), entry.getValue()); 
            mergeSample(uniqueSamples, timerSamples);
        }
        for (SortedMap.Entry<String, Meter> entry : registry.getMeters().entrySet()) {
            MetricFamilySamples meterSamples = fromMeter(entry.getKey(), entry.getValue()); 
            mergeSample(uniqueSamples, meterSamples);
        }
        return new ArrayList<MetricFamilySamples>(uniqueSamples.values());
    }

    private void mergeSample(Map<String, MetricFamilySamples> existingSamples, MetricFamilySamples newSamples) {
        if (existingSamples == null || newSamples == null) {
            return;
        }
        MetricFamilySamples existingSample = existingSamples.get(newSamples.name);
        if (existingSample != null) {
            existingSample.samples.addAll(newSamples.samples);
        } else {
            existingSamples.put(newSamples.name, newSamples);
        }
    }

    @Override
    public List<MetricFamilySamples> describe() {
      return new ArrayList<MetricFamilySamples>();
    }
    
    private static class DropwizardLabels {
        private String dropwizardName;
        private List<String> keys = new ArrayList<String>();
        private List<String> values = new ArrayList<String>();
        
        public DropwizardLabels(String dropwizardName, Map<String, String> labels) {
            this.dropwizardName = dropwizardName;
            for (Map.Entry<String, String> mapEntry : labels.entrySet()) {
                keys.add(mapEntry.getKey());
                values.add(mapEntry.getValue());
            }
        }
        
        public List<String> prependValue(String newValue) {
            List<String> newValues = new ArrayList<String>(2);
            newValues.add(newValue);
            newValues.addAll(values());
            return newValues;
        }
        
        public List<String> prependKey(String newKey) {
            List<String> newKeys = new ArrayList<String>(2);
            newKeys.add(newKey);
            newKeys.addAll(keys());
            return newKeys;
        }
        
        public List<String> keys() {return keys;}
        public List<String> values() {return values;}
        public String dropwizardName() {return dropwizardName;}
    }
}
