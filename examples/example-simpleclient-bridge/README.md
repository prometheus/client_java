# Built-in HTTPServer for Exposing Metrics

## Build

This example is built as part of the `client_java` project.

```
./mvnw package
```

## Run

The build creates a JAR file with the example application in `./examples/example-simpleclient-bridge/target/`.

```
java -jar ./examples/example-simpleclient-bridge/target/example-simpleclient-bridge.jar
```

This should expose a metrics endpoint on [http://localhost:9400/metrics](http://localhost:9400/metrics).
The `events_total` counter should be exposed.with a Web browser should yield an example of a counter metric.

```
# HELP events_total total number of events
# TYPE events_total counter
events_total 1.0
```
