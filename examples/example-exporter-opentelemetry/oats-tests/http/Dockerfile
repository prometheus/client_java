FROM eclipse-temurin:21.0.8_9-jre@sha256:e48ae1e7797cee055b4392e619c19a6653a018a47ab36bac9d6eb0e4d5061656

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
