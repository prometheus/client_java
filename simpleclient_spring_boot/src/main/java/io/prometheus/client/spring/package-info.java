/**
 * This package provides Spring JavaConfig style utilities for Prometheus Collectors.
 *
 * <p>
 * To enable this functionality, add the following <code>{@literal @}EnableXxxx</code>
 * annotations to a Configuration class in your application.
 *
 * <p>
 * When combined, any {@link io.prometheus.client.Collector} defined as a Spring bean
 * will be automatically registered with the {@link io.prometheus.client.CollectorRegistry}.
 *
 * <p>
 * <pre><code>
 *{@literal @}Configuration
 *{@literal @}EnablePrometheusCollectorRegistration
 *{@literal @}EnablePrometheusMetrics
 * public class MyConfiguration {
 *
 *  {@literal @}Bean
 *   public Counter counter() {
 *     return new Counter.Builder()
 *       .namespace("testnamespace")
 *       .subsystem("testsubsystem")
 *       .name("testname")
 *       .help("Help for testname")
 *       .create();
 *   }
 *
 * }
 * </code></pre>
 *
 * @see io.prometheus.client.spring.EnablePrometheusCollectorRegistration
 * @see io.prometheus.client.spring.EnablePrometheusMetrics
 * @author Stuart Williams (pidster)
 */
package io.prometheus.client.spring;