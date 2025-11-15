FROM eclipse-temurin:25.0.1_8-jre@sha256:73fe03b88c2a48ec41f9a86428c65ea5913229488ca218cd6a4f55f8cb0ece1f

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
