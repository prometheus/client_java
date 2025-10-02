FROM eclipse-temurin:21.0.8_9-jre@sha256:ce3a015629cbfa6cf98a93afcc87d5d882453afd812cbc0338b076ae1e2b1028

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
