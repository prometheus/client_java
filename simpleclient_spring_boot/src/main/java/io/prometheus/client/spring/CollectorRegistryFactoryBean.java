package io.prometheus.client.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import io.prometheus.client.CollectorRegistry;

/**
 * A standard <code>FactoryBean</code> for Spring-ifying the {@link CollectorRegistry}.
 *
 * @author Stuart Williams (pidster)
 */
public class CollectorRegistryFactoryBean implements InitializingBean, FactoryBean<CollectorRegistry> {

  private boolean useDefault = true;

  private CollectorRegistry collectorRegistry;

  @Override
  public CollectorRegistry getObject() throws Exception {
    return collectorRegistry;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.collectorRegistry =  useDefault ? CollectorRegistry.defaultRegistry : new CollectorRegistry();
  }

  @Override
  public Class<?> getObjectType() {
    return CollectorRegistry.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setUseDefault(boolean useDefault) {
    this.useDefault = useDefault;
  }
}
