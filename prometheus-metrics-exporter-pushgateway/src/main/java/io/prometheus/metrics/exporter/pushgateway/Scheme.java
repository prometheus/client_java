package io.prometheus.metrics.exporter.pushgateway;

public enum Scheme {

    HTTP("http"),
    HTTPS("https");

    private final String name;

    Scheme(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Scheme fromString(String name) {
        switch (name) {
            case "http":
                return HTTP;
            case "https":
                return HTTPS;
            default:
                throw new IllegalArgumentException(name + ": Unsupported scheme. Expecting 'http' or 'https'.");
        }
    }
}
