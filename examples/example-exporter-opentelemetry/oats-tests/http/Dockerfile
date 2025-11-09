FROM eclipse-temurin:25-jre@sha256:dbfb5253ed2fd373759dc36c0b7f2a634c72fcf35f19146d92da6c97e19f8be9

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
