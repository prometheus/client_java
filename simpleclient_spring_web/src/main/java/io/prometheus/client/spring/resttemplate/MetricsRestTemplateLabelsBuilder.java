package io.prometheus.client.spring.resttemplate;

import java.util.LinkedList;
import java.util.List;

public class MetricsRestTemplateLabelsBuilder {

  private List<MetricsRestTemplateLabelType> metricsRestTemplateLabelTypes;

  public MetricsRestTemplateLabelsBuilder() {
    this.metricsRestTemplateLabelTypes = new LinkedList<MetricsRestTemplateLabelType>();
  }

  public MetricsRestTemplateLabelsBuilder withMethod() {
    metricsRestTemplateLabelTypes.add(MetricsRestTemplateLabelType.METHOD);
    return this;
  }

  public MetricsRestTemplateLabelsBuilder withHost() {
    metricsRestTemplateLabelTypes.add(MetricsRestTemplateLabelType.HOST);
    return this;
  }

  public MetricsRestTemplateLabelsBuilder withPath() {
    metricsRestTemplateLabelTypes.add(MetricsRestTemplateLabelType.PATH);
    return this;
  }

  public List<MetricsRestTemplateLabelType> build() {
    return metricsRestTemplateLabelTypes;
  }
}
