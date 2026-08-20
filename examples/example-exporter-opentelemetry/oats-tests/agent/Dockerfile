FROM eclipse-temurin:25.0.3_9-jre@sha256:7c1c6297dc3a3ff947922f3ab14ecd326e29083b9edaa8dbff3b94fef1688311

COPY target/example-exporter-opentelemetry.jar ./app.jar
# check that the resource attributes from the agent are used, epsecially the service.instance.id should be the same
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.21.0/opentelemetry-javaagent.jar /usr/src/app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/usr/src/app/opentelemetry-javaagent.jar

#ENTRYPOINT [ "java",  "-Dotel.javaagent.debug=true","-jar", "./app.jar" ] # for debugging
ENTRYPOINT [ "java", "-jar", "./app.jar" ]
