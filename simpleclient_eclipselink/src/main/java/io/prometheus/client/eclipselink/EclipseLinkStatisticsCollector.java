package io.prometheus.client.eclipselink;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.tools.profiler.PerformanceMonitor;

/**
 * Collect metrics from one or more EclipseLink Session instances.
 * <p>
 * Usage example for a single session:
 * <pre>
 * new EclipseLinkStatisticsCollector(session, "name").register();
 * </pre>
 * Usage example for multiple sessions:
 * <pre>
 * new EclipseLinkStatisticsCollector()
 *     .add(session1, "name1")
 *     .add(session2, "name2")
 *     .register();
 * </pre>
 * Session instance can be obtained from EntityManager instance like this:
 * <pre>
 * Session session = ((JpaEntityManager)entityManager.getDelegate()).getSession();
 * </pre>
 *
 * @author Viktoriia Bakalova (based on HibernateStatisticsCollector from Christian Kaltepoth);
 */
public class EclipseLinkStatisticsCollector extends Collector {

  private static final List<String> LABEL_NAMES = Arrays.asList("session_name");
  private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

  /**
   * Creates an empty collector. If you use this constructor, you have to add one or more session
   * sessions to the collector by calling the {@link #add(Session, String)} method.
   */
  public EclipseLinkStatisticsCollector() {
  }

  /**
   * Creates a new collector for the given session. Calling this constructor has the same effect as
   * creating an empty collector and adding the session using {@link #add(Session, String)}.
   *
   * @param session The EclipseLink Session to collect metrics for
   * @param name A unique name for this Session
   */
  public EclipseLinkStatisticsCollector(Session session, String name) {
    add(session, name);
  }

  /**
   * Registers an EclipseLink Session with this collector.
   *
   * @param session The EclipseLink Session to collect metrics for
   * @param name A unique name for this Session
   * @return Returns the collector
   */
  public EclipseLinkStatisticsCollector add(Session session, String name) {
    sessions.put(name, session);
    return this;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> metrics = new ArrayList<MetricFamilySamples>();
    metrics.addAll(getCounters());
    metrics.addAll(getTimers());
    return metrics;
  }

  @Override
  public <T extends Collector> T register(CollectorRegistry registry) {
    if (sessions.isEmpty()) {
      throw new IllegalStateException("You must register at least one Session.");
    }
    return super.register(registry);
  }

