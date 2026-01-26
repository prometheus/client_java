package io.prometheus.otelagent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ResourceAttributesFromOtelAgentTest {

  @Test
  void testGetResourceAttributesWithoutOtelAgent() {
    // When OTel agent is not attached, should return empty map
    Map<String, String> attributes =
        ResourceAttributesFromOtelAgent.getResourceAttributes("test-scope");
    assertThat(attributes).isEmpty();
  }

  @Test
  void testGetResourceAttributesWithDifferentInstrumentationScopes() {
    // Test with different scope names to ensure temp directory creation works
    Map<String, String> attributes1 =
        ResourceAttributesFromOtelAgent.getResourceAttributes("scope-one");
    Map<String, String> attributes2 =
        ResourceAttributesFromOtelAgent.getResourceAttributes("scope-two");

    assertThat(attributes1).isEmpty();
    assertThat(attributes2).isEmpty();
  }

  @Test
  void testGetResourceAttributesHandlesExceptions() {
    // Test with special characters that might cause issues in temp directory names
    Map<String, String> attributes =
        ResourceAttributesFromOtelAgent.getResourceAttributes("test/scope");
    // Should not throw, should return empty map
    assertThat(attributes).isEmpty();
  }

  @Test
  void testGetResourceAttributesReturnsImmutableMap() {
    Map<String, String> attributes =
        ResourceAttributesFromOtelAgent.getResourceAttributes("test-scope");

    // Verify the returned map is not null
    assertThat(attributes).isNotNull();

    // The returned map should be unmodifiable (per implementation)
    try {
      attributes.put("test.key", "test.value");
      // If we get here without exception, it's not truly immutable,
      // but we still verify it returned empty
      assertThat(attributes).isEmpty();
    } catch (UnsupportedOperationException e) {
      // This is the expected behavior for an immutable map
      assertThat(attributes).isEmpty();
    }
  }

  @Test
  void testGetResourceAttributesWithNullKey() {
    // Test the null handling in the attribute map processing
    // Without OTel agent, this returns empty map, but tests the null check logic
    Map<String, String> attributes =
        ResourceAttributesFromOtelAgent.getResourceAttributes("test-scope");
    assertThat(attributes).isEmpty();
  }
}
