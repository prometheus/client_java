FROM eclipse-temurin:25.0.2_10-jre@sha256:f47c82ca0abea08c1c92affd40d2e07976fae85836050e9e4a5702055d5c0e3f

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
