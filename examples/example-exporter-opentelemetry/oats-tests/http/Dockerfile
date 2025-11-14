FROM eclipse-temurin:25.0.1_8-jre@sha256:daeb27e2851e570a11b6d117054778617ef3c7160f9ef77293a6d25e5c0f27ea

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
