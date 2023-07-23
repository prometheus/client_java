package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

/**
 * Info metric. Example:
 * <pre>{@code
 * Info info = Info.newBuilder()
 *         .withName("java_runtime_info")
 *         .withHelp("Java runtime info")
 *         .withLabelNames("env", "version", "vendor", "runtime")
 *         .register();
 *
 * String version = System.getProperty("java.runtime.version", "unknown");
 * String vendor = System.getProperty("java.vm.vendor", "unknown");
 * String runtime = System.getProperty("java.runtime.name", "unknown");
 *
 * info.infoLabelValues("prod", version, vendor, runtime);
 * info.infoLabelValues("dev", version, vendor, runtime);
 * }</pre>
 */
public class Info extends MetricWithFixedMetadata {

    private final List<Labels> labels = new CopyOnWriteArrayList<>();

    private Info(Builder builder) {
        super(builder);
    }

    /**
     * Create an info data point with the given label values.
     */
    public void infoLabelValues(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            if (labelValues.length == 0) {
                throw new IllegalArgumentException(getClass().getSimpleName() + " " + getMetadata().getName() + " was created with label names, so you must call withLabelValues(...) when using it.");
            } else {
                throw new IllegalArgumentException("Expected " + labelNames.length + " label values, but got " + labelValues.length + ".");
            }
        }
        labels.add(Labels.of(labelNames, labelValues));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InfoSnapshot collect() {
        List<InfoSnapshot.InfoDataPointSnapshot> data = new ArrayList<>(labels.size());
        for (int i = 0; i < labels.size(); i++) {
            data.add(new InfoSnapshot.InfoDataPointSnapshot(labels.get(i).merge(constLabels)));
        }
        return new InfoSnapshot(getMetadata(), data);
    }

    public static Builder newBuilder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder newBuilder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder extends MetricWithFixedMetadata.Builder<Builder, Info> {

        private Builder(PrometheusProperties config) {
            super(Collections.emptyList(), config);
        }

        /**
         * The {@code _info} suffix will automatically be appended if it's missing.
         * <pre>{@code
         * Info info1 = Info.newBuilder()
         *     .withName("runtime_info")
         *     .build();
         * Info info2 = Info.newBuilder()
         *     .withName("runtime")
         *     .build();
         * }</pre>
         * In the example above both {@code info1} and {@code info2} will be named {@code "runtime_info"} in Prometheus.
         * <p>
         * Throws an {@link IllegalArgumentException} if
         * {@link io.prometheus.metrics.model.snapshots.PrometheusNaming#isValidMetricName(String) MetricMetadata.isValidMetricName(name)}
         * is {@code false}.
         */
        @Override
        public Builder withName(String name) {
            return super.withName(stripInfoSuffix(name));
        }

        /**
         * Throws an {@link UnsupportedOperationException} because Info metrics cannot have a unit.
         */
        @Override
        public Builder withUnit(Unit unit) {
            if (unit != null) {
                throw new UnsupportedOperationException("Info metrics cannot have a unit.");
            }
            return this;
        }

        private static String stripInfoSuffix(String name) {
            if (name != null && (name.endsWith("_info") || name.endsWith(".info"))) {
                name = name.substring(0, name.length() - 5);
            }
            return name;
        }

        @Override
        public Info build() {
            return new Info(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
