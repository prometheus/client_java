package io.prometheus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Summary metric family, for custom collectors and exporters.
 * <p>
 * Most users want a normal {@link Summary} instead.
 *
 * Example usage:
 * <pre>
 * {@code
 *   class YourCustomCollector extends Collector {
 *     List<MetricFamilySamples> collect() {
 *       List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
 *       // With no labels.
 *       mfs.add(new SummaryMetricFamily("my_summary", "help", 1, 42));
 *       // With labels. Record 95th percentile as 3, and 99th percentile as 5.
 *       SummaryMetricFamily labeledSummary = new SummaryMetricFamily("my_other_summary", "help", 
 *           Arrays.asList("labelname"), Arrays.asList(.95, .99));
 *       labeledSummary.addMetric(Arrays.asList("foo"), 2, 10, Arrays.asList(3.0, 5.0));
 *       mfs.add(labeledSummary);
 *       return mfs;
 *     }
 *   }
 * }
 * </pre>
 */
public class SummaryMetricFamily extends Collector.MetricFamilySamples {

  private List<String> labelNames;
  private List<Double> quantiles;

  public SummaryMetricFamily(String name, String help, double count, double sum) {
    super(name, Collector.Type.SUMMARY, help, new ArrayList<Sample>());
    this.labelNames = Collections.emptyList();
    this.quantiles = Collections.emptyList();
    addMetric(Collections.<String>emptyList(), count, sum);
  }

  public SummaryMetricFamily(String name, String help, List<String> labelNames) {
    this(name, help, labelNames, Collections.<Double>emptyList());
  }
  public SummaryMetricFamily(String name, String help, List<String> labelNames, List<Double>quantiles) {
    super(name, Collector.Type.SUMMARY, help, new ArrayList<Sample>());
    this.labelNames = labelNames;
    this.quantiles = quantiles;
  }

  public SummaryMetricFamily addMetric(List<String> labelValues, double count, double sum) {
    return addMetric(labelValues, count, sum, Collections.<Double>emptyList());
  }
  public SummaryMetricFamily addMetric(List<String> labelValues, double count, double sum, List<Double> quantiles) {
    if (labelValues.size() != labelNames.size()) {
      throw new IllegalArgumentException("Incorrect number of labels.");
    }
    if (this.quantiles.size() != quantiles.size()) {
      throw new IllegalArgumentException("Incorrect number of quantiles.");
    }
    samples.add(new Sample(name + "_count", labelNames, labelValues, count));
    samples.add(new Sample(name + "_sum", labelNames, labelValues, sum));
    List<String> labelNamesWithQuantile = new ArrayList<String>(labelNames);
    labelNamesWithQuantile.add("quantile");
    for (int i = 0; i < quantiles.size(); i++) {
      List<String> labelValuesWithQuantile = new ArrayList<String>(labelValues);
      labelValuesWithQuantile.add(Collector.doubleToGoString(this.quantiles.get(i)));
      samples.add(new Sample(name, labelNamesWithQuantile, labelValuesWithQuantile, quantiles.get(i)));
    }
    return this;
  }
}
