FROM eclipse-temurin:25.0.4_7-jre@sha256:41162a70cf9579d1dcbbb74614e432ff81f9d3a1d785c8f6bd63b7d82d5b3a21

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
