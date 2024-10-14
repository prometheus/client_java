FROM eclipse-temurin:21-jre

WORKDIR /usr/src/app/

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
