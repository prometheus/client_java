# OpenTelemetry Exemplars

The `DefaultExemplarSampler` will provide OpenTelemetry exemplars if the [opentelemetry-java](https://github.com/open-telemetry/opentelemetry-java) library version 0.16.0 or higher is found. When `client_java` 0.11.0 was released, the current [opentelemetry-java](https://github.com/open-telemetry/opentelemetry-java) version was 1.2.0.

## Running the Example

If you want to see this in action, you can run the example from the `ExemplarsClientJavaIT`:

```
./mvnw package
cd integration_tests/exemplars_otel_agent/target/
curl -LO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.0.1/opentelemetry-javaagent-all.jar
java -Dotel.traces.exporter=logging -Dotel.metrics.exporter=none -javaagent:./opentelemetry-javaagent-all.jar -jar ./sample-rest-application.jar
```

Now you have a Spring REST service running on [http://localhost:8080/hello](http://localhost:8080/hello) that is instrumented with the OpenTelemetry Java agent.

In order to get metrics in [OpenMetrics](http://openmetrics.io) format, run

```
curl -H 'Accept: application/openmetrics-text; version=1.0.0; charset=utf-8' http://localhost:8080/metrics
```

You should see metrics with Exemplars, for example in the `request_duration_histogram` metric:

```
request_duration_histogram_bucket{path="/god-of-fire",le="0.004"} 4.0 # {trace_id="043cd631811e373e4180a678c06b128e",span_id="cd122e457d2ca5b0"} 0.0033 1618261159.027
```

Note that this is an example application for a unit test, so durations don't represent real durations, and some example metrics might not make sense in the real world.

## Disabling OpenTelemetry Exemplars

If you use OpenTelemetry tracing but don't want Exemplars, you can disable OpenTelemetries in multiple ways.

### Disabling OpenTelemetry Exemplars in Code

The default exemplar sampler can be disabled via the `ExemplarConfig` API. This is described in [README.md], as this is not specific to OpenTelemetry.

### Disabling OpenTelemetry Exemplars at Compile Time

If you don't want to change code, but still build an application that uses OpenTelemetry but does not provide OpenTelemetry exemplars,
you can exclude the corresponding dependencies in your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient</artifactId>
    <version>0.11.0</version>
    <exclusions>
      <!-- The following will disable OpenTelemetry exemplars when your application uses OpenTelemetry directly -->
      <exclusion>
        <groupId>io.prometheus</groupId>
        <artifactId>simpleclient_tracer_otel</artifactId>
      </exclusion>
      <!-- The following will disable OpenTelemetry exemplars when your application uses the OpenTelemetry Java agent -->
      <exclusion>
        <groupId>io.prometheus</groupId>
        <artifactId>simpleclient_tracer_otel_agent</artifactId>
      </exclusion>
    </exclusions>
  </dependency>
</dependencies>
```

### Disable OpenTelemetry Exemplars at Runtime

If your application uses OpenTelemetry tracing, but you want to disable OpenTelemetry at runtime without changing code,
start your application with the `otelExemplars` system property:

```
java -DotelExemplars=inactive -jar my-application.jar
```

Alternatively, you can set the environment variable `OTEL_EXEMPLARS=inactive`:

```
export OTEL_EXEMPLARS=inactive
java -jar my-application.jar
```
