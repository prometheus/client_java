package io.prometheus.metrics.examples.httpserver;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.ExtendedCollector;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;

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
