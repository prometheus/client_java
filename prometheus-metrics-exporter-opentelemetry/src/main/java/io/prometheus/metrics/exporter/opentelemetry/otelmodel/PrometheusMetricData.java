package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.Unit;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.common.InstrumentationScopeInfo;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.DoublePointData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricDataType;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.SumData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.resources.Resource;

class PrometheusMetricData<T extends PrometheusData<?>> implements MetricData {

    private final Resource resource;
    private final InstrumentationScopeInfo instrumentationScopeInfo;
    private final String name;
    private final String description;
    private final String unit;
    T data;

    PrometheusMetricData(MetricMetadata metricMetadata, T data, InstrumentationScopeInfo instrumentationScopeInfo, Resource resource) {
        this.instrumentationScopeInfo = instrumentationScopeInfo;
        this.resource = resource;
        this.name = getNameWithoutUnit(metricMetadata);
        this.description = metricMetadata.getHelp();
        this.unit = convertUnit(metricMetadata.getUnit());
        this.data = data;
    }

    // In OpenTelemetry the unit should not be part of the metric name.
    private String getNameWithoutUnit(MetricMetadata metricMetadata) {
        String name = metricMetadata.getName();
        if (metricMetadata.getUnit() != null) {
            String unit = metricMetadata.getUnit().toString();
            if (name.endsWith(unit)) {
                name = name.substring(0, name.length() - unit.length());
            }
            while (name.endsWith("_")) {
                name = name.substring(0, name.length()-1);
            }
        }
        return name;
    }

    // See https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/6cf4dec6cb42d87d8840e9f67d4acf66d4eb8fda/pkg/translator/prometheus/normalize_name.go#L19
    private String convertUnit(Unit unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.toString()) {
            // Time
            case "days": return "d";
            case "hours": return "h";
            case "minutes": return "min";
            case "seconds": return "s";
            case "milliseconds": return "ms";
            case "microseconds": return "us";
            case "nanoseconds": return "ns";
            // Bytes
            case "bytes": return "By";
            case "kibibytes": return "KiBy";
            case "mebibytes": return "MiBy";
            case "gibibytes": return "GiBy";
            case "tibibytes": return "TiBy";
            case "kilobytes": return "KBy";
            case "megabytes": return "MBy";
            case "gigabytes": return "GBy";
            case "terabytes": return "TBy";
            // SI
            case "meters": return "m";
            case "volts": return "V";
            case "amperes": return "A";
            case "joules": return "J";
            case "watts": return "W";
            case "grams": return "g";
            // Misc
            case "celsius": return "Cel";
            case "hertz": return "Hz";
            case "percent": return "%";
            // default
            default:
                return unit.toString();
        }
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public MetricDataType getType() {
        return data.getType();
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public SumData<DoublePointData> getDoubleSumData() {
        if (data instanceof PrometheusCounter) {
            return (PrometheusCounter) data;
        }
        if (data instanceof PrometheusStateSet) {
            return (PrometheusStateSet) data;
        }
        if (data instanceof PrometheusInfo) {
            return (PrometheusInfo) data;
        }
        return MetricData.super.getDoubleSumData();
    }
}
