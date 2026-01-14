FROM eclipse-temurin:25.0.1_8-jre@sha256:968f1917fcfa8250e72c39c68118798a5c86923a709c174578179b318f9033e6

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
