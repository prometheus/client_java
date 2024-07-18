package io.prometheus.metrics.exporter.pushgateway;

import io.prometheus.metrics.config.ExporterPushgatewayProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.prometheus.metrics.exporter.pushgateway.Scheme.HTTP;

/**
 * Export metrics via the <a href="https://github.com/prometheus/pushgateway">Prometheus Pushgateway</a>
 * <p>
 * The Prometheus Pushgateway exists to allow ephemeral and batch jobs to expose their metrics to Prometheus.
 * Since these kinds of jobs may not exist long enough to be scraped, they can instead push their metrics
 * to a Pushgateway. This Java class allows pushing the contents of a {@link PrometheusRegistry} to a Pushgateway.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * void executeBatchJob() throws Exception {
 *     PrometheusRegistry registry = new PrometheusRegistry();
 *     Gauge duration = Gauge.builder()
 *             .name("my_batch_job_duration_seconds")
 *             .help("Duration of my batch job in seconds.")
 *             .register(registry);
 *     Timer durationTimer = duration.startTimer();
 *     try {
 *         // Your code here.
 *
 *         // This is only added to the registry after success,
 *         // so that a previous success in the Pushgateway isn't overwritten on failure.
 *         Gauge lastSuccess = Gauge.builder()
 *                 .name("my_batch_job_last_success")
 *                 .help("Last time my batch job succeeded, in unixtime.")
 *                 .register(registry);
 *         lastSuccess.set(System.currentTimeMillis());
 *     } finally {
 *         durationTimer.observeDuration();
 *         PushGateway pg = PushGateway.builder()
 *                 .address("127.0.0.1:9091")
 *                 .job("my_batch_job")
 *                 .registry(registry)
 *                 .build();
 *         pg.pushAdd();
 *     }
 * }
 * }
 * </pre>
 * <p>
 * See <a href="https://github.com/prometheus/pushgateway">https://github.com/prometheus/pushgateway</a>.
 */
public class PushGateway {

    private static final int MILLISECONDS_PER_SECOND = 1000;

    private final URL url;
    private final Format format;
    private final Map<String, String> requestHeaders;
    private final PrometheusRegistry registry;
    private final HttpConnectionFactory connectionFactory;

    private PushGateway(PrometheusRegistry registry, Format format, URL url, HttpConnectionFactory connectionFactory, Map<String, String> requestHeaders) {
        this.registry = registry;
        this.format = format;
        this.url = url;
        this.requestHeaders = Collections.unmodifiableMap(new HashMap<>(requestHeaders));
        this.connectionFactory = connectionFactory;
    }

    /**
     * Push all metrics. All metrics with the same job and grouping key are replaced.
     * <p>
     * This uses the PUT HTTP method.
     */
    public void push() throws IOException {
        doRequest(registry, "PUT");
    }

