package io.prometheus.client.filter;

import io.prometheus.client.Histogram;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The StatusAwareMetricsFilter class provides an histogram metrics with the following labels : path, method, and status.
 *
 * This filter behave similarly than MetricsFilter with an extra label that report the status.
 *
 * Do not use MetricsFilter and StatusAwareMetricsFilter with the same metric name. Due to their different labels doing
 * so would raise cardinality issues.
 *
 * {@code
 * <filter>
 *   <filter-name>statusAwarePrometheusFilter</filter-name>
 *   <filter-class>io.prometheus.client.filter.StatusAwareMetricsFilter</filter-class>
 *   <init-param>
 *      <param-name>metric-name</param-name>
 *      <param-value>webapp_metrics_filter</param-value>
 *   </init-param>
 *    <init-param>
 *      <param-name>help</param-name>
 *      <param-value>The time taken fulfilling servlet requests</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>buckets</param-name>
 *      <param-value>0.005,0.01,0.025,0.05,0.075,0.1,0.25,0.5,0.75,1,2.5,5,7.5,10</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>path-components</param-name>
 *      <param-value>0</param-value>
 *   </init-param>
 * </filter>
 * }
 */
public class StatusAwareMetricsFilter extends MetricsFilter {

    @Override
    Histogram.Builder addLabelsNames(Histogram.Builder builder) {
        return builder.labelNames("path", "method", "status");
    }

    @Override
    Histogram.Child addLabelsValues(Histogram builder, HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();
        String status = Integer.toString((response).getStatus());
        return builder.labels(getComponents(path), request.getMethod(), status);
    }

}
