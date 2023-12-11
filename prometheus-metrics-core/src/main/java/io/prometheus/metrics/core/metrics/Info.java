package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Info metric. Example:
 * <pre>{@code
 * Info info = Info.builder()
 *         .name("java_runtime_info")
 *         .help("Java runtime info")
 *         .labelNames("env", "version", "vendor", "runtime")
 *         .register();
 *
 * String version = System.getProperty("java.runtime.version", "unknown");
 * String vendor = System.getProperty("java.vm.vendor", "unknown");
 * String runtime = System.getProperty("java.runtime.name", "unknown");
 *
 * info.addLabelValues("prod", version, vendor, runtime);
 * info.addLabelValues("dev", version, vendor, runtime);
 * }</pre>
 */
public class Info extends MetricWithFixedMetadata {

    private final Set<Labels> labels = new CopyOnWriteArraySet<>();

    private Info(Builder builder) {
        super(builder);
    }

    /**
     * Set the info label values. This will replace any previous values,
     * i.e. the info metric will only have one data point after calling setLabelValues().
     * This is good for a metric like {@code target_info} where you want only one single data point.
     */
    public void setLabelValues(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " " + getMetadata().getName() + " was created with " + labelNames.length + " label names, but you called setLabelValues() with " + labelValues.length + " label values.");
        }
        Labels newLabels = Labels.of(labelNames, labelValues);
        labels.add(newLabels);
        labels.retainAll(Collections.singletonList(newLabels));
    }

    /**
     * Create an info data point with the given label values.
     */
    public void addLabelValues(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " " + getMetadata().getName() + " was created with " + labelNames.length + " label names, but you called addLabelValues() with " + labelValues.length + " label values.");
        }
        labels.add(Labels.of(labelNames, labelValues));
    }

    /**
     * Remove the data point with the specified label values.
     */
    public void remove(String... labelValues) {
        if (labelValues.length != labelNames.length) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " " + getMetadata().getName() + " was created with " + labelNames.length + " label names, but you called remove() with " + labelValues.length + " label values.");
        }
        Labels toBeRemoved = Labels.of(labelNames, labelValues);
        labels.remove(toBeRemoved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InfoSnapshot collect() {
        List<InfoSnapshot.InfoDataPointSnapshot> data = new ArrayList<>(labels.size());
        if (labelNames.length == 0) {
            data.add(new InfoSnapshot.InfoDataPointSnapshot(constLabels));
        } else {
            for (Labels label : labels) {
                data.add(new InfoSnapshot.InfoDataPointSnapshot(label.merge(constLabels)));
            }
        }
        return new InfoSnapshot(getMetadata(), data);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder extends MetricWithFixedMetadata.Builder<Builder, Info> {

        private Builder(PrometheusProperties config) {
            super(Collections.emptyList(), config);
        }

        /**
         * The {@code _info} suffix will automatically be appended if it's missing.
         * <pre>{@code
         * Info info1 = Info.builder()
         *     .name("runtime_info")
         *     .build();
         * Info info2 = Info.builder()
         *     .name("runtime")
         *     .build();
         * }</pre>
         * In the example above both {@code info1} and {@code info2} will be named {@code "runtime_info"} in Prometheus.
         * <p>
         * Throws an {@link IllegalArgumentException} if
         * {@link io.prometheus.metrics.model.snapshots.PrometheusNaming#isValidMetricName(String) MetricMetadata.isValidMetricName(name)}
         * is {@code false}.
         */
        @Override
        public Builder name(String name) {
            return super.name(stripInfoSuffix(name));
        }

        /**
         * Throws an {@link UnsupportedOperationException} because Info metrics cannot have a unit.
         */
        @Override
        public Builder unit(Unit unit) {
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
