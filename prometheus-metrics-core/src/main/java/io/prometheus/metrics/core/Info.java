package io.prometheus.metrics.core;

import io.prometheus.metrics.model.InfoSnapshot;
import io.prometheus.metrics.model.Label;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.MetricType;

import java.util.Collections;

public class Info extends Metric {

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
        return new InfoSnapshot(getMetadata(), Collections.singletonList(new InfoSnapshot.InfoData(labels.merge(constLabels))));
    }

    public static class Builder extends Metric.Builder<Builder, Info> {

        private Builder() {
            super(Collections.emptyList());
        }

        @Override
        protected MetricType getType() {
            return MetricType.INFO;
        }

        @Override
        public Builder withUnit(String unit) {
            if (!unit.isEmpty()) {
                throw new UnsupportedOperationException("Info metrics cannot have a unit.");
            }
            return super.withUnit(unit);
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
        return new Builder();
    }
}
