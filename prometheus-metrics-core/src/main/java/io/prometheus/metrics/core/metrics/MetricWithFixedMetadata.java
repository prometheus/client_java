package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.List;

public abstract class MetricWithFixedMetadata extends Metric {

    private final MetricMetadata metadata;

    protected MetricWithFixedMetadata(Builder<?, ?> builder) {
        super(builder);
        this.metadata = new MetricMetadata(makeName(builder.name, builder.unit), builder.help, builder.unit);
    }

    protected MetricMetadata getMetadata() {
        return metadata;
    }

    private String makeName(String name, Unit unit) {
        if (unit != null) {
            String suffix = "_" + unit;
            if (!name.endsWith(suffix)) {
                name = name + suffix;
            }
        }
        return name;
    }

    protected static abstract class Builder<B extends Builder<B, M>, M extends MetricWithFixedMetadata> extends Metric.Builder<B, M> {

        protected String name;
        private Unit unit;
        private String help;

        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            super(illegalLabelNames, properties);
        }

        public B withName(String name) {
            if (!MetricMetadata.isValidMetricName(name)) {
                throw new IllegalArgumentException("'" + name + "': Illegal metric name.");
            }
            this.name = name;
            return self();
        }

        public B withUnit(Unit unit) {
            this.unit = unit;
            return self();
        }

        public B withHelp(String help) {
            this.help = help;
            return self();
        }

        public abstract M build();

        /*
        public M register() {
            return register(PrometheusRegistry.defaultRegistry);
        }

        public M register(PrometheusRegistry registry) {
            M metric = build();
            registry.register(metric);
            return metric;
        }
         */

        protected abstract B self();
    }
}
