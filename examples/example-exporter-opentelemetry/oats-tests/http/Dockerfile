FROM eclipse-temurin:25.0.3_9-jre@sha256:671061ae5a8f0d6827816e15bc39d21e224c43ff96998a97cd0d3989c4d17815

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
