package io.prometheus.metrics.it.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class Application {

  private Application() {
    // Utility class
  }

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
