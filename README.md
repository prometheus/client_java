# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-25T04:37:10Z
- **Commit:** [`d68b4e7`](https://github.com/prometheus/client_java/commit/d68b4e7ad4f4e1cb6d82f56b72b9e8c5b61a84e0)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.27K | ± 1.50K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.09K | ± 136.38 | ops/s | 1.1x slower |
| prometheusAdd | 51.56K | ± 521.54 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 45.56K | ± 2.58K | ops/s | 1.4x slower |
| simpleclientInc | 6.55K | ± 31.44 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.35K | ± 48.78 | ops/s | 10x slower |
| simpleclientAdd | 6.33K | ± 191.47 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.84K | ± 312.32 | ops/s | 17x slower |
| openTelemetryAdd | 3.27K | ± 318.64 | ops/s | 20x slower |
| openTelemetryInc | 3.27K | ± 223.89 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.37K | ± 1.36K | ops/s | **fastest** |
| simpleclient | 4.46K | ± 70.95 | ops/s | 1.2x slower |
| prometheusNative | 2.99K | ± 349.38 | ops/s | 1.8x slower |
| openTelemetryClassic | 752.14 | ± 33.37 | ops/s | 7.1x slower |
| openTelemetryExponential | 603.18 | ± 46.00 | ops/s | 8.9x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.27K | ± 224.70 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.98K | ± 196.37 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 507.17K | ± 10.94K | ops/s | **fastest** |
| prometheusWriteToByteArray | 499.57K | ± 6.33K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.80K | ± 3.56K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.64K | ± 6.15K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      45559.722   ± 2584.982  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3271.217    ± 318.639  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3269.211    ± 223.890  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3835.238    ± 312.324  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51558.805    ± 521.536  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65266.610   ± 1499.298  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57089.606    ± 136.379  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6331.660    ± 191.470  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6552.523     ± 31.439  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6348.886     ± 48.781  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        752.136     ± 33.372  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        603.178     ± 46.001  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5373.218   ± 1356.462  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2992.342    ± 349.378  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4455.906     ± 70.953  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23982.750    ± 196.366  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24271.249    ± 224.701  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479641.394   ± 6148.013  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487799.857   ± 3555.448  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     499569.204   ± 6331.225  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507174.338  ± 10936.110  ops/s
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
