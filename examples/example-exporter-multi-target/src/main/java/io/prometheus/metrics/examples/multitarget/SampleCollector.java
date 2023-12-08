package io.prometheus.metrics.examples.multitarget;

import java.util.function.Predicate;

import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot.Builder;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;

public class SampleCollector implements Collector {
	@Override
	public MetricSnapshots collect(Predicate<String> nameFilter, PrometheusScrapeRequest scrapeRequest) {
		return collectMetricSnapshots(scrapeRequest).filter(nameFilter);
	}

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
		MetricSnapshots msnaps = new MetricSnapshots(counterBuilder.build(), gaugeBuilder.build());
		return msnaps;
	}
}
