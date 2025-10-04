FROM eclipse-temurin:21.0.8_9-jre@sha256:0a9f4e4a46a052522c092d4bc1ef9301362291d3e285bf5f1354b539644e1d1c

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
