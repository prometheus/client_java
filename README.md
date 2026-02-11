# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-11T04:28:51Z
- **Commit:** [`09bbeee`](https://github.com/prometheus/client_java/commit/09bbeee1225edb7d7e4acb6c4525c9c53fb2e613)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 56.43K | ± 1.98K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.01K | ± 406.23 | ops/s | 1.1x slower |
| prometheusAdd | 47.96K | ± 258.45 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.94K | ± 756.07 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.38K | ± 39.78 | ops/s | 8.8x slower |
| simpleclientInc | 6.14K | ± 38.83 | ops/s | 9.2x slower |
| simpleclientAdd | 5.96K | ± 157.62 | ops/s | 9.5x slower |
| openTelemetryInc | 1.43K | ± 133.44 | ops/s | 39x slower |
| openTelemetryAdd | 1.38K | ± 59.97 | ops/s | 41x slower |
| openTelemetryIncNoLabels | 1.35K | ± 126.86 | ops/s | 42x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.71K | ± 1.74K | ops/s | **fastest** |
| simpleclient | 4.62K | ± 140.81 | ops/s | 1.5x slower |
| prometheusNative | 2.85K | ± 265.79 | ops/s | 2.4x slower |
| openTelemetryClassic | 621.87 | ± 23.67 | ops/s | 11x slower |
| openTelemetryExponential | 519.16 | ± 24.84 | ops/s | 13x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 535.64K | ± 7.45K | ops/s | **fastest** |
| prometheusWriteToByteArray | 524.44K | ± 4.41K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 522.86K | ± 3.43K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 504.28K | ± 5.84K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44936.852    ± 756.073  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1381.818     ± 59.969  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1434.559    ± 133.442  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1346.293    ± 126.865  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47960.491    ± 258.452  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      56433.442   ± 1983.540  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51007.598    ± 406.235  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5959.977    ± 157.619  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6136.219     ± 38.831  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6382.523     ± 39.781  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        621.867     ± 23.666  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        519.158     ± 24.836  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6709.099   ± 1736.883  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2854.041    ± 265.794  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4624.321    ± 140.812  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     504278.783   ± 5838.810  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     522857.410   ± 3434.485  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     524444.825   ± 4409.992  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     535637.879   ± 7445.697  ops/s
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
