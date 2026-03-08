# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-08T04:25:25Z
- **Commit:** [`e6eb2f9`](https://github.com/prometheus/client_java/commit/e6eb2f91d6da13485a83c4eab5171f510382f800)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.93K | ± 1.04K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.79K | ± 333.50 | ops/s | 1.1x slower |
| prometheusAdd | 51.16K | ± 595.44 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.25K | ± 1.72K | ops/s | 1.3x slower |
| simpleclientInc | 6.68K | ± 135.20 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.58K | ± 185.62 | ops/s | 9.9x slower |
| simpleclientAdd | 6.44K | ± 172.52 | ops/s | 10x slower |
| openTelemetryAdd | 1.45K | ± 209.46 | ops/s | 45x slower |
| openTelemetryInc | 1.27K | ± 20.21 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.20K | ± 39.04 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.57K | ± 28.87 | ops/s | **fastest** |
| prometheusClassic | 4.31K | ± 404.32 | ops/s | 1.1x slower |
| prometheusNative | 3.03K | ± 294.39 | ops/s | 1.5x slower |
| openTelemetryClassic | 681.00 | ± 9.15 | ops/s | 6.7x slower |
| openTelemetryExponential | 538.35 | ± 11.21 | ops/s | 8.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 494.78K | ± 929.27 | ops/s | **fastest** |
| prometheusWriteToByteArray | 487.18K | ± 4.04K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.34K | ± 3.59K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.96K | ± 5.58K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49251.197   ± 1715.993  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1453.558    ± 209.463  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1265.318     ± 20.211  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1195.431     ± 39.042  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51163.764    ± 595.440  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64927.697   ± 1038.499  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56787.583    ± 333.499  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6444.433    ± 172.518  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6683.843    ± 135.195  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6576.904    ± 185.624  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        680.995      ± 9.150  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        538.348     ± 11.211  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4306.828    ± 404.316  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3031.433    ± 294.391  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4569.324     ± 28.869  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479960.468   ± 5584.630  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482340.735   ± 3593.062  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     487182.463   ± 4038.384  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     494782.779    ± 929.268  ops/s
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
