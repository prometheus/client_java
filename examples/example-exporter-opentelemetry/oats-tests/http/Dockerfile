FROM eclipse-temurin:21.0.8_9-jre@sha256:b2fa3ede055fd93f0ee9dff41fa560a956a9f666022f28c9254c8260ab476c10

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
