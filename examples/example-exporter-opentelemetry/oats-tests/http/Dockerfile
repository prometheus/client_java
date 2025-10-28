FROM eclipse-temurin:25-jre@sha256:80be98a5d899661f7fc84f5b67eab635cdb019d25b34660cf7be1b9aea91476c

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
