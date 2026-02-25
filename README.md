# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-25T04:26:41Z
- **Commit:** [`fc21983`](https://github.com/prometheus/client_java/commit/fc219837f90c194962b33dadab179f19738d75b3)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.58K | ± 142.39 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.31K | ± 173.03 | ops/s | 1.1x slower |
| prometheusAdd | 50.98K | ± 517.65 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.12K | ± 3.48K | ops/s | 1.3x slower |
| simpleclientInc | 6.80K | ± 8.10 | ops/s | 9.5x slower |
| simpleclientNoLabelsInc | 6.49K | ± 184.63 | ops/s | 10.0x slower |
| simpleclientAdd | 6.24K | ± 236.84 | ops/s | 10x slower |
| openTelemetryInc | 1.46K | ± 219.67 | ops/s | 44x slower |
| openTelemetryIncNoLabels | 1.24K | ± 63.33 | ops/s | 52x slower |
| openTelemetryAdd | 1.21K | ± 63.84 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.86K | ± 453.62 | ops/s | **fastest** |
| simpleclient | 4.55K | ± 74.76 | ops/s | 1.1x slower |
| prometheusNative | 2.84K | ± 286.94 | ops/s | 1.7x slower |
| openTelemetryClassic | 681.93 | ± 24.57 | ops/s | 7.1x slower |
| openTelemetryExponential | 545.36 | ± 35.90 | ops/s | 8.9x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 498.87K | ± 1.57K | ops/s | **fastest** |
| prometheusWriteToByteArray | 485.82K | ± 5.43K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 483.51K | ± 6.33K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.13K | ± 5.21K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49117.299   ± 3481.394  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1212.030     ± 63.838  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1456.430    ± 219.670  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1241.205     ± 63.334  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50982.684    ± 517.653  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64579.627    ± 142.392  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57307.524    ± 173.031  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6244.621    ± 236.839  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6803.629      ± 8.095  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6486.713    ± 184.628  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        681.925     ± 24.575  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        545.363     ± 35.895  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4857.228    ± 453.624  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2839.751    ± 286.944  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4551.272     ± 74.759  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483125.292   ± 5211.892  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     483508.073   ± 6329.818  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485818.942   ± 5427.626  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     498869.638   ± 1572.378  ops/s
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
