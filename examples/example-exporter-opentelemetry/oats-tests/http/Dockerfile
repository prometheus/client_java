FROM eclipse-temurin:21.0.8_9-jre@sha256:cb42271ee30b4ce483edf28e6722c74530c20a17199d8d5d566b199d02434372

COPY target/example-exporter-opentelemetry.jar ./app.jar

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
