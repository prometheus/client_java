package io.prometheus.metrics.model.registry;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

public class PrometheusRegistry {

	public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();

	private final Set<String> prometheusNames = ConcurrentHashMap.newKeySet();
	private final List<Collector> collectors = new CopyOnWriteArrayList<>();
	private final List<MultiCollector> multiCollectors = new CopyOnWriteArrayList<>();

	public void register(Collector collector) {
		String prometheusName = collector.getPrometheusName();
		if (prometheusName != null) {
			if (!prometheusNames.add(prometheusName)) {
				throw new IllegalStateException("Can't register " + prometheusName + " because a metric with that name is already registered.");
			}
		}
		collectors.add(collector);
	}

	public void register(MultiCollector collector) {
		for (String prometheusName : collector.getPrometheusNames()) {
			if (!prometheusNames.add(prometheusName)) {
				throw new IllegalStateException("Can't register " + prometheusName + " because that name is already registered.");
			}
		}
		multiCollectors.add(collector);
	}

	public void unregister(Collector collector) {
		collectors.remove(collector);
		String prometheusName = collector.getPrometheusName();
		if (prometheusName != null) {
			prometheusNames.remove(collector.getPrometheusName());
		}
	}

	public void unregister(MultiCollector collector) {
		multiCollectors.remove(collector);
		for (String prometheusName : collector.getPrometheusNames()) {
			prometheusNames.remove(prometheusName(prometheusName));
		}
	}

	public MetricSnapshots scrape() {
		return scrape((PrometheusScrapeRequest) null);
	}

	public MetricSnapshots scrape(PrometheusScrapeRequest scrapeRequest) {
		MetricSnapshots.Builder result = MetricSnapshots.builder();
		for (Collector collector : collectors) {
			MetricSnapshot snapshot = scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
			if (snapshot != null) {
				if (result.containsMetricName(snapshot.getMetadata().getName())) {
					throw new IllegalStateException(snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
				}
				result.metricSnapshot(snapshot);
			}
		}
		for (MultiCollector collector : multiCollectors) {
			MetricSnapshots snaphots = scrapeRequest == null ? collector.collect() : collector.collect(scrapeRequest);
			for (MetricSnapshot snapshot : snaphots) {
				if (result.containsMetricName(snapshot.getMetadata().getName())) {
					throw new IllegalStateException(snapshot.getMetadata().getPrometheusName() + ": duplicate metric name.");
				}
				result.metricSnapshot(snapshot);
			}
		}
		return result.build();
	}

	public MetricSnapshots scrape(Predicate<String> includedNames) {
		if (includedNames == null) {
			return scrape();
		}
		return scrape(includedNames, null);
	}

	public MetricSnapshots scrape(Predicate<String> includedNames, PrometheusScrapeRequest scrapeRequest) {
		if (includedNames == null) {
			return scrape(scrapeRequest);
		}
		MetricSnapshots.Builder result = MetricSnapshots.builder();
		for (Collector collector : collectors) {
			String prometheusName = collector.getPrometheusName();
			// prometheusName == null means the name is unknown, and we have to scrape to learn the name.
			// prometheusName != null means we can skip the scrape if the name is excluded.
			if (prometheusName == null || includedNames.test(prometheusName)) {
				MetricSnapshot snapshot = scrapeRequest == null ? collector.collect(includedNames) : collector.collect(includedNames, scrapeRequest);
				if (snapshot != null) {
					result.metricSnapshot(snapshot);
				}
			}
		}
		for (MultiCollector collector : multiCollectors) {
			List<String> prometheusNames = collector.getPrometheusNames();
			// empty prometheusNames means the names are unknown, and we have to scrape to learn the names.
			// non-empty prometheusNames means we can exclude the collector if all names are excluded by the filter.
			boolean excluded = !prometheusNames.isEmpty();
			for (String prometheusName : prometheusNames) {
				if (includedNames.test(prometheusName)) {
					excluded = false;
					break;
				}
			}
			if (!excluded) {
				MetricSnapshots snapshots = scrapeRequest == null ? collector.collect(includedNames) : collector.collect(includedNames, scrapeRequest);
				for (MetricSnapshot snapshot : snapshots) {
					if (snapshot != null) {
						result.metricSnapshot(snapshot);
					}
				}
			}
		}
		return result.build();
	}

}
