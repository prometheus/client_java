package io.prometheus.client.servlet.jakarta;

import io.prometheus.client.servlet.common.adapter.FilterConfigAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletRequestAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletResponseAdapter;

import io.prometheus.client.servlet.common.adapter.ServletConfigAdapter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class Adapter {

    private static class HttpServletRequestAdapterImpl implements HttpServletRequestAdapter {

        private final HttpServletRequest delegate;

        HttpServletRequestAdapterImpl(HttpServletRequest delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getHeader(String name) {
            return delegate.getHeader(name);
        }

        @Override
        public String getRequestURI() {
            return delegate.getRequestURI();
        }

        @Override
        public String getMethod() {
            return delegate.getMethod();
        }

        @Override
        public String[] getParameterValues(String name) {
            return delegate.getParameterValues(name);
        }

        @Override
        public String getContextPath() {
            return delegate.getContextPath();
        }
    }

    private static class HttpServletResponseAdapterImpl implements HttpServletResponseAdapter {

        private final HttpServletResponse delegate;

        HttpServletResponseAdapterImpl(HttpServletResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        public void setStatus(int httpStatusCode) {
            delegate.setStatus(httpStatusCode);
        }

        @Override
        public void setContentType(String contentType) {
            delegate.setContentType(contentType);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return delegate.getWriter();
        }

        @Override
        public int getStatus() {
            return delegate.getStatus();
        }
    }

    private static class FilterConfigAdapterImpl implements FilterConfigAdapter {

        private final FilterConfig delegate;

        private FilterConfigAdapterImpl(FilterConfig delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getInitParameter(String name) {
            return delegate.getInitParameter(name);
        }
    }

    private static class ServletConfigAdapterImpl implements ServletConfigAdapter {

        final ServletConfig delegate;

        private ServletConfigAdapterImpl(ServletConfig delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getInitParameter(String name) {
            return delegate.getInitParameter(name);
        }
    }

    public static HttpServletRequestAdapter wrap(HttpServletRequest req) {
        return new HttpServletRequestAdapterImpl(req);
    }

    public static HttpServletResponseAdapter wrap(HttpServletResponse resp) {
        return new HttpServletResponseAdapterImpl(resp);
    }

    public static FilterConfigAdapter wrap(FilterConfig filterConfig) {
        return new FilterConfigAdapterImpl(filterConfig);
    }

    public static ServletConfigAdapter wrap(ServletConfig servletConfig) {
        return new ServletConfigAdapterImpl(servletConfig);
    }
}