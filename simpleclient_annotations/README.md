
# Annotations for interfaces
Add metrics to classes implementing an interface by decorating them with a PrometheusMonitor proxy.
```
class MyClass implements MyInterface {
  @CountInvocations
  @CountCompletions
  @CountExceptions
  @Summarize(namespace = "summary_of")
  public void myFunction() {
    return;
  }

  public static void main() {
    MyInterface instance = PrometheusMonitor.monitor(new MyClass());
    instance.myFunction();
  }
}
```
