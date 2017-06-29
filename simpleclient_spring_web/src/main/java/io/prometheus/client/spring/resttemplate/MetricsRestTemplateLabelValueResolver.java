package io.prometheus.client.spring.resttemplate;

import org.springframework.http.HttpRequest;

public interface MetricsRestTemplateLabelValueResolver {

  String resolve(HttpRequest request);
}
