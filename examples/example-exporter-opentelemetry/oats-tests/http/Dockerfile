FROM eclipse-temurin:25.0.3_9-jre@sha256:9c9e7c4f5f3840e5254be62ea9a7de56b2d0af23864032a8a3654bf63c31cd5b

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
