FROM eclipse-temurin:21.0.8_9-jre@sha256:ea350fe1d8bc9b18dea4ad46bb9a0f4d6fbd0dbb3548092e3ff8f1a3fef09efe

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
