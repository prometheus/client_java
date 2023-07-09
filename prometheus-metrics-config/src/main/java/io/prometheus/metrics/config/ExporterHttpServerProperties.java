package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.exporter.httpServer
 */
public class ExporterHttpServerProperties {

    // TODO: Not used yet, will be used when we port the simpleclient_httpserver module to the new data model.

    private static final String PORT = "port";
    private final Integer port;

    public ExporterHttpServerProperties(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    static ExporterHttpServerProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        Integer port = Util.loadInteger(prefix + "." + PORT, properties);
        Util.assertValue(port, t -> t > 0, "Expecting value > 0", prefix, PORT);
        return new ExporterHttpServerProperties(port);
    }
}
