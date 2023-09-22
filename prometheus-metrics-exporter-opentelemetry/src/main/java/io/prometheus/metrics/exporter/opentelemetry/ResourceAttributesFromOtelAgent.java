package io.prometheus.metrics.exporter.opentelemetry;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static java.nio.file.Files.createTempDirectory;

public class ResourceAttributesFromOtelAgent {

    private static final String[] OTEL_JARS = new String[]{"opentelemetry-api-1.29.0.jar", "opentelemetry-context-1.29.0.jar"};

    /**
     * This grabs resource attributes like {@code service.name} and {@code service.instance.id} from
     * the OTel Java agent (if present) and adds them to {@code result}.
     * <p>
     * The way this works is as follows: If the OTel Java agent is attached, it modifies the
     * {@code GlobalOpenTelemetry.get()} method to return an agent-specific object.
     * From that agent-specific object we can get the resource attributes via reflection.
     * <p>
     * So we load the {@code GlobalOpenTelemetry} class (in a separate class loader from the JAR files
     * that are bundled with this module), call {@code .get()}, and inspect the returned object.
     * <p>
     * After that we discard the class loader so that all OTel specific classes are unloaded.
     * No runtime dependency on any OTel version remains.
     */
    public static void addIfAbsent(Map<String, String> result, String instrumentationScopeName) {
        try {
            Path tmpDir = createTempDirectory(instrumentationScopeName + "-");
            try {
                URL[] otelJars = copyOtelJarsToTempDir(tmpDir, instrumentationScopeName);

                try (URLClassLoader classLoader = new URLClassLoader(otelJars)) {
                    Class<?> globalOpenTelemetryClass = classLoader.loadClass("io.opentelemetry.api.GlobalOpenTelemetry");
                    Object globalOpenTelemetry = globalOpenTelemetryClass.getMethod("get").invoke(null);
                    if (globalOpenTelemetry.getClass().getSimpleName().contains("ApplicationOpenTelemetry")) {
                        // GlobalOpenTelemetry is injected by the OTel Java aqent
                        Object applicationMeterProvider = callMethod("getMeterProvider", globalOpenTelemetry);
                        Object agentMeterProvider = getField("agentMeterProvider", applicationMeterProvider);
                        Object sdkMeterProvider = getField("delegate", agentMeterProvider);
                        Object sharedState = getField("sharedState", sdkMeterProvider);
                        Object resource = callMethod("getResource", sharedState);
                        Object attributes = callMethod("getAttributes", resource);
                        Map<?, ?> attributeMap = (Map<?, ?>) callMethod("asMap", attributes);

                        for (Map.Entry<?, ?> entry : attributeMap.entrySet()) {
                            if (entry.getKey() != null && entry.getValue() != null) {
                                result.putIfAbsent(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }
                    }
                }
            } finally {
                deleteTempDir(tmpDir.toFile());
            }
        } catch (Exception ignored) {
        }
    }

    private static Object getField(String name, Object obj) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }

    private static Object callMethod(String name, Object obj) throws Exception {
        Method method = obj.getClass().getMethod(name);
        method.setAccessible(true);
        return method.invoke(obj);
    }

    private static URL[] copyOtelJarsToTempDir(Path tmpDir, String instrumentationScopeName) throws Exception {
        URL[] result = new URL[OTEL_JARS.length];
        for (int i = 0; i < OTEL_JARS.length; i++) {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("lib/" + OTEL_JARS[i]);
            if (inputStream == null) {
                throw new IllegalStateException("Error initializing " + instrumentationScopeName + ": lib/" + OTEL_JARS[i] + " not found in classpath.");
            }
            File outputFile = tmpDir.resolve(OTEL_JARS[i]).toFile();
            Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();
            result[i] = outputFile.toURI().toURL();
        }
        return result;
    }

    private static void deleteTempDir(File tmpDir) {
        // We don't have subdirectories, so this simple implementation should work.
        for (File file : tmpDir.listFiles()) {
            file.delete();
        }
        tmpDir.delete();
    }
}
