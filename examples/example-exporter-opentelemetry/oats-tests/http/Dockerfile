FROM eclipse-temurin:25.0.1_8-jre@sha256:96bccca442e8842a7366df338f5960e3342862ea6b8d42b97009a3dfd8602547

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
