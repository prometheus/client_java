package io.prometheus.client.filter;

public interface PathToLabelMapper {
    String getLabel(String path);
}
