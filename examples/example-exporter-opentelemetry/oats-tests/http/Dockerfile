FROM eclipse-temurin:21.0.8_9-jre@sha256:19ec9faa4bbefbb9538ff237d08d24853ea9ce97bd3213b609be1dcbb76757c6

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
