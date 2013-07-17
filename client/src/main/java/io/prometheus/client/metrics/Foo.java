import io.prometheus.client.Register;
import io.prometheus.client.metrics.Counter;

public class Foo {
  @Register
  private static final Counter operations = Counter.builder().inNamespace("cash_register")
      .named("operation").withDimension("operation", "result")
      .documentedAs("Cash register operations partitioned by type and outcome.").build();

  public float divide(float dividend, float divisor) {
    Counter.Partial result = operations.newPartial().withDimension("operation", "division");
    try {
      float f = dividend / divisor;
      result.withDimension("result", "success");
      return f;
    } catch (ArithmeticException e) {
      result.withDimension("result", "failure");
      throw e;
    } finally {
      result.apply().increment();
    }
  }
}
