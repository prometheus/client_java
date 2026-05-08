# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-08T04:33:36Z
- **Commit:** [`317347c`](https://github.com/prometheus/client_java/commit/317347c6ab5ee6f2ed5c963bb71f39b2a1d624be)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.81K | ± 853.69 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.84K | ± 669.81 | ops/s | 1.2x slower |
| prometheusAdd | 51.52K | ± 305.68 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.82K | ± 8.41K | ops/s | 1.5x slower |
| simpleclientInc | 6.59K | ± 8.35 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 20.55 | ops/s | 11x slower |
| simpleclientAdd | 6.23K | ± 380.91 | ops/s | 11x slower |
| openTelemetryInc | 3.35K | ± 434.66 | ops/s | 20x slower |
| openTelemetryAdd | 3.04K | ± 34.71 | ops/s | 22x slower |
| openTelemetryIncNoLabels | 3.01K | ± 219.24 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.44K | ± 77.40 | ops/s | **fastest** |
| prometheusClassic | 4.37K | ± 608.45 | ops/s | 1.0x slower |
| prometheusNative | 2.60K | ± 112.04 | ops/s | 1.7x slower |
| openTelemetryClassic | 764.55 | ± 8.38 | ops/s | 5.8x slower |
| openTelemetryExponential | 629.05 | ± 35.72 | ops/s | 7.1x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.36K | ± 310.69 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.18K | ± 1.35K | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 500.73K | ± 10.44K | ops/s | **fastest** |
| prometheusWriteToByteArray | 499.11K | ± 2.83K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 486.11K | ± 5.01K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.89K | ± 3.45K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44818.672   ± 8413.689  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3043.369     ± 34.710  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3350.014    ± 434.661  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3011.484    ± 219.241  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51516.301    ± 305.680  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66806.985    ± 853.691  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56844.613    ± 669.807  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6227.473    ± 380.909  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6594.009      ± 8.349  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6335.396     ± 20.549  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        764.545      ± 8.376  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        629.051     ± 35.719  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4372.992    ± 608.447  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2595.973    ± 112.041  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4435.712     ± 77.404  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23180.337   ± 1346.747  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24359.971    ± 310.685  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479886.742   ± 3446.584  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486112.494   ± 5006.846  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     499114.309   ± 2828.954  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     500732.692  ± 10438.810  ops/s
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
