FROM eclipse-temurin:21.0.8_9-jre@sha256:9dc74491b9c961b24cd16939b26c4dc82259df7f55677b232392d85eef7f6ed0

COPY target/example-exporter-opentelemetry.jar ./app.jar
# check that the resource attributes from the agent are used, epsecially the service.instance.id should be the same
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /usr/src/app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/usr/src/app/opentelemetry-javaagent.jar

#ENTRYPOINT [ "java",  "-Dotel.javaagent.debug=true","-jar", "./app.jar" ] # for debugging
ENTRYPOINT [ "java", "-jar", "./app.jar" ]
