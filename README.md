# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-23T04:00:16Z
- **Commit:** [`ac0d68a`](https://github.com/prometheus/client_java/commit/ac0d68a62886473ac4afd736602760e97024b528)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.72K | ± 453.56 | ops/s |
| prometheusNoLabelsInc | 56.68K | ± 313.78 | ops/s |
| prometheusAdd | 51.55K | ± 148.78 | ops/s |
| codahaleIncNoLabels | 42.82K | ± 6.99K | ops/s |
| openTelemetryIncNoLabels | 18.63K | ± 33.36 | ops/s |
| openTelemetryInc | 14.98K | ± 332.79 | ops/s |
| openTelemetryAdd | 12.88K | ± 225.64 | ops/s |
| simpleclientInc | 6.56K | ± 38.76 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 21.74 | ops/s |
| simpleclientAdd | 6.32K | ± 233.30 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.28K | ± 39.27 | ops/s |
| prometheusClassic | 7.05K | ± 2.30K | ops/s |
| prometheusClassicSingleThread | 4.59K | ± 20.89 | ops/s |
| simpleclient | 4.40K | ± 82.90 | ops/s |
| prometheusNative | 2.91K | ± 191.29 | ops/s |
| openTelemetryClassic | 854.04 | ± 61.28 | ops/s |
| openTelemetryExponential | 771.00 | ± 138.67 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.82K | ± 945.79 | ops/s |
| openMetricsWriteToNull | 23.30K | ± 359.85 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 499.32K | ± 2.39K | ops/s |
| prometheusWriteToByteArray | 488.46K | ± 2.77K | ops/s |
| openMetricsWriteToNull | 479.82K | ± 2.37K | ops/s |
| openMetricsWriteToByteArray | 469.56K | ± 2.68K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42820.443   ± 6991.151  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12876.290    ± 225.639  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14982.129    ± 332.792  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18630.209     ± 33.361  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51548.792    ± 148.780  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66717.239    ± 453.562  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56682.401    ± 313.784  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6317.764    ± 233.302  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6555.886     ± 38.762  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6345.874     ± 21.739  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        854.035     ± 61.285  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        771.004    ± 138.670  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7054.872   ± 2304.062  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12281.468     ± 39.272  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4591.671     ± 20.895  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2908.838    ± 191.293  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4403.876     ± 82.903  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23304.762    ± 359.854  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23822.160    ± 945.793  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     469564.295   ± 2681.319  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     479820.837   ± 2369.912  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     488461.734   ± 2767.278  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     499315.974   ± 2394.381  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |
