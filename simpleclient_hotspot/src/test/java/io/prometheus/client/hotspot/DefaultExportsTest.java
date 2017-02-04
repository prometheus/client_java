package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


public class DefaultExportsTest {

  @Test
  public void shouldRegistrerDefaultExportsWithProvidedRegistry() {
    CollectorRegistry providedCollectorRegistry = new CollectorRegistry();
    DefaultExports.initialize(providedCollectorRegistry);

    Set<String> collectors = new HashSet<String>();
    Enumeration<Collector.MetricFamilySamples> metricFamilySamples = providedCollectorRegistry.metricFamilySamples();
    while (metricFamilySamples.hasMoreElements()) {
      Collector.MetricFamilySamples element = metricFamilySamples.nextElement();
      collectors.add(element.name);
    }

    assertTrue("Contains process_cpu_seconds_total", collectors.contains("process_cpu_seconds_total"));
    assertTrue("Contains jvm_memory_bytes_used", collectors.contains("jvm_memory_bytes_used"));
    assertTrue("Contains jvm_gc_collection_seconds", collectors.contains("jvm_gc_collection_seconds"));
    assertTrue("Contains jvm_threads_current", collectors.contains("jvm_threads_current"));
    assertTrue("Contains jvm_classes_loaded", collectors.contains("jvm_classes_loaded"));
  }

}