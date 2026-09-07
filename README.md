# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-07T03:54:09Z
- **Commit:** [`058e544`](https://github.com/prometheus/client_java/commit/058e54406ef2edfbe1885b414c8cd2999279cf47)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 63.72K | ± 461.98 | ops/s |
| prometheusNoLabelsInc | 56.79K | ± 280.88 | ops/s |
| prometheusAdd | 51.19K | ± 276.55 | ops/s |
| codahaleIncNoLabels | 48.15K | ± 1.15K | ops/s |
| openTelemetryIncNoLabels | 18.27K | ± 350.05 | ops/s |
| openTelemetryInc | 15.13K | ± 114.12 | ops/s |
| openTelemetryAdd | 12.84K | ± 228.09 | ops/s |
| simpleclientInc | 6.59K | ± 14.42 | ops/s |
| simpleclientNoLabelsInc | 6.36K | ± 36.45 | ops/s |
| simpleclientAdd | 6.25K | ± 431.25 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.22K | ± 211.73 | ops/s |
| prometheusClassicSingleThread | 4.56K | ± 38.98 | ops/s |
| prometheusClassic | 4.56K | ± 480.83 | ops/s |
| simpleclient | 4.38K | ± 50.66 | ops/s |
| prometheusNative | 2.58K | ± 155.94 | ops/s |
| openTelemetryClassic | 882.24 | ± 76.75 | ops/s |
| openTelemetryExponential | 867.81 | ± 108.53 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 24.09K | ± 488.58 | ops/s |
| openMetricsWriteToNull | 23.78K | ± 730.93 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 507.67K | ± 6.64K | ops/s |
| prometheusWriteToByteArray | 499.43K | ± 2.62K | ops/s |
| openMetricsWriteToNull | 487.97K | ± 3.40K | ops/s |
| openMetricsWriteToByteArray | 484.38K | ± 3.19K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48145.335   ± 1149.835  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12835.945    ± 228.090  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15126.714    ± 114.115  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18271.728    ± 350.052  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51191.935    ± 276.551  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63718.436    ± 461.979  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56786.646    ± 280.883  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6251.340    ± 431.246  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6587.794     ± 14.416  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6355.459     ± 36.453  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        882.244     ± 76.748  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        867.812    ± 108.534  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4555.127    ± 480.831  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12216.907    ± 211.726  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4559.142     ± 38.975  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2580.651    ± 155.939  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4375.089     ± 50.660  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23779.110    ± 730.926  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24087.682    ± 488.578  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     484376.248   ± 3187.554  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487967.015   ± 3402.591  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     499433.075   ± 2622.652  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507672.994   ± 6641.767  ops/s
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
