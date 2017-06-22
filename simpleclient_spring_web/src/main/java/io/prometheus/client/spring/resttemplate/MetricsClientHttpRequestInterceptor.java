package io.prometheus.client.spring.resttemplate;

import io.prometheus.client.Summary;
import io.prometheus.client.Summary.Child;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class MetricsClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  private static final List<MetricsRestTemplateLabelType> DEFAULT_LABELS =
      Arrays.asList(MetricsRestTemplateLabelType.values());

  private final Summary metricClientInterceptorSummary;

  private final List<MetricsRestTemplateLabelType> labelTypes;

  public MetricsClientHttpRequestInterceptor() {
    this(DEFAULT_LABELS);
  }

  public MetricsClientHttpRequestInterceptor(List<MetricsRestTemplateLabelType> labelTypes) {
    this.labelTypes = labelTypes;
    metricClientInterceptorSummary = Summary.build()
        .name("rest_template_request_duration_seconds")
        .labelNames(getLabelNames())
        .help("Rest template request duration (s)")
        .register();
  }

  private String[] getLabelNames() {
    final int size = labelTypes.size();
    final String[] labelNames = new String[size];
    for (int i = 0; i < size; i++) {
      labelNames[i] = labelTypes.get(i).name().toLowerCase();
    }
    return labelNames;
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
    final int size = labelTypes.size();
    final String[] labelValues = new String[size];
    for (int i = 0; i < size; i++) {
      labelValues[i] = labelTypes.get(i).resolve(request);
    }
    return metricClientInterceptorSummary.labels(labelValues);
  }
}
