package io.prometheus.client.spring.boot;

import io.prometheus.client.spring.resttemplate.MetricsClientHttpRequestInterceptor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass(RestTemplate.class)
public class RestTemplateMetricsConfiguration {

  @Bean
  @ConditionalOnMissingBean(MetricsClientHttpRequestInterceptor.class)
  public MetricsClientHttpRequestInterceptor metricsClientHttpRequestInterceptor() {
    return new MetricsClientHttpRequestInterceptor();
  }

  @Bean
  public MetricsInterceptorPostProcessor metricsInterceptorPostProcessor() {
    return new MetricsInterceptorPostProcessor();
  }

  private static class MetricsInterceptorPostProcessor implements BeanPostProcessor,
      ApplicationContextAware {

    private ApplicationContext context;
    private MetricsClientHttpRequestInterceptor interceptor;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
      return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
      if (RestTemplate.class.isAssignableFrom(bean.getClass())) {
        RestTemplate restTemplate = (RestTemplate) bean;
        List<ClientHttpRequestInterceptor> currentInterceptors = restTemplate.getInterceptors();
        boolean metricsInterceptorPresent = isMetricsInterceptorPresent(currentInterceptors);
        if (metricsInterceptorPresent) {
          return bean;
        }
        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
        if (this.interceptor == null) {
          this.interceptor = this.context.getBean(MetricsClientHttpRequestInterceptor.class);
        }
        interceptors.add(interceptor);
        interceptors.addAll(currentInterceptors);
        restTemplate.setInterceptors(interceptors);
      }
      return bean;
    }

    private boolean isMetricsInterceptorPresent(List<ClientHttpRequestInterceptor> interceptors) {
      for (ClientHttpRequestInterceptor interceptor : interceptors) {
        if (ClientHttpRequestInterceptor.class.isAssignableFrom(interceptor.getClass())) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext context)
        throws BeansException {
      this.context = context;
    }
  }
}
