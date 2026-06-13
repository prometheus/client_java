# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-13T04:37:45Z
- **Commit:** [`9672749`](https://github.com/prometheus/client_java/commit/9672749085f9029ccb7328b3e88e8e78fa29e402)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.02K | ± 267.69 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.19K | ± 961.56 | ops/s | 1.2x slower |
| prometheusAdd | 51.46K | ± 319.66 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.11K | ± 172.38 | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 52.74 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.44K | ± 142.99 | ops/s | 10x slower |
| simpleclientAdd | 6.06K | ± 240.67 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 3.58K | ± 314.50 | ops/s | 18x slower |
| openTelemetryInc | 3.01K | ± 308.01 | ops/s | 22x slower |
| openTelemetryAdd | 2.99K | ± 404.33 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.85K | ± 2.11K | ops/s | **fastest** |
| simpleclient | 4.41K | ± 84.05 | ops/s | 1.3x slower |
| prometheusNative | 2.82K | ± 362.33 | ops/s | 2.1x slower |
| openTelemetryClassic | 775.25 | ± 20.05 | ops/s | 7.5x slower |
| openTelemetryExponential | 599.94 | ± 53.47 | ops/s | 9.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.48K | ± 351.40 | ops/s | **fastest** |
| prometheusWriteToNull | 23.90K | ± 363.13 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 510.81K | ± 3.37K | ops/s | **fastest** |
| prometheusWriteToByteArray | 508.16K | ± 2.22K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.23K | ± 2.25K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 479.98K | ± 4.80K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50107.316    ± 172.377  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2990.157    ± 404.331  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3007.612    ± 308.007  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3575.736    ± 314.495  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51456.407    ± 319.665  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66015.079    ± 267.689  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56191.058    ± 961.556  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6061.073    ± 240.672  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6559.028     ± 52.740  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6442.122    ± 142.990  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        775.248     ± 20.048  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        599.944     ± 53.469  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5847.713   ± 2110.369  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2818.980    ± 362.326  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4406.932     ± 84.046  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24482.563    ± 351.401  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23902.815    ± 363.133  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479976.527   ± 4803.278  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485231.333   ± 2249.462  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     508157.927   ± 2219.246  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     510807.516   ± 3373.880  ops/s
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
