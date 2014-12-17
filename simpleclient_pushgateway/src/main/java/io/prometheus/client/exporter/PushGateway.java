package io.prometheus.client.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Export metrics via the Prometheus Pushgateway.
 * <p>
 * The Prometheus Pushgateway exists to allow ephemeral and batch jobs to expose their metrics to Prometheus.
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
 *       PushGateway pg = new PushGateway("127.0.0.1:9091");
 *       pg.pushAdd(registry, "my_batch_job", "my_batch_job");
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * See <a href="https://github.com/prometheus/pushgateway">https://github.com/prometheus/pushgateway</a>
 */
public class PushGateway {

  private final String address;
  private final static int SECONDS_PER_MILLISECOND = 1000;
  /**
   * Construct a Pushgateway, with the given address.
   * <p>
   * @param address  host:port or ip:port of the Pushgateway.
   */
  public PushGateway(String address) {
    this.address = address;
  }

  /**
   * Pushes all metrics in a registry, replacing all those with the same job and instance.
   * <p>
   * See the Pushgateway documentation for detailed implications of the job and
   * instance parameter. instance can be left empty. The Pushgateway will then
   * use the client's IP number instead. This uses the POST HTTP method.
  */
  public void push(CollectorRegistry registry, String job, String instance) throws IOException {
    doRequest(registry, job, instance, "POST");
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and instance.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * See the Pushgateway documentation for detailed implications of the job and
   * instance parameter. instance can be left empty. The Pushgateway will then
   * use the client's IP number instead. This uses the POST HTTP method.
  */
  public void push(Collector collector, String job, String instance) throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    push(registry, job, instance);
  }

  /**
   * Pushes all metrics in a registry, replacing only previously pushed metrics of the same name.
   * <p>
   * See the Pushgateway documentation for detailed implications of the job and
   * instance parameter. instance can be left empty. The Pushgateway will then
   * use the client's IP number instead. This uses the PUT HTTP method.
  */
  public void pushAdd(CollectorRegistry registry, String job, String instance) throws IOException {
    doRequest(registry, job, instance, "PUT");
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * See the Pushgateway documentation for detailed implications of the job and
   * instance parameter. instance can be left empty. The Pushgateway will then
   * use the client's IP number instead. This uses the POST HTTP method.
  */
  public void pushAdd(Collector collector, String job, String instance) throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    pushAdd(registry, job, instance);
  }

  /**
   * Deletes metrics from the Pushgateway.
   * <p>
   * instance can be left empty. The Pushgateway will then
   * use the client's IP number instead. This uses the DELETE HTTP method.
  */
  public void delete(String job, String instance) throws IOException {
    doRequest(null, job, instance, "DELETE");
  }

  void doRequest(CollectorRegistry registry, String job, String instance, String method) throws IOException {
    String url = "http://" + address + "/metrics/jobs/" + URLEncoder.encode(job, "UTF-8");
    if (!instance.isEmpty()) {
      url += "/instances/" + URLEncoder.encode(instance, "UTF-8");
    }
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("Content-Type", TextFormat.CONTENT_TYPE_004);
    if (!method.equals("DELETE")) {
      connection.setDoOutput(true);
    }
    connection.setRequestMethod(method);

    connection.setConnectTimeout(10 * SECONDS_PER_MILLISECOND);
    connection.setReadTimeout(10 * SECONDS_PER_MILLISECOND);
    connection.connect();

    try {
      if (!method.equals("DELETE")) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        TextFormat.write004(writer, registry.metricFamilySamples());
        writer.flush();
        writer.close();
      }

      int response = connection.getResponseCode();
      if (response != HttpURLConnection.HTTP_ACCEPTED) {
        throw new IOException("Response code from " + url + " was " + response);
      }
    } finally {
      connection.disconnect();
    }
  }


}
