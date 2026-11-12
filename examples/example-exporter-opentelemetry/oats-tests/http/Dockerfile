FROM eclipse-temurin:25.0.5_8-jre@sha256:a640968ae55027b322cedd5f8228b01a84baff0b9e9360e47601c6a111ba1699

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
