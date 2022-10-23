package io.prometheus.metrics.model;

// See https://github.com/prometheus/prometheus/blob/main/prompb/types.proto
public interface Label extends Comparable<Label> {

    String getName();
    String getValue();

}
