package io.prometheus.client.spring.resttemplate;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import io.prometheus.client.Summary.Child;
import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Interceptor for Spring Rest Template to collect its execution time and result metrics <p>
 * Usage: You need to create a MetricsClientHttpRequestInterceptor bean and set it to your rest
 * template
 * <pre>
 * {@code
 *   {@literal @}Bean
 *   public MetricsClientHttpRequestInterceptor metricsClientHttpRequestInterceptor() {
 *     return new MetricsClientHttpRequestInterceptor();
 *   }
 *
 *   {@literal @}Bean
 *   RestTemplate restTemplate(MetricsClientHttpRequestInterceptor interceptor) {
 *     RestTemplate restTemplate = new RestTemplate();
 *     restTemplate.setInterceptors(Collections.<ClientHttpRequestInterceptor>singletonList(interceptor));
 *     return restTemplate;
 *   }
 * }
 * </pre>
 * Also, you can use your registry:
 * <pre>
 * {@code
 *    new MetricsClientHttpRequestInterceptor(myRegistry);
 * }
 * </pre>
 *
 * @author Cenk Akin
 */
public class MetricsClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  private final static String[] LABELS = {"method", "host", "path"};

  private final Summary metricClientInterceptorSummary;

  public MetricsClientHttpRequestInterceptor() {
    this(CollectorRegistry.defaultRegistry);
  }

  public MetricsClientHttpRequestInterceptor(CollectorRegistry collectorRegistry) {
    metricClientInterceptorSummary = Summary.build()
        .name("spring_rest_template_request_duration_seconds")
        .labelNames(LABELS)
        .help("Rest template request duration (s)")
        .register(collectorRegistry);
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    final Summary.Timer timer = timer(request).startTimer();
    try {
      return execution.execute(request, body);
    } finally {
      timer.observeDuration();
    }
  }

  private Child timer(HttpRequest request) {
    final String method = MetricsRestTemplateLabelType.METHOD.resolve(request);
    final String host = MetricsRestTemplateLabelType.HOST.resolve(request);
    final String path = MetricsRestTemplateLabelType.PATH.resolve(request);
    return metricClientInterceptorSummary.labels(method, host, path);
  }
}
