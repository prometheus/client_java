---
title: Multi-Target Pattern
weight: 7
---

{{< hint type=note >}}
This is for the upcoming release 1.1.0.
{{< /hint >}}

To support multi-target pattern you can create a custom collector overriding the purposed internal method in ExtendedMultiCollector
see SampleExtendedMultiCollector in io.prometheus.metrics.examples.httpserver

```java
public class SampleExtendedMultiCollector extends ExtendedMultiCollector {

	public SampleExtendedMultiCollector() {
		super();
	}

	@Override
	protected MetricSnapshots collectMetricSnapshots(PrometheusScrapeRequest scrapeRequest) {

		GaugeSnapshot.Builder gaugeBuilder = GaugeSnapshot.builder();
		gaugeBuilder.name("x_load").help("process load");

		CounterSnapshot.Builder counterBuilder = CounterSnapshot.builder();
		counterBuilder.name(PrometheusNaming.sanitizeMetricName("x_calls_total")).help("invocations");

		String[] targetNames = scrapeRequest.getParameterValues("target");
		String targetName;
		String[] procs = scrapeRequest.getParameterValues("proc");
		if (targetNames == null || targetNames.length == 0) {
			targetName = "defaultTarget";
			procs = null; //ignore procs param
		} else {
			targetName = targetNames[0];
		}
		Builder counterDataPointBuilder = CounterSnapshot.CounterDataPointSnapshot.builder();
		io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot.Builder gaugeDataPointBuilder = GaugeSnapshot.GaugeDataPointSnapshot.builder();
		Labels lbls = Labels.of("target", targetName);

		if (procs == null || procs.length == 0) {
			counterDataPointBuilder.labels(lbls.merge(Labels.of("proc", "defaultProc")));
			gaugeDataPointBuilder.labels(lbls.merge(Labels.of("proc", "defaultProc")));
			counterDataPointBuilder.value(70);
			gaugeDataPointBuilder.value(Math.random());

			counterBuilder.dataPoint(counterDataPointBuilder.build());
			gaugeBuilder.dataPoint(gaugeDataPointBuilder.build());

		} else {
			for (int i = 0; i < procs.length; i++) {
				counterDataPointBuilder.labels(lbls.merge(Labels.of("proc", procs[i])));
				gaugeDataPointBuilder.labels(lbls.merge(Labels.of("proc", procs[i])));
				counterDataPointBuilder.value(Math.random());
				gaugeDataPointBuilder.value(Math.random());

				counterBuilder.dataPoint(counterDataPointBuilder.build());
				gaugeBuilder.dataPoint(gaugeDataPointBuilder.build());
			}
		}
		Collection<MetricSnapshot> snaps = new ArrayList<MetricSnapshot>();
		snaps.add(counterBuilder.build());
		snaps.add(gaugeBuilder.build());
		MetricSnapshots msnaps = new MetricSnapshots(snaps);
		return msnaps;
	}

	public List<String> getPrometheusNames() {
		List<String> names = new ArrayList<String>();
		names.add("x_calls_total");
		names.add("x_load");
		return names;
	}

}

```
`PrometheusScrapeRequest` provides methods to access http-related infos from the request originally received by the endpoint

```java
public interface PrometheusScrapeRequest {
	String getRequestURI();

	String[] getParameterValues(String name);
}

```


Sample Prometheus scrape_config

```
  - job_name: "multi-target"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.
    params:
      proc: [proc1, proc2]
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: localhost:9401 
    static_configs:
      - targets: ["target1", "target2"]
```
It's up to the specific MultiCollector implementation how to interpret the _target_ parameter.
It might be an explicit real target (i.e. via host name/ip address) or as an alias in some internal configuration.
The latter is more suitable when the MultiCollector implementation is a proxy (see https://github.com/prometheus/snmp_exporter)
In this case, invoking real target might require extra parameters (e.g. credentials) that might be complex to manage in Prometheus configuration
(not considering the case where the proxy might become an "open relay")