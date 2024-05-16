package io.prometheus.metrics.exporter.opentelemetry;

import io.prometheus.metrics.exporter.opentelemetry.otelmodel.MetricDataFactory;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.Attributes;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.api.common.AttributesBuilder;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.common.InstrumentationScopeInfo;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.data.MetricData;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.export.CollectionRegistration;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.resources.Resource;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.resources.ResourceBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PrometheusMetricProducer implements CollectionRegistration {

    private final PrometheusRegistry registry;
    private final Resource resource;
    private final InstrumentationScopeInfo instrumentationScopeInfo;

    public PrometheusMetricProducer(PrometheusRegistry registry, InstrumentationScopeInfo instrumentationScopeInfo, Resource resource) {
        this.registry = registry;
        this.instrumentationScopeInfo = instrumentationScopeInfo;
        this.resource = resource;
    }

    @Override
    public Collection<MetricData> collectAllMetrics() {
        // TODO: We could add a filter configuration for the OpenTelemetry exporter and call registry.scrape(filter) if a filter is configured, like in the Servlet exporter.
        MetricSnapshots snapshots = registry.scrape();
        Resource resourceWithTargetInfo = resource.merge(resourceFromTargetInfo(snapshots));
        InstrumentationScopeInfo scopeFromInfo = instrumentationScopeFromOTelScopeInfo(snapshots);
        List<MetricData> result = new ArrayList<>(snapshots.size());
        MetricDataFactory factory = new MetricDataFactory(resourceWithTargetInfo, scopeFromInfo != null ? scopeFromInfo : instrumentationScopeInfo, System.currentTimeMillis());
        for (MetricSnapshot snapshot : snapshots) {
            if (snapshot instanceof CounterSnapshot) {
                addUnlessNull(result, factory.create((CounterSnapshot) snapshot));
            } else if (snapshot instanceof GaugeSnapshot) {
                addUnlessNull(result, factory.create((GaugeSnapshot) snapshot));
            } else if (snapshot instanceof HistogramSnapshot) {
                if (!((HistogramSnapshot) snapshot).isGaugeHistogram()) {
                    addUnlessNull(result, factory.create((HistogramSnapshot) snapshot));
                }
            } else if (snapshot instanceof SummarySnapshot) {
                addUnlessNull(result, factory.create((SummarySnapshot) snapshot));
            } else if (snapshot instanceof InfoSnapshot) {
                String name = snapshot.getMetadata().getPrometheusName();
                if (!name.equals("target") && !name.equals("otel_scope")) {
                    addUnlessNull(result, factory.create((InfoSnapshot) snapshot));
                }
            } else if (snapshot instanceof StateSetSnapshot) {
                addUnlessNull(result, factory.create((StateSetSnapshot) snapshot));
            } else if (snapshot instanceof UnknownSnapshot) {
                addUnlessNull(result, factory.create((UnknownSnapshot) snapshot));
            }
        }
        return result;
    }

    private Resource resourceFromTargetInfo(MetricSnapshots snapshots) {
        ResourceBuilder result = Resource.builder();
        for (MetricSnapshot snapshot : snapshots) {
            if (snapshot.getMetadata().getName().equals("target") && snapshot instanceof InfoSnapshot) {
                InfoSnapshot targetInfo = (InfoSnapshot) snapshot;
                if (targetInfo.getDataPoints().size() > 0) {
                    InfoSnapshot.InfoDataPointSnapshot data = targetInfo.getDataPoints().get(0);
                    Labels labels = data.getLabels();
                    for (int i = 0; i < labels.size(); i++) {
                        result.put(labels.getName(i), labels.getValue(i));
                    }
                }
            }
        }
        return result.build();
    }

    private InstrumentationScopeInfo instrumentationScopeFromOTelScopeInfo(MetricSnapshots snapshots) {
        for (MetricSnapshot snapshot : snapshots) {
            if (snapshot.getMetadata().getPrometheusName().equals("otel_scope") && snapshot instanceof InfoSnapshot) {
                InfoSnapshot scopeInfo = (InfoSnapshot) snapshot;
                if (scopeInfo.getDataPoints().size() > 0) {
                    Labels labels = scopeInfo.getDataPoints().get(0).getLabels();
                    String name = null;
                    String version = null;
                    AttributesBuilder attributesBuilder = Attributes.builder();
                    for (int i = 0; i < labels.size(); i++) {
                        if (labels.getPrometheusName(i).equals("otel_scope_name")) {
                            name = labels.getValue(i);
                        } else if (labels.getPrometheusName(i).equals("otel_scope_version")) {
                            version = labels.getValue(i);
                        } else {
                            attributesBuilder.put(labels.getName(i), labels.getValue(i));
                        }
                    }
                    if (name != null) {
                        return InstrumentationScopeInfo.builder(name)
                                .setVersion(version)
                                .setAttributes(attributesBuilder.build())
                                .build();
                    }
                }
            }
        }
        return null;
    }

    private void addUnlessNull(List<MetricData> result, MetricData data) {
        if (data != null) {
            result.add(data);
        }
    }
}
