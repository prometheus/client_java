FROM eclipse-temurin:25.0.3_9-jre@sha256:7c1c6297dc3a3ff947922f3ab14ecd326e29083b9edaa8dbff3b94fef1688311

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
