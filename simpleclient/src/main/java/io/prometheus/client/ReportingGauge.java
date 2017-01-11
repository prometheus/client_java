package io.prometheus.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReportingGauge extends Collector implements Collector.Describable {

    private final ConcurrentHashMap<Reporter, Reporter> gauges = new ConcurrentHashMap<Reporter, Reporter>();
    private final String name;
    private final String help;
    private final List<String> labelNames;


    public ReportingGauge(String name, String help, List<String> labelNames) {
        this.name = name;
        this.help = help;
        this.labelNames = labelNames;
    }

    public void add(Reporter gauge) {
        gauges.put(gauge, gauge);
    }


    public void remove(Reporter gauge) {
        gauges.remove(gauge);
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        GaugeMetricFamily activeConnections = new GaugeMetricFamily(name, help, labelNames);
        mfs.add(activeConnections);

        for(Map.Entry<Reporter, Reporter> entry : gauges.entrySet()){
            ReportingGauge.Sample sample = entry.getValue().get();
            activeConnections.addMetric(sample.getLabels(), sample.getValue());
        }

        return mfs;
    }

    @Override
    public List<Collector.MetricFamilySamples> describe() {
        List<Collector.MetricFamilySamples> mfsList = new ArrayList<Collector.MetricFamilySamples>();
        mfsList.add(new GaugeMetricFamily(name, help, labelNames));
        return mfsList;
    }

    public static abstract class Reporter {
        public abstract Sample get();
    }

    public static class Sample {
        private List<String> labels;
        private double value;

        public Sample(List<String> labels, double value) {
            this.labels = labels;
            this.value = value;
        }

        public List<String> getLabels() {
            return labels;
        }

        public double getValue() {
            return value;
        }

        public static Sample withLabels(String... labels) {
            return new Sample(Arrays.asList(labels), 0.0);
        }

        public Sample value(double newVal) {
            this.value = newVal;
            return this;
        }

    }

}
