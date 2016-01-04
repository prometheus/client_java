package io.prometheus.client.dropwizard;


import com.codahale.metrics.*;
import io.prometheus.client.exporter.common.MetricMapper;

import java.io.Reader;
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
    private MetricMapper metricMapper;
    private static final Logger LOGGER = Logger.getLogger(DropwizardExports.class.getName());

    /**
     * @param registry   a metric registry to export in prometheus.
     * @param yamlConfig a yaml mapping configuration
     */
    DropwizardExports(MetricRegistry registry, String yamlConfig) {
        this.registry = registry;
        this.metricMapper = MetricMapper.load(yamlConfig);
    }

    /**
     * @param registry   a metric registry to export in prometheus.
     * @param yamlReader a yaml configuration reader.
     */
    DropwizardExports(MetricRegistry registry, Reader yamlReader) {
        this.registry = registry;
        this.metricMapper = MetricMapper.load(yamlReader);
    }

    /**
     * @param registry a metric registry to export in prometheus.
     */
    DropwizardExports(MetricRegistry registry) {
        this.registry = registry;
        this.metricMapper = MetricMapper.load();
    }

    /**
     * Export counter as prometheus counter.
     */
    List<MetricFamilySamples> fromCounter(String name, Counter counter) {
        MetricMapper.MetricMapping mapping = metricMapper.map(name);
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(name, mapping.getLabelNames(),
                mapping.getLabelValues(), new Long(counter.getCount()).doubleValue());
        return Arrays.asList(new MetricFamilySamples(name, Type.GAUGE, mapping.getHelp(), Arrays.asList(sample)));
    }

    /**
     * Export gauge as a prometheus gauge.
     */
    List<MetricFamilySamples> fromGauge(String name, Gauge gauge) {
        MetricMapper.MetricMapping mapping = metricMapper.map(name);
        Object obj = gauge.getValue();
        Double value;
        if (obj instanceof Integer) {
            value = Double.valueOf(((Integer) obj).doubleValue());
        } else if (obj instanceof Double) {
            value = (Double) obj;
        } else if (obj instanceof Float) {
            value = Double.valueOf(((Float) obj).doubleValue());
        } else if (obj instanceof Long) {
            value = Double.valueOf(((Long) obj).doubleValue());
        } else {
            LOGGER.log(Level.FINE, String.format("Invalid type for Gauge %s: %s", name,
                    obj.getClass().getName()));
            return new ArrayList<MetricFamilySamples>();
        }
        MetricFamilySamples.Sample sample = new MetricFamilySamples.Sample(mapping.getName(),
                mapping.getLabelNames(), mapping.getLabelValues(), value);
        return Arrays.asList(new MetricFamilySamples(name, Type.GAUGE, mapping.getHelp(), Arrays.asList(sample)));
    }

    private static List<String> mergeList(List<String> list, String... elements) {
        List<String> targetList = new ArrayList<String>(list);
        targetList.addAll(Arrays.asList(elements));
        return targetList;
    }

    /**
     * Export a histogram snapshot as a prometheus SUMMARY.
     *
     * @param name     metric name.
     * @param snapshot the histogram snapshot.
     * @param count    the total sample count for this snapshot.
     * @param factor   a factor to apply to histogram values.
     */
    List<MetricFamilySamples> fromSnapshotAndCount(String name, Snapshot snapshot, long count, double factor) {
        long sum = 0;
        for (long i : snapshot.getValues()) {
            sum += i;
        }
        MetricMapper.MetricMapping mapping = metricMapper.map(name);
        List<MetricFamilySamples.Sample> samples = Arrays.asList(
                new MetricFamilySamples.Sample(mapping.getName(), mergeList(mapping.getLabelNames(), "quantile"),
                        mergeList(mapping.getLabelValues(), "0.5"), snapshot.getMedian() * factor),
                new MetricFamilySamples.Sample(mapping.getName(), mergeList(mapping.getLabelNames(), "quantile"),
                        mergeList(mapping.getLabelValues(), "0.75"), snapshot.get75thPercentile() * factor),
                new MetricFamilySamples.Sample(mapping.getName(), mergeList(mapping.getLabelNames(), "quantile"),
                        mergeList(mapping.getLabelValues(), "0.95"), snapshot.get95thPercentile() * factor),
                new MetricFamilySamples.Sample(mapping.getName(), mergeList(mapping.getLabelNames(), "quantile"),
                        mergeList(mapping.getLabelValues(), "0.98"), snapshot.get98thPercentile() * factor),
                new MetricFamilySamples.Sample(mapping.getName(), mergeList(mapping.getLabelNames(), "quantile"),
                        mergeList(mapping.getLabelValues(), "0.99"), snapshot.get99thPercentile() * factor),
                new MetricFamilySamples.Sample(mapping.getName(), mergeList(mapping.getLabelNames(), "quantile"),
                        mergeList(mapping.getLabelValues(), "0.999"), snapshot.get999thPercentile() * factor),
                new MetricFamilySamples.Sample(mapping.getName() + "_count", mapping.getLabelNames(),
                        mapping.getLabelValues(), count),
                new MetricFamilySamples.Sample(mapping.getName() + "_sum", mapping.getLabelNames(),
                        mapping.getLabelValues(), sum * factor)
        );
        return Arrays.asList(
                new MetricFamilySamples(name, Type.SUMMARY, mapping.getHelp(), samples)
        );
    }

    /**
     * Convert histogram snapshot.
     */
    List<MetricFamilySamples> fromHistogram(String name, Histogram histogram) {
        return fromSnapshotAndCount(name, histogram.getSnapshot(), histogram.getCount(), 1.0);
    }

    /**
     * Export dropwizard Timer as a histogram. Use TIME_UNIT as time unit.
     */
    List<MetricFamilySamples> fromTimer(String name, Timer timer) {
        return fromSnapshotAndCount(name, timer.getSnapshot(), timer.getCount(),
                1.0D / TimeUnit.SECONDS.toNanos(1L));
    }

    /**
     * Export a Meter as as prometheus COUNTER.
     */
    List<MetricFamilySamples> fromMeter(String name, Meter meter) {
        MetricMapper.MetricMapping mapping = metricMapper.map(name);
        return Arrays.asList(
                new MetricFamilySamples(name + "_total", Type.COUNTER, mapping.getHelp(),
                        Arrays.asList(new MetricFamilySamples.Sample(name + "_total",
                                mapping.getLabelNames(),
                                mapping.getLabelValues(),
                                meter.getCount())))

        );
    }

    /**
     * Replace all unsupported chars with '_'.
     *
     * @param name metric name.
     * @return the sanitized metric name.
     */
    public static String sanitizeMetricName(String name) {
        return name.replaceAll("[^a-zA-Z0-9:_]", "_");
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
}
