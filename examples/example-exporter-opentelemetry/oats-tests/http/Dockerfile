FROM eclipse-temurin:25.0.2_10-jre@sha256:43a7b50abf8679d77d6d41d52072402a18312a47b4390864046b393e2570137b

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
