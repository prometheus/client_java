package io.prometheus.client;

public interface LabelValueSanitizer {
    String[] sanitize(String... labelValue);
}
