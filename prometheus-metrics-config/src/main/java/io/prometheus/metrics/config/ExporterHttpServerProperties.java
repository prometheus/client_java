package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.exporter.httpServer
 */
public class ExporterHttpServerProperties {

    // TODO: Not used yet, will be used when we port the simpleclient_httpserver module to the new data model.

    private static final String PORT = "port";
    private final Integer port;

    private ExporterHttpServerProperties(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static ExporterHttpServerProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        Integer port = Util.loadInteger(prefix + "." + PORT, properties);
        Util.assertValue(port, t -> t > 0, "Expecting value > 0", prefix, PORT);
        return new ExporterHttpServerProperties(port);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Integer port;

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public ExporterHttpServerProperties build() {
            return new ExporterHttpServerProperties(port);
        }
    }
}
