# Running
    $ export TOOLS_JAR=/path/to/tools.jar  # Set accordingly!
    $ mvn compile assembly:assembly assembly:single
    $ java -classpath ${TOOLS_JAR}:${PWD}/target/jvmstat-*-jar-with-dependencies.jar \
        io.prometheus.client.examples.jvmstat.Main