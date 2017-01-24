package io.prometheus.client.hibernate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.prometheus.client.CollectorRegistry;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;

public class HibernateStatisticsCollectorTest {

  private Statistics statistics;
  private CollectorRegistry registry;

  @Before
  public void before() {
    registry = new CollectorRegistry();
    statistics = mock(Statistics.class);
  }

  @Test
  public void shouldPublishSessionMetrics() {

    when(statistics.getSessionOpenCount()).thenReturn(1L);
    when(statistics.getSessionCloseCount()).thenReturn(2L);
    when(statistics.getFlushCount()).thenReturn(3L);
    when(statistics.getConnectCount()).thenReturn(4L);
    when(statistics.getOptimisticFailureCount()).thenReturn(5L);

    new HibernateStatisticsCollector(statistics).register(registry);

    assertThat(registry.getSampleValue("hibernate_session_open_total"), is(1.0));
    assertThat(registry.getSampleValue("hibernate_session_close_total"), is(2.0));
    assertThat(registry.getSampleValue("hibernate_flush_total"), is(3.0));
    assertThat(registry.getSampleValue("hibernate_connect_total"), is(4.0));
    assertThat(registry.getSampleValue("hibernate_optimistic_failure_total"), is(5.0));

  }

  @Test
  public void shouldPublishConnectionMetrics() {

    when(statistics.getPrepareStatementCount()).thenReturn(1L);
    when(statistics.getCloseStatementCount()).thenReturn(2L);
    when(statistics.getTransactionCount()).thenReturn(3L);
    when(statistics.getSuccessfulTransactionCount()).thenReturn(4L);

    new HibernateStatisticsCollector(statistics).register(registry);

    assertThat(registry.getSampleValue("hibernate_statement_prepare_total"), is(1.0));
    assertThat(registry.getSampleValue("hibernate_statement_close_total"), is(2.0));
    assertThat(registry.getSampleValue("hibernate_transaction_total"), is(3.0));
    assertThat(registry.getSampleValue("hibernate_transaction_success_total"), is(4.0));

  }

  @Test
  public void shouldPublishCacheMetrics() {

    when(statistics.getSecondLevelCacheHitCount()).thenReturn(1L);
    when(statistics.getSecondLevelCacheMissCount()).thenReturn(2L);
    when(statistics.getSecondLevelCachePutCount()).thenReturn(3L);

    when(statistics.getQueryCacheHitCount()).thenReturn(4L);
    when(statistics.getQueryCacheMissCount()).thenReturn(5L);
    when(statistics.getQueryCachePutCount()).thenReturn(6L);

    when(statistics.getNaturalIdCacheHitCount()).thenReturn(7L);
    when(statistics.getNaturalIdCacheMissCount()).thenReturn(8L);
    when(statistics.getNaturalIdCachePutCount()).thenReturn(9L);

    when(statistics.getUpdateTimestampsCacheHitCount()).thenReturn(10L);
    when(statistics.getUpdateTimestampsCacheMissCount()).thenReturn(11L);
    when(statistics.getUpdateTimestampsCachePutCount()).thenReturn(12L);

    new HibernateStatisticsCollector(statistics).register(registry);

    assertThat(registry.getSampleValue("hibernate_second_level_cache_hit_total"), is(1.0));
    assertThat(registry.getSampleValue("hibernate_second_level_cache_miss_total"), is(2.0));
    assertThat(registry.getSampleValue("hibernate_second_level_cache_put_total"), is(3.0));

    assertThat(registry.getSampleValue("hibernate_query_cache_hit_total"), is(4.0));
    assertThat(registry.getSampleValue("hibernate_query_cache_miss_total"), is(5.0));
    assertThat(registry.getSampleValue("hibernate_query_cache_put_total"), is(6.0));

    assertThat(registry.getSampleValue("hibernate_natural_id_cache_hit_total"), is(7.0));
    assertThat(registry.getSampleValue("hibernate_natural_id_cache_miss_total"), is(8.0));
    assertThat(registry.getSampleValue("hibernate_natural_id_cache_put_total"), is(9.0));

    assertThat(registry.getSampleValue("hibernate_update_timestamps_cache_hit_total"), is(10.0));
    assertThat(registry.getSampleValue("hibernate_update_timestamps_cache_miss_total"), is(11.0));
    assertThat(registry.getSampleValue("hibernate_update_timestamps_cache_put_total"), is(12.0));

  }

  @Test
  public void shouldPublishEntityMetrics() {

    when(statistics.getEntityDeleteCount()).thenReturn(1L);
    when(statistics.getEntityInsertCount()).thenReturn(2L);
    when(statistics.getEntityLoadCount()).thenReturn(3L);
    when(statistics.getEntityFetchCount()).thenReturn(4L);
    when(statistics.getEntityUpdateCount()).thenReturn(5L);

    when(statistics.getCollectionLoadCount()).thenReturn(6L);
    when(statistics.getCollectionFetchCount()).thenReturn(7L);
    when(statistics.getCollectionUpdateCount()).thenReturn(8L);
    when(statistics.getCollectionRemoveCount()).thenReturn(9L);
    when(statistics.getCollectionRecreateCount()).thenReturn(10L);

    new HibernateStatisticsCollector(statistics).register(registry);

    assertThat(registry.getSampleValue("hibernate_entity_delete_total"), is(1.0));
    assertThat(registry.getSampleValue("hibernate_entity_insert_total"), is(2.0));
    assertThat(registry.getSampleValue("hibernate_entity_load_total"), is(3.0));
    assertThat(registry.getSampleValue("hibernate_entity_fetch_total"), is(4.0));
    assertThat(registry.getSampleValue("hibernate_entity_update_total"), is(5.0));

    assertThat(registry.getSampleValue("hibernate_collection_load_total"), is(6.0));
    assertThat(registry.getSampleValue("hibernate_collection_fetch_total"), is(7.0));
    assertThat(registry.getSampleValue("hibernate_collection_update_total"), is(8.0));
    assertThat(registry.getSampleValue("hibernate_collection_remove_total"), is(9.0));
    assertThat(registry.getSampleValue("hibernate_collection_recreate_total"), is(10.0));

  }

  @Test
  public void shouldPublishQueryExecutionMetrics() {

    when(statistics.getQueryExecutionCount()).thenReturn(1L);
    when(statistics.getQueryExecutionMaxTime()).thenReturn(2000L);
    when(statistics.getNaturalIdQueryExecutionCount()).thenReturn(3L);
    when(statistics.getNaturalIdQueryExecutionMaxTime()).thenReturn(4000L);

    new HibernateStatisticsCollector(statistics).register(registry);

    assertThat(registry.getSampleValue("hibernate_query_execution_total"), is(1.0));
    assertThat(registry.getSampleValue("hibernate_query_execution_seconds_max"), is(2.0));
    assertThat(registry.getSampleValue("hibernate_natural_id_query_execution_total"), is(3.0));
    assertThat(registry.getSampleValue("hibernate_natural_id_query_execution_seconds_max"), is(4.0));

  }

}