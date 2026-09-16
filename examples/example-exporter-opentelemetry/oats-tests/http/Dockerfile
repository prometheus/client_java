FROM eclipse-temurin:25.0.4_7-jre@sha256:611e326900ba9edcedfb246ede24efc66a911fa496ac623db9f33cae88a2d83b

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
