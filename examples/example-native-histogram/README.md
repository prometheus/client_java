# Native Histogram End-to-End Example

## Build

This example is built as part of the `client_java` project.

```
./mvnw package
```

This should create the file `./examples/example-native-histogram/target/example-native-histogram.jar`.

## Run

With `./examples/example-native-histogram/target/example-native-histogram.jar` present, simply run:

```
cd ./examples/example-native-histogram/
docker-compose up
```

This will run the following Docker containers:

* [http://localhost:9400/metrics](http://localhost:9400/metrics) example application
* [http://localhost:9090](http://localhost:9090) Prometheus server
* [http://localhost:3000](http://localhost:3000) Grafana (user _admin_, password _admin_)

You might need to replace `localhost` with `host.docker.internal` on MacOS or Windows.

The Grafana server is preconfigured with two dashboards, one based on the classic histogram and the other one based on the native histogram.
