# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-24T04:37:58Z
- **Commit:** [`5ee188f`](https://github.com/prometheus/client_java/commit/5ee188ff288806f76e53a89d32431a93bb53da11)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 50.83K | ± 179.04 | ops/s | **fastest** |
| prometheusInc | 50.21K | ± 15.24K | ops/s | 1.0x slower |
| prometheusAdd | 48.77K | ± 701.35 | ops/s | 1.0x slower |
| codahaleIncNoLabels | 43.08K | ± 1.30K | ops/s | 1.2x slower |
| simpleclientInc | 6.21K | ± 118.35 | ops/s | 8.2x slower |
| simpleclientAdd | 6.09K | ± 17.72 | ops/s | 8.3x slower |
| openTelemetryIncNoLabels | 6.04K | ± 228.17 | ops/s | 8.4x slower |
| simpleclientNoLabelsInc | 6.01K | ± 180.70 | ops/s | 8.5x slower |
| openTelemetryInc | 4.64K | ± 1.00K | ops/s | 11x slower |
| openTelemetryAdd | 3.42K | ± 126.26 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.83K | ± 1.15K | ops/s | **fastest** |
| simpleclient | 4.36K | ± 33.94 | ops/s | 1.3x slower |
| prometheusNative | 2.78K | ± 130.70 | ops/s | 2.1x slower |
| openTelemetryClassic | 733.22 | ± 4.72 | ops/s | 8.0x slower |
| openTelemetryExponential | 558.25 | ± 34.09 | ops/s | 10x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.43K | ± 348.31 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.30K | ± 397.09 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 560.37K | ± 2.86K | ops/s | **fastest** |
| prometheusWriteToByteArray | 556.08K | ± 1.98K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 535.84K | ± 5.48K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 520.17K | ± 1.39K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43081.148   ± 1296.724  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3420.900    ± 126.260  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4643.738   ± 1002.904  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       6038.523    ± 228.174  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48767.098    ± 701.354  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      50213.021  ± 15244.805  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50830.786    ± 179.042  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6091.537     ± 17.717  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6211.412    ± 118.346  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6005.851    ± 180.699  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        733.216      ± 4.719  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        558.250     ± 34.090  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5829.150   ± 1148.110  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2776.291    ± 130.699  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4362.376     ± 33.937  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27296.325    ± 397.095  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27430.473    ± 348.313  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     520173.620   ± 1388.163  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     535835.499   ± 5481.072  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     556077.708   ± 1978.893  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     560369.398   ± 2859.716  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |
