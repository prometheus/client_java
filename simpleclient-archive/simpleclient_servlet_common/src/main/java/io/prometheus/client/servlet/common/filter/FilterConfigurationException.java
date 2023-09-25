package io.prometheus.client.servlet.common.filter;

/**
 * Thrown when there is a misconfiguration in {@code web.xml}.
 */
public class FilterConfigurationException extends Exception {
    public FilterConfigurationException(String msg) {
        super(msg);
    }
}
