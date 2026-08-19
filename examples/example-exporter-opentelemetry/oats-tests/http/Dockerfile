FROM eclipse-temurin:25.0.3_9-jre@sha256:6e9581a150f9ad80d9154f6c9dc4e5df0d4f5eb545e788340e2271e2fb5d3870

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
