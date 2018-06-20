package io.prometheus.client.eclipselink;

import io.prometheus.client.CollectorRegistry;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.tools.profiler.PerformanceMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EclipseLinkStatisticsCollectorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Session session;
    private CollectorRegistry registry;
    private PerformanceMonitor monitor;
    private Map<String, Object> metrics = new ConcurrentHashMap<String, Object>();

    @Before
    public void before() {
        registry = new CollectorRegistry();
        session = mock(Session.class);
        monitor = mock(PerformanceMonitor.class);
        when(session.getProfiler()).thenReturn(monitor);

        metrics.put(SessionProfiler.UowCommits, 1L);
        metrics.put(SessionProfiler.UowCreated, 2L);
        metrics.put(SessionProfiler.UowReleased, 3L);
        metrics.put(SessionProfiler.UowRollbacks, 4);
        metrics.put(SessionProfiler.CacheHits, 5);
        metrics.put(SessionProfiler.CacheMisses, 6);
        metrics.put(SessionProfiler.ChangeSetsProcessed, 7);
        metrics.put(SessionProfiler.ChangeSetsNotProcessed, 8);
        metrics.put(SessionProfiler.RemoteChangeSet, 9);
        metrics.put(SessionProfiler.ClientSessionCreated, 10L);
        metrics.put(SessionProfiler.ClientSessionReleased, 11L);
        metrics.put(SessionProfiler.Connects, 12L);
        metrics.put(SessionProfiler.Disconnects, 13);
        metrics.put(SessionProfiler.OptimisticLockException, 14);
        metrics.put(SessionProfiler.RcmReceived, 15);
        metrics.put(SessionProfiler.RcmSent, 16);

        metrics.put(SessionProfiler.UowCommit, 100000000L);
        metrics.put(SessionProfiler.Remote, 110000000);
        metrics.put(SessionProfiler.AssignSequence, 120000000L);
        metrics.put(SessionProfiler.CacheCoordination, 130000000);
        metrics.put(SessionProfiler.CacheCoordinationSerialize, 140000000);
        metrics.put(SessionProfiler.Caching, 150000000L);
        metrics.put(SessionProfiler.ConnectionManagement, 160000000L);
        metrics.put(SessionProfiler.ConnectionPing, 170000000);
        metrics.put(SessionProfiler.DescriptorEvent, 180000000L);
        metrics.put(SessionProfiler.DistributedMerge, 190000000);
        metrics.put(SessionProfiler.JtsAfterCompletion, 200000000L);
        metrics.put(SessionProfiler.JtsBeforeCompletion, 210000000L);
        metrics.put(SessionProfiler.Logging, 220000000L);
        metrics.put(SessionProfiler.Merge, 230000000);
        metrics.put(SessionProfiler.ObjectBuilding, 240000000L);
        metrics.put(SessionProfiler.QueryPreparation, 250000000L);
        metrics.put(SessionProfiler.Register, 260000000);
        metrics.put(SessionProfiler.RemoteLazy, 270000000);
        metrics.put(SessionProfiler.RemoteMetadata, 280000000);
        metrics.put(SessionProfiler.RowFetch, 290000000L);
        metrics.put(SessionProfiler.SessionEvent, 300000000);
        metrics.put(SessionProfiler.SqlGeneration, 310000000L);
        metrics.put(SessionProfiler.SqlPrepare, 320000000L);
        metrics.put(SessionProfiler.StatementExecute, 330000000L);
        metrics.put(SessionProfiler.Transaction, 340000000);

        when(monitor.getOperationTimings()).thenReturn(metrics);
    }

    @Test
    public void shouldPublishCounters() {
        new EclipseLinkStatisticsCollector().add(session, "session1").register(registry);
        assertThat(getSample("eclipselink_unit_of_work_commits_total", "session1"), is(1.0));
        assertThat(getSample("eclipselink_unit_of_work_created_total", "session1"), is(2.0));
        assertThat(getSample("eclipselink_unit_of_work_released_total", "session1"), is(3.0));
        assertThat(getSample("eclipselink_unit_of_work_rollbacks_total", "session1"), is(4.0));
        assertThat(getSample("eclipselink_cache_hits_total", "session1"), is(5.0));
        assertThat(getSample("eclipselink_cache_misses_total", "session1"), is(6.0));
        assertThat(getSample("eclipselink_cache_requests_total", "session1"), is(11.0));
        assertThat(getSample("eclipselink_change_sets_processed_total", "session1"), is(7.0));
        assertThat(getSample("eclipselink_change_sets_not_processed_total", "session1"), is(8.0));
        assertThat(getSample("eclipselink_change_sets_total", "session1"), is(15.0));
        assertThat(getSample("eclipselink_remote_change_sets_total", "session1"), is(9.0));
        assertThat(getSample("eclipselink_client_sessions_created_total", "session1"), is(10.0));
        assertThat(getSample("eclipselink_client_sessions_released_total", "session1"), is(11.0));
        assertThat(getSample("eclipselink_connects_total", "session1"), is(12.0));
        assertThat(getSample("eclipselink_disconnects_total", "session1"), is(13.0));
        assertThat(getSample("eclipselink_optimistic_lock_exceptions_total", "session1"), is(14.0));
        assertThat(getSample("eclipselink_remote_command_manager_received_total", "session1"), is(15.0));
        assertThat(getSample("eclipselink_remote_command_manager_sent_total", "session1"), is(16.0));
    }

    @Test
    public void shouldPublishTimers() {
        new EclipseLinkStatisticsCollector().add(session, "session2").register(registry);
        assertThat(getSample("eclipselink_unit_of_work_commits_duration_seconds", "session2"), is(.1));
        assertThat(getSample("eclipselink_remote_duration_seconds", "session2"), is(.11));
        assertThat(getSample("eclipselink_assign_sequence_duration_seconds", "session2"), is(.12));
        assertThat(getSample("eclipselink_cache_coordination_duration_seconds", "session2"), is(.13));
        assertThat(getSample("eclipselink_cache_coordination_serialize_duration_seconds", "session2"), is(.14));
        assertThat(getSample("eclipselink_caching_duration_seconds", "session2"), is(.15));
        assertThat(getSample("eclipselink_connection_management_duration_seconds", "session2"), is(.16));
        assertThat(getSample("eclipselink_connection_ping_duration_seconds", "session2"), is(.17));
        assertThat(getSample("eclipselink_descriptor_events_duration_seconds", "session2"), is(.18));
        assertThat(getSample("eclipselink_distributed_merge_duration_seconds", "session2"), is(.19));
        assertThat(getSample("eclipselink_jts_after_completion_duration_seconds", "session2"), is(0.2));
        assertThat(getSample("eclipselink_jts_before_completion_duration_seconds", "session2"), is(.21));
        assertThat(getSample("eclipselink_logging_duration_seconds", "session2"), is(.22));
        assertThat(getSample("eclipselink_merge_duration_seconds", "session2"), is(.23));
        assertThat(getSample("eclipselink_object_building_duration_seconds", "session2"), is(.24));
        assertThat(getSample("eclipselink_query_preparation_duration_seconds", "session2"), is(.25));
        assertThat(getSample("eclipselink_register_duration_seconds", "session2"), is(.26));
        assertThat(getSample("eclipselink_remote_lazy_duration_seconds", "session2"), is(.27));
        assertThat(getSample("eclipselink_remote_metadata_duration_seconds", "session2"), is(.28));
        assertThat(getSample("eclipselink_row_fetch_duration_seconds", "session2"), is(.29));
        assertThat(getSample("eclipselink_session_event_duration_seconds", "session2"), is(.3));
        assertThat(getSample("eclipselink_sql_generation_duration_seconds", "session2"), is(.31));
        assertThat(getSample("eclipselink_sql_prepare_duration_seconds", "session2"), is(.32));
        assertThat(getSample("eclipselink_statement_execute_duration_seconds", "session2"), is(.33));
        assertThat(getSample("eclipselink_transaction_duration_seconds", "session2"), is(.34));

    }

    @Test
    public void shouldFailIfNoSessionsAreRegistered() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Session");
        new EclipseLinkStatisticsCollector().register(registry);
    }

    private Double getSample(String metric, String session) {
        return registry.getSampleValue(
                metric, new String[]{"session_name"}, new String[]{session}
        );
    }

}
