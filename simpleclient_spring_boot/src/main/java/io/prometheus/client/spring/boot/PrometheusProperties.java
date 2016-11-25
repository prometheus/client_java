package io.prometheus.client.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pidster
 */
@ConfigurationProperties(prefix = "endpoints.prometheus")
public class PrometheusProperties {

    private boolean enabled = true;

    private boolean sensitive = true;

    private String path = "/prometheus";

    private String publicPrefix = "jvm_spring";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPublicPrefix() {
        return publicPrefix;
    }

    public void setPublicPrefix(String publicPrefix) {
        this.publicPrefix = publicPrefix;
    }
}
