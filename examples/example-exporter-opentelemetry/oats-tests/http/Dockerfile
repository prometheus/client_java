FROM eclipse-temurin:25.0.1_8-jre@sha256:31fb090d244e9167d22fb90755423c54934fb6b6a22edeb413e2201882259d1b

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
