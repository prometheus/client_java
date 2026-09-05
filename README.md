# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-05T03:51:17Z
- **Commit:** [`058e544`](https://github.com/prometheus/client_java/commit/058e54406ef2edfbe1885b414c8cd2999279cf47)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 59.03K | ± 870.26 | ops/s |
| prometheusNoLabelsInc | 49.99K | ± 2.14K | ops/s |
| prometheusAdd | 48.46K | ± 117.38 | ops/s |
| codahaleIncNoLabels | 44.42K | ± 737.95 | ops/s |
| openTelemetryIncNoLabels | 17.08K | ± 67.09 | ops/s |
| openTelemetryInc | 14.11K | ± 306.23 | ops/s |
| openTelemetryAdd | 12.15K | ± 104.65 | ops/s |
| simpleclientInc | 6.14K | ± 45.05 | ops/s |
| simpleclientAdd | 6.02K | ± 175.63 | ops/s |
| simpleclientNoLabelsInc | 5.88K | ± 61.66 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.94K | ± 53.51 | ops/s |
| prometheusClassicSingleThread | 5.81K | ± 19.36 | ops/s |
| simpleclient | 4.51K | ± 49.96 | ops/s |
| prometheusClassic | 4.13K | ± 235.85 | ops/s |
| prometheusNative | 2.93K | ± 227.03 | ops/s |
| openTelemetryClassic | 808.95 | ± 61.94 | ops/s |
| openTelemetryExponential | 682.43 | ± 28.78 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.52K | ± 174.83 | ops/s |
| openMetricsWriteToNull | 26.74K | ± 1.45K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 578.73K | ± 3.59K | ops/s |
| prometheusWriteToByteArray | 572.52K | ± 4.80K | ops/s |
| openMetricsWriteToNull | 550.01K | ± 3.82K | ops/s |
| openMetricsWriteToByteArray | 525.31K | ± 19.41K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44424.426    ± 737.954  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12146.333    ± 104.655  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14106.191    ± 306.234  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17079.352     ± 67.091  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48456.762    ± 117.378  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59027.749    ± 870.262  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      49991.245   ± 2144.157  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6016.986    ± 175.633  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6135.490     ± 45.054  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5878.161     ± 61.662  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        808.950     ± 61.940  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        682.428     ± 28.783  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4130.781    ± 235.851  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13941.447     ± 53.505  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5810.270     ± 19.361  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2933.944    ± 227.029  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4512.354     ± 49.960  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      26736.362   ± 1451.459  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27517.847    ± 174.833  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     525307.487  ± 19412.676  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     550012.428   ± 3823.287  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     572521.636   ± 4803.540  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     578727.299   ± 3587.022  ops/s
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
