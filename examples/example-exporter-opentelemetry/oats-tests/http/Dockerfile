FROM eclipse-temurin:21.0.7_6-jre@sha256:313b22416643b4734f5808f57fe1db1d8729a477034333e09e78760bd0fdf088

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
