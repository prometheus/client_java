package io.prometheus.client.spring.resttemplate;

import org.springframework.http.HttpRequest;

public enum MetricsRestTemplateLabelType implements MetricsRestTemplateLabelValueResolver {

  METHOD {
    @Override
    public String resolve(HttpRequest request) {
      return request.getMethod().name();
    }
  },

  HOST {
    @Override
    public String resolve(HttpRequest request) {
      String host = request.getURI().getHost();
      return host != null ? host : "unknown";
    }
  },

  PATH {
    @Override
    public String resolve(HttpRequest request) {
      String path = request.getURI().getPath();
      return path != null ? path : "";
    }
  }
}
