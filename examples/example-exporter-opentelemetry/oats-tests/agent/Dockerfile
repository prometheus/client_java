FROM eclipse-temurin:21.0.8_9-jre@sha256:615497e30ae2b2654ff7bccc7cb057c27443041994593c726852f04dc99830b1

COPY target/example-exporter-opentelemetry.jar ./app.jar
# check that the resource attributes from the agent are used, epsecially the service.instance.id should be the same
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /usr/src/app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/usr/src/app/opentelemetry-javaagent.jar

#ENTRYPOINT [ "java",  "-Dotel.javaagent.debug=true","-jar", "./app.jar" ] # for debugging
ENTRYPOINT [ "java", "-jar", "./app.jar" ]
