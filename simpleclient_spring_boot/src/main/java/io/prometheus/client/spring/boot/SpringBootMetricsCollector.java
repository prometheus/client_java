package io.prometheus.client.spring.boot;

import io.prometheus.client.Collector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

import java.util.*;

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
  protected MetricFamilySamples httpResponseCountSamples;

  @Autowired
  public SpringBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
    this.publicMetrics = publicMetrics;
  }

  protected void reset() {
    // Spring Boot counters can be decremented, so we play it safe and make this a gauge in Prometheus
    httpResponseCountSamples = new MetricFamilySamples(
            "http_response_count", Type.GAUGE, "HTTP response count by path and status",
            new LinkedList<MetricFamilySamples.Sample>());
  }

  protected void processHttpResponseCount(Metric<?> metric) {
    if (!metric.getName().startsWith("counter.status.")) {
      return;
    }
    // Parse the Spring Boot Actuator metric name
    String[] parts = metric.getName().split("\\.", 4);
    String responseCode = parts[2];
    // We could do more processing, like mapping "root" -> "/", but there's not enough information provided by
    // Spring to determine whether "counter.status.200.foo.bar" means the path "/foo.bar" or the path "/foo/bar",
    // so it's less confusing to just leave the path as it is
    String path = parts[3];
    // Register the sample
    httpResponseCountSamples.samples.add(
            new MetricFamilySamples.Sample(
                    httpResponseCountSamples.name,
                    Arrays.asList("path", "status"),
                    Arrays.asList(path, responseCode),
                    metric.getValue().doubleValue()
            )
    );
  }

  @Override
  public List<MetricFamilySamples> collect() {
    reset();
    ArrayList<MetricFamilySamples> samples = new ArrayList<MetricFamilySamples>();
    for (PublicMetrics publicMetrics : this.publicMetrics) {
      for (Metric<?> metric : publicMetrics.metrics()) {
        String name = Collector.sanitizeMetricName(metric.getName());
        double value = metric.getValue().doubleValue();
        MetricFamilySamples metricFamilySamples = new MetricFamilySamples(
                name, Type.GAUGE, name, Collections.singletonList(
                new MetricFamilySamples.Sample(name, Collections.<String>emptyList(), Collections.<String>emptyList(), value)));
        samples.add(metricFamilySamples);
        processHttpResponseCount(metric);
      }
    }
    if (httpResponseCountSamples.samples.size() > 0) {
      samples.add(httpResponseCountSamples);
    }
    return samples;
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return new ArrayList<MetricFamilySamples>();
  }
}
