package io.prometheus.metrics.config;

public class PrometheusConfigException extends RuntimeException {

    public PrometheusConfigException(String msg) {
        super(msg);
    }

    public PrometheusConfigException(String msg, Exception cause) {
        super(msg, cause);
    }
}
