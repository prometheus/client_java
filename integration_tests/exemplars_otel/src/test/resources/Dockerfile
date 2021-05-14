FROM openjdk:11-jre
RUN mkdir -p /app/
COPY dependency /app/lib
COPY exemplars_otel.jar /app/
CMD java -cp "/app/exemplars_otel.jar:/app/lib/*" io.prometheus.client.it.exemplars_otel.Server