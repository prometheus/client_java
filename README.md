# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-15T04:38:22Z
- **Commit:** [`94b33b7`](https://github.com/prometheus/client_java/commit/94b33b7527ce21b12ff2a3f9cd23c63cdb42e274)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 31.22K | ± 206.07 | ops/s | **fastest** |
| prometheusInc | 31.09K | ± 569.21 | ops/s | 1.0x slower |
| codahaleIncNoLabels | 30.71K | ± 94.45 | ops/s | 1.0x slower |
| prometheusAdd | 28.34K | ± 102.67 | ops/s | 1.1x slower |
| simpleclientInc | 6.88K | ± 107.04 | ops/s | 4.5x slower |
| simpleclientNoLabelsInc | 6.74K | ± 198.53 | ops/s | 4.6x slower |
| simpleclientAdd | 6.44K | ± 137.55 | ops/s | 4.9x slower |
| openTelemetryIncNoLabels | 2.73K | ± 234.85 | ops/s | 11x slower |
| openTelemetryInc | 2.61K | ± 272.52 | ops/s | 12x slower |
| openTelemetryAdd | 2.07K | ± 207.89 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.48K | ± 30.79 | ops/s | **fastest** |
| prometheusClassic | 2.89K | ± 408.44 | ops/s | 1.6x slower |
| prometheusNative | 2.02K | ± 72.90 | ops/s | 2.2x slower |
| openTelemetryClassic | 584.10 | ± 10.57 | ops/s | 7.7x slower |
| openTelemetryExponential | 456.50 | ± 36.37 | ops/s | 9.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 17.96K | ± 98.31 | ops/s | **fastest** |
| openMetricsWriteToNull | 17.85K | ± 140.88 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 262.50K | ± 2.02K | ops/s | **fastest** |
| prometheusWriteToByteArray | 262.09K | ± 1.16K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 246.76K | ± 841.09 | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 245.59K | ± 1.07K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      30709.941     ± 94.450  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2073.300    ± 207.887  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2609.813    ± 272.524  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2726.625    ± 234.848  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28338.717    ± 102.666  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31094.543    ± 569.206  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31223.812    ± 206.065  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6435.746    ± 137.546  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6875.379    ± 107.042  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6735.825    ± 198.526  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        584.105     ± 10.565  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        456.498     ± 36.369  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2886.896    ± 408.441  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2017.800     ± 72.900  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4480.288     ± 30.789  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      17845.943    ± 140.882  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17958.235     ± 98.315  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     245592.733   ± 1065.777  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     246764.629    ± 841.089  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     262094.400   ± 1163.513  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     262500.844   ± 2017.221  ops/s
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
