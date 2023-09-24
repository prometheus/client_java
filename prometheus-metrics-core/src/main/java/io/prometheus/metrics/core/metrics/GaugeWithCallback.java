package io.prometheus.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gauge metric family, for custom collectors and exporters.
 * <p>
 * Most users want a normal {@link Gauge} instead.
 *
 * Example usage:
 * <pre>
 * {@code
 *   class YourCustomCollector extends Collector {
 *     List<MetricFamilySamples> collect() {
 *       List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
 *       // With no labels.
 *       mfs.add(new GaugeMetricFamily("my_gauge", "help", 42));
 *       // With labels
 *       GaugeMetricFamily labeledGauge = new GaugeMetricFamily("my_other_gauge", "help", Arrays.asList("labelname"));
 *       labeledGauge.addMetric(Arrays.asList("foo"), 4);
 *       labeledGauge.addMetric(Arrays.asList("bar"), 5);
 *       mfs.add(labeledGauge);
 *       return mfs;
 *     }
 *   }
 * }
 * </pre>
 */
public class GaugeMetricFamily extends Collector.MetricFamilySamples {

  private final List<String> labelNames;

  public GaugeMetricFamily(String name, String help, double value) {
    super(name, Collector.Type.GAUGE, help, new ArrayList<Sample>());
    labelNames = Collections.emptyList();
    samples.add(
        new Sample(
          name,
          labelNames, 
          Collections.<String>emptyList(),
          value));
  }

  public GaugeMetricFamily(String name, String help, List<String> labelNames) {
    super(name, Collector.Type.GAUGE, help, new ArrayList<Sample>());
    this.labelNames = labelNames;
  }

  public GaugeMetricFamily addMetric(List<String> labelValues, double value) {
    if (labelValues.size() != labelNames.size()) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    samples.add(new Sample(name, labelNames, labelValues, value));
    return this;
  }
}
