FROM eclipse-temurin:21.0.8_9-jre@sha256:49f7a6b514f9f72fd27d178abd553dde1e0430c3508a4f00f3a7d605a4c76b68

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
