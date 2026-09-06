# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-06T03:52:14Z
- **Commit:** [`058e544`](https://github.com/prometheus/client_java/commit/058e54406ef2edfbe1885b414c8cd2999279cf47)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) 6973P-C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusAdd | 36.07K | ± 394.43 | ops/s |
| codahaleIncNoLabels | 35.86K | ± 1.98K | ops/s |
| prometheusInc | 35.07K | ± 426.86 | ops/s |
| prometheusNoLabelsInc | 34.46K | ± 1.29K | ops/s |
| openTelemetryIncNoLabels | 24.58K | ± 492.17 | ops/s |
| openTelemetryInc | 22.23K | ± 462.68 | ops/s |
| openTelemetryAdd | 19.55K | ± 272.65 | ops/s |
| simpleclientInc | 9.13K | ± 116.41 | ops/s |
| simpleclientNoLabelsInc | 9.02K | ± 120.77 | ops/s |
| simpleclientAdd | 8.84K | ± 253.96 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 9.28K | ± 113.57 | ops/s |
| simpleclient | 5.87K | ± 96.78 | ops/s |
| prometheusClassicSingleThread | 4.57K | ± 52.43 | ops/s |
| prometheusClassic | 2.42K | ± 410.43 | ops/s |
| prometheusNative | 2.10K | ± 339.80 | ops/s |
| openTelemetryClassic | 498.68 | ± 27.76 | ops/s |
| openTelemetryExponential | 430.39 | ± 6.10 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 24.73K | ± 355.36 | ops/s |
| prometheusWriteToNull | 24.38K | ± 735.36 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 348.89K | ± 7.33K | ops/s |
| prometheusWriteToByteArray | 348.64K | ± 7.53K | ops/s |
| openMetricsWriteToNull | 323.14K | ± 9.84K | ops/s |
| openMetricsWriteToByteArray | 314.24K | ± 2.84K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      35857.158   ± 1982.895  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19547.822    ± 272.654  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22226.715    ± 462.683  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      24582.956    ± 492.167  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      36074.562    ± 394.428  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      35065.849    ± 426.865  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      34461.628   ± 1290.609  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       8837.411    ± 253.955  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       9130.439    ± 116.407  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       9023.833    ± 120.771  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        498.679     ± 27.762  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        430.386      ± 6.104  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2423.602    ± 410.431  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       9284.165    ± 113.574  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4574.151     ± 52.427  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2096.226    ± 339.805  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5874.730     ± 96.782  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24728.253    ± 355.357  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24379.786    ± 735.364  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     314242.894   ± 2837.129  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     323143.268   ± 9843.230  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     348644.882   ± 7525.894  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     348886.482   ± 7326.378  ops/s
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
