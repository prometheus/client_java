package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.Arrays;
import java.util.List;

/**
 * Almost all metrics have fixed metadata, i.e. the metric name is known when the metric is created.
 * <p>
 * An exception would be a metric that is a bridge to a 3rd party metric library, where the metric name
 * has to be retrieved from the 3rd party metric library at scrape time.
 */
public abstract class MetricWithFixedMetadata extends Metric {

    private final MetricMetadata metadata;
    protected final String[] labelNames;

    protected MetricWithFixedMetadata(Builder<?, ?> builder) {
        super(builder);
        this.metadata = new MetricMetadata(makeName(builder.name, builder.unit), builder.help, builder.unit);
        this.labelNames = Arrays.copyOf(builder.labelNames, builder.labelNames.length);
    }

    protected MetricMetadata getMetadata() {
        return metadata;
    }

    private String makeName(String name, Unit unit) {
        if (unit != null) {
            if (!name.endsWith("_" + unit) && !name.endsWith("." + unit)) {
                name += "_" + unit;
            }
        }
        return name;
    }

    @Override
    public String getPrometheusName() {
        return metadata.getPrometheusName();
    }

    public static abstract class Builder<B extends Builder<B, M>, M extends MetricWithFixedMetadata> extends Metric.Builder<B, M> {

        protected String name;
        private Unit unit;
        private String help;
        private String[] labelNames = new String[0];

        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            super(illegalLabelNames, properties);
        }

        public B name(String name) {
            if (!PrometheusNaming.isValidMetricName(name)) {
                throw new IllegalArgumentException("'" + name + "': Illegal metric name.");
            }
            this.name = name;
            return self();
        }

        public B unit(Unit unit) {
            this.unit = unit;
            return self();
        }

        public B help(String help) {
            this.help = help;
            return self();
        }

        public B labelNames(String... labelNames) {
            for (String labelName : labelNames) {
                if (!PrometheusNaming.isValidLabelName(labelName)) {
                    throw new IllegalArgumentException(labelName + ": illegal label name");
                }
                if (illegalLabelNames.contains(labelName)) {
                    throw new IllegalArgumentException(labelName + ": illegal label name for this metric type");
                }
                if (constLabels.contains(labelName)) {
                    throw new IllegalArgumentException(labelName + ": duplicate label name");
                }
            }
            this.labelNames = labelNames;
            return self();
        }

        public B constLabels(Labels constLabels) {
            for (String labelName : labelNames) {
                if (constLabels.contains(labelName)) { // Labels.contains() treats dots like underscores
                    throw new IllegalArgumentException(labelName + ": duplicate label name");
                }
            }
            return super.constLabels(constLabels);
        }

        @Override
        public abstract M build();

        @Override
        protected abstract B self();
    }
}
