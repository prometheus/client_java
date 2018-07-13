package io.prometheus.client.hibernate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.prometheus.client.CollectorRegistry;
import org.hibernate.SessionFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HibernateStatisticsCollectorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private SessionFactory sessionFactory;
  private Statistics statistics;
  private QueryStatistics queryStatistics;
  private CollectorRegistry registry;

  @Before
  public void before() {
    registry = new CollectorRegistry();
    sessionFactory = mock(SessionFactory.class);
    statistics = mock(Statistics.class);
    queryStatistics = mock(QueryStatistics.class);
    when(sessionFactory.getStatistics()).thenReturn(statistics);
  }

  @Test
  public void shouldPublishSessionMetrics() {

    when(statistics.getSessionOpenCount()).thenReturn(1L);
    when(statistics.getSessionCloseCount()).thenReturn(2L);
    when(statistics.getFlushCount()).thenReturn(3L);
    when(statistics.getConnectCount()).thenReturn(4L);
    when(statistics.getOptimisticFailureCount()).thenReturn(5L);

    new HibernateStatisticsCollector()
        .add(sessionFactory, "factory1")
        .register(registry);

    assertThat(getSample("hibernate_session_opened_total", "factory1"), is(1.0));
    assertThat(getSample("hibernate_session_closed_total", "factory1"), is(2.0));
    assertThat(getSample("hibernate_flushed_total", "factory1"), is(3.0));
    assertThat(getSample("hibernate_connect_total", "factory1"), is(4.0));
    assertThat(getSample("hibernate_optimistic_failure_total", "factory1"), is(5.0));

  }

  @Test
  public void shouldPublishConnectionMetrics() {

    when(statistics.getPrepareStatementCount()).thenReturn(1L);
    when(statistics.getCloseStatementCount()).thenReturn(2L);
    when(statistics.getTransactionCount()).thenReturn(3L);
    when(statistics.getSuccessfulTransactionCount()).thenReturn(4L);

    new HibernateStatisticsCollector()
        .add(sessionFactory, "factory2")
        .register(registry);

    assertThat(getSample("hibernate_statement_prepared_total", "factory2"), is(1.0));
    assertThat(getSample("hibernate_statement_closed_total", "factory2"), is(2.0));
    assertThat(getSample("hibernate_transaction_total", "factory2"), is(3.0));
    assertThat(getSample("hibernate_transaction_success_total", "factory2"), is(4.0));

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

    new HibernateStatisticsCollector()
        .add(sessionFactory, "factory3")
        .register(registry);

    assertThat(getSample("hibernate_second_level_cache_hit_total", "factory3"), is(1.0));
    assertThat(getSample("hibernate_second_level_cache_miss_total", "factory3"), is(2.0));
    assertThat(getSample("hibernate_second_level_cache_put_total", "factory3"), is(3.0));

    assertThat(getSample("hibernate_query_cache_hit_total", "factory3"), is(4.0));
    assertThat(getSample("hibernate_query_cache_miss_total", "factory3"), is(5.0));
    assertThat(getSample("hibernate_query_cache_put_total", "factory3"), is(6.0));

    assertThat(getSample("hibernate_natural_id_cache_hit_total", "factory3"), is(7.0));
    assertThat(getSample("hibernate_natural_id_cache_miss_total", "factory3"), is(8.0));
    assertThat(getSample("hibernate_natural_id_cache_put_total", "factory3"), is(9.0));

    assertThat(getSample("hibernate_update_timestamps_cache_hit_total", "factory3"), is(10.0));
    assertThat(getSample("hibernate_update_timestamps_cache_miss_total", "factory3"), is(11.0));
    assertThat(getSample("hibernate_update_timestamps_cache_put_total", "factory3"), is(12.0));

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

    new HibernateStatisticsCollector()
        .add(sessionFactory, "factory4")
        .register(registry);

    assertThat(getSample("hibernate_entity_delete_total", "factory4"), is(1.0));
    assertThat(getSample("hibernate_entity_insert_total", "factory4"), is(2.0));
    assertThat(getSample("hibernate_entity_load_total", "factory4"), is(3.0));
    assertThat(getSample("hibernate_entity_fetch_total", "factory4"), is(4.0));
    assertThat(getSample("hibernate_entity_update_total", "factory4"), is(5.0));

    assertThat(getSample("hibernate_collection_load_total", "factory4"), is(6.0));
    assertThat(getSample("hibernate_collection_fetch_total", "factory4"), is(7.0));
    assertThat(getSample("hibernate_collection_update_total", "factory4"), is(8.0));
    assertThat(getSample("hibernate_collection_remove_total", "factory4"), is(9.0));
    assertThat(getSample("hibernate_collection_recreate_total", "factory4"), is(10.0));

  }

  @Test
  public void shouldPublishQueryExecutionMetrics() {

    when(statistics.getQueryExecutionCount()).thenReturn(1L);
    when(statistics.getNaturalIdQueryExecutionCount()).thenReturn(3L);

    new HibernateStatisticsCollector()
        .add(sessionFactory, "factory5")
        .register(registry);

    assertThat(getSample("hibernate_query_execution_total", "factory5"), is(1.0));
    assertThat(getSample("hibernate_natural_id_query_execution_total", "factory5"), is(3.0));

  }

  @Test
  public void shouldPublishPerQueryMetricsWhenEnabled() {
    String query = "query";
    mockQueryStatistics(query);

    new HibernateStatisticsCollector()
            .add(sessionFactory, "factory6")
            .enablePerQueryMetrics()
            .register(registry);

    assertThat(getSampleForQuery("hibernate_per_query_cache_hit_total", "factory6", query), is(1.0));
    assertThat(getSampleForQuery("hibernate_per_query_cache_miss_total", "factory6", query), is(2.0));
    assertThat(getSampleForQuery("hibernate_per_query_cache_put_total", "factory6", query), is(3.0));
    assertThat(getSampleForQuery("hibernate_per_query_execution_max_seconds", "factory6", query), is(0.555d));
    assertThat(getSampleForQuery("hibernate_per_query_execution_min_seconds", "factory6", query), is(0.123d));
    assertThat(getSampleForQuery("hibernate_per_query_execution_rows_total", "factory6", query), is(7.0));
    assertThat(getSampleForQuery("hibernate_per_query_execution_total", "factory6", query), is(8.0));
    assertThat(getSampleForQuery("hibernate_per_query_execution_seconds_total", "factory6", query), is(102.540d));

  }

  @Test
  public void shouldNotPublishPerQueryMetricsByDefault() {
    String query = "query";
    mockQueryStatistics(query);

    new HibernateStatisticsCollector()
            .add(sessionFactory, "factory7")
            .register(registry);

    assertThat(getSampleForQuery("hibernate_per_query_cache_hit_total", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_cache_miss_total", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_cache_put_total", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_execution_max_seconds", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_execution_min_seconds", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_execution_rows_total", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_execution_total", "factory7", query), nullValue());
    assertThat(getSampleForQuery("hibernate_per_query_execution_seconds", "factory7", query), nullValue());

  }

  private void mockQueryStatistics(String query) {
    when(statistics.getQueries()).thenReturn(new String[]{query});
    when(statistics.getQueryStatistics(eq(query))).thenReturn(queryStatistics);
    when(queryStatistics.getCacheHitCount()).thenReturn(1L);
    when(queryStatistics.getCacheMissCount()).thenReturn(2L);
    when(queryStatistics.getCachePutCount()).thenReturn(3L);
    when(queryStatistics.getExecutionMaxTime()).thenReturn(555L);
    when(queryStatistics.getExecutionMinTime()).thenReturn(123L);
    when(queryStatistics.getExecutionRowCount()).thenReturn(7L);
    when(queryStatistics.getExecutionCount()).thenReturn(8L);
    when(queryStatistics.getExecutionTotalTime()).thenReturn(102540L);
  }

  @Test
  public void shouldFailIfNoSessionFactoriesAreRegistered() {

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("SessionFactory");

    new HibernateStatisticsCollector().register(registry);

  }

  private Double getSample(String metric, String factory) {
    return registry.getSampleValue(
        metric, new String[]{"unit"}, new String[]{factory}
    );
  }

  private Double getSampleForQuery(String metric, String factory, String query) {
    return registry.getSampleValue(
            metric, new String[]{"unit", "query"}, new String[]{factory, query}
    );
  }

}