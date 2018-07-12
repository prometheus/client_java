package io.prometheus.client.tomcat;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

/**
 * Collect metrics from Tomcat's session manager.
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * new TomcatSessionMetricsCollector().register();
 * </pre>
 *
 * @author Christian Kaltepoth
 */
public class TomcatSessionMetricsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Arrays.asList( "host", "context" );

  private final MBeanServer server;

  /**
   * Creates a new collector using the platform default MBeanServer
   */
  public TomcatSessionMetricsCollector() {
    this( ManagementFactory.getPlatformMBeanServer() );
  }

  /**
   * Creates a new collector using a custom MBeanServer
   *
   * @param server MBeanServer to use
   */
  public TomcatSessionMetricsCollector( MBeanServer server ) {
    this.server = server;
  }

  @Override
  public List<MetricFamilySamples> collect() {

    GaugeMetricFamily sessionActive = new GaugeMetricFamily(
      "tomcat_session_active",
      "Number of active sessions at this moment",
      LABEL_NAMES
    );

    CounterMetricFamily sessionCreated = new CounterMetricFamily(
      "tomcat_session_created_total",
      "Total number of sessions created by this manager",
      LABEL_NAMES
    );

    CounterMetricFamily sessionExpired = new CounterMetricFamily(
      "tomcat_session_expired_total",
      "Number of sessions that expired (doesn't include explicit invalidations)",
      LABEL_NAMES
    );

    CounterMetricFamily sessionRejected = new CounterMetricFamily(
      "tomcat_session_rejected_total",
      "Number of sessions we rejected due to maxActive being reached",
      LABEL_NAMES
    );

    CounterMetricFamily processingTime = new CounterMetricFamily(
      "tomcat_session_processing_time_seconds_total",
      "Time spent doing housekeeping and expiration",
      LABEL_NAMES
    );

    try {

      ObjectName objectNamePattern = new ObjectName( "Catalina:type=Manager,host=*,context=*" );

      for( ObjectName beanObjectName : server.queryNames( objectNamePattern, null ) ) {

        List<String> labelValues = Arrays.asList(
          beanObjectName.getKeyProperty( "host" ),
          beanObjectName.getKeyProperty( "context" )
        );

        sessionActive.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "activeSessions" ) ).doubleValue()
        );

        sessionCreated.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "sessionCounter" ) ).doubleValue()
        );

        sessionExpired.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "expiredSessions" ) ).doubleValue()
        );

        sessionRejected.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "rejectedSessions" ) ).doubleValue()
        );

        processingTime.addMetric( labelValues,
          ( (Number) server.getAttribute( beanObjectName, "processingTime" ) ).doubleValue() / 1000L
        );

      }

    }
    catch( JMException e ) {
      throw new IllegalStateException( e );
    }

    return Arrays.asList(
      sessionActive, sessionCreated, sessionExpired, sessionRejected, processingTime
    );

  }
}
