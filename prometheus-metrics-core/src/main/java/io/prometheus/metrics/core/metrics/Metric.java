package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Common base class for all metrics.
 */
public abstract class Metric implements Collector {

    protected final Labels constLabels;

    protected Metric(Builder<?, ?> builder) {
        this.constLabels = builder.constLabels;
    }

    @Override
    public abstract MetricSnapshot collect();

    protected static abstract class Builder<B extends Builder<B, M>, M extends Metric> {

        protected final List<String> illegalLabelNames;
        protected final PrometheusProperties properties;
        protected Labels constLabels = Labels.EMPTY;

        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            this.illegalLabelNames = new ArrayList<>(illegalLabelNames);
            this.properties = properties;
        }

        // ConstLabels are only used rarely. In particular, do not use them to
        // attach the same labels to all your metrics. Those use cases are
        // better covered by target labels set by the scraping Prometheus
        // server, or by one specific metric (e.g. a build_info or a
        // machine_role metric). See also
        // https://prometheus.io/docs/instrumenting/writing_exporters/#target-labels-not-static-scraped-labels
        public B constLabels(Labels constLabels) {
            for (Label label : constLabels) { // NPE if constLabels is null
                if (illegalLabelNames.contains(label.getName())) {
                    throw new IllegalArgumentException(label.getName() + ": illegal label name for this metric type");
                }
            }
            this.constLabels = constLabels;
            return self();
        }

        public M register() {
            return register(PrometheusRegistry.defaultRegistry);
        }

        public M register(PrometheusRegistry registry) {
            M metric = build();
            registry.register(metric);
            return metric;
        }

        public abstract M build();

        protected abstract B self();
    }
}
