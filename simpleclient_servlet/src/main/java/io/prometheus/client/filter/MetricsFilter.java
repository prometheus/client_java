package io.prometheus.client.filter;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by andrewstuart on 11/19/16.
 *
 * The MetricsFilter class exists to provide a high-level filter that enables tunable collection of metrics for Servlet
 * performance.
 *
 * By default, this filter will provide metrics that distinguish 3 levels deep for the request path
 * (including servlet context path).
 */
public class MetricsFilter implements Filter {
    public static final String PATH_COMPONENT_PARAM = "path-components";
    public static final int DEFAULT_PATH_COMPONENTS = 3;
    public static final String FILTER_NAME = "servlet_request_latency";

    private static Histogram servletLatency = null;

    private static int pathComponents = DEFAULT_PATH_COMPONENTS;

    int getPathComponents() {
        return pathComponents;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        servletLatency = Histogram.build()
                .name(FILTER_NAME)
                .help("The time taken fulfilling uportal requests")
                .labelNames("path", "verb")
                .register();
        if (filterConfig != null && !StringUtils.isEmpty(filterConfig.getInitParameter(PATH_COMPONENT_PARAM))) {
            pathComponents = Integer.valueOf(filterConfig.getInitParameter(PATH_COMPONENT_PARAM));
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletLatency == null || !(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String path = request.getRequestURI();
        int lastSlash = StringUtils.ordinalIndexOf(path, "/", pathComponents+1);

        Histogram.Timer timer = servletLatency
            .labels(lastSlash == -1 ? path : path.substring(0, lastSlash), request.getMethod())
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
