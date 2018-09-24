package io.prometheus.client.tomcat;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;

/**
 * Collect metrics from Tomcat's GlobalRequestProcessor.
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * new TomcatRequestMetricsCollector().register();
 * </pre>
 *
 * @author Christian Kaltepoth
 */
public class TomcatRequestMetricsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Collections.singletonList( "connector" );

  private final MBeanServer server;

  /**
   * Creates a new collector using the platform default MBeanServer
   */
  public TomcatRequestMetricsCollector() {
    this( ManagementFactory.getPlatformMBeanServer() );
  }

  /**
   * Creates a new collector using a custom MBeanServer
   *
   * @param server MBeanServer to use
   */
  public TomcatRequestMetricsCollector( MBeanServer server ) {
    this.server = server;
  }

  @Override
  public List<MetricFamilySamples> collect() {

    CounterMetricFamily bytesReceived = new CounterMetricFamily(
      "tomcat_bytes_received_total",
      "The total number of bytes received",
      LABEL_NAMES
    );

    CounterMetricFamily bytesSent = new CounterMetricFamily(
      "tomcat_bytes_sent_total",
      "The total number of bytes sent",
      LABEL_NAMES
    );

    CounterMetricFamily errorCount = new CounterMetricFamily(
      "tomcat_error_count_total",
      "The total number of errors",
      LABEL_NAMES
    );

    CounterMetricFamily requestCount = new CounterMetricFamily(
      "tomcat_request_count_total",
      "The total number of requests processed",
      LABEL_NAMES
    );

    CounterMetricFamily processingTime = new CounterMetricFamily(
      "tomcat_processing_time_total",
      "The total processing time",
      LABEL_NAMES
    );

    try {

      ObjectName objectNamePattern = new ObjectName( "Catalina:type=GlobalRequestProcessor,name=*" );

      for( ObjectName beanObjectName : server.queryNames( objectNamePattern, null ) ) {

        List<String> labelValues = Collections.singletonList(
          beanObjectName.getKeyProperty( "name" )
        );

        bytesReceived.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "bytesReceived" ) ).doubleValue()
        );

        bytesSent.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "bytesSent" ) ).doubleValue()
        );

        errorCount.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "errorCount" ) ).doubleValue()
        );

        requestCount.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "requestCount" ) ).doubleValue()
        );

        processingTime.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "processingTime" ) ).doubleValue()
        );

      }

    }
    catch( JMException e ) {
      throw new IllegalStateException( e );
    }

    return Arrays.<MetricFamilySamples>asList(
      bytesReceived, bytesSent, errorCount, requestCount, processingTime
    );

  }
}
