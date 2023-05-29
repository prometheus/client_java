package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.httpServer
 */
public class HttpServerConfig {
    private static final String PORT = "port";
    private final Integer port;

    public HttpServerConfig(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    static HttpServerConfig load(String prefix, Map<Object, Object> properties) throws PrometheusConfigException {
        Integer port = Util.loadInteger(prefix + "." + PORT, properties);
        Util.assertValue(port, t -> t > 0, "Expecting value > 0", prefix, PORT);
        return new HttpServerConfig(port);
    }
}
