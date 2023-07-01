package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.Collections;

public class Info extends MetricWithFixedMetadata {

    private volatile Labels labels = Labels.EMPTY;

    private Info(Builder builder) {
        super(builder);
    }

    public void info(Labels labels) {
        // throws NPE if labels == null
        for (Label label : labels) {
            for (Label constLabel : constLabels) {
                if (constLabel.getName().equals(label.getName())) {
                    throw new IllegalArgumentException("Can't set label " + label.getName() + " because this label name is already used as a const Label.");
                }
            }
        }
        this.labels = labels;
    }

    @Override
    public InfoSnapshot collect() {
        return new InfoSnapshot(getMetadata(), Collections.singletonList(new InfoSnapshot.InfoDataPointSnapshot(labels.merge(constLabels))));
    }

    public static class Builder extends MetricWithFixedMetadata.Builder<Builder, Info> {

        private Builder(PrometheusProperties config) {
            super(Collections.emptyList(), config);
        }

        @Override
        public Builder withName(String name) {
            if (name != null && name.endsWith("_info")) {
                name = name.substring(0, name.length() - 5);
            }
            return super.withName(name);
        }

        @Override
        public Builder withUnit(Unit unit) {
            throw new UnsupportedOperationException("Info metrics cannot have a unit.");
        }

        private static String normalizeName(String name) {
            if (name != null && name.endsWith("_info")) {
                name = name.substring(0, name.length() - 5);
            }
            return name;
        }

        @Override
        public Info build() {
            return new Info(withName(normalizeName(name)));
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder newBuilder(PrometheusProperties config) {
        return new Builder(config);
    }
}
