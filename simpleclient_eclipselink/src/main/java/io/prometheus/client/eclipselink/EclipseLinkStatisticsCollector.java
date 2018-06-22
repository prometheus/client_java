package io.prometheus.client.eclipselink;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.tools.profiler.PerformanceMonitor;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
 * </p>
 */
public class EclipseLinkStatisticsCollector extends Collector {

    private static final List<String> LABEL_NAMES = Arrays.asList("session_name");
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

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
     * @param name    A unique name for this Session
     */
    public EclipseLinkStatisticsCollector(Session session, String name) {
        add(session, name);
    }

    /**
     * Registers an EclipseLink Session with this collector.
     *
     * @param session The EclipseLink Session to collect metrics for
     * @param name    A unique name for this Session
     * @return Returns the collector
     */
    public EclipseLinkStatisticsCollector add(Session session, String name) {
        sessions.put(name, session);
        return this;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> metrics = new ArrayList<>();
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
        return Arrays.asList(
                createCounter(
                        "eclipselink_unit_of_work_commits_total",
                        "Total number of unit of work commits",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.UowCommits, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_unit_of_work_created_total",
                        "Total number of created units of work",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.UowCreated, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_unit_of_work_released_total",
                        "Total number of released units of work",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.UowReleased, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_unit_of_work_rollbacks_total",
                        "Total number of unit of work rollbacks",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.UowRollbacks, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_cache_hits_total",
                        "Total number of cache hits",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.CacheHits, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_cache_misses_total",
                        "Total number of cache misses",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.CacheMisses, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_cache_requests_total",
                        "Total number of cache requests",
                        metrics -> {
                            Object hits = metrics.getOrDefault(SessionProfiler.CacheHits, 0);
                            Object misses = metrics.getOrDefault(SessionProfiler.CacheMisses, 0);
                            double hitsValue;
                            double missesValue;
                            if (hits instanceof Long) {
                                hitsValue = ((Long) hits).doubleValue();
                            } else {
                                hitsValue = ((Integer) hits).doubleValue();
                            }
                            if (misses instanceof Long) {
                                missesValue = ((Long) misses).doubleValue();
                            } else {
                                missesValue = ((Integer) misses).doubleValue();
                            }
                            return hitsValue + missesValue;
                        }
                ),
                createCounter(
                        "eclipselink_change_sets_processed_total",
                        "Total number of processed change sets",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ChangeSetsProcessed, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_change_sets_not_processed_total",
                        "Total number of non-processed change sets",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ChangeSetsNotProcessed, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_change_sets_total",
                        "Total number of change sets",
                        metrics -> {
                            Object processed = metrics.getOrDefault(SessionProfiler.ChangeSetsProcessed, 0);
                            Object notProcessed = metrics.getOrDefault(SessionProfiler.ChangeSetsNotProcessed, 0);
                            double processedValue;
                            double notProcessedValue;
                            if (processed instanceof Long) {
                                processedValue = ((Long) processed).doubleValue();
                            } else {
                                processedValue = ((Integer) processed).doubleValue();
                            }
                            if (notProcessed instanceof Long) {
                                notProcessedValue = ((Long) notProcessed).doubleValue();
                            } else {
                                notProcessedValue = ((Integer) notProcessed).doubleValue();
                            }
                            return processedValue + notProcessedValue;
                        }
                ),
                createCounter(
                        "eclipselink_remote_change_sets_total",
                        "Total number of remote change sets",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.RemoteChangeSet, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_client_sessions_created_total",
                        "Total number of client sessions",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ClientSessionCreated, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_client_sessions_released_total",
                        "Total number of released client sessions",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ClientSessionReleased, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_connects_total",
                        "Total number of connects",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Connects, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_disconnects_total",
                        "Total number of disconnects",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Disconnects, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_optimistic_lock_exceptions_total",
                        "Total number of optimistic lock exceptions",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.OptimisticLockException, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_remote_command_manager_received_total",
                        "Total number of messages received",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.RcmReceived, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                ),
                createCounter(
                        "eclipselink_remote_command_manager_sent_total",
                        "Total number of sent messages",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.RcmSent, 0);
                            if (value instanceof Long) {
                                return ((Long) value).doubleValue();
                            }
                            return ((Integer) value).doubleValue();
                        }
                )
        );
    }

    private List<MetricFamilySamples> getTimers() {
        return Arrays.asList(
                createGauge(
                        "eclipselink_unit_of_work_commits_duration_seconds",
                        "Total duration of unit of work commits",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.UowCommit, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_remote_duration_seconds",
                        "Total duration of remote operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Remote, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_assign_sequence_duration_seconds",
                        "Total duration of assign sequence operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.AssignSequence, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_cache_coordination_duration_seconds",
                        "Total duration of cache coordination",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.CacheCoordination, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_cache_coordination_serialize_duration_seconds",
                        "Total duration of cache coordination serialization",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.CacheCoordinationSerialize, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_caching_duration_seconds",
                        "Total duration of caching",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Caching, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_connection_management_duration_seconds",
                        "Total duration of connection management operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ConnectionManagement, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_connection_ping_duration_seconds",
                        "Total duration of connection pings",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ConnectionPing, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_descriptor_events_duration_seconds",
                        "Total duration of descriptor events",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.DescriptorEvent, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_distributed_merge_duration_seconds",
                        "Total duration of distributed merges",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.DistributedMerge, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_jts_after_completion_duration_seconds",
                        "Total duration of JTS after completion",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.JtsAfterCompletion, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_jts_before_completion_duration_seconds",
                        "Total duration of JTS before completion",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.JtsBeforeCompletion, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_logging_duration_seconds",
                        "Total duration of logging",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Logging, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_merge_duration_seconds",
                        "Total duration of merges",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Merge, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_object_building_duration_seconds",
                        "Total duration of object building",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.ObjectBuilding, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_query_preparation_duration_seconds",
                        "Total duration of query preparation",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.QueryPreparation, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_register_duration_seconds",
                        "Total duration of registrations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Register, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_remote_lazy_duration_seconds",
                        "Total duration of remote lazy operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.RemoteLazy, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_remote_metadata_duration_seconds",
                        "Total duration of remote metadata operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.RemoteMetadata, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_row_fetch_duration_seconds",
                        "Total duration of row fetch operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.RowFetch, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_session_event_duration_seconds",
                        "Total duration of session events",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.SessionEvent, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_sql_generation_duration_seconds",
                        "Total duration of SQL generation operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.SqlGeneration, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_sql_prepare_duration_seconds",
                        "Total duration of SQL preparation operations",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.SqlPrepare, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_statement_execute_duration_seconds",
                        "Total duration of statement executions",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.StatementExecute, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
                        }
                ),
                createGauge(
                        "eclipselink_transaction_duration_seconds",
                        "Total duration of transactions",
                        metrics -> {
                            Object value = metrics.getOrDefault(SessionProfiler.Transaction, 0);
                            if (value instanceof Long) {
                                // divide by 1 000 000 000 because EclipseLink returns statistics in nanoseconds
                                return ((Long) value).doubleValue() / 1000000000;
                            }
                            return ((Integer) value).doubleValue() / 1000000000;
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
