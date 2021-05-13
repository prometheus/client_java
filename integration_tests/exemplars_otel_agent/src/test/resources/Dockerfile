FROM openjdk:11-jre
RUN mkdir -p /app/
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.1.0/opentelemetry-javaagent-all.jar /app/
COPY sample-rest-application.jar /app/
CMD java \
        -Dotel.traces.exporter=logging \
        -Dotel.metrics.exporter=none \
        -javaagent:/app/opentelemetry-javaagent-all.jar \
        -jar \
        /app/sample-rest-application.jar
