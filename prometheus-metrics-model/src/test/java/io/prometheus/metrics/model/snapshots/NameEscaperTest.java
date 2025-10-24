package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.NameEscaper.escapeName;
import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NameEscaperTest {
  @ParameterizedTest
  @MethodSource("escapeNameLegacyTestCases")
  public void testEscapeName(String input, EscapingScheme escapingScheme, String expected) {
    assertThat(escapeName(input, escapingScheme)).isEqualTo(expected);
  }

  static Stream<Arguments> escapeNameLegacyTestCases() {
    return Stream.of(
        Arguments.of("", EscapingScheme.UNDERSCORE_ESCAPING, ""),
        Arguments.of("", EscapingScheme.DOTS_ESCAPING, ""),
        Arguments.of("", EscapingScheme.VALUE_ENCODING_ESCAPING, ""),
        Arguments.of(
            "no:escaping_required", EscapingScheme.UNDERSCORE_ESCAPING, "no:escaping_required"),
        // Dots escaping will escape underscores even though it's not strictly
        // necessary for compatibility.
        Arguments.of("no:escaping_required", EscapingScheme.DOTS_ESCAPING, "no:escaping__required"),
        Arguments.of(
            "no:escaping_required", EscapingScheme.VALUE_ENCODING_ESCAPING, "no:escaping_required"),
        Arguments.of(
            "no:escaping_required", EscapingScheme.UNDERSCORE_ESCAPING, "no:escaping_required"),
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.DOTS_ESCAPING,
            "mysystem_dot_prod_dot_west_dot_cpu_dot_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.DOTS_ESCAPING,
            "mysystem_dot_prod_dot_west_dot_cpu_dot_load__total"),
        Arguments.of("http.status:sum", EscapingScheme.DOTS_ESCAPING, "http_dot_status:sum"),
        Arguments.of("label with 😱", EscapingScheme.UNDERSCORE_ESCAPING, "label_with__"),
        Arguments.of("label with 😱", EscapingScheme.DOTS_ESCAPING, "label__with____"),
        Arguments.of(
            "label with 😱", EscapingScheme.VALUE_ENCODING_ESCAPING, "U__label_20_with_20__1f631_"),
        // name with unicode characters > 0x100
        Arguments.of("花火", EscapingScheme.UNDERSCORE_ESCAPING, "__"),
        // Dots-replacement does not know the difference between two replaced
        Arguments.of("花火", EscapingScheme.DOTS_ESCAPING, "____"),
        Arguments.of("花火", EscapingScheme.VALUE_ENCODING_ESCAPING, "U___82b1__706b_"),
        // name with spaces and edge-case value
        Arguments.of("label with Ā", EscapingScheme.UNDERSCORE_ESCAPING, "label_with__"),
        Arguments.of("label with Ā", EscapingScheme.DOTS_ESCAPING, "label__with____"),
        Arguments.of(
            "label with Ā", EscapingScheme.VALUE_ENCODING_ESCAPING, "U__label_20_with_20__100_"),
        // name with dots - needs UTF-8 validation for escaping to occur
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "mysystem_prod_west_cpu_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__mysystem_2e_prod_2e_west_2e_cpu_2e_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "mysystem_prod_west_cpu_load_total"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__mysystem_2e_prod_2e_west_2e_cpu_2e_load__total"),
        Arguments.of("http.status:sum", EscapingScheme.UNDERSCORE_ESCAPING, "http_status:sum"),
        Arguments.of(
            "http.status:sum", EscapingScheme.VALUE_ENCODING_ESCAPING, "U__http_2e_status:sum"));
  }
}
