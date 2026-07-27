package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.prometheus.metrics.config.ExporterFilterProperties;
import io.prometheus.metrics.exporter.opentelemetry.otelmodel.MetricDataFactory;
import io.prometheus.metrics.model.registry.MetricNameFilter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

class PrometheusMetricProducer implements CollectionRegistration {

  private final PrometheusRegistry registry;
  private final Resource resource;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final boolean preserveNames;
  @Nullable private final Predicate<String> nameFilter;

  private PrometheusMetricProducer(
      PrometheusRegistry registry,
      InstrumentationScopeInfo instrumentationScopeInfo,
      Resource resource,
      boolean preserveNames,
      @Nullable Predicate<String> nameFilter) {
    this.registry = registry;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.resource = resource;
    this.preserveNames = preserveNames;
    this.nameFilter = nameFilter;
  }

  /**
   * Creates a builder for a producer with no metric name filtering by default, i.e. all metrics in
   * {@code registry} are exported unless filter properties are configured on the builder.
   */
  static Builder builder(
      PrometheusRegistry registry,
      InstrumentationScopeInfo instrumentationScopeInfo,
      Resource resource,
      boolean preserveNames) {
    return new Builder(registry, instrumentationScopeInfo, resource, preserveNames);
  }

  /**
   * Builds a name filter from {@code io.prometheus.exporter.filter.*} properties, mirroring how
   * {@code PrometheusScrapeHandler} builds its filter for the Servlet/HTTPServer exporters so that
   * filtering config behaves consistently across exporters.
   *
   * <p>OpenTelemetry's own Views API also supports filtering and aggregation, and may be preferable
   * for OpenTelemetry-specific deployments; this filter is intended for users who want the same
   * {@code io.prometheus.exporter.filter.*} config to apply regardless of which exporter they use.
   *
   * @return {@code null} if no filter properties are set, to avoid the overhead of testing every
   *     metric name against a filter that matches everything.
   */
  @Nullable
  private static Predicate<String> makeNameFilter(ExporterFilterProperties props) {
    if (props.getAllowedMetricNames() == null
        && props.getExcludedMetricNames() == null
        && props.getAllowedMetricNamePrefixes() == null
        && props.getExcludedMetricNamePrefixes() == null) {
      return null;
    }
    return MetricNameFilter.builder()
        .nameMustBeEqualTo(props.getAllowedMetricNames())
        .nameMustNotBeEqualTo(props.getExcludedMetricNames())
        .nameMustStartWith(props.getAllowedMetricNamePrefixes())
        .nameMustNotStartWith(props.getExcludedMetricNamePrefixes())
        .build();
  }

  @Override
  public Collection<MetricData> collectAllMetrics() {
    MetricSnapshots snapshots =
        nameFilter != null ? registry.scrape(nameFilter) : registry.scrape();
    Resource resourceWithTargetInfo = resource.merge(resourceFromTargetInfo(snapshots));
    InstrumentationScopeInfo scopeFromInfo = instrumentationScopeFromOtelScopeInfo(snapshots);
    List<MetricData> result = new ArrayList<>(snapshots.size());
    MetricDataFactory factory =
        new MetricDataFactory(
            resourceWithTargetInfo,
            scopeFromInfo != null ? scopeFromInfo : instrumentationScopeInfo,
            System.currentTimeMillis(),
            preserveNames);
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
        if (!targetInfo.getDataPoints().isEmpty()) {
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

  @Nullable
  private InstrumentationScopeInfo instrumentationScopeFromOtelScopeInfo(
      MetricSnapshots snapshots) {
    for (MetricSnapshot snapshot : snapshots) {
      if (snapshot.getMetadata().getPrometheusName().equals("otel_scope")
          && snapshot instanceof InfoSnapshot) {
        InfoSnapshot scopeInfo = (InfoSnapshot) snapshot;
        if (!scopeInfo.getDataPoints().isEmpty()) {
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

  private void addUnlessNull(List<MetricData> result, @Nullable MetricData data) {
    if (data != null) {
      result.add(data);
    }
  }

  static class Builder {
    private final PrometheusRegistry registry;
    private final Resource resource;
    private final InstrumentationScopeInfo instrumentationScopeInfo;
    private final boolean preserveNames;
    private ExporterFilterProperties filterProperties = ExporterFilterProperties.builder().build();

    private Builder(
        PrometheusRegistry registry,
        InstrumentationScopeInfo instrumentationScopeInfo,
        Resource resource,
        boolean preserveNames) {
      this.registry = registry;
      this.instrumentationScopeInfo = instrumentationScopeInfo;
      this.resource = resource;
      this.preserveNames = preserveNames;
    }

    Builder exporterFilterProperties(ExporterFilterProperties filterProperties) {
      this.filterProperties = filterProperties;
      return this;
    }

    PrometheusMetricProducer build() {
      return new PrometheusMetricProducer(
          registry,
          instrumentationScopeInfo,
          resource,
          preserveNames,
          makeNameFilter(filterProperties));
    }
  }
}
