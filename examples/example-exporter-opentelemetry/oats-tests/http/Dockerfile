FROM eclipse-temurin:25.0.1_8-jre@sha256:f6b092537e68d9836e86f676344e94102f2be325bbc652133cd9ef85b27d3ea9

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
