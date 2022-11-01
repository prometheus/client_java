package io.prometheus.expositionformat.protobuf;

import io.prometheus.expositionformat.protobuf.generated.Metrics;
import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Metric;
import io.prometheus.metrics.model.MetricType;
import io.prometheus.metrics.model.Snapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public class Protobuf {

    public static void writeProtobuf(ByteArrayOutputStream response, Collection<Metric> metrics) throws IOException {
        for (Metric metric : metrics) {
            convert(metric).writeDelimitedTo(response);
        }
    }

    private static Metrics.MetricFamily convert(Metric metric) {
        Metrics.MetricFamily.Builder builder = Metrics.MetricFamily.newBuilder()
                .setName(metric.getName())
                .setHelp(metric.getHelp());
        switch (metric.getType()) {
            case MetricType.COUNTER:
                builder.setType(Metrics.MetricType.COUNTER);
                builder.addAllMetric(makeCounters(metric));
                break;
            case MetricType.EXPONENTIAL_BUCKETS_HISTOGRAM:
                builder.setType(Metrics.MetricType.HISTOGRAM);
                builder.addAllMetric(makeExponentialHistograms(metric));
                break;
            default:
                throw new IllegalStateException("Unknown metric type " + metric.getType());
        }
        return builder.build();
    }

    private static List<Metrics.Metric> makeCounters(Metric metric) {
        List<Metrics.Metric> result = new ArrayList<Metrics.Metric>();
        for (Snapshot snapshot : metric.snapshot()) {
            if (! (snapshot instanceof CounterSnapshot)) {
                throw new IllegalStateException("Metric of type " + metric.getType() + " produced snapshot of type " + snapshot.getClass().getName());
            }

        }
        for (Collector.MetricFamilySamples.Sample sample : metricFamily.samples) {
            if (sample.name.endsWith("_created")) {
                continue;
            }
            result.add(Metrics.Metric.newBuilder()
                    .addAllLabel(makeLabels(sample))
                    .setCounter(Metrics.Counter.newBuilder()
                            .setValue(sample.value)
                            //.setExemplar()
                            .build())
                    .build());
        }
        return result;

    }
}
