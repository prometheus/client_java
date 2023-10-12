package io.prometheus.metrics.examples.httpserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.ExtendedMultiCollector;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

public class SampleExtendedMultiCollector extends ExtendedMultiCollector {

	public SampleExtendedMultiCollector() {
		super();
	}

	@Override
	protected MetricSnapshots collectMetricSnapshots(PrometheusScrapeRequest scrapeRequest) {
		Counter sampleCounter;
		Gauge sampleGauge;
		sampleCounter = Counter.builder().name("x_calls_total").labelNames("target", "proc").build();
		sampleGauge = Gauge.builder().name("x_load").labelNames("target", "proc").build();
		String[] targetName = scrapeRequest.getParameterValues("target");
		String[] procs = scrapeRequest.getParameterValues("proc");
		if (targetName == null || targetName.length == 0) {
			sampleCounter.labelValues("defaultTarget", "defaultProc").inc();
			sampleGauge.labelValues("defaultTarget", "defaultProc").set(Math.random());
		} else {
			if (procs == null || procs.length == 0) {
				sampleCounter.labelValues(targetName[0], "defaultProc").inc(Math.random());
				sampleGauge.labelValues(targetName[0], "defaultProc").set(Math.random());
			} else {
				for (int i = 0; i < procs.length; i++) {
					sampleCounter.labelValues(targetName[0], procs[i]).inc(Math.random());
					sampleGauge.labelValues(targetName[0], procs[i]).set(Math.random());
				}

			}
		}
		Collection<MetricSnapshot> snaps = new ArrayList<MetricSnapshot>();
		snaps.add(sampleCounter.collect());
		snaps.add(sampleGauge.collect());
		MetricSnapshots msnaps = new MetricSnapshots(snaps);
		return msnaps;
	}

	public List<String> getPrometheusNames() {
		List<String> names = new ArrayList<String>();
		names.add("Multi");
		return names ;
	}

}
