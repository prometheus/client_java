# Prometheus JVM Client
It supports Java, Clojure, Scala, JRuby, and anything else that runs on the JVM.
 
[![Build Status](https://travis-ci.org/prometheus/client_java.png?branch=master)](https://travis-ci.org/prometheus/client_java)

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
  <version>0.0.20</version>
</dependency>
<!-- Hotspot JVM metrics-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_hotspot</artifactId>
  <version>0.0.20</version>
</dependency>
<!-- Exposition servlet-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_servlet</artifactId>
  <version>0.0.20</version>
</dependency>
<!-- Pushgateway exposition-->
<dependency>
  <groupId>io.prometheus</groupId>
  <artifactId>simpleclient_pushgateway</artifactId>
  <version>0.0.20</version>
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
     .name("requests_total").help("Total requests.")
     .labelNames("method").register();
  
  void processGetRequest() {
    requests.labels("get").inc();
    // Your code here.
  }
}
```

### Included Collectors

The Java client includes collectors for garbage collection, memory pools, JMX, classloading, and thread counts.
These can be added individually or just use the `DefaultExports` to conveniently register them. 

```java
DefaultExports.initialize();
```

####Logging

There are logging collectors for log4j and logback.

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

#### Hibernate

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
