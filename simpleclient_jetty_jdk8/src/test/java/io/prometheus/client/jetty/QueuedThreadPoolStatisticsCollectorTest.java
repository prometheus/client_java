package io.prometheus.client.jetty;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.prometheus.client.CollectorRegistry;
import java.util.concurrent.BlockingQueue;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QueuedThreadPoolStatisticsCollectorTest {

  private static final String[] LABEL_NAMES = {"unit"};

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private Server server;
  private QueuedThreadPool queuedThreadPool;

  @Before
  public void setUp() {
    BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(8, 1024, 1024);
    queuedThreadPool = new QueuedThreadPool(200, 8, 60000, queue);
    server = new Server(queuedThreadPool);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void metricsGathered() throws Exception {
    String unit = "queue1";
    String[] labelValues = {unit};
    new QueuedThreadPoolStatisticsCollector(queuedThreadPool, unit).register();

    server.start();

    assertTrue(
        CollectorRegistry.defaultRegistry.getSampleValue("jetty_queued_thread_pool_threads",
            LABEL_NAMES, labelValues) > 0);
    assertTrue(
        CollectorRegistry.defaultRegistry.getSampleValue("jetty_queued_thread_pool_threads_idle",
            LABEL_NAMES, labelValues) > 0);
    assertTrue(
        CollectorRegistry.defaultRegistry.getSampleValue("jetty_queued_thread_pool_threads_max",
            LABEL_NAMES, labelValues) == 200);
    assertNotNull(CollectorRegistry.defaultRegistry.getSampleValue("jetty_queued_thread_pool_jobs",
        LABEL_NAMES, labelValues));
  }

  @Test
  public void shouldFailIfNoQueueThreadPoolsAreRegistered() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("QueuedThreadPool");

    new QueuedThreadPoolStatisticsCollector().register();
  }
}
