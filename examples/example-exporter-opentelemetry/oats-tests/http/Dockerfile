FROM eclipse-temurin:21.0.8_9-jre@sha256:9dc74491b9c961b24cd16939b26c4dc82259df7f55677b232392d85eef7f6ed0

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
