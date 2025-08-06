FROM eclipse-temurin:21.0.8_9-jre@sha256:1297ef4131440ba9d8c17335fa27969e76550acb72b648eed7618d93bec98f98

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
