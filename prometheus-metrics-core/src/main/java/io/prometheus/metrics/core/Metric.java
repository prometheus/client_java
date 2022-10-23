package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.Snapshot;
import io.prometheus.metrics.registry.PrometheusRegistry;

public abstract class Metric implements io.prometheus.metrics.model.Metric {

    private final String name;
    private final String unit;
    private final String help;
    protected final Labels constLabels;

    protected Metric(Builder<?, ?> builder) {
        this.name = builder.name;
        this.unit = builder.unit;
        this.help = builder.help;
        this.constLabels = builder.constLabels;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public String getHelp() {
        return help;
    }

    static abstract class Builder<B extends Builder<B, M>, M extends Metric> {
        private String name;
        private String unit;
        private String help;
        private Labels constLabels;

        protected Builder() {}

        public B withName(String name) {
            this.name = name;
            return self();
        }

        public B withUnit(String unit) {
            this.unit = unit;
            return self();
        }

        public B withHelp(String help) {
            this.help = help;
            return self();
        }

        // ConstLabels are only used rarely. In particular, do not use them to
        // attach the same labels to all your metrics. Those use cases are
        // better covered by target labels set by the scraping Prometheus
        // server, or by one specific metric (e.g. a build_info or a
        // machine_role metric). See also
        // https://prometheus.io/docs/instrumenting/writing_exporters/#target-labels-not-static-scraped-labels
        public B withConstLabels(Labels constLabels) {
            this.constLabels = constLabels;
            return self();
        }

        public abstract M build();

        public M register() {
            return register(PrometheusRegistry.defaultRegistry);
        }

        public M register(PrometheusRegistry registry) {
            M metric = build();
            registry.register(metric);
            return metric;
        }

        protected abstract B self();
    }
}
