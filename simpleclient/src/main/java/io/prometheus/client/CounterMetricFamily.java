package io.prometheus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Counter metric family, for custom collectors and exporters.
 * <p>
 * Most users want a normal {@link Counter} instead.
 *
 * Example usage:
 * <pre>
 * {@code
 *   class YourCustomCollector extends Collector {
 *     List<MetricFamilySamples> collect() {
 *       List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
 *       // With no labels.
 *       mfs.add(new CounterMetricFamily("my_counter_total", "help", 42));
 *       // With labels
 *       CounterMetricFamily labeledCounter = new CounterMetricFamily("my_other_counter_total", "help", Arrays.asList("labelname"));
 *       labeledCounter.addMetric(Arrays.asList("foo"), 4);
 *       labeledCounter.addMetric(Arrays.asList("bar"), 5);
 *       mfs.add(labeledCounter);
 *       return mfs;
 *     }
 *   }
 * }
 * </pre>
 */
public class CounterMetricFamily extends Collector.MetricFamilySamples {

  private List<String> labelNames;

  public CounterMetricFamily(String name, String help, double value) {
    super(name, Collector.Type.COUNTER, help, new ArrayList<Sample>());
    labelNames = Collections.emptyList();
    samples.add(
        new Sample(
          name,
          labelNames, 
          Collections.<String>emptyList(),
          value));
  }

  public CounterMetricFamily(String name, String help, List<String> labelNames) {
    super(name, Collector.Type.COUNTER, help, new ArrayList<Sample>());
    this.labelNames = labelNames;
  }

  public CounterMetricFamily addMetric(List<String> labelValues, double value) {
    if (labelValues.size() != labelNames.size()) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    samples.add(new Sample(name, labelNames, labelValues, value));
    return this;
  }
}
