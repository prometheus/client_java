# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-16T04:28:50Z
- **Commit:** [`2a63f5e`](https://github.com/prometheus/client_java/commit/2a63f5e59e3f2523b8af40e720c05c333c11fde8)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 30.74K | ± 1.25K | ops/s | **fastest** |
| codahaleIncNoLabels | 30.22K | ± 1.82K | ops/s | 1.0x slower |
| prometheusNoLabelsInc | 29.90K | ± 1.22K | ops/s | 1.0x slower |
| prometheusAdd | 28.45K | ± 14.96 | ops/s | 1.1x slower |
| simpleclientInc | 6.95K | ± 55.87 | ops/s | 4.4x slower |
| simpleclientAdd | 6.49K | ± 240.70 | ops/s | 4.7x slower |
| simpleclientNoLabelsInc | 6.44K | ± 277.17 | ops/s | 4.8x slower |
| openTelemetryIncNoLabels | 2.69K | ± 131.59 | ops/s | 11x slower |
| openTelemetryInc | 2.57K | ± 141.65 | ops/s | 12x slower |
| openTelemetryAdd | 2.50K | ± 262.45 | ops/s | 12x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| simpleclient | 4.44K | ± 59.61 | ops/s | **fastest** |
| prometheusClassic | 2.89K | ± 1.11K | ops/s | 1.5x slower |
| prometheusNative | 2.28K | ± 37.61 | ops/s | 1.9x slower |
| openTelemetryClassic | 632.31 | ± 42.24 | ops/s | 7.0x slower |
| openTelemetryExponential | 438.07 | ± 11.86 | ops/s | 10x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 18.21K | ± 106.26 | ops/s | **fastest** |
| openMetricsWriteToNull | 18.18K | ± 125.28 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToByteArray | 319.68K | ± 1.08K | ops/s | **fastest** |
| prometheusWriteToNull | 319.52K | ± 5.62K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 296.34K | ± 1.91K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 295.42K | ± 1.95K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      30216.245   ± 1824.988  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2499.343    ± 262.446  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2569.231    ± 141.654  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2689.623    ± 131.590  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28450.122     ± 14.960  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30743.927   ± 1248.339  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      29899.699   ± 1219.893  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6491.791    ± 240.695  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6951.890     ± 55.869  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6437.887    ± 277.173  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        632.309     ± 42.238  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        438.071     ± 11.856  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2891.206   ± 1107.150  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2282.144     ± 37.613  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4444.323     ± 59.615  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18175.439    ± 125.275  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18208.770    ± 106.264  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     296338.030   ± 1911.161  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     295421.184   ± 1951.781  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     319684.929   ± 1080.456  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     319520.926   ± 5618.337  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |
