package io.prometheus.client.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.prometheus.client.Summary;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;

/**
 * A {@link Configuration} class that imports the Prometheus
 * JVM management metrics Collectors from the <code>simpleclient_hotspot</code> package.
 *
 * @author pidster
 */
@Configuration
public class HotspotMetricCollectorsConfiguration {

  @Bean
  public ClassLoadingExports classLoadingExports() {
    return new ClassLoadingExports();
  }

  @Bean
  public GarbageCollectorExports garbageCollectorExports() {
    return new GarbageCollectorExports();
  }

  @Bean
  public MemoryPoolsExports memoryPoolsExports() {
    return new MemoryPoolsExports();
  }

  @Bean
  public StandardExports standardExports() {
    return new StandardExports();
  }

  @Bean
  public ThreadExports threadExports() {
    return new ThreadExports();
  }

}
