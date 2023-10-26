package io.prometheus.metrics.exporter.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;

import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;

public interface PrometheusHttpRequest extends PrometheusScrapeRequest {

    /**
     * See {@code jakarta.servlet.http.HttpServletRequest.getQueryString()}
     */
    String getQueryString();

    /**
     * See {@code jakarta.servlet.http.HttpServletRequest.getHeaders(String)}
     */
    Enumeration<String> getHeaders(String name);

    /**
     * See {@code jakarta.servlet.http.HttpServletRequest.getMethod()}
     */
    String getMethod();

    /**
     * See {@code jakarta.servlet.http.HttpServletRequest.getHeader(String)}
     */
    default String getHeader(String name) {
        Enumeration<String> headers = getHeaders(name);
        if (headers == null || !headers.hasMoreElements()) {
            return null;
        } else {
            return headers.nextElement();
        }
    }

    /**
     * See {@code jakarta.servlet.ServletRequest.getParameter(String)}
     */
    default String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values == null || values.length == 0) {
            return null;
        } else {
            return values[0];
        }
    }

    /**
     * See {@code jakarta.servlet.ServletRequest.getParameterValues(String)}
     */
    default String[] getParameterValues(String name) {
        try {
            ArrayList<String> result = new ArrayList<>();
            String queryString = getQueryString();
            if (queryString != null) {
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals(name)) {
                        result.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                    }
                }
            }
            if (result.isEmpty()) {
                // Servlet API: getParameterValues() returns null if the parameter does not exist.
                return null;
            } else {
                return result.toArray(new String[0]);
            }
        } catch (UnsupportedEncodingException e) {
            // UTF-8 encoding not supported.
            throw new RuntimeException(e);
        }
    }
}
