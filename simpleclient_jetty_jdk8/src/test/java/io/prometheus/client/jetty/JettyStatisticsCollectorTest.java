package io.prometheus.client.jetty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import io.prometheus.client.CollectorRegistry;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Test;

public class JettyStatisticsCollectorTest {

  private final Server server = new Server(0);

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void collect() throws Exception {
    HandlerCollection handlers = new HandlerCollection();

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");

    StatisticsHandler statisticsHandler = new StatisticsHandler();
    statisticsHandler.setServer(server);
    statisticsHandler.setHandler(context);

    handlers.addHandler(statisticsHandler);

    // register collector
    new JettyStatisticsCollector(statisticsHandler).register();

    server.setHandler(handlers);

    server.start();

    ServerConnector connector = (ServerConnector) server.getConnectors()[0];
    int port = connector.getLocalPort();

    // send GET request
    try {
      HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://127.0.0.1:" + port)
          .openConnection();
      urlConnection.getInputStream().close();
      urlConnection.disconnect();
    } catch (FileNotFoundException ignored) {
    }

    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_requests_total"), is(1.0));
    assertThat(CollectorRegistry.defaultRegistry.getSampleValue("jetty_requests_active"), is(0.0));
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
