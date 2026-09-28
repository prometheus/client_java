FROM eclipse-temurin:25.0.4_7-jre@sha256:bb036ed6cfdc57e3da7c22634d15f1b840d2caf76183861c80e81ca4b5104abb

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
