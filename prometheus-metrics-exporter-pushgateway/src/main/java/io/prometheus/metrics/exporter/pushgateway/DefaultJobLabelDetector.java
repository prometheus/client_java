package io.prometheus.metrics.exporter.pushgateway;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The default {@code job} label is the name of the JAR file being executed.
 * <p>
 * This is copy-and-paste from {@code ResourceAttributesFromJarFileName}
 * in the {@code prometheus-metrics-exporter-opentelemetry} module.
 */
class DefaultJobLabelDetector {

    static String getDefaultJobLabel() {
        Path jarPath = getJarPathFromSunCommandLine();
        if (jarPath == null) {
            return "unknown_job";
        }
        return getServiceName(jarPath);
    }

    private static String getServiceName(Path jarPath) {
        String jarName = jarPath.getFileName().toString();
        int dotIndex = jarName.lastIndexOf(".");
        return dotIndex == -1 ? jarName : jarName.substring(0, dotIndex);
    }

    private static Path getJarPathFromSunCommandLine() {
        String programArguments = System.getProperty("sun.java.command");
        if (programArguments == null) {
            return null;
        }
        // Take the path until the first space. If the path doesn't exist extend it up to the next
        // space. Repeat until a path that exists is found or input runs out.
        int next = 0;
        while (true) {
            int nextSpace = programArguments.indexOf(' ', next);
            if (nextSpace == -1) {
                return pathIfExists(programArguments);
            }
            Path path = pathIfExists(programArguments.substring(0, nextSpace));
            next = nextSpace + 1;
            if (path != null) {
                return path;
            }
        }
    }

    private static Path pathIfExists(String programArguments) {
        Path candidate;
        try {
            candidate = Paths.get(programArguments);
        } catch (InvalidPathException e) {
            return null;
        }
        return Files.isRegularFile(candidate) ? candidate : null;
    }
}
