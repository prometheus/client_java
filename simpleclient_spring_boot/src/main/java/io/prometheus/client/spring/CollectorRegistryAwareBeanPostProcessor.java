package io.prometheus.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import io.prometheus.client.CollectorRegistry;

/**
 * The <code>BeanPostProcessor</code> that enables the {@link CollectorRegistryAware} interface.
 *
 * @author Stuart Williams (pidster)
 * @see CollectorRegistryAware
 */
public class CollectorRegistryAwareBeanPostProcessor implements BeanPostProcessor {

  private final CollectorRegistry collectorRegistry;

  public CollectorRegistryAwareBeanPostProcessor(CollectorRegistry collectorRegistry) {
    this.collectorRegistry = collectorRegistry;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof CollectorRegistryAware) {
      ((CollectorRegistryAware) bean).setCollectorRegistry(collectorRegistry);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

}
