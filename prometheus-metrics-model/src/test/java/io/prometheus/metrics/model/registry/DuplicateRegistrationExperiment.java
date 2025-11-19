package io.prometheus.metrics.model.registry;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.jupiter.api.Test;

/**
 * Experimental test to explore the behavior of duplicate metric registrations.
 *
 * <p>This test explores what happens when we allow duplicate metric names with: 1. Different label
 * sets (intended use case for Micrometer compatibility) 2. Same label sets (edge case - should we
 * allow this?) 3. Scraping behavior and output
 */
class DuplicateRegistrationExperiment {

  @Test
  void experiment_duplicateNames_differentLabels() {
    System.out.println("\n=== Experiment 1: Same name, different labels ===");

    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter 1: api_responses_total with labels {uri, outcome}
    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Counter 2: api_responses_total with labels {uri, outcome, error}
    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/hello", "outcome", "FAILURE", "error", "TIMEOUT"))
                        .value(10)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Register both
    System.out.println("Registering counter1 (labels: uri, outcome)...");
    registry.register(counter1);
    System.out.println("✓ Registered successfully");

    System.out.println("Registering counter2 (labels: uri, outcome, error)...");
    registry.register(counter2);
    System.out.println("✓ Registered successfully");

    // Try to scrape
    System.out.println("\nAttempting to scrape...");
    try {
      MetricSnapshots snapshots = registry.scrape();
      System.out.println("✓ Scrape succeeded!");
      System.out.println("Number of snapshots: " + snapshots.size());

      int snapshotNum = 1;
      for (MetricSnapshot snapshot : snapshots) {
        System.out.println("\nSnapshot " + snapshotNum + ":");
        System.out.println("  Name: " + snapshot.getMetadata().getPrometheusName());
        System.out.println("  Data points: " + snapshot.getDataPoints().size());
        for (var dp : snapshot.getDataPoints()) {
          System.out.println("    - " + dp);
        }
        snapshotNum++;
      }
    } catch (Exception e) {
      System.out.println(
          "✗ Scrape failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Test
  void experiment_duplicateNames_sameLabels() {
    System.out.println("\n=== Experiment 2: Same name, same labels ===");

    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter 1: api_responses_total with labels {uri, outcome}
    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Counter 2: SAME NAME and SAME LABELS but different value
    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(50) // Different value, but same labels!
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Register both
    System.out.println("Registering counter1 (labels: uri=/hello, outcome=SUCCESS, value=100)...");
    registry.register(counter1);
    System.out.println("✓ Registered successfully");

    System.out.println("Registering counter2 (labels: uri=/hello, outcome=SUCCESS, value=50)...");
    registry.register(counter2);
    System.out.println("✓ Registered successfully");

    // Try to scrape
    System.out.println("\nAttempting to scrape...");
    try {
      MetricSnapshots snapshots = registry.scrape();
      System.out.println("✓ Scrape succeeded!");
      System.out.println("Number of snapshots: " + snapshots.size());

      int snapshotNum = 1;
      for (MetricSnapshot snapshot : snapshots) {
        System.out.println("\nSnapshot " + snapshotNum + ":");
        System.out.println("  Name: " + snapshot.getMetadata().getPrometheusName());
        System.out.println("  Data points: " + snapshot.getDataPoints().size());
        for (var dp : snapshot.getDataPoints()) {
          System.out.println("    - " + dp);
        }
        snapshotNum++;
      }

      System.out.println("\n⚠ Note: We have two separate time series with identical labels!");
      System.out.println("   This could be confusing in Prometheus queries.");
    } catch (Exception e) {
      System.out.println(
          "✗ Scrape failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Test
  void experiment_duplicateNames_multipleDataPoints() {
    System.out.println("\n=== Experiment 3: Multiple data points in each collector ===");

    PrometheusRegistry registry = new PrometheusRegistry(true);

    // Counter 1: Multiple data points with labels {uri, outcome}
    Collector counter1 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/world", "outcome", "SUCCESS"))
                        .value(200)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Counter 2: Multiple data points with labels {uri, outcome, error}
    Collector counter2 =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/hello", "outcome", "FAILURE", "error", "TIMEOUT"))
                        .value(10)
                        .build())
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/world", "outcome", "FAILURE", "error", "NOT_FOUND"))
                        .value(5)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        };

    // Register both
    System.out.println("Registering counter1 (2 data points with labels: uri, outcome)...");
    registry.register(counter1);
    System.out.println("✓ Registered successfully");

    System.out.println("Registering counter2 (2 data points with labels: uri, outcome, error)...");
    registry.register(counter2);
    System.out.println("✓ Registered successfully");

    // Try to scrape
    System.out.println("\nAttempting to scrape...");
    try {
      MetricSnapshots snapshots = registry.scrape();
      System.out.println("✓ Scrape succeeded!");
      System.out.println("Number of snapshots: " + snapshots.size());

      int snapshotNum = 1;
      for (MetricSnapshot snapshot : snapshots) {
        System.out.println("\nSnapshot " + snapshotNum + ":");
        System.out.println("  Name: " + snapshot.getMetadata().getPrometheusName());
        System.out.println("  Data points: " + snapshot.getDataPoints().size());
        for (var dp : snapshot.getDataPoints()) {
          System.out.println("    - " + dp);
        }
        snapshotNum++;
      }

      System.out.println(
          "\nTotal data points across all snapshots: "
              + snapshots.stream().mapToInt(s -> s.getDataPoints().size()).sum());
    } catch (Exception e) {
      System.out.println(
          "✗ Scrape failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }
}
