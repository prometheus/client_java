# Prometheus JVM Client
It supports Java, Clojure, Scala, JRuby, and anything else that runs on the JVM.

[![Build Status](https://circleci.com/gh/prometheus/client_java.svg?style=svg)](https://circleci.com/gh/prometheus/client_java)


Table of Contents
=================

  * [Using](#using)
     * [Assets](#assets)
     * [Javadocs](#javadocs)
  * [Instrumenting](#instrumenting)
     * [Counter](#counter)
     * [Gauge](#gauge)
     * [Summary](#summary)
     * [Histogram](#histogram)
     * [Labels](#labels)
     * [Registering Metrics](#registering-metrics)
  * [Exemplars](#exemplars)
     * [Global Exemplar Samplers](#global-exemplar-samplers)
     * [Per Metric Exemplar Samplers](#per-metric-exemplar-samplers)
     * [Per Observation Exemplars](#per-observation-exemplars)
     * [Built-in Support for Tracing Systems](#built-in-support-for-tracing-systems)
  * [Included Collectors](#included-collectors)
     * [Logging](#logging)
     * [Caches](#caches)
     * [Hibernate](#hibernate)
     * [Jetty](#jetty)
  * [Exporting](#exporting)
     * [HTTP](#http)
  * [Exporting to a Pushgateway](#exporting-to-a-pushgateway)
  * [Bridges](#bridges)
     * [Graphite](#graphite)
  * [Custom Collectors](#custom-collectors)
  * [Contact](#contact)

## Using
### Assets
If you use Maven, you can simply reference the assets below.  The latest
version can be found on in the maven repository for
[io.prometheus](http://mvnrepository.com/artifact/io.prometheus).

```xml
<!-- The client -->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient</artifactId>
  <version>0.16.0</version>
</dependency>
<!-- Hotspot JVM metrics-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_hotspot</artifactId>
  <version>0.16.0</version>
</dependency>
<!-- Exposition HTTPServer-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_httpserver</artifactId>
  <version>0.16.0</version>
</dependency>
<!-- Pushgateway exposition-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_pushgateway</artifactId>
  <version>0.16.0</version>
</dependency>
```

### Javadocs
There are canonical examples defined in the class definition Javadoc of the client packages.

Documentation can be found at the [Java Client
Github Project Page](http://prometheus.github.io/client_java).

### Disabling `_created` metrics

By default, counters, histograms, and summaries export an additional series
suffixed with `_created` and a value of the unix timestamp for when the metric
was created. If this information is not helpful, it can be disabled by setting
the environment variable `PROMETHEUS_DISABLE_CREATED_SERIES=true`.

## Instrumenting

Four types of metrics are offered: Counter, Gauge, Summary and Histogram.
See the documentation on [metric types](http://prometheus.io/docs/concepts/metric_types/)
and [instrumentation best practices](http://prometheus.io/docs/practices/instrumentation/#counter-vs.-gauge,-summary-vs.-histogram)
on how to use them.

### Counter

Counters go up, and reset when the process restarts.


```java
import io.prometheus.client.Counter;
class YourClass {
  static final Counter requests = Counter.build()
     .name("requests_total").help("Total requests.").register();

  void processRequest() {
    requests.inc();
    // Your code here.
  }
}
```

### Gauge

Gauges can go up and down.

```java
class YourClass {
  static final Gauge inprogressRequests = Gauge.build()
     .name("inprogress_requests").help("Inprogress requests.").register();

  void processRequest() {
    inprogressRequests.inc();
    // Your code here.
    inprogressRequests.dec();
  }
}
```

There are utilities for common use cases:

```java
gauge.setToCurrentTime(); // Set to current unixtime.
```

As an advanced use case, a `Gauge` can also take its value from a callback by using the
[setChild()](https://prometheus.io/client_java/io/prometheus/client/SimpleCollector.html#setChild-Child-java.lang.String...-)
method. Keep in mind that the default `inc()`, `dec()` and `set()` methods on Gauge take care of thread safety, so
when using this approach ensure the value you are reporting accounts for concurrency.


### Summary

Summaries and Histograms can both be used to monitor distributions, like latencies or request sizes.

An overview of when to use Summaries and when to use Histograms can be found on [https://prometheus.io/docs/practices/histograms](https://prometheus.io/docs/practices/histograms).

The following example shows how to measure latencies and request sizes:

```java
class YourClass {

  private static final Summary requestLatency = Summary.build()
      .name("requests_latency_seconds")
      .help("request latency in seconds")
      .register();

  private static final Summary receivedBytes = Summary.build()
      .name("requests_size_bytes")
      .help("request size in bytes")
      .register();

  public void processRequest(Request req) {
    Summary.Timer requestTimer = requestLatency.startTimer();
    try {
      // Your code here.
    } finally {
      requestTimer.observeDuration();
      receivedBytes.observe(req.size());
    }
  }
}
```

The `Summary` class provides different utility methods for observing values, like `observe(double)`, `startTimer(); timer.observeDuration()`, `time(Callable)`, etc.

By default, `Summary` metrics provide the `count` and the `sum`. For example, if you measure latencies of a REST service, the `count` will tell you how often the REST service was called, and the `sum` will tell you the total aggregated response time. You can calculate the average response time using a Prometheus query dividing `sum / count`.

In addition to `count` and `sum`, you can configure a Summary to provide quantiles:

```java
Summary requestLatency = Summary.build()
    .name("requests_latency_seconds")
    .help("Request latency in seconds.")
    .quantile(0.5, 0.01)    // 0.5 quantile (median) with 0.01 allowed error
    .quantile(0.95, 0.005)  // 0.95 quantile with 0.005 allowed error
    // ...
    .register();
```

As an example, a `0.95` quantile of `120ms` tells you that `95%` of the calls were faster than `120ms`, and `5%` of the calls were slower than `120ms`.

Tracking exact quantiles require a large amount of memory, because all observations need to be stored in a sorted list. Therefore, we allow an error to significantly reduce memory usage.

In the example, the allowed error of `0.005` means that you will not get the exact `0.95` quantile, but anything between the `0.945` quantile and the `0.955` quantile.

Experiments show that the `Summary` typically needs to keep less than 100 samples to provide that precision, even if you have hundreds of millions of observations.

There are a few special cases:

* You can set an allowed error of `0`, but then the `Summary` will keep all observations in memory.
* You can track the minimum value with `.quantile(0, 0)`. This special case will not use additional memory even though the allowed error is `0`.
* You can track the maximum value with `.quantile(1, 0)`. This special case will not use additional memory even though the allowed error is `0`.

Typically, you don't want to have a `Summary` representing the entire runtime of the application, but you want to look at a reasonable time interval. `Summary` metrics implement a configurable sliding time window:

```java
Summary requestLatency = Summary.build()
    .name("requests_latency_seconds")
    .help("Request latency in seconds.")
    .maxAgeSeconds(10 * 60)
    .ageBuckets(5)
    // ...
    .register();
```

The default is a time window of 10 minutes and 5 age buckets, i.e. the time window is 10 minutes wide, and * we slide it forward every 2 minutes.

### Histogram

Like Summaries, Histograms can be used to monitor latencies (or other things like request sizes).

An overview of when to use Summaries and when to use Histograms can be found on [https://prometheus.io/docs/practices/histograms](https://prometheus.io/docs/practices/histograms).

```java
class YourClass {
  static final Histogram requestLatency = Histogram.build()
     .name("requests_latency_seconds").help("Request latency in seconds.").register();

  void processRequest(Request req) {
    Histogram.Timer requestTimer = requestLatency.startTimer();
    try {
      // Your code here.
    } finally {
      requestTimer.observeDuration();
    }
  }
}
```

The default buckets are intended to cover a typical web/rpc request from milliseconds to seconds.
They can be overridden with the `buckets()` method on the [Histogram.Builder](https://prometheus.github.io/client_java/io/prometheus/client/Histogram.Builder.html#buckets-double...-).

There are utilities for timing code:

```java
class YourClass {
  static final Histogram requestLatency = Histogram.build()
     .name("requests_latency_seconds").help("Request latency in seconds.").register();

  void processRequest(Request req) {
    requestLatency.time(new Runnable() {
      public abstract void run() {
        // Your code here.
      }
    });


    // Or the Java 8 lambda equivalent
    requestLatency.time(() -> {
      // Your code here.
    });
  }
}
```

### Labels

All metrics can have labels, allowing grouping of related time series.

See the best practices on [naming](http://prometheus.io/docs/practices/naming/)
and [labels](http://prometheus.io/docs/practices/instrumentation/#use-labels).

Taking a counter as an example:

```java
class YourClass {
  static final Counter requests = Counter.build()
     .name("my_library_requests_total").help("Total requests.")
     .labelNames("method").register();

  void processGetRequest() {
    requests.labels("get").inc();
    // Your code here.
  }
}
```

### Registering Metrics

The best way to register a metric is via a `static final` class variable as is common with loggers.

```java
static final Counter requests = Counter.build()
   .name("my_library_requests_total").help("Total requests.").labelNames("path").register();
```

Using the default registry with variables that are `static` is ideal since registering a metric with the same name
is not allowed and the default registry is also itself static. You can think of registering a metric, more like
registering a definition (as in the `TYPE` and `HELP` sections). The metric 'definition' internally holds the samples
that are reported and pulled out by Prometheus. Here is an example of registering a metric that has no labels.

```java
class YourClass {
  static final Gauge activeTransactions = Gauge.build()
     .name("my_library_transactions_active")
     .help("Active transactions.")
     .register();

  void processThatCalculates(String key) {
    activeTransactions.inc();
    try {
        // Perform work.
    } finally{
        activeTransactions.dec();
    }
  }
}
```

To create timeseries with labels, include `labelNames()` with the builder. The `labels()` method looks up or creates
the corresponding labelled timeseries. You might also consider storing the labelled timeseries as an instance variable if it is
appropriate. It is thread safe and can be used multiple times, which can help performance.


```java
class YourClass {
  static final Counter calculationsCounter = Counter.build()
     .name("my_library_calculations_total").help("Total calls.")
     .labelNames("key").register();

  void processThatCalculates(String key) {
    calculationsCounter.labels(key).inc();
    // Run calculations.
  }
}
```

## Exemplars

Exemplars are a feature of the [OpenMetrics](http://openmetrics.io) format that allows applications to link metrics to example traces.
In order to see exemplars, you need to set the `Accept` header for the [OpenMetrics](http://openmetrics.io) format like this:

```
curl -H 'Accept: application/openmetrics-text; version=1.0.0; charset=utf-8' http://localhost:8080/metrics
```

Exemplars are supported since `client_java` version 0.11.0. Exemplars are supported for `Counter` and `Histogram` metrics.

### Global Exemplar Samplers

An `ExemplarSampler` is used implicitly under the hood to add exemplars to your metrics. The idea is that you get exemplars without changing your code.

The `DefaultExemplarSampler` comes with built-in support for [OpenTelemetry tracing](https://github.com/open-telemetry/opentelemetry-java), see  [built-in support for tracing systems](#built-in-support-for-tracing-systems) below.

You can disable the default exemplar samplers globally with:

```java
ExemplarConfig.disableExemplars();
```

You can set your own custom implementation of `ExemplarSampler` as a global default like this:

```java
ExemplarSampler myExemplarSampler = new MyExemplarSampler();
ExemplarConfig.setCounterExemplarSampler(myExemplarSampler);
ExemplarConfig.setHistogramExemplarSampler(myExemplarSampler);
```

### Per Metric Exemplar Samplers

The metric builders for `Counter` and `Histogram` have methods for setting the exemplar sampler for that individual metric. This takes precedence over the global setting in `ExemplarConfig`.

The following calls enable the default exemplar sampler for individual metrics. This is useful if you disabled the exemplar sampler globally with `ExemplarConfig.disableExemplars()`.

```java
Counter myCounter = Counter.build()
    .name("number_of_events_total")
    .help("help")
    .withExemplars()
    ...
    .register();
```

```java
Histogram myHistogram = Histogram.build()
    .name("my_latency")
    .help("help")
    .withExemplars()
    ...
    .register();
```

The following calls disable exemplars for individual metrics:

```java
Counter myCounter = Counter.build()
    .name("number_of_events_total")
    .help("help")
    .withoutExemplars()
    ...
    .register();
```

```java
Histogram myHistogram = Histogram.build()
    .name("my_latency")
    .help("help")
    .withoutExemplars()
    ...
    .register();
```

The following calls enable exemplars and set a custom `MyExemplarSampler` for individual metrics:

```java
// CounterExemplarSampler
Counter myCounter = Counter.build()
    .name("number_of_events_total")
    .help("help")
    .withExemplarSampler(new MyExemplarSampler())
    ...
    .register();
```

```java
// HistogramExemplarSampler
Histogram myHistogram = Histogram.build()
    .name("my_latency")
    .help("help")
    .withExemplarSampler(new MyExemplarSampler())
    ...
    .register();
```

### Per Observation Exemplars

You can explicitly provide an exemplar for an individual observation. This takes precedence over the exemplar sampler configured with the metric.

The following call will increment a counter, and create an exemplar with the specified `span_id` and `trace_id` labels:

```java
myCounter.incWithExemplar("span_id", "abcdef", "trace_id", "123456");
```

The following call will observe a value of `0.12` in a histogram, and create an exemplar with the specified `span_id` and `trace_id` labels:

```java
myHistogram.observeWithExemplar(0.12, "span_id", "abcdef", "trace_id", "123456");
```

All methods for observing and incrementing values have `...withExemplar` equivalents. There are versions taking the exemplar labels as a `String...` as shown in the example, as well as versions taking the exemplar labels as a `Map<String, String>`.

### Built-in Support for Tracing Systems

The `DefaultExemplarSampler` detects if a tracing library is found on startup, and provides exemplars for that tracing library by default. Currently, only [OpenTelemetry tracing](https://github.com/open-telemetry/opentelemetry-java) is supported.
If you are a tracing vendor, feel free to open a PR and add support for your tracing library.

Documentation of the individual tracer integrations:

* [OTEL_EXEMPLARS.md](OTEL_EXEMPLARS.md): OpenTelemetry

## Included Collectors

The Java client includes collectors for garbage collection, memory pools, classloading, and thread counts.
These can be added individually or just use the `DefaultExports` to conveniently register them.

```java
DefaultExports.initialize();
```

### Logging

There are logging collectors for log4j, log4j2 and logback.

To register the Logback collector can be added to the root level like so:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>

    <appender name="METRICS" class="io.prometheus.client.logback.InstrumentedAppender" />

    <root level="INFO">
        <appender-ref ref="METRICS" />
    </root>

</configuration>
```

To register the log4j collector at root level:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
    <appender name="METRICS" class="io.prometheus.client.log4j.InstrumentedAppender"/>
    <root>
        <priority value ="info" />
        <appender-ref ref="METRICS" />
    </root>
</log4j:configuration>
```

To register the log4j2 collector at root level:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="io.prometheus.client.log4j2">
    <Appenders>
        <Prometheus name="METRICS"/>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="METRICS"/>
        </Root>
    </Loggers>
</Configuration>
```

See `./integration_tests/it_log4j2/` for a log4j2 example.

### Caches

To register the Guava cache collector, be certain to add `recordStats()` when building
the cache and adding it to the registered collector.

```java
CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();

Cache<String, String> cache = CacheBuilder.newBuilder().recordStats().build();
cacheMetrics.addCache("myCacheLabel", cache);
```

The Caffeine equivalent is nearly identical. Again, be certain to call `recordStats()`
 when building the cache so that metrics are collected.

```java
CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();

Cache<String, String> cache = Caffeine.newBuilder().recordStats().build();
cacheMetrics.addCache("myCacheLabel", cache);
```

### Hibernate

There is a collector for Hibernate which allows to collect metrics from one or more
`SessionFactory` instances.

If you want to collect metrics from a single `SessionFactory`, you can register
the collector like this:

```java
new HibernateStatisticsCollector(sessionFactory, "myapp").register();
```

In some situations you may want to collect metrics from multiple factories. In this
case just call `add()` on the collector for each of them.

```java
new HibernateStatisticsCollector()
    .add(sessionFactory1, "myapp1")
    .add(sessionFactory2, "myapp2")
    .register();
```

If you are using Hibernate in a JPA environment and only have access to the `EntityManager`
or `EntityManagerFactory`, you can use this code to access the underlying `SessionFactory`:

```java
SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
```

respectively 

```java
SessionFactory sessionFactory = entityManager.unwrap(Session.class).getSessionFactory();
```
### Jetty

There is a collector for recording various Jetty server metrics. You can do it by  registering the collector like this:

```java
// Configure StatisticsHandler.
StatisticsHandler stats = new StatisticsHandler();
stats.setHandler(server.getHandler());
server.setHandler(stats);
// Register collector.
new JettyStatisticsCollector(stats).register();

```

Also, you can collect `QueuedThreadPool` metrics. If there is a single `QueuedThreadPool`
to keep track of, use the following:

```java
new QueuedThreadPoolStatisticsCollector(queuedThreadPool, "myapp").register();
```

If you want to collect multiple `QueuedThreadPool` metrics, also you can achieve it like this:

```java
new QueuedThreadPoolStatisticsCollector()
    .add(queuedThreadPool1, "myapp1")
    .add(queuedThreadPool2, "myapp2")
    .register();
```

#### Servlet Filter

There is a servlet filter available for measuring the duration taken by servlet requests:

* `javax` version: `io.prometheus.client.filter.MetricsFilter` provided by `simpleclient_servlet`
* `jakarta` version: `io.prometheus.client.servlet.jakarta.filter.MetricsFilter` provided by `simpleclient_servlet_jakarta`

Both versions implement the same functionality.

Configuration is as follows:

The `metric-name` init parameter is required, and is the name of the
metric prometheus will expose for the timing metrics. Help text via the `help`
init parameter is not required, although it is highly recommended.  The number
of buckets is overridable, and can be configured by passing a comma-separated
string of doubles as the `buckets` init parameter. The granularity of path
measuring is also configurable, via the `path-components` init parameter. By
default, the servlet filter will record each path differently, but by setting an
integer here, you can tell the filter to only record up to the Nth slashes. That
is, all requests with greater than N "/" characters in the servlet URI path will
be measured in the same bucket, and you will lose that granularity. The init
parameter `strip-context-path` can be used to strip the leading part of the URL
which is part of the deployment context (i.e. the folder the servlet is deployed to),
so that the same servlet deployed to different paths can lead to similar metrics.

The code below is an example of the XML configuration for the filter. You will
need to place this (replace your own values) code in your
`webapp/WEB-INF/web.xml` file.

```xml
<filter>
  <filter-name>prometheusFilter</filter-name>
  <!-- This example shows the javax version. For Jakarta you would use -->
  <!-- <filter-class>io.prometheus.client.filter.servlet.jakarta.MetricsFilter</filter-class> -->
  <filter-class>io.prometheus.client.filter.MetricsFilter</filter-class>
  <init-param>
    <param-name>metric-name</param-name>
    <param-value>webapp_metrics_filter</param-value>
  </init-param>
  <!-- help is optional, defaults to the message below -->
  <init-param>
    <param-name>help</param-name>
    <param-value>This is the help for your metrics filter</param-value>
  </init-param>
  <!-- buckets is optional, unless specified the default buckets from io.prometheus.client.Histogram are used -->
  <init-param>
    <param-name>buckets</param-name>
    <param-value>0.005,0.01,0.025,0.05,0.075,0.1,0.25,0.5,0.75,1,2.5,5,7.5,10</param-value>
  </init-param>
  <!-- path-components is optional, anything less than 1 (1 is the default) means full granularity -->
  <init-param>
    <param-name>path-components</param-name>
    <param-value>1</param-value>
  </init-param>
  <!-- strip-context-path is optional, defaults to false -->
  <init-param>
    <param-name>strip-context-path</param-name>
    <param-value>false</param-value>
  </init-param>
</filter>

<!-- You will most likely want this to be the first filter in the chain
(therefore the first <filter-mapping> in the web.xml file), so that you can get
the most accurate measurement of latency. -->
<filter-mapping>
  <filter-name>prometheusFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

Additionally, you can instantiate your servlet filter directly in Java code. To
do this, you just need to call the non-empty constructor. The first parameter,
the metric name, is required. The second, help, is optional but highly
recommended. The other parameters are optional and will default sensibly if omitted.

#### Spring AOP

There is a Spring AOP collector that allows you to annotate methods that you
would like to instrument with a [Summary](#Summary), but without going through
the process of manually instantiating and registering your metrics classes. To
use the metrics annotations, simply add `simpleclient_spring_web` as a
dependency, annotate a configuration class with `@EnablePrometheusTiming`, then
annotate your Spring components as such:

```java
@Controller
public class MyController {
  @RequestMapping("/")
  @PrometheusTimeMethod(name = "my_controller_path_duration_seconds", help = "Some helpful info here")
  public Object handleMain() {
    // Do something
  }
}
```

## Exporting

There are several options for exporting metrics.

### HTTP

Metrics are usually exposed over HTTP, to be read by the Prometheus server.

There are HTTPServer, Servlet, SpringBoot, and Vert.x integrations included in the client library.
The simplest of these is the HTTPServer:

```java
HTTPServer server = new HTTPServer.Builder()
    .withPort(1234)
    .build();
```

The `HTTPServer.Builder` supports configuration of a `SampleNameFilter` which can be used to
restrict the time series being exported by name.

To add Prometheus exposition to an existing HTTP server using servlets, see the `MetricsServlet`.
It also serves as a simple example of how to write a custom endpoint.

To expose the metrics used in your code, you would add the Prometheus servlet to your Jetty server:

```java
Server server = new Server(1234);
ServletContextHandler context = new ServletContextHandler();
context.setContextPath("/");
server.setHandler(context);

context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
```

Like the HTTPServer, the `MetricsServlet` can be configured with a `SampleNameFilter` which can
be used to restrict the time series being exported by name. See `integration_tests/servlet_jakarta_exporter_webxml/`
for an example how to configure this in `web.xml`.

All HTTP exposition integrations support restricting which time series to return
using `?name[]=` URL parameters. Due to implementation limitations, this may
have false negatives.

## Exporting to a Pushgateway

The [Pushgateway](https://github.com/prometheus/pushgateway)
allows ephemeral and batch jobs to expose their metrics to Prometheus.

```java
void executeBatchJob() throws Exception {
  CollectorRegistry registry = new CollectorRegistry();
  Gauge duration = Gauge.build()
     .name("my_batch_job_duration_seconds").help("Duration of my batch job in seconds.").register(registry);
  Gauge.Timer durationTimer = duration.startTimer();
  try {
    // Your code here.

    // This is only added to the registry after success,
    // so that a previous success in the Pushgateway isn't overwritten on failure.
    Gauge lastSuccess = Gauge.build()
       .name("my_batch_job_last_success").help("Last time my batch job succeeded, in unixtime.").register(registry);
    lastSuccess.setToCurrentTime();
  } finally {
    durationTimer.setDuration();
    PushGateway pg = new PushGateway("127.0.0.1:9091");
    pg.pushAdd(registry, "my_batch_job");
  }
}
 ```

A separate registry is used, as the default registry may contain other metrics
such as those from the Process Collector. See the
[Pushgateway documentation](https://github.com/prometheus/pushgateway/blob/master/README.md)
for more information.


#### with Basic Auth
```java
PushGateway pushgateway = new PushGateway("127.0.0.1:9091");
pushgateway.setConnectionFactory(new BasicAuthHttpConnectionFactory("my_user", "my_password"));
```

#### with Custom Connection Preparation Logic
```java
PushGateway pushgateway = new PushGateway("127.0.0.1:9091");
pushgateway.setConnectionFactory(new MyHttpConnectionFactory());
```
where
```java
class MyHttpConnectionFactory implements HttpConnectionFactory {
    @Override
    public HttpURLConnection create(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        // add any connection preparation logic you need
        return connection;
    }
}
```

## Bridges

It is also possible to expose metrics to systems other than Prometheus.
This allows you to take advantage of Prometheus instrumentation even
if you are not quite ready to fully transition to Prometheus yet.

### Graphite

Metrics are pushed over TCP in the Graphite plaintext format.

```java
Graphite g = new Graphite("localhost", 2003);
// Push the default registry once.
g.push(CollectorRegistry.defaultRegistry);

// Push the default registry every 60 seconds.
Thread thread = g.start(CollectorRegistry.defaultRegistry, 60);
// Stop pushing.
thread.interrupt();
thread.join();
```

## Custom Collectors

Sometimes it is not possible to directly instrument code, as it is not
in your control. This requires you to proxy metrics from other systems.

To do so you need to create a custom collector (which will need to be registered as a normal metric), for example:

```java
class YourCustomCollector extends Collector {
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    // With no labels.
    mfs.add(new GaugeMetricFamily("my_gauge", "help", 42));
    // With labels
    GaugeMetricFamily labeledGauge = new GaugeMetricFamily("my_other_gauge", "help", Arrays.asList("labelname"));
    labeledGauge.addMetric(Arrays.asList("foo"), 4);
    labeledGauge.addMetric(Arrays.asList("bar"), 5);
    mfs.add(labeledGauge);
    return mfs;
  }
}

// Registration
static final YourCustomCollector requests = new YourCustomCollector().register()
```

`SummaryMetricFamily` works similarly.

A collector may implement a `describe` method which returns metrics in the same
format as `collect` (though you don't have to include the samples). This is
used to predetermine the names of time series a `CollectorRegistry` exposes and
thus to detect collisions and duplicate registrations.

Usually custom collectors do not have to implement `describe`. If `describe` is
not implemented and the CollectorRegistry was created with `auto_describe=True`
(which is the case for the default registry) then `collect` will be called at
registration time instead of `describe`. If this could cause problems, either
implement a proper `describe`, or if that's not practical have `describe`
return an empty list.

### DropwizardExports Collector

DropwizardExports collector is available to proxy metrics from Dropwizard.

```java
// Dropwizard MetricRegistry
MetricRegistry metricRegistry = new MetricRegistry();
new DropwizardExports(metricRegistry).register();
```

By default Dropwizard metrics are translated to Prometheus sample sanitizing their names, i.e. replacing unsupported chars with `_`, for example:
```
Dropwizard metric name:
org.company.controller.save.status.400
Prometheus metric:
org_company_controller_save_status_400
```

It is also possible add custom labels and name to newly created `Sample`s by using a `CustomMappingSampleBuilder` with custom `MapperConfig`s:

```java
// Dropwizard MetricRegistry
MetricRegistry metricRegistry = new MetricRegistry();
MapperConfig config = new MapperConfig();
// The match field in MapperConfig is a simplified glob expression that only allows * wildcard.
config.setMatch("org.company.controller.*.status.*");
// The new Sample's template name.
config.setName("org.company.controller");
Map<String, String> labels = new HashMap<String,String>();
// ... more configs
// Labels to be extracted from the metric. Key=label name. Value=label template
labels.put("name", "${0}");
labels.put("status", "${1}");
config.setLabels(labels);

SampleBuilder sampleBuilder = new CustomMappingSampleBuilder(Arrays.asList(config));
new DropwizardExports(metricRegistry, sampleBuilder).register();
```

When a new metric comes to the collector, `MapperConfig`s are scanned to find the first one that matches the incoming metric name. The name set in the configuration will
be used and labels will be extracted. Using the `CustomMappingSampleBuilder` in the previous example leads to the following result:
```
Dropwizard metric name
org.company.controller.save.status.400
Prometheus metric
org_company_controller{name="save",status="400"}
```

Template with placeholders can be used both as names and label values. Placeholders are in the `${n}` format where n is the zero based index of the Dropwizard metric name wildcard group we want to extract.

## Contact
The [Prometheus Users Mailinglist](https://groups.google.com/forum/?fromgroups#!forum/prometheus-users) is the best place to ask questions.

Details for those wishing to develop the library can be found on the [wiki](https://github.com/prometheus/client_java/wiki/Development)
