package io.prometheus.client.jetty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import io.prometheus.client.CollectorRegistry;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JettyStatisticsCollectorTest {

  private final Server server = new Server();
  private final ServerConnector connector = new ServerConnector(server);

  @Before
  public void setUp() throws Exception {
    server.addConnector(connector);
    HandlerCollection handlers = new HandlerCollection();

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    handlers.setHandlers(new Handler[]{context});

    StatisticsHandler stats = new StatisticsHandler();
    stats.setHandler(handlers);
    server.setHandler(stats);

    // register collector
    new JettyStatisticsCollector(stats).register();

    server.setHandler(stats);
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void collect() throws Exception {
    // send GET request
    try {
      final String spec = "http://127.0.0.1:" + connector.getLocalPort();
      final HttpURLConnection urlConnection = (HttpURLConnection) new URL(spec).openConnection();
      urlConnection.getInputStream().close();
      urlConnection.disconnect();
    } catch (FileNotFoundException ignored) {
    }

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_requests_total"), is(1.0));
    Double jettyRequestsActive = null;
    for (int i=0; i<10; i++) {
      jettyRequestsActive = CollectorRegistry.defaultRegistry.getSampleValue("jetty_requests_active");
      if (Double.valueOf(0.0).equals(jettyRequestsActive)) {
        break;
      }
      Thread.sleep(50);
    }
    assertThat(jettyRequestsActive, is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_requests_active_max"),
        is(1.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_request_time_max_seconds"),
        is(notNullValue()));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_request_time_seconds_total"),
        is(notNullValue()));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_dispatched_total"), is(1.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_dispatched_active"),
        is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_dispatched_active_max"),
        is(greaterThan(0.0)));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_dispatched_time_max"),
        is(notNullValue()));
    assertThat(
        CollectorRegistry.defaultRegistry.getSampleValue("jetty_dispatched_time_seconds_total"),
        is(notNullValue()));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_async_requests_total"),
        is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_async_requests_waiting"),
        is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_async_requests_waiting_max"),
        is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_async_dispatches_total"),
        is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_expires_total"), is(0.0));

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_responses_total",
        new String[]{"code"}, new String[]{"1xx"}), is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_responses_total",
        new String[]{"code"}, new String[]{"2xx"}), is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_responses_total",
        new String[]{"code"}, new String[]{"3xx"}), is(0.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_responses_total",
        new String[]{"code"}, new String[]{"4xx"}), is(1.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_responses_total",
        new String[]{"code"}, new String[]{"5xx"}), is(0.0));

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_stats_seconds"),
        is(notNullValue()));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_responses_bytes_total"),
        is(notNullValue()));
  }
}
