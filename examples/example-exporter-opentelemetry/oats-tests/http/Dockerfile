FROM eclipse-temurin:25.0.1_8-jre@sha256:1d1ef205d1b0556fb16299ccf7621994d11df3a1330059b6b7482ae2fe998bbd

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
