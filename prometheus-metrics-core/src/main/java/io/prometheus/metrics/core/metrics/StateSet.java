package io.prometheus.metrics.core.metrics;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.StateSetDataPoint;
import io.prometheus.metrics.model.registry.MetricType;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * StateSet metric. Example:
 *
 * <pre>{@code
 * public enum Feature {
 *
 *     FEATURE_1("feature1"),
 *     FEATURE_2("feature2");
 *
 *     private final String name;
 *
 *     Feature(String name) {
 *         this.name = name;
 *     }
 *
 *     // Override
 *     public String toString() {
 *         return name;
 *     }
 * }
 *
 * public static void main(String[] args) {
 *
 *     StateSet stateSet = StateSet.builder()
 *             .name("feature_flags")
 *             .help("Feature flags")
 *             .labelNames("env")
 *             .states(Feature.class)
 *             .register();
 *
 *     stateSet.labelValues("dev").setFalse(FEATURE_1);
 *     stateSet.labelValues("dev").setTrue(FEATURE_2);
 * }
 * }</pre>
 *
 * The example above shows how to use a StateSet with an enum. You don't have to use enum, you can
 * use regular strings as well.
 */
public class StateSet extends StatefulMetric<StateSetDataPoint, StateSet.DataPoint>
    implements StateSetDataPoint {

  private final String[] names;

  private StateSet(Builder builder, String[] names) {
    super(builder);
    this.names = names;
    for (String name : names) {
      if (this.getMetadata().getPrometheusName().equals(prometheusName(name))) {
        throw new IllegalArgumentException(
            "Label name "
                + name
                + " is illegal (can't use the metric name as label name in state set metrics)");
      }
    }
  }

  @Override
  public StateSetSnapshot collect() {
    return (StateSetSnapshot) super.collect();
  }

  @Override
  protected StateSetSnapshot collect(List<Labels> labels, List<DataPoint> metricDataList) {
    List<StateSetSnapshot.StateSetDataPointSnapshot> data = new ArrayList<>(labels.size());
    for (int i = 0; i < labels.size(); i++) {
      data.add(
          new StateSetSnapshot.StateSetDataPointSnapshot(
              names, metricDataList.get(i).values, labels.get(i)));
    }
    return new StateSetSnapshot(getMetadata(), data);
  }

  @Override
  public MetricType getMetricType() {
    return MetricType.STATESET;
  }

  @Override
  public void setTrue(String state) {
    getNoLabels().setTrue(state);
  }

  @Override
  public void setFalse(String state) {
    getNoLabels().setFalse(state);
  }

  @Override
  protected DataPoint newDataPoint() {
    return new DataPoint();
  }

  class DataPoint implements StateSetDataPoint {

    private final boolean[] values = new boolean[names.length];

    private DataPoint() {}

    @Override
    public void setTrue(String state) {
      set(state, true);
    }

    @Override
    public void setFalse(String state) {
      set(state, false);
    }

    private void set(String name, boolean value) {
      for (int i = 0; i < names.length; i++) {
        if (names[i].equals(name)) {
          values[i] = value;
          return;
        }
      }
      throw new IllegalArgumentException(name + ": unknown state");
    }
  }

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder extends StatefulMetric.Builder<Builder, StateSet> {

    @Nullable private String[] names;

    private Builder(PrometheusProperties config) {
      super(Collections.emptyList(), config);
    }

    /** Declare the states that should be represented by this StateSet. */
    public Builder states(Class<? extends Enum<?>> enumClass) {
      return states(
          Stream.of(enumClass.getEnumConstants()).map(Enum::toString).toArray(String[]::new));
    }

    /** Declare the states that should be represented by this StateSet. */
    public Builder states(String... stateNames) {
      if (stateNames.length == 0) {
        throw new IllegalArgumentException("states cannot be empty");
      }
      this.names = Stream.of(stateNames).distinct().sorted().toArray(String[]::new);
      return this;
    }

    @Override
    public StateSet build() {
      if (names == null) {
        throw new IllegalStateException("State names are required when building a StateSet.");
      }
      return new StateSet(this, names);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
