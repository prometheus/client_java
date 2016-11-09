package io.prometheus.client.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
 *       pg.pushAdd(registry, "my_batch_job");
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * Example usage with Basic authentication and SSL:
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
 *       CredentialsProvider creds = new BasicCredentialsProvider();
 *       creds.setCredentials(
 *          new AuthScope(new HttpHost("localhost")),
 *          new UsernamePasswordCredentials("username", "password"));
 *       CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(creds).build();
 *       PushGateway pg = new PushGateway("https", "localhost", "443", httpClient);
 *       pg.pushAdd(registry, "my_batch_job");
 *     }
 *   }
 * }
 * </pre>
 * For information about authentication using the HttpClient see:
 * <a href="https://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html">
 * https://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html</a>
 * <p>
 * See <a href="https://github.com/prometheus/pushgateway">https://github.com/prometheus/pushgateway</a>
 */
public class PushGateway {

  private static final int TIMEOUT = 10000;

  private final HttpClient httpClient;

  private final String urlBase;

  private BasicHttpContext localContext;

  /**
   * Construct a Pushgateway, with the given url (without scheme).
   * The uri will be split by ':' into address and port.
   * <p>
   *
   * @param url host:port or ip:port of the Pushgateway.
   */
  public PushGateway(String url) {
    this(url.split(":")[0], url.split(":")[1]);
  }

  /**
   * Construct a Pushgateway, with the given address and port.
   * <p>
   *
   * @param address host or ip of the Pushgateway.
   * @param port    port of the Pushgateway.
   */
  public PushGateway(String address, String port) {
    this("http", address, port, HttpClients.createDefault());
  }

  /**
   * Construct a Pushgateway, with the given address.
   * <p>
   *
   * @param scheme     address scheme of the PushGateway.
   * @param address    host or ip of the PushGateway.
   * @param port       port of the PushGateway.
   * @param httpClient httpClient for the connection to the PushGateway
   */
  public PushGateway(String scheme, String address, String port, HttpClient httpClient) {
    this.httpClient = httpClient;
    this.urlBase = scheme + "://" + address + ":" + port + "/metrics/job/";
    localContext = new BasicHttpContext();
    localContext.setAttribute("preemptive-auth", new BasicScheme());
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

  /**
   * Pushes all metrics in a registry, replacing all those with the same job and no grouping key.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void push(CollectorRegistry registry, String job) throws IOException {
    push(registry, job, new HashMap<String, String>());
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and no grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void push(Collector collector, String job) throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    push(registry, job);
  }

  /**
   * Pushes all metrics in a registry, replacing all those with the same job and grouping key.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void push(CollectorRegistry registry, String job, Map<String, String> groupingKey) throws IOException {
    HttpPut request = new HttpPut();
    request.setEntity(createOutputEntity(registry));
    doRequest(registry, job, groupingKey, request);
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the PUT HTTP method.
   */
  public void push(Collector collector, String job, Map<String, String> groupingKey) throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    push(registry, job, groupingKey);
  }

  /**
   * Pushes all metrics in a registry, replacing only previously pushed metrics of the same name and job and no grouping key.
   * <p>
   * This uses the POST HTTP method.
   */
  public void pushAdd(CollectorRegistry registry, String job) throws IOException {
    pushAdd(registry, job, new HashMap<String, String>());
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name and job and no grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the POST HTTP method.
   */
  public void pushAdd(Collector collector, String job) throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    pushAdd(registry, job);
  }

  /**
   * Pushes all metrics in a registry, replacing only previously pushed metrics of the same name, job and grouping key.
   * <p>
   * This uses the POST HTTP method.
   */
  public void pushAdd(CollectorRegistry registry, String job, Map<String, String> groupingKey) throws IOException {
    HttpPost request = new HttpPost();
    request.setEntity(createOutputEntity(registry));
    doRequest(registry, job, groupingKey, request);
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name, job and grouping key.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the POST HTTP method.
   */
  public void pushAdd(Collector collector, String job, Map<String, String> groupingKey) throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    collector.register(registry);
    pushAdd(registry, job, groupingKey);
  }

