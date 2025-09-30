FROM eclipse-temurin:25-jre@sha256:74d5c631e5db5a44e7f5a2dd49f93f0c6f7b8c22c1dc1b8e1caec7009872c5c3

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
