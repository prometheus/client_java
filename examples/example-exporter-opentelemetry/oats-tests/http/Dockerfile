FROM eclipse-temurin:21.0.8_9-jre@sha256:ed2d2869908887da08ab838b0ee33cf5fb51aca7c31ca8bb8ee0bd3b176fbbfa

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
