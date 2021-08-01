package io.prometheus.client.servlet.jakarta.filter;

import io.prometheus.client.servlet.jakarta.Adapter;
import io.prometheus.client.servlet.common.filter.Filter;
import io.prometheus.client.servlet.common.filter.FilterConfigurationException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The MetricsFilter class provides a high-level filter that enables tunable collection of metrics for Servlet
 * performance.
 *
 * This is the Jakarta version of the MetricsFilter. If you are using Javax Servlet, there is a Javax version
 * available in {@code simpleclient-servlet}.
 *
 * <p>The metric name itself is required, and configured with a {@code metric-name} init parameter.
 *
 * <p>The help parameter, configured with the {@code help} init parameter, is not required but strongly recommended.
 *
 * <p>The Histogram buckets can be configured with a {@code buckets} init parameter whose value is a comma-separated
 * list * of valid {@code double} values. If omitted, the default buckets from {@link io.prometheus.client.Histogram}
 * are used.
 *
 * <p>By default, this filter will provide metrics that distinguish only 1 level deep for the request path
 * (including servlet context path), but can be configured with the {@code path-components} init parameter. Any number
 * provided that is less than 1 will provide the full path granularity (warning, this may affect performance).
 *
 * <p>The {@code strip-context-path} init parameter can be used to avoid including the leading path components which are
 * part of the context (i.e. the folder where the servlet is deployed) so that the same project deployed under different
 * paths can produce the same metrics.
 *
 * <p>HTTP statuses will be aggregated via Counter. The name for this counter will be derived from the
 * {@code metric-name} init parameter.
 *
 * <pre>{@code
 * <filter>
 *   <filter-name>prometheusFilter</filter-name>
 *   <!-- This example shows the javax version. For Jakarta you would use -->
 *   <!-- <filter-class>io.prometheus.client.filter.servlet.jakarta.MetricsFilter</filter-class> -->
 *   <filter-class>io.prometheus.client.filter.MetricsFilter</filter-class>
 *   <init-param>
 *     <param-name>metric-name</param-name>
 *     <param-value>webapp_metrics_filter</param-value>
 *   </init-param>
 *   <!-- help is optional, defaults to the message below -->
 *   <init-param>
 *     <param-name>help</param-name>
 *     <param-value>This is the help for your metrics filter</param-value>
 *   </init-param>
 *   <!-- buckets is optional, unless specified the default buckets from io.prometheus.client.Histogram are used -->
 *   <init-param>
 *     <param-name>buckets</param-name>
 *     <param-value>0.005,0.01,0.025,0.05,0.075,0.1,0.25,0.5,0.75,1,2.5,5,7.5,10</param-value>
 *   </init-param>
 *   <!-- path-components is optional, anything less than 1 (1 is the default) means full granularity -->
 *   <init-param>
 *     <param-name>path-components</param-name>
 *     <param-value>1</param-value>
 *   </init-param>
 *   <!-- strip-context-path is optional, defaults to false -->
 *   <init-param>
 *     <param-name>strip-context-path</param-name>
 *     <param-value>false</param-value>
 *   </init-param>
 * </filter>
 *
 * <!-- You will most likely want this to be the first filter in the chain
 * (therefore the first <filter-mapping> in the web.xml file), so that you can get
 * the most accurate measurement of latency. -->
 * <filter-mapping>
 *   <filter-name>prometheusFilter</filter-name>
 *   <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }</pre>
 */
public class MetricsFilter implements jakarta.servlet.Filter {

    private final Filter delegate;

    public MetricsFilter() {
        this.delegate = new Filter();
    }

    /**
     * See {@link Filter#Filter(String, String, Integer, double[], boolean)}.
     */
    public MetricsFilter(
            String metricName,
            String help,
            Integer pathComponents,
            double[] buckets,
            boolean stripContextPath) {
        this.delegate = new Filter(metricName, help, pathComponents, buckets, stripContextPath);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            delegate.init(Adapter.wrap(filterConfig));
        } catch (FilterConfigurationException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        Filter.MetricData data = delegate.startTimer(Adapter.wrap((HttpServletRequest) servletRequest));
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            delegate.observeDuration(data, Adapter.wrap((HttpServletResponse) servletResponse));
        }
    }

    @Override
    public void destroy() {
    }
}
