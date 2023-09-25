# prometheus.properties Example

## Build

This example is built as part of the `client_java` project.

```
./mvnw package
```

This should create the file `./examples/example-prometheus-properties/target/example-prometheus-properties.jar`.

## Run

```
java -jar ./examples/example-prometheus-properties/target/example-prometheus-properties.jar
```

View the metrics on [http://localhost:9401/metrics?name[]=request_duration_seconds&name[]=request_size_bytes](http://localhost:9401/metrics?name[]=request_duration_seconds&name[]=request_size_bytes).

The example has a `prometheus.properties` file in the classpath with a few examples of how to change settings at runtime.

There are multiple alternative ways to specify the location of the `prometheus.properties` file:

* Put it in the classpath, like in this example.
* Set the environment variable `PROMETHEUS_CONFIG` to the file location.
* Set the `prometheus.config` System property to the file location.
