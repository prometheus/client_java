# Prometheus JVM Client
It supports Java, Clojure, Scala, JRuby, and anything else that runs on the JVM.
 
[![Build Status](https://travis-ci.org/prometheus/client_java.png?branch=master)](https://travis-ci.org/prometheus/client_java)

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
  * [Included Collectors](#included-collectors)
     * [Logging](#logging)
     * [Caches](#caches)
     * [Hibernate](#hibernate)
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
  <version>0.0.21</version>
</dependency>
<!-- Hotspot JVM metrics-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_hotspot</artifactId>
  <version>0.0.21</version>
</dependency>
<!-- Exposition servlet-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_servlet</artifactId>
  <version>0.0.21</version>
</dependency>
<!-- Pushgateway exposition-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_pushgateway</artifactId>
  <version>0.0.21</version>
</dependency>
```

### Javadocs
There are canonical examples defined in the class definition Javadoc of the client packages.

Documentation can be found at the [Java Client
Github Project Page](http://prometheus.github.io/client_java).

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
    inprogressRequest.inc();
    // Your code here.
    inprogressRequest.dec();
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

Summaries track the size and number of events.

```java
class YourClass {
  static final Summary receivedBytes = Summary.build()
     .name("requests_size_bytes").help("Request size in bytes.").register();
  static final Summary requestLatency = Summary.build()
     .name("requests_latency_seconds").help("Request latency in seconds.").register();
  
  void processRequest(Request req) {
    Summary.Timer requestTimer = requestLatency.startTimer();
    try {
      // Your code here.
    } finally {
      receivedBytes.observe(req.size());
      requestTimer.observeDuration();
    }
  }
}
```

There are utilities for timing code and support for [quantiles](https://prometheus.io/docs/practices/histograms/#quantiles).
Essentially quantiles aren't aggregatable and add some client overhead for the calculation.

```java
class YourClass {
  static final Summary requestLatency = Summary.build()
    .quantile(0.5, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
    .quantile(0.9, 0.01)   // Add 90th percentile with 1% tolerated error
    .name("requests_latency_seconds").help("Request latency in seconds.").register();
  
  void processRequest(Request req) {
    requestLatency.timer(new Runnable() {
      public abstract void run() {
        // Your code here.    
      }
    });  
      
      
    // Or the Java 8 lambda equivalent   
    requestLatency.timer(() -> {
      // Your code here.
    });
  }
}
```

### Histogram

Histograms track the size and number of events in buckets.
This allows for aggregatable calculation of quantiles.

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
They can be overridden with the `buckets()` method on the [Histogram.Builder](https://prometheus.io/client_java/io/prometheus/client/Histogram.Builder.html#buckets-double...-).

There are utilities for timing code:

```java
class YourClass {
  static final Histogram requestLatency = Histogram.build()
     .name("requests_latency_seconds").help("Request latency in seconds.").register();
  
  void processRequest(Request req) {
    requestLatency.timer(new Runnable() {
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


## Included Collectors

The Java client includes collectors for garbage collection, memory pools, JMX, classloading, and thread counts.
These can be added individually or just use the `DefaultExports` to conveniently register them. 

```java
DefaultExports.initialize();
```

###Logging

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

#### Servlet Filter

There is a servlet filter available for measuring the duration taken by servlet
requests. The `metric-name` init parameter is required, and is the name of the
metric prometheus will expose for the timing metrics. Help text via the `help`
init parameter is not required, although it is highly recommended.  The number
of buckets is overridable, and can be configured by passing a comma-separated
string of doubles as the `buckets` init parameter. The granularity of path
measuring is also configurable, via the `path-components` init parameter. By
default, the servlet filter will record each path differently, but by setting an
integer here, you can tell the filter to only record up to the Nth slashes. That
is, all reqeusts with greater than N "/" characters in the servlet URI path will
be measured in the same bucket and you will lose that granularity.

The code below is an example of the XML configuration for the filter. You will
need to place this (replace your own values) code in your
`webapp/WEB-INF/web.xml` file.

```xml
<filter>
  <filter-name>prometheusFilter</filter-name>
  <filter-class>net.cccnext.ssp.portal.spring.filter.PrometheusMetricsFilter</filter-class>
  <init-param>
    <param-name>metric-name</param-name>
    <param-value>webapp_metrics_filter</param-value>
  </init-param>
  <init-param>
    <param-name>help</param-name>
    <param-value>This is the help for your metrics filter</param-value>
  </init-param>
  <init-param>
    <param-name>buckets</param-name>
    <param-value>0.005,0.01,0.025,0.05,0.075,0.1,0.25,0.5,0.75,1,2.5,5,7.5,10</param-value>
  </init-param>
  <!-- Optionally override path components; anything less than 1 (1 is the default)
       means full granularity -->
  <init-param>
    <param-name>path-components</param-name>
    <param-value>1</param-value>
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
recommended. The last two (path-components, and buckets) are optional and will
default sensibly if omitted.

#### Spring AOP

There is a Spring AOP collector that allows you to annotate methods that you
would like to instrument with a [Summary](#Summary), but without going through
the process of manually instaniating and registering your metrics classes. To
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

There are Servlet, SpringBoot, and Vert.x integrations included in the client library.

To add Prometheus exposition to an existing HTTP server, see the `MetricsServlet`. 
It also serves as a simple example of how to write a custom endpoint.

To expose the metrics used in your code, you would add the Prometheus servlet to your Jetty server:

```java
Server server = new Server(1234);
ServletContextHandler context = new ServletContextHandler();
context.setContextPath("/");
server.setHandler(context);

context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
```



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
  List<MetricFamilySamples> collect() {
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

`SummaryMetricFamily` and `HistogramMetricFamily` work similarly.

A collector may implement a `describe` method which returns metrics in the same
format as `collect` (though you don't have to include the samples). This is
used to predetermine the names of time series a `CollectorRegistry` exposes and
thus to detect collisions and duplicate registrations.

Usually custom collectors do not have to implement `describe`. If `describe` is
not implemented and the CollectorRegistry was created with `auto_desribe=True`
(which is the case for the default registry) then `collect` will be called at
registration time instead of `describe`. If this could cause problems, either
implement a proper `describe`, or if that's not practical have `describe`
return an empty list.

## Contact
The [Prometheus Users Mailinglist](https://groups.google.com/forum/?fromgroups#!forum/prometheus-users) is the best place to ask questions.

Details for those wishing to develop the library can be found on the [wiki](https://github.com/prometheus/client_java/wiki/Development)
