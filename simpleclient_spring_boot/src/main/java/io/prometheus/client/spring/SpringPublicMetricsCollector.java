package io.prometheus.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;

/**
 * @author pidster
 */
final class SpringPublicMetricsCollector extends Collector implements BeanPostProcessor {

  private final String prefix;
  private final Collection<PublicMetrics> publicMetrics = new HashSet<PublicMetrics>();

  SpringPublicMetricsCollector(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof PublicMetrics) {
      publicMetrics.add((PublicMetrics) bean);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof PublicMetrics) {
      publicMetrics.add((PublicMetrics) bean);
    }
    return bean;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Collector> T register() {
    // NO-OP, because it's taken care of by Spring and we don't want accidents
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Collector> T register(CollectorRegistry registry) {
    // NO-OP, because it's taken care of by Spring and we don't want accidents
    return (T) this;
  }

  @Override
  public List<MetricFamilySamples> collect() {

    List<MetricFamilySamples> samples = new ArrayList<>();

    for (PublicMetrics pm : publicMetrics) {
      for (Metric<?> metric : pm.metrics()) {
        String metricName = sanitizeMetricName(metric.getName());

        String name = String.format("%s_%s", prefix, metricName);
        double value = metric.getValue().doubleValue();

        Sample sample = new Sample(name, new ArrayList<String>(), new ArrayList<String>(), value);
        MetricFamilySamples metricFamilySamples = new MetricFamilySamples(name, Type.GAUGE, metricName, Collections.singletonList(sample));
        samples.add(metricFamilySamples);
      }
    }

    return samples;
  }
}
