FROM eclipse-temurin:21.0.7_6-jre@sha256:f7d9b212856985f86445a09330518ccf3d5e5b2ade00e3608a75420d95f5cf27

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
