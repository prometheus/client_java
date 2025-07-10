FROM eclipse-temurin:21.0.7_6-jre@sha256:f7d9b212856985f86445a09330518ccf3d5e5b2ade00e3608a75420d95f5cf27

COPY target/example-exporter-opentelemetry.jar ./app.jar
# check that the resource attributes from the agent are used, epsecially the service.instance.id should be the same
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /usr/src/app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/usr/src/app/opentelemetry-javaagent.jar

#ENTRYPOINT [ "java",  "-Dotel.javaagent.debug=true","-jar", "./app.jar" ] # for debugging
ENTRYPOINT [ "java", "-jar", "./app.jar" ]
