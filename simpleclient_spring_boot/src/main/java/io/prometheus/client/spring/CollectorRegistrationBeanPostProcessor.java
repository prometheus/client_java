package io.prometheus.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

/**
 * A <code>BeanPostProcessor</code> that will register any Bean implementing
 * <code>io.prometheus.client.Collector</code> with the <code>io.prometheus.client.CollectorRegistry</code> bean.
 *
 * @author Stuart Williams (pidster)
 */
public class CollectorRegistrationBeanPostProcessor implements BeanPostProcessor {

  private final CollectorRegistry collectorRegistry;

  public CollectorRegistrationBeanPostProcessor(CollectorRegistry collectorRegistry) {
    this.collectorRegistry = collectorRegistry;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof Collector) {
      collectorRegistry.register((Collector) bean);
    }
    return bean;
  }

}