  /**
   * Deletes metrics from the Pushgateway.
   * <p>
   * Deletes metrics with no grouping key and the provided job.
   * This uses the DELETE HTTP method.
   */
  public void delete(String job) throws IOException {
    delete(job, new HashMap<String, String>());
  }

  /**
   * Deletes metrics from the Pushgateway.
   * <p>
   * Deletes metrics with the provided job and grouping key.
   * This uses the DELETE HTTP method.
   */
  public void delete(String job, Map<String, String> groupingKey) throws IOException {
    doRequest(null, job, groupingKey, new HttpDelete());
  }

  /**
   * Pushes all metrics in a registry, replacing all those with the same job and instance.
   * <p>
   * This uses the PUT HTTP method.
   *
   * @deprecated use {@link #push(CollectorRegistry, String, Map)}
   */
  @Deprecated
  public void push(CollectorRegistry registry, String job, String instance) throws IOException {
    push(registry, job, Collections.singletonMap("instance", instance));
  }

  /**
   * Pushes all metrics in a Collector, replacing all those with the same job and instance.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the PUT HTTP method.
   *
   * @deprecated use {@link #push(Collector, String, Map)}
   */
  @Deprecated
  public void push(Collector collector, String job, String instance) throws IOException {
    push(collector, job, Collections.singletonMap("instance", instance));
  }

  /**
   * Pushes all metrics in a registry, replacing only previously pushed metrics of the same name.
   * <p>
   * This uses the POST HTTP method.
   *
   * @deprecated use {@link #pushAdd(CollectorRegistry, String, Map)}
   */
  @Deprecated
  public void pushAdd(CollectorRegistry registry, String job, String instance) throws IOException {
    pushAdd(registry, job, Collections.singletonMap("instance", instance));
  }

  /**
   * Pushes all metrics in a Collector, replacing only previously pushed metrics of the same name.
   * <p>
   * This is useful for pushing a single Gauge.
   * <p>
   * This uses the POST HTTP method.
   *
   * @deprecated use {@link #pushAdd(Collector, String, Map)}
   */
  @Deprecated
  public void pushAdd(Collector collector, String job, String instance) throws IOException {
    pushAdd(collector, job, Collections.singletonMap("instance", instance));
  }

  /**
   * Creates the output entity for the POST or PUT requests based on the passed registry
   *
   * @param registry the metric collector registry
   * @return the {@link StringEntity} for the request
   * @throws IOException
   */
  private StringEntity createOutputEntity(CollectorRegistry registry) throws IOException {
    Writer writer = new StringWriter();
    TextFormat.write004(writer, registry.metricFamilySamples());
    return new StringEntity(writer.toString());
  }

  /**
   * Deletes metrics from the Pushgateway.
   * <p>
   * This uses the DELETE HTTP method.
   *
   * @deprecated use {@link #delete(String, Map)}
   */
  @Deprecated
  public void delete(String job, String instance) throws IOException {
    delete(job, Collections.singletonMap("instance", instance));
  }

  void doRequest(CollectorRegistry registry, String job, Map<String, String> groupingKey, HttpRequestBase request) throws IOException {
    String url = urlBase + URLEncoder.encode(job,
            "UTF-8");
    if (groupingKey != null) {
      for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
        url += "/" + entry.getKey() + "/" + URLEncoder.encode(entry.getValue(), "UTF-8");
      }
    }
    customizeRequestConfig(request);

    request.setURI(URI.create(url));

    request.setHeader(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);

    try {
      HttpResponse response = httpClient.execute(request, localContext);
      int responseCode = response.getStatusLine().getStatusCode();
      if (responseCode != HttpStatus.SC_ACCEPTED) {
        throw new IOException("Response code from " + url + " was " + response);
      }
    } finally {
      request.releaseConnection();
    }
  }

  /**
   * Customizes the request config for the passed request by adding additional parameters.
   *
   * @param request the http request
   */
  private void customizeRequestConfig(HttpRequestBase request) {
    RequestConfig config = request.getConfig();
    RequestConfig.Builder builder;
    if (config == null) {
      builder = RequestConfig.custom();
    } else {
      builder = RequestConfig.copy(config);

    }
    config = builder.setConnectTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).build();
    request.setConfig(config);
  }
}
