FROM eclipse-temurin:21.0.7_6-jre@sha256:28bede20f9b759d5a07065d42fe19917286f0bbe596a5935bd000a7869644374

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
