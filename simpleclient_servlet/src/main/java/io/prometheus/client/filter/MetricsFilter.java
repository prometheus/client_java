package io.prometheus.client.filter;

import io.prometheus.client.Histogram;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The MetricsFilter class exists to provide a high-level filter that enables tunable collection of metrics for Servlet
 * performance.
 *
 * The Histogram name itself is required, and configured with a {@code metric-name} init parameter.
 *
 * By default, this filter will provide metrics that distinguish 3 levels deep for the request path
 * (including servlet context path), but can be configured with the {@code path-components} init parameter.
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
 *   <init-param>
 *      <param-name>buckets</param-name>
 *      <param-value>.001,.002,.005,.010,.020,.040,.080,.120,.200</param-value>
 *   </init-param>
 * </filter>
 * }
 *
 * @author Andrew Stuart &lt;andrew.stuart2@gmail.com&gt;
 */
public class MetricsFilter implements Filter {
    public static final String PATH_COMPONENT_PARAM = "path-components";
    public static final String METRIC_NAME_PARAM = "metric-name";
    public static final String BUCKET_CONFIG_PARAM = "buckets";

    private Histogram histogram = null;

    // Package-level for testing purposes
    int pathComponents = 0;

    public MetricsFilter() {};

    public MetricsFilter(Histogram histogram, int pathComponents) {
        this.histogram = histogram;
        this.pathComponents = pathComponents;
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private String getComponents(String str) {
        if (str == null || pathComponents <= 0) {
            return str;
        }
        int count = 0;
        int i =  -1;
        do {
            i = str.indexOf("/", i + 1);
            if (i < 0) {
                // Path is longer than specified pathComponents.
                return str;
            }
            count++;
        } while (count <= pathComponents);

        return str.substring(0, i);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Histogram.Builder builder = Histogram.build()
                .help("The time taken fulfilling servlet requests")
                .labelNames("path", "method");

        if (filterConfig == null) {
            throw new ServletException("Please provide a configuration object, even for testing.");
        }

        if (isEmpty(filterConfig.getInitParameter(METRIC_NAME_PARAM))) {
            throw new ServletException("Init parameter \"" + METRIC_NAME_PARAM + "\" is required. Please supply a value");
        }

        // "metric-name" is required
        builder.name(filterConfig.getInitParameter(METRIC_NAME_PARAM));

        // Allow overriding of the path "depth" to track
        if (!isEmpty(filterConfig.getInitParameter(PATH_COMPONENT_PARAM))) {
            pathComponents = Integer.valueOf(filterConfig.getInitParameter(PATH_COMPONENT_PARAM));
        }

        // Allow users to override the default bucket configuration
        if (!isEmpty(filterConfig.getInitParameter(BUCKET_CONFIG_PARAM))) {
            String[] bucketParams = filterConfig.getInitParameter(BUCKET_CONFIG_PARAM).split(",");
            double[] buckets = new double[bucketParams.length];

            for (int i = 0; i < bucketParams.length; i++) {
                buckets[i] = Double.parseDouble(bucketParams[i]);
            }

            builder.buckets(buckets);
        }

        histogram = builder.register();
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
            .labels(getComponents(path), request.getMethod())
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
