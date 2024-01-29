package io.prometheus.metrics.exporter.servlet.javax;

import io.prometheus.metrics.exporter.common.PrometheusHttpExchange;
import io.prometheus.metrics.exporter.common.PrometheusHttpRequest;
import io.prometheus.metrics.exporter.common.PrometheusHttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * This class is an adapter for HTTP exchanges, implementing the PrometheusHttpExchange interface.
 * It wraps HttpServletRequest and HttpServletResponse objects into Request and Response inner classes.
 */
public class HttpExchangeAdapter implements PrometheusHttpExchange {

    private final Request request;
    private final Response response;

    /**
     * Constructs a new HttpExchangeAdapter with the given HttpServletRequest and HttpServletResponse.
     *
     * @param request  the HttpServletRequest to be adapted
     * @param response the HttpServletResponse to be adapted
     */
    public HttpExchangeAdapter(HttpServletRequest request, HttpServletResponse response) {
        this.request = new Request(request);
        this.response = new Response(response);
    }

    /**
     * Returns the adapted HttpServletRequest.
     *
     * @return the adapted HttpServletRequest
     */
    @Override
    public PrometheusHttpRequest getRequest() {
        return request;
    }

    /**
     * Returns the adapted HttpServletResponse.
     *
     * @return the adapted HttpServletResponse
     */
    @Override
    public PrometheusHttpResponse getResponse() {
        return response;
    }

    @Override
    public void handleException(IOException e) throws IOException {
        throw e; // leave exception handling to the servlet container
    }

    @Override
    public void handleException(RuntimeException e) {
        throw e; // leave exception handling to the servlet container
    }

    @Override
    public void close() {
        // nothing to do for Servlets.
    }

    /**
     * This inner class adapts a HttpServletRequest to a PrometheusHttpRequest.
     */
    public static class Request implements PrometheusHttpRequest {

        private final HttpServletRequest request;

        /**
         * Constructs a new Request with the given HttpServletRequest.
         *
         * @param request the HttpServletRequest to be adapted
         */
        public Request(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public String getQueryString() {
            return request.getQueryString();
        }


        @Override
        public Enumeration<String> getHeaders(String name) {
            return request.getHeaders(name);
        }


        @Override
        public String getMethod() {
            return request.getMethod();
        }


        @Override
        public String getRequestPath() {
            StringBuilder uri = new StringBuilder();
            String contextPath = request.getContextPath();
            if (contextPath.startsWith("/")) {
                uri.append(contextPath);
            }
            String servletPath = request.getServletPath();
            if (servletPath.startsWith("/")) {
                uri.append(servletPath);
            }
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                uri.append(pathInfo);
            }
            return uri.toString();
        }
    }

    /**
     * This inner class adapts a HttpServletResponse to a PrometheusHttpResponse.
     */
    public static class Response implements PrometheusHttpResponse {

        private final HttpServletResponse response;

        /**
         * Constructs a new Response with the given HttpServletResponse.
         *
         * @param response the HttpServletResponse to be adapted
         */
        public Response(HttpServletResponse response) {
            this.response = response;
        }


        @Override
        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }


        @Override
        public OutputStream sendHeadersAndGetBody(int statusCode, int contentLength) throws IOException {
            if (response.getHeader("Content-Length") == null && contentLength > 0) {
                response.setContentLength(contentLength);
            }
            response.setStatus(statusCode);
            return response.getOutputStream();
        }
    }
}