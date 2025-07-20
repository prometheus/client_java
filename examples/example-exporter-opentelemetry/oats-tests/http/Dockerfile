FROM eclipse-temurin:21.0.7_6-jre@sha256:bca347dc76e38a60a1a01b29a7d1312e514603a97ba594268e5a2e4a1a0c9a8f

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
