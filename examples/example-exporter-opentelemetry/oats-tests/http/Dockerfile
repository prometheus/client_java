FROM eclipse-temurin:21.0.7_6-jre@sha256:ffed502a66e8be6e27b6317fcd5913e967dd79e1e2f5135b2c1ffce557930ec3

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
