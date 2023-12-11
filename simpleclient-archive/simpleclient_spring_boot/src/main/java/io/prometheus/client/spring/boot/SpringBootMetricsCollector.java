package io.prometheus.client.spring.boot;

import io.prometheus.client.Collector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>Spring boot metrics integration for Prometheus exporter.</p>
 *
 * <pre><code>{@literal @}Bean
 * public SpringBootMetricsCollector springBootMetricsCollector(Collection{@literal <}PublicMetrics{@literal >} publicMetrics) {
 *   SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(publicMetrics);
 *   springBootMetricsCollector.register();
 *   return springBootMetricsCollector;
 * }
 * </code></pre>
 */
@Component
public class SpringBootMetricsCollector extends Collector implements Collector.Describable {
  private final Collection<PublicMetrics> publicMetrics;

  @Autowired
  public SpringBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
    this.publicMetrics = publicMetrics;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    ArrayList<MetricFamilySamples> samples = new ArrayList<MetricFamilySamples>();
    for (PublicMetrics publicMetrics : this.publicMetrics) {
      for (Metric<?> metric : publicMetrics.metrics()) {
        String name = Collector.sanitizeMetricName(metric.getName());
        double value = metric.getValue().doubleValue();
        MetricFamilySamples metricFamilySamples = new MetricFamilySamples(
                name, Type.GAUGE, name, Collections.singletonList(
                new MetricFamilySamples.Sample(name, Collections.<String>emptyList(), Collections.<String>emptyList(), value)));
        samples.add(metricFamilySamples);
      }
    }
    return samples;
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return new ArrayList<MetricFamilySamples>();
  }
}
