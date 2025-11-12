FROM eclipse-temurin:25.0.1_8-jre@sha256:ac6ce0888fa8377a8f95dcaa91619cb1fe3eaf85103ad215b838288cb128c860

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
