FROM openjdk:11-jre
RUN mkdir -p /app/
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.2.0/opentelemetry-javaagent-1.2.0-all.jar /app/
COPY sample-rest-application.jar /app/
CMD java \
        -Dotel.traces.exporter=logging \
        -Dotel.metrics.exporter=none \
        -javaagent:/app/opentelemetry-javaagent-1.2.0-all.jar \
        -jar \
        /app/sample-rest-application.jar
