FROM eclipse-temurin:21.0.8_9-jre@sha256:2f696f62306d28d32cce2ea6bbb2f2f5b8df736adcc019be61d0f90ce88f07d5

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
