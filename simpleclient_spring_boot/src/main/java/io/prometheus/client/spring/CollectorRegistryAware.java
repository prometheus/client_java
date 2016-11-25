package io.prometheus.client.spring;

import org.springframework.beans.factory.Aware;

import io.prometheus.client.CollectorRegistry;

/**
 * An <code>Aware</code> interface for beans that wish to receive a
 * <code>io.prometheus.client.CollectorRegistry</code> during startup.
 *
 * @author Stuart Williams (pidster)
 * @see CollectorRegistry
 */
public interface CollectorRegistryAware extends Aware {

    /**
     * @param collectorRegistry to set
     */
    void setCollectorRegistry(CollectorRegistry collectorRegistry);

}
