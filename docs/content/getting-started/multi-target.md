---
title: Multi-Target Pattern
weight: 7
---

To support multi-target pattern you can create a custom collector overriding the purposed internal method in ExtendedCollector
see SampleExtendedCollector in io.prometheus.metrics.examples.httpserver

```java
public class SampleExtendedCollector extends ExtendedCollector {

	public SampleExtendedCollector() {
		super();
	}

	@Override
	protected MetricSnapshot collectMetricSnapshot(PrometheusScrapeRequest scrapeRequest) {
		Counter sampleCounter;
		sampleCounter = Counter.builder().name("x_counter_total").labelNames("target", "proc").build();
		String[] targetName = scrapeRequest.getParameterValues("target");
		String[] procs = scrapeRequest.getParameterValues("proc");
		if (targetName == null || targetName.length == 0) {
			sampleCounter.labelValues("defaultTarget", "defaultProc").inc();
		} else {
			if (procs == null || procs.length == 0) {
				sampleCounter.labelValues(targetName[0], "defaultProc").inc(Math.random());
			} else {
				for (int i = 0; i < procs.length; i++) {
					sampleCounter.labelValues(targetName[0], procs[i]).inc(Math.random());
				}

			}
		}
		return sampleCounter.collect();
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

