package io.prometheus.metrics.config;

import java.util.Map;

public class ExporterPushgatewayProperties {

    private static final String ADDRESS = "address";
    private static final String JOB = "job";
    private static final String SCHEME = "scheme";
    private final String scheme;
    private final String address;
    private final String job;

    private ExporterPushgatewayProperties(String address, String job, String scheme) {
        this.address = address;
        this.job = job;
        this.scheme = scheme;
    }

    /**
     * Address of the Pushgateway in the form {@code host:port}.
     * Default is {@code localhost:9091}
     */
    public String getAddress() {
        return address;
    }

    /**
     * {@code job} label for metrics being pushed.
     * Default is the name of the JAR file that is running.
     */
    public String getJob() {
        return job;
    }

    /**
     * Scheme to be used when pushing metrics to the pushgateway.
     * Must be "http" or "https". Default is "http".
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static ExporterPushgatewayProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String address = Util.loadString(prefix + "." + ADDRESS, properties);
        String job = Util.loadString(prefix + "." + JOB, properties);
        String scheme = Util.loadString(prefix + "." +  SCHEME, properties);
        if (scheme != null) {
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new PrometheusPropertiesException(prefix + "." + SCHEME + "=" + scheme + ": Illegal value. Expecting 'http' or 'https'.");
            }
        }
        return new ExporterPushgatewayProperties(address, job, scheme);
    }
}
