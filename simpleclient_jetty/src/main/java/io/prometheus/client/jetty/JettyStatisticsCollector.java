package io.prometheus.client.jetty;

import io.prometheus.client.Collector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.jetty.server.handler.StatisticsHandler;

/**
 * Collect metrics from jetty's org.eclipse.jetty.server.handler.StatisticsHandler.
 * <pre>
 * Server server = new Server(8080);
 *
 * ServletContextHandler context = new ServletContextHandler();
 * context.setContextPath("/");
 * server.setHandler(context);
 *
 * HandlerCollection handlers = new HandlerCollection();
 *
 * StatisticsHandler statisticsHandler = new StatisticsHandler();
 * statisticsHandler.setServer(server);
 * handlers.addHandler(statisticsHandler);
 *
 * // Register collector.
 * new JettyStatisticsCollector(statisticsHandler).register();
 *
 * server.setHandler(handlers);
 *
 * server.start();
 * </pre>
 */
public class JettyStatisticsCollector extends Collector {
  private final StatisticsHandler statisticsHandler;
  private static final List<String> EMPTY_LIST = new ArrayList<String>();

  public JettyStatisticsCollector(StatisticsHandler statisticsHandler) {
    this.statisticsHandler = statisticsHandler;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return Arrays.asList(
            buildCounter("jetty_requests_total", "Number of requests", statisticsHandler.getRequests()),
            buildGauge("jetty_requests_active", "Number of requests currently active", statisticsHandler.getRequestsActive()),
            buildGauge("jetty_requests_active_max", "Maximum number of requests that have been active at once", statisticsHandler.getRequestsActiveMax()),
            buildGauge("jetty_request_time_max_seconds", "Maximum time spent handling requests", statisticsHandler.getRequestTimeMax() / 1000.0),
            buildCounter("jetty_request_time_seconds_total", "Total time spent in all request handling", statisticsHandler.getRequestTimeTotal() / 1000.0),
            buildCounter("jetty_dispatched_total", "Number of dispatches", statisticsHandler.getDispatched()),
            buildGauge("jetty_dispatched_active", "Number of dispatches currently active", statisticsHandler.getDispatchedActive()),
            buildGauge("jetty_dispatched_active_max", "Maximum number of active dispatches being handled", statisticsHandler.getDispatchedActiveMax()),
            buildGauge("jetty_dispatched_time_max", "Maximum time spent in dispatch handling", statisticsHandler.getDispatchedTimeMax()),
            buildCounter("jetty_dispatched_time_seconds_total", "Total time spent in dispatch handling", statisticsHandler.getDispatchedTimeTotal() / 1000.0),
            buildCounter("jetty_async_requests_total", "Total number of async requests", statisticsHandler.getAsyncRequests()),
            buildGauge("jetty_async_requests_waiting", "Currently waiting async requests", statisticsHandler.getAsyncRequestsWaiting()),
            buildGauge("jetty_async_requests_waiting_max", "Maximum number of waiting async requests", statisticsHandler.getAsyncRequestsWaitingMax()),
            buildCounter("jetty_async_dispatches_total", "Number of requested that have been asynchronously dispatched", statisticsHandler.getAsyncDispatches()),
            buildCounter("jetty_expires_total", "Number of async requests requests that have expired", statisticsHandler.getExpires()),
            buildStatusCounter(),
            buildGauge("jetty_stats_seconds", "Time in seconds stats have been collected for", statisticsHandler.getStatsOnMs() / 1000.0),
            buildCounter("jetty_responses_bytes_total", "Total number of bytes across all responses", statisticsHandler.getResponsesBytesTotal())
    );
  }

  private static MetricFamilySamples buildGauge(String name, String help, double value) {
    return new MetricFamilySamples(
            name,
            Type.GAUGE,
            help,
            Collections.singletonList(new MetricFamilySamples.Sample(name, EMPTY_LIST, EMPTY_LIST, value)));
  }

  private static MetricFamilySamples buildCounter(String name, String help, double value) {
    return new MetricFamilySamples(
        name,
        Type.COUNTER,
        help,
        Collections.singletonList(new MetricFamilySamples.Sample(name, EMPTY_LIST, EMPTY_LIST, value)));
  }

  private MetricFamilySamples buildStatusCounter() {
    String name = "jetty_responses_total";
    return new MetricFamilySamples(
            name,
            Type.COUNTER,
            "Number of requests with response status",
            Arrays.asList(
                    buildStatusSample(name, "1xx", statisticsHandler.getResponses1xx()),
                    buildStatusSample(name, "2xx", statisticsHandler.getResponses2xx()),
                    buildStatusSample(name, "3xx", statisticsHandler.getResponses3xx()),
                    buildStatusSample(name, "4xx", statisticsHandler.getResponses4xx()),
                    buildStatusSample(name, "5xx", statisticsHandler.getResponses5xx())
            )
    );
  }

  private static MetricFamilySamples.Sample buildStatusSample(String name, String status, double value) {
    return new MetricFamilySamples.Sample(
            name,
            Collections.singletonList("code"),
            Collections.singletonList(status),
            value);
  }
}
