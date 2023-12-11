package io.prometheus.client.servlet.common.adapter;

public interface HttpServletRequestAdapter {
    String getHeader(String name);
    String getRequestURI();
    String getMethod();
    String[] getParameterValues(String name);
    String getContextPath();
}