  private List<MetricFamilySamples> getCounters() {
    return Arrays.<MetricFamilySamples>asList(
        createCounter(
            "eclipselink_unit_of_work_commits_total",
            "Total number of unit of work commits",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.UowCommits);
                if (value != null) {
                  return ((Long) value).doubleValue();
                }
                return 0L;
              }
            }
        ),
        createCounter(
            "eclipselink_unit_of_work_created_total",
            "Total number of created units of work",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.UowCreated);
                if (value != null) {
                  return ((Long) value).doubleValue();
                }
                return 0L;
              }
            }
        ),
        createCounter(
            "eclipselink_unit_of_work_released_total",
            "Total number of released units of work",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.UowReleased);
                if (value != null) {
                  return ((Long) value).doubleValue();
                }
                return 0L;
              }
            }
        ),
        createCounter(
            "eclipselink_unit_of_work_rollbacks_total",
            "Total number of unit of work rollbacks",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.UowRollbacks);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_cache_hits_total",
            "Total number of cache hits",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.CacheHits);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_cache_misses_total",
            "Total number of cache misses",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.CacheMisses);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_cache_requests_total",
            "Total number of cache requests",
            new ValueProvider() {
                @Override
                public double getValue(Map<String, Object> metrics) {
                    Object hits = metrics.get(SessionProfiler.CacheHits);
                    Object misses = metrics.get(SessionProfiler.CacheMisses);
                    if (hits != null && misses != null) {
                        return ((Integer) hits).doubleValue() + ((Integer) misses).doubleValue();
                    }
                    return 0;
                }
            }
        ),
        createCounter(
            "eclipselink_change_sets_processed_total",
            "Total number of processed change sets",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ChangeSetsProcessed);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_change_sets_not_processed_total",
            "Total number of non-processed change sets",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ChangeSetsNotProcessed);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_change_sets_total",
            "Total number of change sets",
            new ValueProvider() {
                @Override
                public double getValue(Map<String, Object> metrics) {
                    Object processed = metrics.get(SessionProfiler.ChangeSetsProcessed);
                    Object nonProcessed = metrics.get(SessionProfiler.ChangeSetsNotProcessed);
                    if (processed != null && nonProcessed != null) {
                        return ((Integer) processed).doubleValue() + ((Integer) nonProcessed).doubleValue();
                    }
                    return 0;
                }
            }
        ),
        createCounter(
            "eclipselink_remote_change_sets_total",
            "Total number of remote change sets",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.RemoteChangeSet);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_client_sessions_created_total",
            "Total number of client sessions",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ClientSessionCreated);
                if (value != null) {
                  return ((Long) value).doubleValue();
                }
                return 0L;
              }
            }
        ),
        createCounter(
            "eclipselink_client_sessions_released_total",
            "Total number of released client sessions",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ClientSessionReleased);
                if (value != null) {
                  return ((Long) value).doubleValue();
                }
                return 0L;
              }
            }
        ),
        createCounter(
            "eclipselink_connects_total",
            "Total number of connects",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Connects);
                if (value != null) {
                  return ((Long) value).doubleValue();
                }
                return 0L;
              }
            }
        )
        ,
        createCounter(
            "eclipselink_disconnects_total",
            "Total number of disconnects",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Disconnects);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }

        ),
        createCounter(
            "eclipselink_optimistic_lock_exceptions_total",
            "Total number of optimistic lock exceptions",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.OptimisticLockException);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_remote_command_manager_received_total",
            "Total number of messages received",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.RcmReceived);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        ),
        createCounter(
            "eclipselink_remote_command_manager_sent_total",
            "Total number of sent messages",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.RcmSent);
                if (value != null) {
                  return ((Integer) value).doubleValue();
                }
                return 0;
              }
            }
        )
    );
  }

  private List<MetricFamilySamples> getTimers() {
    return Arrays.<MetricFamilySamples>asList(
        createGauge(
            "eclipselink_unit_of_work_commits_duration_seconds",
            "Total duration of unit of work commits",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.UowCommit);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_remote_duration_seconds",
            "Total duration of remote operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Remote);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_assign_sequence_duration_seconds",
            "Total duration of assign sequence operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.AssignSequence);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_cache_coordination_duration_seconds",
            "Total duration of cache coordination",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.CacheCoordination);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_cache_coordination_serialize_duration_seconds",
            "Total duration of cache coordination serialization",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.CacheCoordinationSerialize);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }

        ),
        createGauge(
            "eclipselink_caching_duration_seconds",
            "Total duration of caching",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Caching);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }

        ),
        createGauge(
            "eclipselink_connection_management_duration_seconds",
            "Total duration of connection management operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ConnectionManagement);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_connection_ping_duration_seconds",
            "Total duration of connection pings",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ConnectionPing);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }

        ),
        createGauge(
            "eclipselink_descriptor_events_duration_seconds",
            "Total duration of descriptor events",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.DescriptorEvent);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }

        ),
        createGauge(
            "eclipselink_distributed_merge_duration_seconds",
            "Total duration of distributed merges",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.DistributedMerge);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }

        ),
        createGauge(
            "eclipselink_jts_after_completion_duration_seconds",
            "Total duration of JTS after completion",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.JtsAfterCompletion);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_jts_before_completion_duration_seconds",
            "Total duration of JTS before completion",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.JtsBeforeCompletion);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }

        ),
        createGauge(
            "eclipselink_logging_duration_seconds",
            "Total duration of logging",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Logging);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_merge_duration_seconds",
            "Total duration of merges",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Merge);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_object_building_duration_seconds",
            "Total duration of object building",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.ObjectBuilding);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_query_preparation_duration_seconds",
            "Total duration of query preparation",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.QueryPreparation);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_register_duration_seconds",
            "Total duration of registrations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Register);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_remote_lazy_duration_seconds",
            "Total duration of remote lazy operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.RemoteLazy);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_remote_metadata_duration_seconds",
            "Total duration of remote metadata operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.RemoteMetadata);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_row_fetch_duration_seconds",
            "Total duration of row fetch operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.RowFetch);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_session_event_duration_seconds",
            "Total duration of session events",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.SessionEvent);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        ),
        createGauge(
            "eclipselink_sql_generation_duration_seconds",
            "Total duration of SQL generation operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.SqlGeneration);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_sql_prepare_duration_seconds",
            "Total duration of SQL preparation operations",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.SqlPrepare);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_statement_execute_duration_seconds",
            "Total duration of statement executions",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.StatementExecute);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Long) value).doubleValue() / 1000000000;
                }
                return 0L;
              }
            }
        ),
        createGauge(
            "eclipselink_transaction_duration_seconds",
            "Total duration of transactions",
            new ValueProvider() {
              @Override
              public double getValue(Map<String, Object> metrics) {
                Object value = metrics.get(SessionProfiler.Transaction);
                if (value != null) {
                  // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                  return ((Integer) value).doubleValue() / 1000000000;
                }
                return 0;
              }
            }
        )
    );
  }

  private CounterMetricFamily createCounter(String metric, String help, ValueProvider provider) {

    CounterMetricFamily metricFamily = new CounterMetricFamily(metric, help, LABEL_NAMES);

    for (Entry<String, Session> entry : sessions.entrySet()) {
      Map<String, Object> metrics = ((PerformanceMonitor) entry.getValue().getProfiler())
          .getOperationTimings();
      metricFamily.addMetric(
          Collections.singletonList(entry.getKey()),
          provider.getValue(metrics)
      );
    }
    return metricFamily;
  }

  private GaugeMetricFamily createGauge(String metric, String help, ValueProvider provider) {

    GaugeMetricFamily metricFamily = new GaugeMetricFamily(metric, help, LABEL_NAMES);

    for (Entry<String, Session> entry : sessions.entrySet()) {
      Map<String, Object> metrics = ((PerformanceMonitor) entry.getValue().getProfiler())
          .getOperationTimings();
      metricFamily.addMetric(
          Collections.singletonList(entry.getKey()),
          provider.getValue(metrics)
      );
    }
    return metricFamily;
  }

  private interface ValueProvider {

    double getValue(Map<String, Object> metrics);
  }


}
