package io.prometheus.client.spring;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * TODO docs explaining this
 *
 * @author Stuart Williams (pidster)
 */
class EnablePrometheusCollectorRegistrationRegistrar implements ImportBeanDefinitionRegistrar {

  private static final String ENABLE_CLASS = EnablePrometheusCollectorRegistration.class.getName();

  private static final String REGISTRY_FACTORY_BEAN = "collectorRegistryFactoryBean";

  private static final String REGISTRY_AWARE_BEAN = "collectorRegistryAwareBeanPostProcessor";

  private static final String REGISTRATION_BEAN = "collectorRegistrationBeanPostProcessor";

  private static final String USE_DEFAULT_FIELD = "useDefault";

  private static final String REGISTER_FIELD = "registerCollectors";

  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

    boolean useDefault = true;
    boolean registerCollectors = true;

    if (metadata.hasAnnotation(ENABLE_CLASS)) {
      Map<String, Object> attributes = metadata.getAnnotationAttributes(ENABLE_CLASS);
      Object valueDefault = attributes.get(USE_DEFAULT_FIELD);
      if (valueDefault != null && valueDefault instanceof Boolean) {
        useDefault = (Boolean) valueDefault;
      }
      Object valueRegister = attributes.get(REGISTER_FIELD);
      if (valueRegister != null && valueRegister instanceof Boolean) {
        registerCollectors = (Boolean) valueRegister;
      }
    }

    registerFactory(registry, useDefault);
    registerBeanPostProcessor(REGISTRY_AWARE_BEAN, registry, CollectorRegistryAwareBeanPostProcessor.class);

    if (registerCollectors) {
      registerBeanPostProcessor(REGISTRATION_BEAN, registry, CollectorRegistrationBeanPostProcessor.class);
    }
  }

  private void registerFactory(BeanDefinitionRegistry registry, boolean useDefault) {

    if (registry.containsBeanDefinition(REGISTRY_FACTORY_BEAN)) {
      return;
    }

    AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
        .rootBeanDefinition(CollectorRegistryFactoryBean.class)
        .addPropertyValue(USE_DEFAULT_FIELD, useDefault)
        .getBeanDefinition();

    registry.registerBeanDefinition(REGISTRY_FACTORY_BEAN, beanDefinition);
  }

  private void registerBeanPostProcessor(String name, BeanDefinitionRegistry registry, Class<?> clazz) {

    if (registry.containsBeanDefinition(name)) {
      return;
    }

    AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
        .rootBeanDefinition(clazz)
        .addConstructorArgReference(REGISTRY_FACTORY_BEAN)
        .getBeanDefinition();

    registry.registerBeanDefinition(name, beanDefinition);
  }

}