    /**
     * Push a single metric. All metrics with the same job and grouping key are replaced.
     * <p>
     * This is useful for pushing a single Gauge.
     * <p>
     * This uses the PUT HTTP method.
     */
    public void push(Collector collector) throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(collector);
        doRequest(registry, "PUT");
    }

    /**
     * Push a single collector. All metrics with the same job and grouping key are replaced.
     * <p>
     * This uses the PUT HTTP method.
     */
    public void push(MultiCollector collector) throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(collector);
        doRequest(registry, "PUT");
    }

    /**
     * Like {@link #push()}, but only metrics with the same name as the newly pushed metrics are replaced.
     * <p>
     * This uses the POST HTTP method.
     */
    public void pushAdd() throws IOException {
        doRequest(registry, "POST");
    }

    /**
     * Like {@link #push(Collector)}, but only the specified metric will be replaced.
     * <p>
     * This uses the POST HTTP method.
     */
    public void pushAdd(Collector collector) throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(collector);
        doRequest(registry, "POST");
    }

    /**
     * Like {@link #push(MultiCollector)}, but only the metrics from the collector will be replaced.
     * <p>
     * This uses the POST HTTP method.
     */
    public void pushAdd(MultiCollector collector) throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        registry.register(collector);
        doRequest(registry, "POST");
    }

    /**
     * Deletes metrics from the Pushgateway.
     * <p>
     * This uses the DELETE HTTP method.
     */
    public void delete() throws IOException {
        doRequest(null, "DELETE");
    }

    private void doRequest(PrometheusRegistry registry, String method) throws IOException {
        try {
            HttpURLConnection connection = connectionFactory.create(url);
            requestHeaders.forEach(connection::setRequestProperty);
            if (format == Format.PROMETHEUS_TEXT) {
                connection.setRequestProperty("Content-Type", PrometheusTextFormatWriter.CONTENT_TYPE);
            } else {
                connection.setRequestProperty("Content-Type", PrometheusProtobufWriter.CONTENT_TYPE);
            }
            if (!method.equals("DELETE")) {
                connection.setDoOutput(true);
            }
            connection.setRequestMethod(method);

            connection.setConnectTimeout(10 * MILLISECONDS_PER_SECOND);
            connection.setReadTimeout(10 * MILLISECONDS_PER_SECOND);
            connection.connect();

            try {
                if (!method.equals("DELETE")) {
                    OutputStream outputStream = connection.getOutputStream();
                    if (format == Format.PROMETHEUS_TEXT) {
                        new PrometheusTextFormatWriter(false).write(outputStream, registry.scrape());
                    } else {
                        new PrometheusProtobufWriter().write(outputStream, registry.scrape());
                    }
                    outputStream.flush();
                    outputStream.close();
                }

                int response = connection.getResponseCode();
                if (response / 100 != 2) {
                    String errorMessage;
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        String errBody = readFromStream(errorStream);
                        errorMessage = "Response code from " + url + " was " + response + ", response body: " + errBody;
                    } else {
                        errorMessage = "Response code from " + url + " was " + response;
                    }
                    throw new IOException(errorMessage);
                }

            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            if (url.getPort() != -1) {
                baseUrl += ":" + url.getPort();
            }
            throw new IOException("Failed to push metrics to the Prometheus Pushgateway on " + baseUrl + ": " + e.getMessage(), e);
        }
    }

    private static String readFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static Builder builder() {
        return builder(PrometheusProperties.get());
    }

    /**
     * The {@link PrometheusProperties} will be used to override what is set in the {@link Builder}.
     */
    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;
        private Format format;
        private String address;
        private Scheme scheme;
        private String job;
        private final Map<String, String> requestHeaders = new HashMap<>();
        private PrometheusRegistry registry = PrometheusRegistry.defaultRegistry;
        private HttpConnectionFactory connectionFactory = new DefaultHttpConnectionFactory();
        private Map<String, String> groupingKey = new TreeMap<>();

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Default is {@link Format#PROMETHEUS_PROTOBUF}.
         */
        public Builder format(Format format) {
            if (format == null) {
                throw new NullPointerException();
            }
            this.format = format;
            return this;
        }

        /**
         * Address of the Pushgateway in format {@code host:port}.
         * Default is {@code localhost:9091}.
         * Can be overwritten at runtime with the {@code io.prometheus.exporter.pushgateway.address} property.
         */
        public Builder address(String address) {
            if (address == null) {
                throw new NullPointerException();
            }
            this.address = address;
            return this;
        }

        /**
         * Username and password for HTTP basic auth when pushing to the Pushgateway.
         */
        public Builder basicAuth(String user, String password) {
            if (user == null || password == null) {
                throw new NullPointerException();
            }
            byte[] credentialsBytes = (user + ":" + password).getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getEncoder().encodeToString(credentialsBytes);
            requestHeaders.put("Authorization", String.format("Basic %s", encoded));
            return this;
        }

        /**
         * Bearer token authorization when pushing to the Pushgateway.
         */
        public Builder bearerToken(String token) {
            if (token == null)  {
                throw new NullPointerException();
            }
            requestHeaders.put("Authorization", String.format("Bearer %s", token));
            return this;
        }

        /**
         * Specify if metrics should be pushed using HTTP or HTTPS. Default is HTTP.
         * Can be overwritten at runtime with the {@code io.prometheus.exporter.pushgateway.scheme} property.
         */
        public Builder scheme(Scheme scheme) {
            if (scheme == null) {
                throw new NullPointerException();
            }
            this.scheme = scheme;
            return this;
        }

        /**
         * Custom connection factory. Default is {@link DefaultHttpConnectionFactory}.
         * <p>
         * The {@code PushGatewayTestApp} in {@code integration-tests/it-pushgateway/} has an example of a custom
         * connection factory that skips SSL certificate validation for HTTPS connections.
         */
        public Builder connectionFactory(HttpConnectionFactory connectionFactory) {
            if (connectionFactory == null) {
                throw new NullPointerException();
            }
            this.connectionFactory = connectionFactory;
            return this;
        }

        /**
         * The {@code job} label to be used when pushing metrics.
         * If not provided, the name of the JAR file will be used by default.
         * Can be overwritten at runtime with the {@code io.prometheus.exporter.pushgateway.job} property.
         */
        public Builder job(String job) {
            if (job == null) {
                throw new NullPointerException();
            }
            this.job = job;
            return this;
        }

        /**
         * Grouping keys to be used when pushing/deleting metrics.
         * Call this method multiple times for adding multiple grouping keys.
         */
        public Builder groupingKey(String name, String value) {
            if (name == null || value == null) {
                throw new NullPointerException();
            }
            groupingKey.put(name, value);
            return this;
        }

        /**
         * Convenience method for adding the current IP address as an "instance" label.
         */
        public Builder instanceIpGroupingKey() throws UnknownHostException {
            return groupingKey("instance", InetAddress.getLocalHost().getHostAddress());
        }

        /**
         * Push metrics from this registry instead of {@link PrometheusRegistry#defaultRegistry}.
         */
        public Builder registry(PrometheusRegistry registry) {
            if (registry == null) {
                throw new NullPointerException();
            }
            this.registry = registry;
            return this;
        }

        private Scheme getScheme(ExporterPushgatewayProperties properties) {
            if (properties != null && properties.getScheme() != null) {
                return Scheme.valueOf(properties.getScheme());
            } else if (this.scheme != null) {
                return this.scheme;
            } else {
                return HTTP;
            }
        }

        private String getAddress(ExporterPushgatewayProperties properties) {
            if (properties != null && properties.getAddress() != null) {
                return properties.getAddress();
            } else if (this.address != null) {
                return this.address;
            } else {
                return "localhost:9091";
            }
        }

        private String getJob(ExporterPushgatewayProperties properties) {
            if (properties != null && properties.getJob() != null) {
                return properties.getJob();
            } else if (this.job != null) {
                return this.job;
            } else {
                return DefaultJobLabelDetector.getDefaultJobLabel();
            }
        }

        private Format getFormat(ExporterPushgatewayProperties properties) {
            // currently not configurable via properties
            if (this.format != null) {
                return this.format;
            }
            return Format.PROMETHEUS_PROTOBUF;
        }

        private URL makeUrl(ExporterPushgatewayProperties properties) throws UnsupportedEncodingException, MalformedURLException {
            String url = getScheme(properties) + "://" + getAddress(properties) + "/metrics/";
            String job = getJob(properties);
            if (job.contains("/")) {
                url += "job@base64/" + base64url(job);
            } else {
                url += "job/" + URLEncoder.encode(job, "UTF-8");
            }
            if (groupingKey != null) {
                for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
                    if (entry.getValue().isEmpty()) {
                        url += "/" + entry.getKey() + "@base64/=";
                    } else if (entry.getValue().contains("/")) {
                        url += "/" + entry.getKey() + "@base64/" + base64url(entry.getValue());
                    } else {
                        url += "/" + entry.getKey() + "/" + URLEncoder.encode(entry.getValue(), "UTF-8");
                    }
                }
            }
            return URI.create(url).normalize().toURL();
        }

        private String base64url(String v) {
            return Base64.getEncoder().encodeToString(v.getBytes(StandardCharsets.UTF_8)).replace("+", "-").replace("/", "_");
        }

        public PushGateway build() {
            ExporterPushgatewayProperties properties = config == null ? null : config.getExporterPushgatewayProperties();
            try {
                return new PushGateway(registry, getFormat(properties), makeUrl(properties), connectionFactory, requestHeaders);
            } catch (MalformedURLException e) {
                throw new PrometheusPropertiesException(address + ": Invalid address. Expecting <host>:<port>");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e); // cannot happen, UTF-8 is always supported
            }
        }
    }
}
