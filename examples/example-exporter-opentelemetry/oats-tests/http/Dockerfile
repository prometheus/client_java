FROM eclipse-temurin:25-jre@sha256:4a26dfc66e9597fcc71381d6e5f3b894b706baab750741d86a44d07ec36e4408

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
