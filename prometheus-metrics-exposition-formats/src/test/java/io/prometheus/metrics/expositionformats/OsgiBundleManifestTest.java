package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;

class OsgiBundleManifestTest {

  private static final String INTERNAL_PACKAGE = "io.prometheus.metrics.expositionformats.internal";
  private static final String GENERATED_PREFIX =
      "io.prometheus.metrics.expositionformats.generated";
  private static final String SHADED_BSN = "io.prometheus.metrics-exposition-formats";
  private static final String TEXTFORMATS_BSN = "io.prometheus.metrics-exposition-textformats";

  @Test
  void formatsBundleExportsInternalPackage() throws Exception {
    Manifest manifest = loadBundleManifest(PrometheusProtobufWriterImpl.class);
    assertThat(bundleSymbolicName(manifest)).contains("exposition-formats");
    List<PackageClause> exported = parsePackageHeader(manifest, "Export-Package");
    assertThat(names(exported)).contains(INTERNAL_PACKAGE);
    assertThat(exported).anyMatch(clause -> clause.name.startsWith(GENERATED_PREFIX));
    for (PackageClause clause : exported) {
      if (!INTERNAL_PACKAGE.equals(clause.name) && !clause.name.startsWith(GENERATED_PREFIX)) {
        continue;
      }
      assertThat(clause.attributes.get("version"))
          .as("version of %s", clause.name)
          .isNotBlank()
          .doesNotContain(".SNAPSHOT")
          .doesNotContain("-SNAPSHOT");
    }
  }

  @Test
  void textformatsBundleImportsInternalPackageOptionally() throws Exception {
    Manifest manifest = loadBundleManifest(PrometheusProtobufWriter.class);
    assertThat(bundleSymbolicName(manifest)).isEqualTo(TEXTFORMATS_BSN);
    PackageClause internal =
        requireClause(parsePackageHeader(manifest, "Import-Package"), INTERNAL_PACKAGE);
    assertThat(internal.directives.get("resolution")).isEqualTo("optional");
  }

  @Test
  void protobufImportMatchesShading() throws Exception {
    Manifest manifest = loadBundleManifest(PrometheusProtobufWriterImpl.class);
    List<String> imported = names(parsePackageHeader(manifest, "Import-Package"));
    if (SHADED_BSN.equals(bundleSymbolicName(manifest))) {
      assertThat(imported).doesNotContain("com.google.protobuf");
    } else {
      assertThat(imported).contains("com.google.protobuf");
    }
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

  private static PackageClause requireClause(List<PackageClause> clauses, String packageName) {
    return clauses.stream()
        .filter(clause -> packageName.equals(clause.name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("missing package clause " + packageName));
  }

  private static List<String> names(List<PackageClause> clauses) {
    List<String> names = new ArrayList<>();
    for (PackageClause clause : clauses) {
      names.add(clause.name);
    }
    return names;
  }

  /** OSGi headers are comma-separated clauses; attributes may contain quoted commas. */
  private static List<PackageClause> parsePackageHeader(Manifest manifest, String header) {
    String value = manifest.getMainAttributes().getValue(header);
    assertThat(value).as("%s", header).isNotBlank();
    List<PackageClause> clauses = new ArrayList<>();
    for (String rawClause : splitRespectingQuotes(value, ',')) {
      List<String> parts = splitRespectingQuotes(rawClause, ';');
      if (parts.isEmpty()) {
        continue;
      }
      String name = parts.get(0).trim();
      if (name.isEmpty()) {
        continue;
      }
      Map<String, String> attributes = new LinkedHashMap<>();
      Map<String, String> directives = new LinkedHashMap<>();
      for (int i = 1; i < parts.size(); i++) {
        String part = parts.get(i).trim();
        int directiveEq = part.indexOf(":=");
        int attributeEq = part.indexOf('=');
        if (directiveEq >= 0 && (attributeEq < 0 || directiveEq <= attributeEq)) {
          directives.put(
              part.substring(0, directiveEq).trim(),
              unquote(part.substring(directiveEq + 2).trim()));
        } else if (attributeEq >= 0) {
          attributes.put(
              part.substring(0, attributeEq).trim(),
              unquote(part.substring(attributeEq + 1).trim()));
        }
      }
      clauses.add(new PackageClause(name, attributes, directives));
    }
    return clauses;
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

  private static String unquote(String value) {
    if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  static final class PackageClause {
    final String name;
    final Map<String, String> attributes;
    final Map<String, String> directives;

    PackageClause(String name, Map<String, String> attributes, Map<String, String> directives) {
      this.name = name;
      this.attributes = attributes;
      this.directives = directives;
    }
  }
}
