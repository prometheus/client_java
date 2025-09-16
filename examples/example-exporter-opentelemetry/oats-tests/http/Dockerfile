FROM eclipse-temurin:21.0.8_9-jre@sha256:bc03ecb547faf36e4ca8a12686f14eb0f7ffcd8b402c45966ad1d051ebb3d3e3

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
