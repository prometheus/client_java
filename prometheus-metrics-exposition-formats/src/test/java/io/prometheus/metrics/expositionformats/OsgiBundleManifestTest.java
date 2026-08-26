package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
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

  private static final String INTERNAL_PACKAGE =
      "io.prometheus.metrics.expositionformats.internal";

  @Test
  void formatsBundleExportsInternalPackage() throws Exception {
    Manifest manifest = loadBundleManifest(PrometheusProtobufWriterImpl.class);
    assertThat(bundleSymbolicName(manifest)).contains("exposition-formats");
    assertThat(packageNames(manifest, "Export-Package")).contains(INTERNAL_PACKAGE);
  }

  @Test
  void textformatsBundleImportsInternalPackage() throws Exception {
    Manifest manifest = loadBundleManifest(PrometheusProtobufWriter.class);
    assertThat(bundleSymbolicName(manifest))
        .isEqualTo("io.prometheus.metrics-exposition-textformats");
    assertThat(packageNames(manifest, "Import-Package")).contains(INTERNAL_PACKAGE);
  }

  private static String bundleSymbolicName(Manifest manifest) {
    return manifest.getMainAttributes().getValue("Bundle-SymbolicName");
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

  /** OSGi headers are comma-separated clauses; attributes may contain quoted commas. */
  private static List<String> packageNames(Manifest manifest, String header) {
    String value = manifest.getMainAttributes().getValue(header);
    assertThat(value).as("%s", header).isNotBlank();
    List<String> names = new ArrayList<>();
    int i = 0;
    int n = value.length();
    while (i < n) {
      while (i < n && value.charAt(i) == ' ') {
        i++;
      }
      int start = i;
      while (i < n) {
        char c = value.charAt(i);
        if (c == ';' || c == ',') {
          break;
        }
        i++;
      }
      names.add(value.substring(start, i).trim());
      boolean inQuote = false;
      while (i < n) {
        char c = value.charAt(i++);
        if (c == '"') {
          inQuote = !inQuote;
        } else if (!inQuote && c == ',') {
          break;
        }
      }
    }
    return names;
  }
}
