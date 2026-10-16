package io.prometheus.metrics.core.exemplars;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;

class OsgiBundleManifestTest {

  private static final String TRACER_INITIALIZER = "io.prometheus.metrics.tracer.initializer";

  @Test
  void tracerInitializerImportIsOptional() throws Exception {
    Manifest manifest = loadBundleManifest(ExemplarSampler.class);
    String header = manifest.getMainAttributes().getValue("Import-Package");
    assertThat(header).as("Import-Package").isNotBlank();
    String clause = requireClause(header, TRACER_INITIALIZER);
    assertThat(clause)
        .as("OSGi resolution of %s", TRACER_INITIALIZER)
        .contains("resolution:=optional");
  }

  private static Manifest loadBundleManifest(Class<?> type) throws Exception {
    CodeSource codeSource = type.getProtectionDomain().getCodeSource();
    assertThat(codeSource).as("code source for %s", type.getName()).isNotNull();
    URI location = codeSource.getLocation().toURI();
    Path path = Path.of(location);
    if (Files.isDirectory(path)) {
      Path manifestFile = path.resolve("META-INF/MANIFEST.MF");
      assertThat(Files.exists(manifestFile))
          .as("bnd MANIFEST.MF for %s at %s", type.getName(), manifestFile)
          .isTrue();
      try (InputStream in = Files.newInputStream(manifestFile)) {
        return new Manifest(in);
      }
    }
    try (JarFile jar = new JarFile(path.toFile())) {
      Manifest manifest = jar.getManifest();
      assertThat(manifest).as("MANIFEST.MF in %s", path).isNotNull();
      return manifest;
    }
  }

  private static String requireClause(String header, String packageName) {
    for (String rawClause : splitRespectingQuotes(header, ',')) {
      String name = rawClause.trim();
      int separator = indexOfUnquoted(name, ';');
      if (separator >= 0) {
        name = name.substring(0, separator).trim();
      }
      if (packageName.equals(name)) {
        return rawClause.trim();
      }
    }
    throw new AssertionError("missing Import-Package clause " + packageName);
  }

  private static int indexOfUnquoted(String value, char target) {
    boolean inQuote = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '"') {
        inQuote = !inQuote;
      } else if (!inQuote && c == target) {
        return i;
      }
    }
    return -1;
  }

  private static List<String> splitRespectingQuotes(String value, char separator) {
    List<String> parts = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuote = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '"') {
        inQuote = !inQuote;
        current.append(c);
      } else if (!inQuote && c == separator) {
        parts.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }
    parts.add(current.toString());
    return parts;
  }
}
