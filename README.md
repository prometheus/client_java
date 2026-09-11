# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-11T03:52:53Z
- **Commit:** [`39a91dd`](https://github.com/prometheus/client_java/commit/39a91ddb316ebbeebb8740a436109f9b9cca7e17)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 59.68K | ± 567.55 | ops/s |
| prometheusNoLabelsInc | 51.44K | ± 938.46 | ops/s |
| prometheusAdd | 48.70K | ± 789.89 | ops/s |
| codahaleIncNoLabels | 44.27K | ± 312.11 | ops/s |
| openTelemetryIncNoLabels | 15.63K | ± 2.48K | ops/s |
| openTelemetryInc | 13.62K | ± 160.37 | ops/s |
| openTelemetryAdd | 12.23K | ± 14.00 | ops/s |
| simpleclientInc | 6.18K | ± 55.23 | ops/s |
| simpleclientAdd | 6.10K | ± 100.40 | ops/s |
| simpleclientNoLabelsInc | 6.04K | ± 199.49 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.94K | ± 49.50 | ops/s |
| prometheusClassic | 6.16K | ± 945.76 | ops/s |
| prometheusClassicSingleThread | 5.85K | ± 19.07 | ops/s |
| simpleclient | 4.50K | ± 65.76 | ops/s |
| prometheusNative | 2.92K | ± 229.23 | ops/s |
| openTelemetryClassic | 794.39 | ± 27.96 | ops/s |
| openTelemetryExponential | 721.72 | ± 30.45 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 27.52K | ± 71.65 | ops/s |
| prometheusWriteToNull | 27.24K | ± 490.57 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 578.16K | ± 1.78K | ops/s |
| prometheusWriteToByteArray | 564.10K | ± 5.15K | ops/s |
| openMetricsWriteToNull | 539.76K | ± 3.40K | ops/s |
| openMetricsWriteToByteArray | 529.89K | ± 2.59K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44269.572    ± 312.106  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12230.760     ± 14.004  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13617.035    ± 160.370  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      15634.596   ± 2478.144  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48696.030    ± 789.888  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59675.608    ± 567.549  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51438.851    ± 938.459  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6102.993    ± 100.398  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6178.754     ± 55.234  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6041.980    ± 199.487  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        794.391     ± 27.959  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        721.717     ± 30.453  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6155.376    ± 945.760  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13937.355     ± 49.499  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5847.900     ± 19.072  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2919.524    ± 229.229  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4500.761     ± 65.761  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27516.913     ± 71.645  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27236.313    ± 490.566  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     529889.171   ± 2587.180  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     539762.336   ± 3396.875  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     564102.636   ± 5147.428  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     578159.123   ± 1779.563  ops/s
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
