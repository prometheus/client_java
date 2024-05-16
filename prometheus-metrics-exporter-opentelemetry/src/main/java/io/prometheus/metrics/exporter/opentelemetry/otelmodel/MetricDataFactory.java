package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.common.InstrumentationScopeInfo;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.resources.Resource;

public class MetricDataFactory {

    private final Resource resource;
    private final InstrumentationScopeInfo instrumentationScopeInfo;
    private final long currentTimeMillis;

    public MetricDataFactory(Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long currentTimeMillis) {
        this.resource = resource;
        this.instrumentationScopeInfo = instrumentationScopeInfo;
        this.currentTimeMillis = currentTimeMillis;
    }

    public MetricData create(CounterSnapshot snapshot) {
        return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusCounter(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
    }

    public MetricData create(GaugeSnapshot snapshot) {
        return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusGauge(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
    }

    public MetricData create(HistogramSnapshot snapshot) {
        if (!snapshot.getDataPoints().isEmpty()) {
            HistogramSnapshot.HistogramDataPointSnapshot firstDataPoint = snapshot.getDataPoints().get(0);
            if (firstDataPoint.hasNativeHistogramData()) {
                return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusNativeHistogram(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
            } else if (firstDataPoint.hasClassicHistogramData()) {
                return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusClassicHistogram(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
            }
        }
        return null;
    }

    public MetricData create(SummarySnapshot snapshot) {
        return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusSummary(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
    }

    public MetricData create(InfoSnapshot snapshot) {
        return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusInfo(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
    }

    public MetricData create(StateSetSnapshot snapshot) {
        return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusStateSet(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
    }

    public MetricData create(UnknownSnapshot snapshot) {
        return new PrometheusMetricData<>(snapshot.getMetadata(), new PrometheusUnknown(snapshot, currentTimeMillis), instrumentationScopeInfo, resource);
    }
}
