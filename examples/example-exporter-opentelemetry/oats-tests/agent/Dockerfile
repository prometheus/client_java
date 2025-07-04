FROM eclipse-temurin:21.0.7_6-jre@sha256:ffed502a66e8be6e27b6317fcd5913e967dd79e1e2f5135b2c1ffce557930ec3

COPY target/example-exporter-opentelemetry.jar ./app.jar
# check that the resource attributes from the agent are used, epsecially the service.instance.id should be the same
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /usr/src/app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/usr/src/app/opentelemetry-javaagent.jar

#ENTRYPOINT [ "java",  "-Dotel.javaagent.debug=true","-jar", "./app.jar" ] # for debugging
ENTRYPOINT [ "java", "-jar", "./app.jar" ]
