# Native Histograms with Custom Buckets - Verification Report

## Issue #1838: Verify that client_java supports native histograms with custom buckets

### Summary

This report documents the verification that the Prometheus Java client library properly supports native histograms with custom bucket configurations (NHCB - Native Histograms with Custom Buckets).

### Background

According to the [Prometheus Native Histograms specification](https://prometheus.io/docs/specs/native_histograms/), native histograms with custom buckets (schema -53) are a feature that allows representing classic histograms as native histograms with explicit bucket boundaries.

**Key findings from the specification:**
- Schema -53 is used for custom bucket boundaries
- There is currently no dedicated protobuf field for custom bucket boundaries
- Custom-bucket histograms are exposed as **classic histograms** with custom boundaries
- Prometheus servers convert these to NHCB upon ingestion when configured with `convert_classic_histograms_to_nhcb`

### Verification Approach

The Java client library already supports custom bucket configurations through the `classicUpperBounds()`, `classicLinearUpperBounds()`, and `classicExponentialUpperBounds()` builder methods. These methods allow users to define custom bucket boundaries for histograms.

Since NHCB is handled by Prometheus servers during ingestion (not by client libraries), our verification focuses on ensuring that:

1. Histograms with custom bucket boundaries can be created
2. Custom buckets are correctly exposed in both text and protobuf formats
3. Both classic-only and dual (classic+native) histograms work with custom buckets
4. Various custom bucket configurations work correctly

### Test Implementation

Created comprehensive test suite: `CustomBucketsHistogramTest.java`

The test suite includes 11 tests covering:

#### 1. **Custom Buckets with Arbitrary Boundaries** (`testCustomBucketsWithArbitraryBoundaries`)
- Tests histogram with arbitrary custom bucket boundaries
- Verifies observations are distributed correctly across buckets
- Validates count and sum calculations

#### 2. **Custom Buckets with Linear Boundaries** (`testCustomBucketsWithLinearBoundaries`)
- Tests histogram with linear custom bucket boundaries (equal-width buckets)
- Use case: Queue size monitoring with fixed intervals

#### 3. **Custom Buckets with Exponential Boundaries** (`testCustomBucketsWithExponentialBoundaries`)
- Tests histogram with exponential custom bucket boundaries
- Use case: Metrics spanning multiple orders of magnitude (e.g., response sizes)

#### 4. **Classic-Only Histogram with Custom Buckets** (`testCustomBucketsClassicOnlyHistogram`)
- Verifies custom buckets work when using `.classicOnly()`
- Confirms no native histogram representation is maintained

#### 5. **Dual-Mode Histogram with Custom Buckets** (`testCustomBucketsDualModeHistogram`)
- Tests the default mode (both classic and native representations)
- Verifies custom classic buckets coexist with native histogram representation
- **This is the most relevant test for NHCB support**

#### 6. **Text Format Output** (`testCustomBucketsTextFormatOutput`)
- Verifies custom buckets are correctly serialized in Prometheus text format
- Validates bucket labels (le) and counts

#### 7. **Protobuf Format Output** (`testCustomBucketsProtobufFormatOutput`)
- Verifies custom buckets are correctly serialized in Prometheus protobuf format
- Validates bucket upper bounds and cumulative counts
- Confirms native histogram fields are present (for dual-mode)

#### 8. **Custom Buckets with Negative Values** (`testCustomBucketsWithNegativeValues`)
- Tests custom buckets with negative boundary values
- Use case: Temperature or other metrics with negative ranges

#### 9. **Custom Buckets with Labels** (`testCustomBucketsWithLabels`)
- Verifies custom buckets work correctly with labeled histograms
- Tests multiple label combinations

#### 10. **Boundary Edge Cases** (`testCustomBucketsBoundaryEdgeCases`)
- Tests observations exactly on bucket boundaries
- Verifies buckets are inclusive of their upper bound

#### 11. **Fine-Grained Custom Buckets** (`testCustomBucketsFineBoundaries`)
- Tests with very precise custom bucket boundaries
- Use case: High-precision measurements

### Test Results

All 11 tests pass successfully:

```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Key Findings

1. **Custom bucket support is fully functional**: The Java client library correctly handles histograms with custom bucket boundaries.

2. **Dual-mode operation**: By default, histograms maintain both classic (with custom buckets) and native representations, which is ideal for NHCB support.

3. **Correct serialization**: Custom buckets are properly serialized in both:
   - Text format (with `le` labels)
   - Protobuf format (with bucket upper bounds and cumulative counts)

4. **Native histogram fields present**: When using dual-mode (default), the protobuf output includes native histogram fields (schema, zero_count, etc.) alongside the classic buckets.

5. **Flexible bucket configurations**: The library supports:
   - Arbitrary custom boundaries
   - Linear boundaries (equal-width)
   - Exponential boundaries
   - Negative values
   - Very fine-grained precision

### Conclusion

**The Prometheus Java client library (client_java) fully supports native histograms with custom buckets.**

The library correctly:
- Allows users to define custom bucket boundaries
- Maintains both classic and native histogram representations by default
- Exposes custom buckets in the classic histogram format
- Serializes correctly in both text and protobuf formats

Prometheus servers can convert these histograms to NHCB (schema -53) upon ingestion when configured with the `convert_classic_histograms_to_nhcb` option.

### Recommendations

1. **Documentation**: Consider documenting the NHCB support in the user-facing documentation, explaining that:
   - Custom buckets are supported via the existing `classicUpperBounds()` API
   - Prometheus servers handle the conversion to NHCB (schema -53)
   - The default dual-mode is recommended for NHCB compatibility

2. **Example**: Consider adding an example demonstrating custom bucket usage for NHCB scenarios.

3. **Close issue #1838**: This verification confirms that custom bucket support is working correctly.

### Test File Location

- `prometheus-metrics-core/src/test/java/io/prometheus/metrics/core/metrics/CustomBucketsHistogramTest.java`

### References

- [Prometheus Native Histograms Specification](https://prometheus.io/docs/specs/native_histograms/)
- [GitHub Issue #1838](https://github.com/prometheus/client_java/issues/1838)
- [Prometheus client_model Repository](https://github.com/prometheus/client_model)
