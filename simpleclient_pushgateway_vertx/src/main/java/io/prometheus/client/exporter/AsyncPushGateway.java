package io.prometheus.client.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Export metrics asynchronously via the Prometheus Pushgateway.
 * <p>
 * The Prometheus AsyncPushgateway exists to allow ephemeral and batch jobs to expose their metrics to Prometheus.
 * Since these kinds of jobs may not exist long enough to be scraped, they can instead push their metrics
 * to a Pushgateway. This class allows pushing the contents of a {@link CollectorRegistry} to
 * a Pushgateway.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   void executeBatchJob() throws Exception {
 *     CollectorRegistry registry = new CollectorRegistry();
 *     Gauge duration = Gauge.build()
 *         .name("my_batch_job_duration_seconds").help("Duration of my batch job in seconds.").register(registry);
 *     Gauge.Timer durationTimer = duration.startTimer();
 *     try {
 *       // Your code here.
 *
 *       // This is only added to the registry after success,
 *       // so that a previous success in the Pushgateway isn't overwritten on failure.
 *       Gauge lastSuccess = Gauge.build()
 *           .name("my_batch_job_last_success").help("Last time my batch job succeeded, in unixtime.").register(registry);
 *       lastSuccess.setToCurrentTime();
 *     } finally {
 *       durationTimer.setDuration();
 *       PushGateway pg = new PushGateway(vertx, "127.0.0.1:9091");
 *       pg.pushAdd(registry, "my_batch_job", res -> { ... });
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * See <a href="https://github.com/prometheus/pushgateway">https://github.com/prometheus/pushgateway</a>
 */
public class AsyncPushGateway {

  private static final int SECONDS_PER_MILLISECOND = 1000;

  /**
   * Wrap a Vert.x Buffer as a Writer so it can be used with
   * TextFormat writer
   */
  private static class BufferWriter extends Writer {

    private final Buffer buffer = Buffer.buffer();

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      buffer.appendString(new String(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {
      // NO-OP
    }

    @Override
    public void close() throws IOException {
      // NO-OP
    }

    Buffer getBuffer() {
      return buffer;
    }
  }

  private final Vertx vertx;
  private final String hostname;
  private final int port;

  /**
   * Construct a Pushgateway, with the given address and port.
   * <p>
   *
   * @param address  host:port or ip:port of the Pushgateway.
   */
  public AsyncPushGateway(Vertx vertx, String address) {
    this.vertx = vertx;
    String[] parts = address.split(":");
    hostname = parts[0];
    if (parts.length == 2) {
      try {
        port = Integer.parseInt(parts[1]);
      } catch (NumberFormatException e) {
        throw new RuntimeException(e);
      }
    } else {
      // default HTTP port
      port = 80;
    }
  }

  /**
   * Pushes all metrics in a registry, replacing all those with the same job as the grouping key.
   * <p>
   * This uses the POST HTTP method.
   */
  public void push(CollectorRegistry registry, String job, Handler<AsyncResult<Void>> handler) {
    doRequest(registry, job, null, "POST", handler);
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and no grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the POST HTTP method.
   */
  public void push(Collector collector, String job, Handler<AsyncResult<Void>> handler) {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    push(registry, job, handler);
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the POST HTTP method.
   */
  public void push(CollectorRegistry registry, String job, Map<String, String> groupingKey, Handler<AsyncResult<Void>> handler) {
    doRequest(registry, job, groupingKey, "POST", handler);
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the POST HTTP method.
   */
  public void push(Collector collector, String job, Map<String, String> groupingKey, Handler<AsyncResult<Void>> handler) {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    push(registry, job, groupingKey, handler);
  }

  /**
   * Pushes all metrics in a registry, replacing only previously pushed metrics of the same name and job and no grouping key.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void pushAdd(CollectorRegistry registry, String job, Handler<AsyncResult<Void>> handler) {
    doRequest(registry, job, null, "PUT", handler);
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name and job and no grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void pushAdd(Collector collector, String job, Handler<AsyncResult<Void>> handler) {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    pushAdd(registry, job, handler);
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name, job and grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void pushAdd(CollectorRegistry registry, String job, Map<String, String> groupingKey, Handler<AsyncResult<Void>> handler) {
    doRequest(registry, job, groupingKey, "PUT", handler);
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name, job and grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void pushAdd(Collector collector, String job, Map<String, String> groupingKey, Handler<AsyncResult<Void>> handler) {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    pushAdd(registry, job, groupingKey, handler);
  }


  /**
   * Deletes metrics from the Pushgateway.
   * <p>
   * Deletes metrics with no grouping key and the provided job.
   * This uses the DELETE HTTP method.
   */
  public void delete(String job, Handler<AsyncResult<Void>> handler) {
    doRequest(null, job, null, "DELETE", handler);
  }

  /**
   * Deletes metrics from the Pushgateway.
   * <p>
   * Deletes metrics with the provided job and grouping key.
   * This uses the DELETE HTTP method.
   */
  public void delete(String job, Map<String, String> groupingKey, Handler<AsyncResult<Void>> handler) {
    doRequest(null, job, groupingKey, "DELETE", handler);
  }


  private void doRequest(CollectorRegistry registry, String job, Map<String, String> groupingKey, String method, Handler<AsyncResult<Void>> handler) {
    try {
      final HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

      String resource = "/metrics/job/" + URLEncoder.encode(job, "UTF-8");
      if (groupingKey != null) {
        for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
          resource += "/" + entry.getKey() + "/" + URLEncoder.encode(entry.getValue(), "UTF-8");
        }
      }

      final HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions()
              .setDefaultPort(port)
              .setDefaultHost(hostname)
              .setConnectTimeout(10 * SECONDS_PER_MILLISECOND)
              .setIdleTimeout(10 * SECONDS_PER_MILLISECOND));

      final HttpClientRequest req = httpClient.request(httpMethod, resource, res -> {
        if (res.statusCode() != 202) {
          httpClient.close();
          handler.handle(Future.failedFuture(res.statusMessage()));
        } else {
          handler.handle(Future.succeededFuture());
        }
      });

      req.putHeader("Content-Type", TextFormat.CONTENT_TYPE_004);

      if (httpMethod != HttpMethod.DELETE) {
        try {
          final BufferWriter writer = new BufferWriter();
          TextFormat.write004(writer, registry.metricFamilySamples());
          req.putHeader("Content-Length", Integer.toString(writer.getBuffer().length()));
          req.write(writer.getBuffer());
        } catch (IOException e) {
          handler.handle(Future.failedFuture(e));
        }
      }

      // perform the real request
      req.end();
    } catch (UnsupportedEncodingException e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  /**
   * Returns a grouping key with the instance label set to the machine's IP address.
   * <p>
   * This is a convenience function, and should only be used where you want to
   * push per-instance metrics rather than cluster/job level metrics.
   */
  public static Map<String, String> instanceIPGroupingKey() throws UnknownHostException {
    Map<String, String> groupingKey = new HashMap<String, String>();
    groupingKey.put("instance", InetAddress.getLocalHost().getHostAddress());
    return groupingKey;
  }
}
