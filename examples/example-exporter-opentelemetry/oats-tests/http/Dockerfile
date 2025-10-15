FROM eclipse-temurin:21.0.8_9-jre@sha256:615497e30ae2b2654ff7bccc7cb057c27443041994593c726852f04dc99830b1

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
