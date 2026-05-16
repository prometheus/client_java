# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-16T04:35:19Z
- **Commit:** [`94b33b7`](https://github.com/prometheus/client_java/commit/94b33b7527ce21b12ff2a3f9cd23c63cdb42e274)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.41K | ± 387.13 | ops/s | **fastest** |
| prometheusNoLabelsInc | 54.92K | ± 2.60K | ops/s | 1.2x slower |
| prometheusAdd | 51.26K | ± 202.42 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.94K | ± 142.13 | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 39.14 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.37K | ± 32.96 | ops/s | 10x slower |
| simpleclientAdd | 5.98K | ± 236.85 | ops/s | 11x slower |
| openTelemetryInc | 3.19K | ± 453.20 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 3.03K | ± 316.41 | ops/s | 22x slower |
| openTelemetryAdd | 2.87K | ± 85.42 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.07K | ± 918.18 | ops/s | **fastest** |
| simpleclient | 4.33K | ± 39.61 | ops/s | 1.4x slower |
| prometheusNative | 2.75K | ± 334.89 | ops/s | 2.2x slower |
| openTelemetryClassic | 749.34 | ± 39.38 | ops/s | 8.1x slower |
| openTelemetryExponential | 628.26 | ± 47.46 | ops/s | 9.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.67K | ± 448.01 | ops/s | **fastest** |
| prometheusWriteToNull | 23.38K | ± 475.13 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 516.19K | ± 6.38K | ops/s | **fastest** |
| prometheusWriteToByteArray | 508.30K | ± 3.48K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 489.79K | ± 3.40K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 483.33K | ± 5.03K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49936.721    ± 142.133  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2867.495     ± 85.425  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3188.946    ± 453.199  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3034.644    ± 316.405  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51255.933    ± 202.425  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65411.643    ± 387.131  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54921.355   ± 2595.971  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5982.023    ± 236.846  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6561.679     ± 39.136  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6369.731     ± 32.961  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        749.336     ± 39.377  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        628.260     ± 47.463  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6068.578    ± 918.182  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2751.745    ± 334.894  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4330.132     ± 39.607  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23672.042    ± 448.011  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23379.502    ± 475.130  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483332.868   ± 5028.706  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489793.529   ± 3397.193  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     508299.243   ± 3482.562  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     516192.357   ± 6382.513  ops/s
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
