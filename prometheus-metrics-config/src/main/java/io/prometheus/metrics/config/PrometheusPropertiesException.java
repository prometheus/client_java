package io.prometheus.metrics.config;

public class PrometheusPropertiesException extends RuntimeException {

    public PrometheusPropertiesException(String msg) {
        super(msg);
    }

    public PrometheusPropertiesException(String msg, Exception cause) {
        super(msg, cause);
    }
}
