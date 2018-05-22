package io.prometheus.client.filter;

import io.prometheus.client.Histogram;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The MetricsFilter class exists to provide a high-level filter that enables tunable collection of metrics for Servlet
 * performance.
 *
 * The Histogram name itself is required, and configured with a {@code metric-name} init parameter.
 *
 * The help parameter, configured with the {@code help} init parameter, is not required but strongly recommended.
 *
 * By default, this filter will provide metrics that distinguish only 1 level deep for the request path
 * (including servlet context path), but can be configured with the {@code path-components} init parameter. Any number
 * provided that is less than 1 will provide the full path granularity (warning, this may affect performance).
 *
 * The Histogram buckets can be configured with a {@code buckets} init parameter whose value is a comma-separated list
 * of valid {@code double} values.
 *
 * {@code
 * <filter>
 *   <filter-name>prometheusFilter</filter-name>
 *   <filter-class>net.cccnext.ssp.portal.spring.filter.PrometheusMetricsFilter</filter-class>
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
 *
 * @author Andrew Stuart &lt;andrew.stuart2@gmail.com&gt;
 */
public class MetricsFilter implements Filter {
    static final String PATH_COMPONENT_PARAM = "path-components";
    static final String HELP_PARAM = "help";
    static final String METRIC_NAME_PARAM = "metric-name";
    static final String BUCKET_CONFIG_PARAM = "buckets";

    private Histogram histogram = null;

    private PathToLabelMapper pathToLabelMapper = new SlashLimitingPathMapper(1);
    private String metricName = null;
    private String help = "The time taken fulfilling servlet requests";
    private double[] buckets = null;

    public MetricsFilter() {}

    public MetricsFilter(
            String metricName,
            String help,
            PathToLabelMapper pathToLabelMapper,
            double[] buckets) {
        this.metricName = metricName;
        this.buckets = buckets;
        if (help != null) {
            this.help = help;
        }
        if (pathToLabelMapper != null) {
            this.pathToLabelMapper = pathToLabelMapper;
        }
    }

    public MetricsFilter(
            String metricName,
            String help,
            Integer pathComponents,
            double[] buckets) {
        this(metricName, help, pathComponents != null ? new SlashLimitingPathMapper(pathComponents) : null, buckets);
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Histogram.Builder builder = Histogram.build()
                .labelNames("path", "method");

        if (filterConfig == null && isEmpty(metricName)) {
            throw new ServletException("No configuration object provided, and no metricName passed via constructor");
        }

        if (filterConfig != null) {
            if (isEmpty(metricName)) {
                metricName = filterConfig.getInitParameter(METRIC_NAME_PARAM);
                if (isEmpty(metricName)) {
                    throw new ServletException("Init parameter \"" + METRIC_NAME_PARAM + "\" is required; please supply a value");
                }
            }

            if (!isEmpty(filterConfig.getInitParameter(HELP_PARAM))) {
                help = filterConfig.getInitParameter(HELP_PARAM);
            }

            // Allow overriding of the path "depth" to track
            if (!isEmpty(filterConfig.getInitParameter(PATH_COMPONENT_PARAM))) {
                pathToLabelMapper = new SlashLimitingPathMapper(Integer.valueOf(filterConfig.getInitParameter(PATH_COMPONENT_PARAM)));
            }

            // Allow users to override the default bucket configuration
            if (!isEmpty(filterConfig.getInitParameter(BUCKET_CONFIG_PARAM))) {
                String[] bucketParams = filterConfig.getInitParameter(BUCKET_CONFIG_PARAM).split(",");
                buckets = new double[bucketParams.length];

                for (int i = 0; i < bucketParams.length; i++) {
                    buckets[i] = Double.parseDouble(bucketParams[i]);
                }
            }
        }

        if (buckets != null) {
            builder = builder.buckets(buckets);
        }

        histogram = builder
                .help(help)
                .name(metricName)
                .register();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String path = request.getRequestURI();

        Histogram.Timer timer = histogram
            .labels(pathToLabelMapper.getLabel(path), request.getMethod())
            .startTimer();

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            timer.observeDuration();
        }
    }

    @Override
    public void destroy() {
    }
}
