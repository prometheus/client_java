package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

/**
 * Intended mostly for exclusion of undesired collectors from {@link DefaultExports}
 * (see {@link DefaultExports.Builder#exclude(Class)})
 */
public interface HotspotCollector {

    <T extends Collector> T register(CollectorRegistry registry);
}
