package io.prometheus.metrics.simpleclient.bridge;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Bridge from {@code simpleclient} (version 0.16.0 and older) to the new {@code prometheus-metrics} (version 1.0.0 and newer).
 * <p>
 * Usage: The following line will register all metrics from a {@code simpleclient} {@link CollectorRegistry#defaultRegistry}
 * to a {@code prometheus-metrics} {@link PrometheusRegistry#defaultRegistry}:
 * <pre>{@code
 * SimpleclientCollector.newBuilder().register();
 * }</pre>
 * <p>
 * If you have custom registries (not the default registries), use the following snippet:
 * <pre>{@code
 * CollectorRegistry simpleclientRegistry = ...;
 * PrometheusRegistry prometheusRegistry = ...;
 * SimpleclientCollector.newBuilder()
 *     .withCollectorRegistry(simpleclientRegistry)
 *     .register(prometheusRegistry);
 * }</pre>
 */
public class SimpleclientCollector implements MultiCollector {

    private final CollectorRegistry simpleclientRegistry;

    private SimpleclientCollector(CollectorRegistry simpleclientRegistry) {
        this.simpleclientRegistry = simpleclientRegistry;
    }

    @Override
    public MetricSnapshots collect() {
        return convert(simpleclientRegistry.metricFamilySamples());
    }

    @Override
    public MetricSnapshots collect(Predicate<String> includedNames) {
        return MultiCollector.super.collect(includedNames);
    }

    @Override
    public List<String> getPrometheusNames() {
        return MultiCollector.super.getPrometheusNames();
    }

    private MetricSnapshots convert(Enumeration<Collector.MetricFamilySamples> samples) {
        MetricSnapshots.Builder result = MetricSnapshots.newBuilder();
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples sample = samples.nextElement();
            switch (sample.type) {
                case COUNTER:
                    result.addMetricSnapshot(convertCounter(sample));
                    break;
                case GAUGE:
                    result.addMetricSnapshot(convertGauge(sample));
                    break;
                case HISTOGRAM:
                    result.addMetricSnapshot(convertHistogram(sample, false));
                    break;
                case GAUGE_HISTOGRAM:
                    result.addMetricSnapshot(convertHistogram(sample, true));
                    break;
                case SUMMARY:
                    result.addMetricSnapshot(convertSummary(sample));
                    break;
                case INFO:
                    result.addMetricSnapshot(convertInfo(sample));
                    break;
                case STATE_SET:
                    result.addMetricSnapshot(convertStateSet(sample));
                    break;
                case UNKNOWN:
                    result.addMetricSnapshot(convertUnknown(sample));
                    break;
                default:
                    throw new IllegalStateException(sample.type + ": Unexpected metric type");
            }
        }
        return result.build();
    }

    private MetricSnapshot convertCounter(Collector.MetricFamilySamples samples) {
        CounterSnapshot.Builder counter = CounterSnapshot.newBuilder()
                .withName(stripSuffix(samples.name, "_total"))
                .withHelp(samples.help)
                .withUnit(convertUnit(samples));
        Map<Labels, CounterSnapshot.CounterDataPointSnapshot.Builder> dataPoints = new HashMap<>();
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            Labels labels = Labels.of(sample.labelNames, sample.labelValues);
            CounterSnapshot.CounterDataPointSnapshot.Builder dataPoint = dataPoints.computeIfAbsent(labels, l -> CounterSnapshot.CounterDataPointSnapshot.newBuilder().withLabels(labels));
            if (sample.name.endsWith("_created")) {
                dataPoint.withCreatedTimestampMillis((long) Unit.secondsToMillis(sample.value));
            } else {
                dataPoint.withValue(sample.value).withExemplar(convertExemplar(sample.exemplar));
                if (sample.timestampMs != null) {
                    dataPoint.withScrapeTimestampMillis(sample.timestampMs);
                }
            }
        }
        for (CounterSnapshot.CounterDataPointSnapshot.Builder dataPoint : dataPoints.values()) {
            counter.addDataPoint(dataPoint.build());
        }
        return counter.build();
    }

    private MetricSnapshot convertGauge(Collector.MetricFamilySamples samples) {
        GaugeSnapshot.Builder gauge = GaugeSnapshot.newBuilder()
                .withName(samples.name)
                .withHelp(samples.help)
                .withUnit(convertUnit(samples));
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            GaugeSnapshot.GaugeDataPointSnapshot.Builder dataPoint = GaugeSnapshot.GaugeDataPointSnapshot.newBuilder()
                    .withValue(sample.value)
                    .withLabels(Labels.of(sample.labelNames, sample.labelValues))
                    .withExemplar(convertExemplar(sample.exemplar));
            if (sample.timestampMs != null) {
                dataPoint.withScrapeTimestampMillis(sample.timestampMs);
            }
            gauge.addDataPoint(dataPoint.build());
        }
        return gauge.build();
    }

    private MetricSnapshot convertHistogram(Collector.MetricFamilySamples samples, boolean isGaugeHistogram) {
        HistogramSnapshot.Builder histogram = HistogramSnapshot.newBuilder()
                .withName(samples.name)
                .withHelp(samples.help)
                .withUnit(convertUnit(samples));
        if (isGaugeHistogram) {
            histogram.asGaugeHistogram();
        }
        Map<Labels, HistogramSnapshot.HistogramDataPointSnapshot.Builder> dataPoints = new HashMap<>();
        Map<Labels, Map<Double, Long>> cumulativeBuckets = new HashMap<>();
        Map<Labels, Exemplars.Builder> exemplars = new HashMap<>();
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            Labels labels = labelsWithout(sample, "le");
            dataPoints.computeIfAbsent(labels, l -> HistogramSnapshot.HistogramDataPointSnapshot.newBuilder()
                    .withLabels(labels));
            cumulativeBuckets.computeIfAbsent(labels, l -> new HashMap<>());
            exemplars.computeIfAbsent(labels, l -> Exemplars.newBuilder());
            if (sample.name.endsWith("_sum")) {
                dataPoints.get(labels).withSum(sample.value);
            }
            if (sample.name.endsWith("_bucket")) {
                addBucket(cumulativeBuckets.get(labels), sample);
            }
            if (sample.name.endsWith("_created")) {
                dataPoints.get(labels).withCreatedTimestampMillis((long) Unit.secondsToMillis(sample.value));
            }
            if (sample.exemplar != null) {
                exemplars.get(labels).addExemplar(convertExemplar(sample.exemplar));
            }
            if (sample.timestampMs != null) {
                dataPoints.get(labels).withScrapeTimestampMillis(sample.timestampMs);
            }
        }
        for (Labels labels : dataPoints.keySet()) {
            histogram.addDataPoint(dataPoints.get(labels)
                    .withClassicHistogramBuckets(makeBuckets(cumulativeBuckets.get(labels)))
                    .withExemplars(exemplars.get(labels).build())
                    .build());
        }
        return histogram.build();
    }

    private MetricSnapshot convertSummary(Collector.MetricFamilySamples samples) {
        SummarySnapshot.Builder summary = SummarySnapshot.newBuilder()
                .withName(samples.name)
                .withHelp(samples.help)
                .withUnit(convertUnit(samples));
        Map<Labels, SummarySnapshot.SummaryDataPointSnapshot.Builder> dataPoints = new HashMap<>();
        Map<Labels, Quantiles.Builder> quantiles = new HashMap<>();
        Map<Labels, Exemplars.Builder> exemplars = new HashMap<>();
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            Labels labels = labelsWithout(sample, "quantile");
            dataPoints.computeIfAbsent(labels, l -> SummarySnapshot.SummaryDataPointSnapshot.newBuilder()
                    .withLabels(labels));
            quantiles.computeIfAbsent(labels, l -> Quantiles.newBuilder());
            exemplars.computeIfAbsent(labels, l -> Exemplars.newBuilder());
            if (sample.name.endsWith("_sum")) {
                dataPoints.get(labels).withSum(sample.value);
            } else if (sample.name.endsWith("_count")) {
                dataPoints.get(labels).withCount((long) sample.value);
            } else if (sample.name.endsWith("_created")) {
                dataPoints.get(labels).withCreatedTimestampMillis((long) Unit.secondsToMillis(sample.value));
            } else {
                for (int i=0; i<sample.labelNames.size(); i++) {
                    if (sample.labelNames.get(i).equals("quantile")) {
                        quantiles.get(labels).addQuantile(new Quantile(Double.parseDouble(sample.labelValues.get(i)), sample.value));
                        break;
                    }
                }
            }
            if (sample.exemplar != null) {
                exemplars.get(labels).addExemplar(convertExemplar(sample.exemplar));
            }
            if (sample.timestampMs != null) {
                dataPoints.get(labels).withScrapeTimestampMillis(sample.timestampMs);
            }
        }
        for (Labels labels : dataPoints.keySet()) {
            summary.addDataPoint(dataPoints.get(labels)
                    .withQuantiles(quantiles.get(labels).build())
                    .withExemplars(exemplars.get(labels).build())
                    .build());
        }
        return summary.build();
    }

    private MetricSnapshot convertStateSet(Collector.MetricFamilySamples samples) {
        StateSetSnapshot.Builder stateSet = StateSetSnapshot.newBuilder()
                .withName(samples.name)
                .withHelp(samples.help);
        Map<Labels, StateSetSnapshot.StateSetDataPointSnapshot.Builder> dataPoints = new HashMap<>();
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            Labels labels = labelsWithout(sample, sample.name);
            dataPoints.computeIfAbsent(labels, l -> StateSetSnapshot.StateSetDataPointSnapshot.newBuilder().withLabels(labels));
            String stateName = null;
            for (int i=0; i<sample.labelNames.size(); i++) {
                if (sample.labelNames.get(i).equals(sample.name)) {
                    stateName = sample.labelValues.get(i);
                    break;
                }
            }
            if (stateName == null) {
                throw new IllegalStateException("Invalid StateSet metric: No state name found.");
            }
            dataPoints.get(labels).addState(stateName, sample.value == 1.0);
            if (sample.timestampMs != null) {
                dataPoints.get(labels).withScrapeTimestampMillis(sample.timestampMs);
            }
        }
        for (StateSetSnapshot.StateSetDataPointSnapshot.Builder dataPoint : dataPoints.values()) {
            stateSet.addDataPoint(dataPoint.build());
        }
        return stateSet.build();
    }

    private MetricSnapshot convertUnknown(Collector.MetricFamilySamples samples) {
        UnknownSnapshot.Builder unknown = UnknownSnapshot.newBuilder()
                .withName(samples.name)
                .withHelp(samples.help)
                .withUnit(convertUnit(samples));
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            UnknownSnapshot.UnknownDataPointSnapshot.Builder dataPoint = UnknownSnapshot.UnknownDataPointSnapshot.newBuilder()
                    .withValue(sample.value)
                    .withLabels(Labels.of(sample.labelNames, sample.labelValues))
                    .withExemplar(convertExemplar(sample.exemplar));
            if (sample.timestampMs != null) {
                dataPoint.withScrapeTimestampMillis(sample.timestampMs);
            }
            unknown.addDataPoint(dataPoint.build());
        }
        return unknown.build();
    }

    private String stripSuffix(String name, String suffix) {
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - suffix.length());
        } else {
            return name;
        }
    }

    private Unit convertUnit(Collector.MetricFamilySamples samples) {
        if (samples.unit != null && !samples.unit.isEmpty()) {
            return new Unit(samples.unit);
        } else {
            return null;
        }
    }

    private ClassicHistogramBuckets makeBuckets(Map<Double, Long> cumulativeBuckets) {
        List<Double> upperBounds = new ArrayList<>(cumulativeBuckets.size());
        Collections.sort(upperBounds);
        upperBounds.addAll(cumulativeBuckets.keySet());
        ClassicHistogramBuckets.Builder result = ClassicHistogramBuckets.newBuilder();
        long previousCount = 0L;
        for (Double upperBound : upperBounds) {
            long cumulativeCount = cumulativeBuckets.get(upperBound);
            result.addBucket(upperBound, cumulativeCount - previousCount);
            previousCount = cumulativeCount;
        }
        return result.build();
    }

    private void addBucket(Map<Double, Long> buckets, Collector.MetricFamilySamples.Sample sample) {
        for (int i = 0; i < sample.labelNames.size(); i++) {
            if (sample.labelNames.get(i).equals("le")) {
                double upperBound;
                switch (sample.labelValues.get(i)) {
                    case "+Inf":
                        upperBound = Double.POSITIVE_INFINITY;
                        break;
                    case "-Inf": // Doesn't make sense as count would always be zero. Catch this anyway.
                        upperBound = Double.NEGATIVE_INFINITY;
                        break;
                    default:
                        upperBound = Double.parseDouble(sample.labelValues.get(i));
                }
                buckets.put(upperBound, (long) sample.value);
                return;
            }
        }
        throw new IllegalStateException(sample.name + " does not have a le label.");
    }


    private Labels labelsWithout(Collector.MetricFamilySamples.Sample sample, String excludedLabelName) {
        Labels.Builder labels = Labels.newBuilder();
        for (int i = 0; i < sample.labelNames.size(); i++) {
            if (!sample.labelNames.get(i).equals(excludedLabelName)) {
                labels.addLabel(sample.labelNames.get(i), sample.labelValues.get(i));
            }
        }
        return labels.build();
    }

    private MetricSnapshot convertInfo(Collector.MetricFamilySamples samples) {
        InfoSnapshot.Builder info = InfoSnapshot.newBuilder()
                .withName(stripSuffix(samples.name, "_info"))
                .withHelp(samples.help);
        for (Collector.MetricFamilySamples.Sample sample : samples.samples) {
            info.addDataPoint(InfoSnapshot.InfoDataPointSnapshot.newBuilder()
                    .withLabels(Labels.of(sample.labelNames, sample.labelValues))
                    .build());
        }
        return info.build();
    }

    private Exemplar convertExemplar(io.prometheus.client.exemplars.Exemplar exemplar) {
        if (exemplar == null) {
            return null;
        }
        Exemplar.Builder result = Exemplar.newBuilder().withValue(exemplar.getValue());
        if (exemplar.getTimestampMs() != null) {
            result.withTimestampMillis(exemplar.getTimestampMs());
        }
        Labels.Builder labels = Labels.newBuilder();
        for (int i = 0; i < exemplar.getNumberOfLabels(); i++) {
            labels.addLabel(exemplar.getLabelName(i), exemplar.getLabelValue(i));
        }
        return result.withLabels(labels.build()).build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private CollectorRegistry collectorRegistry;
        private Builder() {}

        public Builder withCollectorRegistry(CollectorRegistry registry) {
            this.collectorRegistry = registry;
            return this;
        }

        public SimpleclientCollector build() {
            return collectorRegistry != null ? new SimpleclientCollector(collectorRegistry) : new SimpleclientCollector(CollectorRegistry.defaultRegistry);
        }

        public SimpleclientCollector register() {
            return register(PrometheusRegistry.defaultRegistry);
        }

        public SimpleclientCollector register(PrometheusRegistry registry) {
            SimpleclientCollector result = build();
            registry.register(result);
            return result;
        }
    }
}
