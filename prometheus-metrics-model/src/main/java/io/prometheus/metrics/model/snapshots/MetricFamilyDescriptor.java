package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.registry.MetricType;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;

/** Registration-time descriptor for a metric family. */
public final class MetricFamilyDescriptor {

  private final MetricType type;
  private final MetricMetadata metadata;
  private final Set<String> labelNames;

  private MetricFamilyDescriptor(
      MetricType type, MetricMetadata metadata, Collection<String> labelNames) {
    if (type == null) {
      throw new IllegalArgumentException("Missing required field: type is null");
    }
    if (metadata == null) {
      throw new IllegalArgumentException("Missing required field: metadata is null");
    }
    if (labelNames == null) {
      throw new IllegalArgumentException("Missing required field: labelNames is null");
    }
    this.type = type;
    this.metadata = metadata;
    this.labelNames = Collections.unmodifiableSet(new LinkedHashSet<>(labelNames));
  }

  public static MetricFamilyDescriptor of(MetricType type, MetricMetadata metadata) {
    return of(type, metadata, Collections.emptySet());
  }

  public static MetricFamilyDescriptor of(
      MetricType type, MetricMetadata metadata, Collection<String> labelNames) {
    return new MetricFamilyDescriptor(type, metadata, labelNames);
  }

  public static Builder<?> of(MetricType type, String name) {
    switch (type) {
      case COUNTER:
        return counter(name);
      case GAUGE:
        return gauge(name);
      case HISTOGRAM:
        return histogram(name);
      case SUMMARY:
        return summary(name);
      case INFO:
        return info(name);
      case STATESET:
        return stateSet(name);
      case UNKNOWN:
      default:
        return unknown(name);
    }
  }

  public static CounterBuilder counter(String name) {
    return new CounterBuilder().name(name);
  }

  public static GaugeBuilder gauge(String name) {
    return new GaugeBuilder().name(name);
  }

  public static HistogramBuilder histogram(String name) {
    return new HistogramBuilder().name(name);
  }

  public static SummaryBuilder summary(String name) {
    return new SummaryBuilder().name(name);
  }

  public static InfoBuilder info(String name) {
    return new InfoBuilder().name(name);
  }

  public static StateSetBuilder stateSet(String name) {
    return new StateSetBuilder().name(name);
  }

  public static UnknownBuilder unknown(String name) {
    return new UnknownBuilder().name(name);
  }

  public MetricType getType() {
    return type;
  }

  public MetricMetadata getMetadata() {
    return metadata;
  }

  public Set<String> getLabelNames() {
    return labelNames;
  }

  public String getPrometheusName() {
    return metadata.getPrometheusName();
  }

  public abstract static class Builder<T extends Builder<T>> {

    @Nullable protected String name;
    @Nullable protected String help;
    @Nullable protected Unit unit;
    protected final Set<String> labelNames = new LinkedHashSet<>();

    public T name(String name) {
      this.name = name;
      return self();
    }

    public T help(@Nullable String help) {
      this.help = help;
      return self();
    }

    public T unit(@Nullable Unit unit) {
      this.unit = unit;
      return self();
    }

    public T labelName(String labelName) {
      this.labelNames.add(labelName);
      return self();
    }

    public T labelNames(String... labelNames) {
      Collections.addAll(this.labelNames, labelNames);
      return self();
    }

    public T labelNames(Collection<String> labelNames) {
      this.labelNames.addAll(labelNames);
      return self();
    }

    public MetricFamilyDescriptor build() {
      return new MetricFamilyDescriptor(getType(), buildMetadata(), labelNames);
    }

    protected MetricMetadata buildMetadata() {
      if (name == null) {
        throw new IllegalArgumentException("Missing required field: name is null");
      }
      return MetricMetadataSupport.metricMetadata(name, help, unit);
    }

    protected abstract MetricType getType();

    protected abstract T self();
  }

  public static final class CounterBuilder extends Builder<CounterBuilder> {

    @Override
    protected MetricMetadata buildMetadata() {
      if (name == null) {
        throw new IllegalArgumentException("Missing required field: name is null");
      }
      return MetricMetadataSupport.counterMetadata(name, help, unit);
    }

    @Override
    protected MetricType getType() {
      return MetricType.COUNTER;
    }

    @Override
    protected CounterBuilder self() {
      return this;
    }
  }

  public static final class GaugeBuilder extends Builder<GaugeBuilder> {

    @Override
    protected MetricType getType() {
      return MetricType.GAUGE;
    }

    @Override
    protected GaugeBuilder self() {
      return this;
    }
  }

  public static final class HistogramBuilder extends Builder<HistogramBuilder> {

    @Override
    protected MetricType getType() {
      return MetricType.HISTOGRAM;
    }

    @Override
    protected HistogramBuilder self() {
      return this;
    }
  }

  public static final class SummaryBuilder extends Builder<SummaryBuilder> {

    @Override
    protected MetricType getType() {
      return MetricType.SUMMARY;
    }

    @Override
    protected SummaryBuilder self() {
      return this;
    }
  }

  public static final class InfoBuilder extends Builder<InfoBuilder> {

    @Override
    public InfoBuilder unit(@Nullable Unit unit) {
      throw new IllegalArgumentException("Info metric cannot have a unit.");
    }

    @Override
    protected MetricMetadata buildMetadata() {
      if (name == null) {
        throw new IllegalArgumentException("Missing required field: name is null");
      }
      return MetricMetadataSupport.infoMetadata(name, help);
    }

    @Override
    protected MetricType getType() {
      return MetricType.INFO;
    }

    @Override
    protected InfoBuilder self() {
      return this;
    }
  }

  public static final class StateSetBuilder extends Builder<StateSetBuilder> {

    @Override
    public StateSetBuilder unit(@Nullable Unit unit) {
      throw new IllegalArgumentException("State set metric cannot have a unit.");
    }

    @Override
    protected MetricType getType() {
      return MetricType.STATESET;
    }

    @Override
    protected StateSetBuilder self() {
      return this;
    }
  }

  public static final class UnknownBuilder extends Builder<UnknownBuilder> {

    @Override
    protected MetricType getType() {
      return MetricType.UNKNOWN;
    }

    @Override
    protected UnknownBuilder self() {
      return this;
    }
  }
}
