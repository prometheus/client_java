package io.prometheus.client.servlet.common.filter;

import io.prometheus.client.*;
import io.prometheus.client.servlet.common.adapter.*;

/**
 * Filter implements the common functionality provided by the two MetricsFilter implementations:
 * <ul>
 * <li>javax version: {@code io.prometheus.client.filter.MetricsFilter} provided by {@code simpleclient_servlet}
 * <li>jakarta version: {@code io.prometheus.client.servlet.jakarta.filter.MetricsFilter} provided by {@code simpleclient_servlet_jakarta}
 * </ul>
 * @author Andrew Stuart &lt;andrew.stuart2@gmail.com&gt;
 */
public class Filter {
    static final String PATH_COMPONENT_PARAM = "path-components";
    static final String HELP_PARAM = "help";
    static final String METRIC_NAME_PARAM = "metric-name";
    static final String BUCKET_CONFIG_PARAM = "buckets";
    static final String STRIP_CONTEXT_PATH_PARAM = "strip-context-path";

    private Histogram histogram = null;
    private Counter statusCounter = null;

    // Package-level for testing purposes.
    int pathComponents = 1;
    private String metricName = null;
    boolean stripContextPath = false;
    private String help = "The time taken fulfilling servlet requests";
    private double[] buckets = null;

    public Filter() {}

    /**
     * If you want to configure the filter programmatically instead of via {@code web.xml}, you can
     * pass all configuration parameters to this constructor.
     */
    public Filter(
            String metricName,
            String help,
            Integer pathComponents,
            double[] buckets,
            boolean stripContextPath) {
        this.metricName = metricName;
        this.buckets = buckets;
        if (help != null) {
            this.help = help;
        }
        if (pathComponents != null) {
            this.pathComponents = pathComponents;
        }
        this.stripContextPath = stripContextPath;
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private String getComponents(String str) {
        if (str == null || pathComponents < 1) {
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

    /**
     * Common implementation of {@code javax.servlet.Filter.init()} and {@code jakarta.servlet.Filter.init()}.
     */
    public void init(FilterConfigAdapter filterConfig) throws FilterConfigurationException {
        Histogram.Builder builder = Histogram.build()
                .labelNames("path", "method");

        if (filterConfig == null && isEmpty(metricName)) {
            throw new FilterConfigurationException("No configuration object provided, and no metricName passed via constructor");
        }

        if (filterConfig != null) {
            if (isEmpty(metricName)) {
                metricName = filterConfig.getInitParameter(METRIC_NAME_PARAM);
                if (isEmpty(metricName)) {
                    throw new FilterConfigurationException("Init parameter \"" + METRIC_NAME_PARAM + "\" is required; please supply a value");
                }
            }

            if (!isEmpty(filterConfig.getInitParameter(HELP_PARAM))) {
                help = filterConfig.getInitParameter(HELP_PARAM);
            }

            // Allow users to override the default bucket configuration
            if (!isEmpty(filterConfig.getInitParameter(BUCKET_CONFIG_PARAM))) {
                String[] bucketParams = filterConfig.getInitParameter(BUCKET_CONFIG_PARAM).split(",");
                buckets = new double[bucketParams.length];

                for (int i = 0; i < bucketParams.length; i++) {
                    buckets[i] = Double.parseDouble(bucketParams[i]);
                }
            }

            // Allow overriding of the path "depth" to track
            if (!isEmpty(filterConfig.getInitParameter(PATH_COMPONENT_PARAM))) {
                pathComponents = Integer.parseInt(filterConfig.getInitParameter(PATH_COMPONENT_PARAM));
            }

            if (!isEmpty(filterConfig.getInitParameter(STRIP_CONTEXT_PATH_PARAM))) {
                stripContextPath = Boolean.parseBoolean(filterConfig.getInitParameter(STRIP_CONTEXT_PATH_PARAM));
            }
        }

        if (buckets != null) {
            builder = builder.buckets(buckets);
        }

        histogram = builder
                .help(help)
                .name(metricName)
                .register();

        statusCounter = Counter.build(metricName + "_status_total", "HTTP status codes of " + help)
                .labelNames("path", "method", "status")
                .register();
    }

    /**
     * To be called at the beginning of {@code javax.servlet.Filter.doFilter()} or
     * {@code jakarta.servlet.Filter.doFilter()}.
     */
    public MetricData startTimer(HttpServletRequestAdapter request) {
        String path = request.getRequestURI();
        if (stripContextPath) {
            path = path.substring(request.getContextPath().length());
        }
        String components = getComponents(path);
        String method = request.getMethod();
        Histogram.Timer timer = histogram.labels(components, method).startTimer();
        return new MetricData(components, method, timer);
    }

    /**
     * To be called at the end of {@code javax.servlet.Filter.doFilter()} or
     * {@code jakarta.servlet.Filter.doFilter()}.
     */
    public void observeDuration(MetricData data, HttpServletResponseAdapter resp) {
        String status = Integer.toString(resp.getStatus());
        data.timer.observeDuration();
        statusCounter.labels(data.components, data.method, status).inc();
    }

    public static class MetricData {

        final String components;
        final String method;
        final Histogram.Timer timer;

        private MetricData(String components, String method, Histogram.Timer timer) {
            this.components = components;
            this.method = method;
            this.timer = timer;
        }
    }
}
